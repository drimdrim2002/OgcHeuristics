package dev.brown.improved.alns.repair;

import dev.brown.improved.alns.domain.*;
import dev.brown.improved.alns.parameter.HyperParameter;
import java.util.*;
import java.util.stream.Collectors;

public class LittleRandomRepair implements Repairer {
    private static final double INF = Double.MAX_VALUE;
    private static final List<String> RIDER_TYPES = Arrays.asList("WALK", "BIKE", "CAR");

    private final Map<String, int[][]> promising;
    private final Random random;
    private final HyperParameter hparam;
    private final int K;
    private final int matrixLength;
    private final int[] ordersPtr;
    private final RiderInfo riderInfo;
    private final int[] distMatPtr;
    private final boolean usePower;
    private final int considerSize;

    public LittleRandomRepair(int K, int[] ordersPtr, RiderInfo riderInfo,
        int[] distMatPtr, HyperParameter hparam,
        boolean usePower, int considerSize, long seed) {
        this.K = K;
        this.matrixLength = 2 * K;
        this.ordersPtr = ordersPtr;
        this.riderInfo = riderInfo;
        this.distMatPtr = distMatPtr;
        this.hparam = hparam;
        this.usePower = usePower;
        this.considerSize = considerSize;
        this.random = new Random(seed);
        this.promising = initializePromising();
    }

    @Override
    public void repair(List<Integer> idsToBuild, Solution solution, Map<String, Integer> ridersAvailable) {
        // 삽입 순서 생성
        List<Integer> insertOrder = new ArrayList<>();
        for (int currSize = idsToBuild.size(); currSize >= 1; currSize--) {
            int randomIndex = random.nextInt(currSize);
            insertOrder.add(randomIndex);
        }

        if (hparam.isUseOld()) {
            repairOld(insertOrder, idsToBuild, solution, ridersAvailable);
        } else {
            repairNew(insertOrder, idsToBuild, solution, ridersAvailable);
        }
    }

    private static int f(int i, int j, int l) {
        return i * l + j;
    }

    private Map<String, int[][]> initializePromising() {
        Map<String, int[][]> promising = new HashMap<>();

        for (String riderType : RIDER_TYPES) {
            int[] timeMatrix = riderInfo.getTimeMatrix(riderType);
            int[][] riderPromising = new int[K][K];

            for (int i = 0; i < K; i++) {
                for (int j = 0; j < K; j++) {
                    if (i == j) continue;

                    int dd1 = distMatPtr[f(i, j, matrixLength)];
                    int wt1 = Math.max(ordersPtr[f(j,0,3)] - timeMatrix[f(i,j,matrixLength)]
                        - ordersPtr[f(i,1,3)], 0);
                    int tw1 = Math.max(ordersPtr[f(i,0,3)] + timeMatrix[f(i,j,matrixLength)]
                        - ordersPtr[f(j,1,3)], 0);
                    int score1 = dd1 + (int)(hparam.getWtWeight() * wt1 + hparam.getTwWeight() * tw1);

                    int dd2 = distMatPtr[f(i+K,j+K,matrixLength)];
                    int wt2 = Math.max(ordersPtr[f(j,0,3)] - timeMatrix[f(i+K,j+K,matrixLength)]
                        - ordersPtr[f(i,1,3)], 0);
                    int tw2 = Math.max(ordersPtr[f(i,0,3)] + timeMatrix[f(i+K,j+K,matrixLength)]
                        - ordersPtr[f(j,1,3)], 0);
                    int score2 = dd2 + (int)(hparam.getWtWeight() * wt2 + hparam.getTwWeight() * tw2);

                    riderPromising[i][j] = score1 + score2;
                }
            }
            promising.put(riderType, riderPromising);
        }
        return promising;
    }


    /**
     * 실행 가능한 솔루션을 저장하는 내부 클래스
     */
    private static class FeasibleSolutionTuple {
        final double costIncremental;
        final int bundleId;
        final String riderType;
        final double newCost;
        final List<Integer> newSource;
        final List<Integer> newDest;

        FeasibleSolutionTuple(double costIncremental, int bundleId, String riderType,
            double newCost, List<Integer> newSource, List<Integer> newDest) {
            this.costIncremental = costIncremental;
            this.bundleId = bundleId;
            this.riderType = riderType;
            this.newCost = newCost;
            this.newSource = new ArrayList<>(newSource);
            this.newDest = new ArrayList<>(newDest);
        }
    }

    /**
     * 번들 점수를 저장하는 내부 클래스
     */
    private static class BundleScore implements Comparable<BundleScore> {
        final double score;
        final int bundleId;

        BundleScore(double score, int bundleId) {
            this.score = score;
            this.bundleId = bundleId;
        }

        @Override
        public int compareTo(BundleScore other) {
            return Double.compare(this.score, other.score);
        }
    }

    public void repairNew(List<Integer> insertOrder, List<Integer> idsToBuild,
        Solution solution, Map<String, Integer> ridersAvailable) {
        int maxCapacity = Math.max(Math.max(
                riderInfo.getWalkInfo().get(0),
                riderInfo.getBikeInfo().get(0)),
            riderInfo.getCarInfo().get(0)
        );

        for (int orderIdToAppend : insertOrder) {
            // 실행 가능한 솔루션 리스트 초기화
            List<FeasibleSolutionTuple> feasibleSolutions = new ArrayList<>(
                Collections.nCopies(solution.size() + 1,
                    new FeasibleSolutionTuple(INF, -1, "", 0.0, new ArrayList<>(), new ArrayList<>()))
            );

            // 번들 점수 계산
            List<BundleScore> bundleScores = calculateBundleScores(
                orderIdToAppend, solution, maxCapacity);

            // 번들 인덱스 선택
            List<Integer> bundleIndices = selectBundleIndices(bundleScores);

            // 기존 번들에 추가하는 케이스 처리
            processBundleCases(orderIdToAppend, bundleIndices, solution,
                ridersAvailable, feasibleSolutions);

            // 새로운 번들 생성 케이스 처리
            processEmptyCase(orderIdToAppend, solution, ridersAvailable, feasibleSolutions);

            // idsToBuild에서 처리된 주문 제거
            idsToBuild.remove(Integer.valueOf(orderIdToAppend));

            // 최적의 솔루션 적용
            applyBestSolution(feasibleSolutions, solution, ridersAvailable);
        }
    }

    public void repairOld(List<Integer> insertOrder, List<Integer> idsToBuild,
        Solution solution, Map<String, Integer> ridersAvailable) {
        int maxCapacity = Math.max(Math.max(
                riderInfo.getWalkInfo().get(0),
                riderInfo.getBikeInfo().get(0)),
            riderInfo.getCarInfo().get(0)
        );

        for (int rep = 0; rep < idsToBuild.size(); rep++) {
            int orderIdToAppend = idsToBuild.get(insertOrder.get(rep));

            // 실행 가능한 솔루션 초기화
            List<FeasibleSolutionTuple> feasibleSolutions = new ArrayList<>(
                Collections.nCopies(solution.size() + 1,
                    new FeasibleSolutionTuple(INF, -1, "", 0.0, new ArrayList<>(), new ArrayList<>()))
            );

            int n = solution.size();
            List<BundleScore> vv = new ArrayList<>(
                Collections.nCopies(n, new BundleScore(INF, -1))
            );

            // 병렬 처리로 번들 점수 계산
            for (int bundleId = 0; bundleId < n; bundleId++) {
                String riderType = solution.getRiderType(bundleId);
                int tmp = 0;
                boolean ok = true;
                int filled = 0;

                for (int x : solution.getSource(bundleId)) {
                    tmp += promising.get(riderType)[orderIdToAppend][x];
                    filled += ordersPtr[f(x, 2, 3)];
                    if (ordersPtr[f(orderIdToAppend, 1, 3)] < ordersPtr[f(x, 0, 3)]) {
                        ok = false;
                        break;
                    }
                    if (ordersPtr[f(x, 1, 3)] < ordersPtr[f(orderIdToAppend, 0, 3)]) {
                        ok = false;
                        break;
                    }
                }

                if (filled + ordersPtr[f(orderIdToAppend, 2, 3)] > maxCapacity) {
                    ok = false;
                }

                if (ok) {
                    vv.set(bundleId, new BundleScore(
                        (float)tmp / solution.getSource(bundleId).size(),
                        bundleId
                    ));
                }
            }

            List<Integer> bundleIndices = new ArrayList<>();

            if (usePower) {
                List<BundleScore> vvv = new ArrayList<>();
                for (int i = 0; i < n; i++) {
                    if (vv.get(i).bundleId != -1) {
                        vvv.add(vv.get(i));
                    }
                }
                Collections.sort(vvv);

                int nConsideration = Math.min(Math.max(considerSize, matrixLength/100), vvv.size());
                int nn = Math.min(vvv.size(), 5 * nConsideration);

                List<Integer> candidates = new ArrayList<>(nn);
                for (int i = 0; i < nn; i++) {
                    candidates.add(vvv.get(i).bundleId);
                }

                bundleIndices = new ArrayList<>(nConsideration);
                for (int i = 0; i < nConsideration; i++) {
                    double randomValue = random.nextDouble();
                    int noiseIdx = (int)Math.floor(Math.pow(randomValue, 5) * candidates.size());
                    int bundleId = candidates.get(noiseIdx);
                    bundleIndices.add(bundleId);
                    candidates.remove(noiseIdx);
                }
            } else {
                List<BundleScore> candidates = new ArrayList<>();
                for (BundleScore score : vv) {
                    if (score.bundleId != -1) {
                        candidates.add(score);
                    }
                }
                Collections.sort(candidates);

                int nConsideration = Math.min(Math.max(considerSize, matrixLength/100), candidates.size());
                nConsideration = Math.min((nConsideration + 3) / 4 * 4, candidates.size());

                bundleIndices = new ArrayList<>(nConsideration);
                for (int i = 0; i < nConsideration; i++) {
                    bundleIndices.add(candidates.get(i).bundleId);
                }
            }

            // Case 1: 기존 번들에 추가
            for (int bundleId : bundleIndices) {
                double costBefore = solution.getCost(bundleId);
                InvestigationResult res = InvestigationUtils.investigateOld(
                    orderIdToAppend,
                    solution.getSource(bundleId),
                    solution.getDest(bundleId),
                    riderInfo,
                    ordersPtr,
                    distMatPtr,
                    matrixLength
                );

                for (String riderType : res.getOptimalOrder()) {
                    if (ridersAvailable.getOrDefault(riderType, 0) > 0 ||
                        solution.getRiderType(bundleId).equals(riderType)) {
                        if (res.getFeasibility(riderType)) {
                            double costAfter = res.getCost(riderType);
                            double costIncremental = costAfter - costBefore;

                            feasibleSolutions.set(bundleId, new FeasibleSolutionTuple(
                                costIncremental,
                                bundleId,
                                riderType,
                                costAfter,
                                res.getSource(riderType),
                                res.getDest(riderType)
                            ));
                            break;
                        }
                    }
                }
            }

            // Case 2: 새로운 번들 생성
            InvestigationResult res = InvestigationUtils.investigateOld(
                orderIdToAppend,
                new ArrayList<>(),
                new ArrayList<>(),
                riderInfo,
                ordersPtr,
                distMatPtr,
                matrixLength
            );

            for (String riderType : res.getOptimalOrder()) {
                if (ridersAvailable.getOrDefault(riderType, 0) > 0 && res.getFeasibility(riderType)) {
                    double costIncremental = res.getCost(riderType);
                    feasibleSolutions.set(solution.size(), new FeasibleSolutionTuple(
                        costIncremental,
                        -1,
                        riderType,
                        costIncremental,
                        res.getSource(riderType),
                        res.getDest(riderType)
                    ));
                    break;
                }
            }

            // 최적의 솔루션 찾기
            FeasibleSolutionTuple bestSolution = feasibleSolutions.get(0);
            for (int i = 1; i < feasibleSolutions.size(); i++) {
                if (feasibleSolutions.get(i).costIncremental < bestSolution.costIncremental) {
                    bestSolution = feasibleSolutions.get(i);
                }
            }

            // 기존 번들 제거
            if (bestSolution.bundleId != -1) {
                ridersAvailable.merge(solution.getRiderType(bestSolution.bundleId), 1, Integer::sum);
                solution.remove(bestSolution.bundleId);
            }

            // 새로운 번들 추가
            solution.append(new Bundle(
                bestSolution.riderType,
                bestSolution.newCost,
                bestSolution.newSource,
                bestSolution.newDest
            ));
            ridersAvailable.merge(bestSolution.riderType, -1, Integer::sum);

            // 처리된 주문 제거
            idsToBuild.remove(Integer.valueOf(orderIdToAppend));
        }
    }


    private List<BundleScore> calculateBundleScores(
        int orderIdToAppend,
        Solution solution,
        int maxCapacity) {

        int n = solution.size();
        List<BundleScore> scores = new ArrayList<>(
            Collections.nCopies(n, new BundleScore(INF, -1))
        );

        for (int bundleId = 0; bundleId < n; bundleId++) {
            String riderType = solution.getRiderType(bundleId);
            int tmp = 0;
            boolean ok = true;
            int filled = 0;

            for (int x : solution.getSource(bundleId)) {
                tmp += promising.get(riderType)[orderIdToAppend][x];
                filled += ordersPtr[f(x, 2, 3)];
                if (ordersPtr[f(orderIdToAppend, 1, 3)] < ordersPtr[f(x, 0, 3)]) {
                    ok = false;
                    break;
                }
                if (ordersPtr[f(x, 1, 3)] < ordersPtr[f(orderIdToAppend, 0, 3)]) {
                    ok = false;
                    break;
                }
            }

            if (filled + ordersPtr[f(orderIdToAppend, 2, 3)] > maxCapacity) {
                ok = false;
            }

            if (ok) {
                scores.set(bundleId, new BundleScore(
                    (float)tmp / solution.getSource(bundleId).size(),
                    bundleId
                ));
            }
        }

        return scores;
    }

    /**
     * 번들 인덱스 선택
     */
    private List<Integer> selectBundleIndices(List<BundleScore> bundleScores) {
        if (usePower) {
            return selectBundleIndicesWithPower(bundleScores);
        } else {
            return selectBundleIndicesNormal(bundleScores);
        }
    }

    private List<Integer> selectBundleIndicesWithPower(List<BundleScore> bundleScores) {
        List<BundleScore> validScores = bundleScores.stream()
            .filter(score -> score.bundleId != -1)
            .sorted()
            .collect(Collectors.toList());

        int nConsideration = Math.min(
            Math.max(considerSize, matrixLength/100),
            validScores.size()
        );
        int nn = Math.min(validScores.size(), 5 * nConsideration);

        List<Integer> candidates = validScores.subList(0, nn).stream()
            .map(score -> score.bundleId)
            .collect(Collectors.toList());

        List<Integer> selectedIndices = new ArrayList<>(nConsideration);
        for (int i = 0; i < nConsideration && !candidates.isEmpty(); i++) {
            double randomValue = random.nextDouble();
            int noiseIdx = (int)Math.floor(Math.pow(randomValue, 5) * candidates.size());
            selectedIndices.add(candidates.get(noiseIdx));
            candidates.remove(noiseIdx);
        }

        return selectedIndices;
    }

    private List<Integer> selectBundleIndicesNormal(List<BundleScore> bundleScores) {
        List<BundleScore> validScores = bundleScores.stream()
            .filter(score -> score.bundleId != -1)
            .sorted()
            .collect(Collectors.toList());

        int nConsideration = Math.min(
            Math.max(considerSize, matrixLength/100),
            validScores.size()
        );
        nConsideration = Math.min((nConsideration + 3) / 4 * 4, validScores.size());

        return validScores.subList(0, nConsideration).stream()
            .map(score -> score.bundleId)
            .collect(Collectors.toList());
    }

    /**
     * 기존 번들에 추가하는 케이스 처리
     */
    private void processBundleCases(
        int orderIdToAppend,
        List<Integer> bundleIndices,
        Solution solution,
        Map<String, Integer> ridersAvailable,
        List<FeasibleSolutionTuple> feasibleSolutions) {

        for (int bundleId : bundleIndices) {
            double costBefore = solution.getCost(bundleId);
            InvestigationResult res = InvestigationUtils.investigate(
                orderIdToAppend,
                solution.getSource(bundleId),
                solution.getDest(bundleId),
                riderInfo,
                ordersPtr,
                distMatPtr,
                matrixLength
            );

            for (String riderType : res.getOptimalOrder()) {
                if (ridersAvailable.getOrDefault(riderType, 0) > 0 ||
                    solution.getRiderType(bundleId).equals(riderType)) {
                    if (res.getFeasibility(riderType)) {
                        double costAfter = res.getCost(riderType);
                        feasibleSolutions.set(bundleId, new FeasibleSolutionTuple(
                            costAfter - costBefore,
                            bundleId,
                            riderType,
                            costAfter,
                            res.getSource(riderType),
                            res.getDest(riderType)
                        ));
                        break;
                    }
                }
            }
        }
    }

    /**
     * 새로운 번들 생성 케이스 처리
     */
    private void processEmptyCase(
        int orderIdToAppend,
        Solution solution,
        Map<String, Integer> ridersAvailable,
        List<FeasibleSolutionTuple> feasibleSolutions) {

        InvestigationResult res = InvestigationUtils.investigate(
            orderIdToAppend,
            new ArrayList<>(),
            new ArrayList<>(),
            riderInfo,
            ordersPtr,
            distMatPtr,
            matrixLength
        );

        for (String riderType : res.getOptimalOrder()) {
            if (ridersAvailable.getOrDefault(riderType, 0) > 0 &&
                res.getFeasibility(riderType)) {
                double cost = res.getCost(riderType);
                feasibleSolutions.set(solution.size(), new FeasibleSolutionTuple(
                    cost,
                    -1,
                    riderType,
                    cost,
                    res.getSource(riderType),
                    res.getDest(riderType)
                ));
                break;
            }
        }
    }

    /**
     * 최적의 솔루션 적용
     */
    private void applyBestSolution(
        List<FeasibleSolutionTuple> feasibleSolutions,
        Solution solution,
        Map<String, Integer> ridersAvailable) {

        FeasibleSolutionTuple bestSolution = Collections.min(
            feasibleSolutions,
            Comparator.comparingDouble(s -> s.costIncremental)
        );

        if (bestSolution.bundleId != -1) {
            ridersAvailable.merge(
                solution.getRiderType(bestSolution.bundleId),
                1,
                Integer::sum
            );
            solution.remove(bestSolution.bundleId);
        }

        solution.append(new Bundle(
            bestSolution.riderType,
            bestSolution.newCost,
            bestSolution.newSource,
            bestSolution.newDest
        ));
        ridersAvailable.merge(bestSolution.riderType, -1, Integer::sum);
    }



    private InvestigationResult investigate(int orderId, List<Integer> source,
        List<Integer> dest) {
        return InvestigationUtils.investigate(
            orderId, source, dest, riderInfo, ordersPtr, distMatPtr, matrixLength
        );
    }
}