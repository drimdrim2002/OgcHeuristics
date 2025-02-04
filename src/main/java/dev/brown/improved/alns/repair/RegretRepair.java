package dev.brown.improved.alns.repair;

import dev.brown.improved.alns.domain.Bundle;
import dev.brown.improved.alns.domain.IndexedValue;
import dev.brown.improved.alns.domain.RiderInfo;
import dev.brown.improved.alns.domain.Solution;
import dev.brown.improved.alns.parameter.HyperParameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 후회 기반 복구 전략 구현
 */
public class RegretRepair implements Repairer {
    private static final double ALPHA_MIN = 0.29999;
    private static final double ALPHA_MAX = 0.3;
    private static final List<Double> PROBABILITY_DISTRIBUTION = Arrays.asList(0.8, 0.15, 0.05);

    private final Random random;
    private final HyperParameter hparam;
    private final int K;
    private final int matrixLength;
    private final int[] ordersPtr;
    private final RiderInfo riderInfo;
    private final int[] distMatPtr;

    public RegretRepair(int K, int[] ordersPtr, RiderInfo riderInfo,
        int[] distMatPtr, HyperParameter hparam, long seed) {
        this.K = K;
        this.matrixLength = 2 * K;
        this.ordersPtr = ordersPtr;
        this.riderInfo = riderInfo;
        this.distMatPtr = distMatPtr;
        this.hparam = hparam;
        this.random = new Random(seed);
    }

    @Override
    public void repair(List<Integer> idsToBuild, Solution solution,
        Map<String, Integer> ridersAvailable) {
        repair(idsToBuild, solution, ridersAvailable, 1);
    }

    /**
     * 후회 기반 복구 수행
     */
    public void repair(List<Integer> idsToBuild, Solution solution,
        Map<String, Integer> ridersAvailable, int regretOption) {
        Cache cache = new Cache();

        while (!idsToBuild.isEmpty()) {
            List<List<FeasibleSolution>> feasibleSolutions = new ArrayList<>(K);
            List<List<CacheEntry>> toAppendToCache = new ArrayList<>(K);

            for (int i = 0; i < K; i++) {
                feasibleSolutions.add(new ArrayList<>());
                toAppendToCache.add(new ArrayList<>());
            }

            // 병렬로 각 주문에 대한 가능한 해 탐색
            idsToBuild.parallelStream().forEach(orderId -> {
                investigateOrder(orderId, solution, ridersAvailable, cache,
                    feasibleSolutions, toAppendToCache);
            });

            // 캐시 업데이트
            updateCache(cache, toAppendToCache);

            // 최적의 주문 선택
            Map<Integer, List<FeasibleSolution>> sortedSolutions =
                getSortedFeasibleSolutions(feasibleSolutions);
            int orderIdToAppend = getOrderIdToAppend(sortedSolutions, regretOption);

            if (orderIdToAppend == -1) break;

            // 선택된 주문 처리
            idsToBuild.remove(Integer.valueOf(orderIdToAppend));
            processBestSolution(orderIdToAppend, sortedSolutions, solution,
                ridersAvailable, cache);
        }
    }

    /**
     * 정렬된 실현 가능한 해 목록 반환
     */
    private Map<Integer, List<FeasibleSolution>> getSortedFeasibleSolutions(
        List<List<FeasibleSolution>> feasibleSolutions) {
        Map<Integer, List<FeasibleSolution>> result = new HashMap<>();

        for (int i = 0; i < feasibleSolutions.size(); i++) {
            List<FeasibleSolution> solutions = feasibleSolutions.get(i);
            if (!solutions.isEmpty()) {
                result.put(i, new ArrayList<>(solutions));
                result.get(i).sort(Comparator.comparingDouble(FeasibleSolution::cost));
            }
        }

        return result;
    }

    /**
     * 추가할 주문 ID 선택
     */
    private int getOrderIdToAppend(Map<Integer, List<FeasibleSolution>> feasibleSolutions,
        int regretOption) {
        int howFool = getRandomIndex();

        if (regretOption > 0) {
            return selectWithRegret(feasibleSolutions, regretOption, howFool);
        } else {
            return selectGreedy(feasibleSolutions, howFool);
        }
    }

    private void investigateOrder(int orderId, Solution solution,
        Map<String, Integer> ridersAvailable,
        Cache cache, List<List<FeasibleSolution>> feasibleSolutions,
        List<List<CacheEntry>> toAppendToCache) {
        // 기존 번들에 추가하는 경우 탐색
        for (int bundleId = 0; bundleId < solution.size(); bundleId++) {
            List<Integer> mergedIdList = new ArrayList<>(solution.getDest(bundleId));
            double costBefore = solution.getCost(bundleId);
            mergedIdList.add(orderId);

            InvestigationResult res;
            if (cache.check(mergedIdList)) {
                res = cache.retrieve(mergedIdList);
            } else {
                res = investigate(orderId, solution.getSource(bundleId),
                    solution.getDest(bundleId));
                toAppendToCache.get(orderId).add(new CacheEntry(mergedIdList, res));
            }

            // 가능한 라이더 타입에 대해 검사
            for (String riderType : res.getOptimalOrder()) {
                if (isRiderAvailable(riderType, ridersAvailable, solution, bundleId)
                    && res.getFeasibility(riderType)) {
                    double costAfter = res.getCost(riderType);
                    double costIncremental = costAfter - costBefore;

                    double capacity = calculateRemainingCapacity(riderType, res, orderId);
                    double alpha2 = ALPHA_MIN + (ALPHA_MAX - ALPHA_MIN) * random.nextDouble();
                    double totalCost = (1.0 - alpha2) * costIncremental + alpha2 * capacity;

                    feasibleSolutions.get(orderId).add(
                        new FeasibleSolution(totalCost, bundleId, riderType));
                    break;
                }
            }
        }

        // 새로운 번들 생성하는 경우 탐색
        List<Integer> singleOrder = Collections.singletonList(orderId);
        InvestigationResult res;
        if (cache.check(singleOrder)) {
            res = cache.retrieve(singleOrder);
        } else {
            res = investigate(orderId, new ArrayList<>(), new ArrayList<>());
            toAppendToCache.get(orderId).add(new CacheEntry(singleOrder, res));
        }

        for (String riderType : res.getOptimalOrder()) {
            if (ridersAvailable.getOrDefault(riderType, 0) > 0
                && res.getFeasibility(riderType)) {
                double costIncremental = res.getCost(riderType);
                double capacity = calculateRemainingCapacity(riderType, res, orderId);
                double totalCost = hparam.getAlpha1() * costIncremental
                    + hparam.getAlpha2() * capacity;

                feasibleSolutions.get(orderId).add(
                    new FeasibleSolution(totalCost, -1, riderType));
                break;
            }
        }
    }

    /**
     * 라이더 가용성 확인
     */
    private boolean isRiderAvailable(String riderType, Map<String, Integer> ridersAvailable,
        Solution solution, int bundleId) {
        return ridersAvailable.getOrDefault(riderType, 0) > 0
            || riderType.equals(solution.getRiderType(bundleId));
    }

    /**
     * 남은 용량 계산
     */
    private double calculateRemainingCapacity(String riderType,
        InvestigationResult res, int orderId) {
        Map.Entry<int[], List<Integer>> riderData = riderInfo.prepare(riderType);
        double capacity = riderData.getValue().getFirst();
        for (int id : res.getSource(riderType)) {
            capacity -= ordersPtr[id * 3 + 2];
        }
        return capacity;
    }

    /**
     * 캐시 업데이트
     */
    private void updateCache(Cache cache, List<List<CacheEntry>> toAppendToCache) {
        for (List<CacheEntry> entries : toAppendToCache) {
            for (CacheEntry entry : entries) {
                cache.append(entry.idList, entry.result);
            }
        }
    }

    /**
     * 후회값 기반 선택
     */
    private int selectWithRegret(Map<Integer, List<FeasibleSolution>> feasibleSolutions,
        int regretOption, int howFool) {
        List<IndexedValue> regretValues = new ArrayList<>();

        for (Map.Entry<Integer, List<FeasibleSolution>> entry : feasibleSolutions.entrySet()) {
            int key = entry.getKey();
            List<FeasibleSolution> solutions = entry.getValue();
            double regret = 0;

            for (int i = 1; i <= Math.min(regretOption, solutions.size() - 1); i++) {
                regret += solutions.get(i).cost - solutions.getFirst().cost;
            }

            // 남은 옵션에 대해 무한대 값 추가
            for (int i = solutions.size(); i <= regretOption; i++) {
                regret += Double.MAX_VALUE;
            }

            regretValues.add(new IndexedValue(regret, key));
        }

        Collections.sort(regretValues);
        int n = Math.max(0, regretValues.size() - 1 - howFool);
        return regretValues.get(n).index();
    }

    /**
     * 탐욕적 선택
     */
    private int selectGreedy(Map<Integer, List<FeasibleSolution>> feasibleSolutions,
        int howFool) {
        List<IndexedValue> values = new ArrayList<>();

        for (Map.Entry<Integer, List<FeasibleSolution>> entry : feasibleSolutions.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                values.add(new IndexedValue(
                    entry.getValue().getFirst().cost,
                    entry.getKey()
                ));
            }
        }

        if (values.isEmpty()) return -1;

        Collections.sort(values);
        int n = Math.min(values.size() - 1, howFool);
        return values.get(n).index();
    }

    /**
     * 무작위 인덱스 선택
     */
    private int getRandomIndex() {
        double rand = random.nextDouble();
        double sum = 0.0;
        for (int i = 0; i < PROBABILITY_DISTRIBUTION.size(); i++) {
            sum += PROBABILITY_DISTRIBUTION.get(i);
            if (rand <= sum) {
                return i;
            }
        }
        return PROBABILITY_DISTRIBUTION.size() - 1;
    }

    /**
     * 최적 해 처리
     */
    private void processBestSolution(int orderId,
        Map<Integer, List<FeasibleSolution>> sortedSolutions,
        Solution solution, Map<String, Integer> ridersAvailable,
        Cache cache) {
        FeasibleSolution bestSolution = sortedSolutions.get(orderId).get(0);

        // 기존 번들 제거
        List<Integer> mergedIdList = new ArrayList<>();
        if (bestSolution.bundleId != -1) {
            mergedIdList.addAll(solution.getSource(bestSolution.bundleId));
            ridersAvailable.merge(solution.getRiderType(bestSolution.bundleId), 1, Integer::sum);
            solution.remove(bestSolution.bundleId);
        }
        mergedIdList.add(orderId);

        // 새로운 번들 추가
        InvestigationResult cachedRes = cache.retrieve(mergedIdList);
        solution.append(new Bundle(
            bestSolution.riderType,
            cachedRes.getCost(bestSolution.riderType),
            cachedRes.getSource(bestSolution.riderType),
            cachedRes.getDest(bestSolution.riderType)
        ));
        ridersAvailable.merge(bestSolution.riderType, -1, Integer::sum);
    }

    /**
     * 주문 조사
     */
    private InvestigationResult investigate(int orderId, List<Integer> source,
        List<Integer> dest) {
        // TODO: 실제 조사 로직 구현
        // 이 부분은 별도의 클래스나 유틸리티로 구현되어야 함
        return new InvestigationResult();
    }
}