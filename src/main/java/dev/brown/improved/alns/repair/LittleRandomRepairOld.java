package dev.brown.improved.alns.repair;

import dev.brown.improved.alns.domain.*;
import dev.brown.improved.alns.parameter.HyperParameter;
import java.util.*;

public class LittleRandomRepairOld implements Repairer {
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

    public LittleRandomRepairOld(
        int K,
        int[] ordersPtr,
        RiderInfo riderInfo,
        int[] distMatPtr,
        HyperParameter hparam,
        boolean usePower,
        int considerSize,
        long seed) {
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
        int maxCapacity = Math.max(Math.max(
                riderInfo.getWalkInfo().get(0),
                riderInfo.getBikeInfo().get(0)),
            riderInfo.getCarInfo().get(0)
        );

        int nIter = idsToBuild.size();
        for (int rep = 0; rep < nIter; rep++) {
            int orderIdToAppend = idsToBuild.get(rep);

            // 실행 가능한 솔루션 초기화
            List<FeasibleSolution> feasibleSolutions = new ArrayList<>(
                Collections.nCopies(solution.size() + 1,
                    new FeasibleSolution(INF, -1, ""))
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

            // 최적의 솔루션 적용
            applyBestSolution(orderIdToAppend, feasibleSolutions, solution, ridersAvailable);

            // 처리된 주문 제거
            idsToBuild.remove(Integer.valueOf(orderIdToAppend));
        }
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

    private static int f(int i, int j, int l) {
        return i * l + j;
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
                    (double)tmp / solution.getSource(bundleId).size(),
                    bundleId
                ));
            }
        }

        return scores;
    }

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

    private List<Integer> selectBundleIndices(List<BundleScore> bundleScores) {
        List<Integer> bundleIndices = new ArrayList<>();
        int n = bundleScores.size();

        if (usePower) {
            List<BundleScore> validScores = new ArrayList<>();
            for (BundleScore score : bundleScores) {
                if (score.bundleId != -1) {
                    validScores.add(score);
                }
            }
            Collections.sort(validScores);

            int nConsideration = Math.min(Math.max(considerSize, matrixLength/100), validScores.size());
            int nn = Math.min(validScores.size(), 5 * nConsideration);

            List<Integer> candidates = new ArrayList<>(nn);
            for (int i = 0; i < nn; i++) {
                candidates.add(validScores.get(i).bundleId);
            }

            for (int i = 0; i < nConsideration && !candidates.isEmpty(); i++) {
                double randomValue = random.nextDouble();
                int noiseIdx = (int)Math.floor(Math.pow(randomValue, 5) * candidates.size());
                bundleIndices.add(candidates.get(noiseIdx));
                candidates.remove(noiseIdx);
            }
        } else {
            List<BundleScore> candidates = new ArrayList<>();
            for (BundleScore score : bundleScores) {
                if (score.bundleId != -1) {
                    candidates.add(score);
                }
            }
            Collections.sort(candidates);

            int nConsideration = Math.min(Math.max(considerSize, matrixLength/100), candidates.size());
            nConsideration = Math.min((nConsideration + 3) / 4 * 4, candidates.size());

            for (int i = 0; i < nConsideration; i++) {
                bundleIndices.add(candidates.get(i).bundleId);
            }
        }

        return bundleIndices;
    }

    private void processBundleCases(
        int orderIdToAppend,
        List<Integer> bundleIndices,
        Solution solution,
        Map<String, Integer> ridersAvailable,
        List<FeasibleSolution> feasibleSolutions) {

        for (int bundleId : bundleIndices) {
            double costBefore = solution.getCost(bundleId);
            InvestigationResult res = investigate(
                orderIdToAppend,
                solution.getSource(bundleId),
                solution.getDest(bundleId)
            );

            for (String riderType : res.getOptimalOrder()) {
                if (ridersAvailable.getOrDefault(riderType, 0) > 0 ||
                    solution.getRiderType(bundleId).equals(riderType)) {
                    if (res.getFeasibility(riderType)) {
                        double costAfter = res.getCost(riderType);
                        feasibleSolutions.set(bundleId, new FeasibleSolution(
                            costAfter - costBefore,
                            bundleId,
                            riderType
                        ));
                        break;
                    }
                }
            }
        }
    }

    private void processEmptyCase(
        int orderIdToAppend,
        Solution solution,
        Map<String, Integer> ridersAvailable,
        List<FeasibleSolution> feasibleSolutions) {

        InvestigationResult res = investigate(
            orderIdToAppend,
            new ArrayList<>(),
            new ArrayList<>()
        );

        for (String riderType : res.getOptimalOrder()) {
            if (ridersAvailable.getOrDefault(riderType, 0) > 0 &&
                res.getFeasibility(riderType)) {
                double cost = res.getCost(riderType);
                feasibleSolutions.set(solution.size(), new FeasibleSolution(
                    cost,
                    -1,
                    riderType
                ));
                break;
            }
        }
    }

    private void applyBestSolution(
        int orderIdToAppend,
        List<FeasibleSolution> feasibleSolutions,
        Solution solution,
        Map<String, Integer> ridersAvailable) {

        FeasibleSolution bestSolution = Collections.min(
            feasibleSolutions,
            Comparator.comparingDouble(FeasibleSolution::cost)
        );

        if (bestSolution.bundleId() != -1) {
            ridersAvailable.merge(
                solution.getRiderType(bestSolution.bundleId()),
                1,
                Integer::sum
            );
            solution.remove(bestSolution.bundleId());
        }

        InvestigationResult res = investigate(
            orderIdToAppend,
            bestSolution.bundleId() == -1 ? new ArrayList<>() : solution.getSource(bestSolution.bundleId()),
            bestSolution.bundleId() == -1 ? new ArrayList<>() : solution.getDest(bestSolution.bundleId())
        );

        solution.append(new Bundle(
            bestSolution.riderType(),
            bestSolution.cost(),
            res.getSource(bestSolution.riderType()),
            res.getDest(bestSolution.riderType())
        ));
        ridersAvailable.merge(bestSolution.riderType(), -1, Integer::sum);
    }

    private InvestigationResult investigate(int orderId, List<Integer> source,
        List<Integer> dest) {
        return InvestigationUtils.investigateOld(
            orderId, source, dest, riderInfo, ordersPtr, distMatPtr, matrixLength
        );
    }
}