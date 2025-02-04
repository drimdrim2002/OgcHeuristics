package dev.brown.improved.alns.repair;

import dev.brown.improved.alns.domain.*;
import dev.brown.improved.alns.parameter.HyperParameter;
import java.util.*;

/**
 * 무작위 복구 전략 구현
 */
public class RandomRepair implements Repairer {
    private static final double INF = Double.MAX_VALUE;

    private final Random random;
    private final HyperParameter hparam;
    private final int K;
    private final int matrixLength;
    private final int[] ordersPtr;
    private final RiderInfo riderInfo;
    private final int[] distMatPtr;

    public RandomRepair(int K, int[] ordersPtr, RiderInfo riderInfo,
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
        while (!idsToBuild.isEmpty()) {
            // 무작위로 주문 선택
            int randomIndex = random.nextInt(idsToBuild.size());
            int orderIdToAppend = idsToBuild.get(randomIndex);

            // 가능한 해 저장을 위한 리스트
            List<BundleSolution> feasibleSolutions = new ArrayList<>();

            // Case 1: 기존 번들에 추가
            for (int bundleId = 0; bundleId < solution.size(); bundleId++) {
                double costBefore = solution.getCost(bundleId);

                InvestigationResult res = investigate(
                    orderIdToAppend,
                    solution.getSource(bundleId),
                    solution.getDest(bundleId)
                );

                for (String riderType : res.getOptimalOrder()) {
                    if (isRiderAvailable(riderType, ridersAvailable, solution, bundleId)
                        && res.getFeasibility(riderType)) {

                        double costAfter = res.getCost(riderType);
                        double costIncremental = costAfter - costBefore;

                        // 용량 계산
                        double capacity = calculateRemainingCapacity(
                            riderType, res.getSource(riderType));

                        // 비용 함수
                        double C = hparam.getAlpha1() * costIncremental / 50
                            + hparam.getAlpha2() * capacity;

                        feasibleSolutions.add(new BundleSolution(
                            new FeasibleSolution(C, bundleId, riderType),
                            costAfter,
                            res.getSource(riderType),
                            res.getDest(riderType)
                        ));
                        break;
                    }
                }
            }

            // Case 2: 새로운 번들 생성
            InvestigationResult res = investigate(
                orderIdToAppend,
                new ArrayList<>(),
                new ArrayList<>()
            );

            for (String riderType : res.getOptimalOrder()) {
                if (ridersAvailable.getOrDefault(riderType, 0) > 0
                    && res.getFeasibility(riderType)) {

                    double costIncremental = res.getCost(riderType);
                    double capacity = calculateRemainingCapacity(
                        riderType, res.getSource(riderType));

                    double C = hparam.getAlpha1() * costIncremental / 50
                        + hparam.getAlpha2() * capacity;

                    feasibleSolutions.add(new BundleSolution(
                        new FeasibleSolution(C, -1, riderType),
                        costIncremental,
                        res.getSource(riderType),
                        res.getDest(riderType)
                    ));
                    break;
                }
            }

            // 선택된 주문 제거
            idsToBuild.remove(randomIndex);

            if (!feasibleSolutions.isEmpty()) {
                // 최적의 해 선택
                Collections.sort(feasibleSolutions);
                BundleSolution bestSolution = feasibleSolutions.getFirst();

                // 기존 번들 제거
                if (bestSolution.solution.bundleId() != -1) {
                    ridersAvailable.merge(
                        solution.getRiderType(bestSolution.solution.bundleId()),
                        1,
                        Integer::sum
                    );
                    solution.remove(bestSolution.solution.bundleId());
                }

                // 새로운 번들 추가
                solution.append(new Bundle(
                    bestSolution.solution.riderType(),
                    bestSolution.newCost,
                    bestSolution.newSource,
                    bestSolution.newDest
                ));
                ridersAvailable.merge(bestSolution.solution.riderType(), -1, Integer::sum);
            }
        }
    }

    private boolean isRiderAvailable(String riderType,
        Map<String, Integer> ridersAvailable,
        Solution solution,
        int bundleId) {
        return ridersAvailable.getOrDefault(riderType, 0) > 0
            || riderType.equals(solution.getRiderType(bundleId));
    }

    private double calculateRemainingCapacity(String riderType,
        List<Integer> source) {
        Map.Entry<int[], List<Integer>> riderData = riderInfo.prepare(riderType);
        double capacity = riderData.getValue().getFirst();
        for (int id : source) {
            capacity -= ordersPtr[id * 3 + 2];
        }
        return capacity;
    }

    private InvestigationResult investigate(int orderId,
        List<Integer> source,
        List<Integer> dest) {
        return InvestigationUtils.investigate(
            orderId, source, dest, riderInfo, ordersPtr, distMatPtr, matrixLength
        );
    }

    private static class BundleSolution implements Comparable<BundleSolution> {
        final FeasibleSolution solution;
        final double newCost;
        final List<Integer> newSource;
        final List<Integer> newDest;

        BundleSolution(FeasibleSolution solution, double newCost,
            List<Integer> newSource, List<Integer> newDest) {
            this.solution = solution;
            this.newCost = newCost;
            this.newSource = new ArrayList<>(newSource);
            this.newDest = new ArrayList<>(newDest);
        }

        @Override
        public int compareTo(BundleSolution other) {
            return Double.compare(this.solution.cost(), other.solution.cost());
        }
    }
}
