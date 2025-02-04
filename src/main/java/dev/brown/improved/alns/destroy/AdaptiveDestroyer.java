package dev.brown.improved.alns.destroy;
import dev.brown.improved.alns.domain.Bundle;
import dev.brown.improved.alns.domain.BundleUtils;
import dev.brown.improved.alns.domain.RiderInfo;
import dev.brown.improved.alns.domain.Solution;
import dev.brown.improved.alns.parameter.HyperParameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * 적응형 파괴 전략 관리자
 */
public class AdaptiveDestroyer {
    private static final double INF = 1e9;

    private final HyperParameter hparam;
    private final int numSamplingMethod;
    private final double rho;
    private final double smoothingRatio;
    private final double minProb;
    private final double maxProb;
    private final int updatePeriod;
     double worstNoise;
    boolean useWorst;

    private final int[] numCalled;
    private final int[] numSuccess;

    private int currentMethod;
    private int iteration;
    private final Random random;
    private final int K;
    private final int matrixLength;
    private double[] probabilityDistribution;

    private final int[] ordersPtr;
    private final RiderInfo riderInfo;
    private final int[] distMatPtr;

    double[][] loadRel;
    double[][] shopDistRel;
    double[][] dlvDistRel;
    private final double[][] history;

    // 각 파괴 전략
    private final HARemoval haRemoval;
    private final SemiWorstRemoval semiWorstRemoval;
    private final WorstRemoval worstRemoval;
    private final RandomRemoval randomRemoval;
    private final DistanceOrientedRemoval distanceRemoval;
    private final RouteRemoval routeRemoval;
    private final ShawRemoval shawRemoval;
    private final ShopOrientedRemoval shopRemoval;
    private final DeliveryOrientedRemoval deliveryRemoval;


    public AdaptiveDestroyer(int K, int[] ordersPtr, RiderInfo riderInfo,
        int[] distMatPtr, HyperParameter hparam, long seed) {
        this.K = K;
        this.ordersPtr = ordersPtr;
        this.riderInfo = riderInfo;
        this.distMatPtr = distMatPtr;
        this.hparam = hparam;
        this.matrixLength = 2 * K;

        // 하이퍼파라미터 설정
        this.rho = hparam.getRho();
        this.smoothingRatio = hparam.getSmoothingRatio();
        this.minProb = hparam.getMinProb();
        this.maxProb = hparam.getMaxProb();
        double routeRemovalRatio = hparam.getRouteRemovalRatio();
        this.updatePeriod = hparam.getUpdatePeriod();
        this.worstNoise = hparam.getWorstNoise();
        this.useWorst = hparam.isUseWorst();

        this.numSamplingMethod = useWorst ? 9 : 8;
        this.random = new Random(seed);

        // 배열 초기화
        this.numCalled = new int[numSamplingMethod];
        this.numSuccess = new int[numSamplingMethod];

        // 관계 행렬 초기화
        double[][] distRel = initializeDistRel();
        this.shopDistRel = initializeShopDistRel();
        this.dlvDistRel = initializeDlvDistRel();
        double[][] timeRel = initializeTimeRel();
        this.loadRel = initializeLoadRel();
        this.history = initializeHistory();

        // 확률 분포 초기화
        initializeProbabilityDistribution();

        // 파괴 전략 초기화
        this.randomRemoval = new RandomRemoval(K, seed);
        this.distanceRemoval = new DistanceOrientedRemoval(K, distRel, seed);
        this.routeRemoval = new RouteRemoval(routeRemovalRatio, seed);
        this.shawRemoval = new ShawRemoval(K, hparam, distRel, timeRel, loadRel, seed);
        this.haRemoval = new HARemoval(K, history);
        this.shopRemoval = new ShopOrientedRemoval(K, shopDistRel, seed);
        this.deliveryRemoval = new DeliveryOrientedRemoval(K, dlvDistRel, seed);
        this.semiWorstRemoval = new SemiWorstRemoval(K, riderInfo, distMatPtr,
            matrixLength, worstNoise, seed);
        this.worstRemoval = new WorstRemoval(K, riderInfo, distMatPtr,
            matrixLength, worstNoise, seed);
    }

    /**
     * 메인 파괴 함수
     */
    public List<Integer> destroy(Solution sol, Map<String, Integer> ridersAvailable) {
        iteration++;
        currentMethod = getSamplingMethod();
        int nDestroy = getNumberToDestroy();

        DestroyMethods.historyUpdate(sol, K, history);

        List<Integer> idsDestroyed = switch (currentMethod) {
            case 0 -> randomRemoval.destroy(sol, nDestroy);
            case 1 -> distanceRemoval.destroy(sol, nDestroy);
            case 2 -> routeRemoval.destroy(sol, nDestroy);
            case 3 -> shawRemoval.destroy(sol, nDestroy);
            case 4 -> haRemoval.destroy(sol, nDestroy);
            case 5 -> shopRemoval.destroy(sol, nDestroy);
            case 6 -> deliveryRemoval.destroy(sol, nDestroy);
            case 7 -> semiWorstRemoval.destroy(sol, nDestroy);
            case 8 -> worstRemoval.destroy(sol, nDestroy);
            default -> throw new IllegalStateException("Unexpected method: " + currentMethod);
        };

        if (currentMethod != 8) {
            destroyGivenIds(idsDestroyed, sol, ridersAvailable);
        }
        return idsDestroyed;
    }

    /**
     * 확률 업데이트
     */
    public void update(int updateScore) {
        numCalled[currentMethod]++;
        numSuccess[currentMethod] += updateScore;

        if (iteration % updatePeriod == 0) {
            updateProbabilities();
        }
    }

    // ... (나머지 private 메서드들)


    /**
     * 제거할 주문 수 결정
     */
    private int getNumberToDestroy() {
        double prob = minProb + (maxProb - minProb) * random.nextDouble();
        int nDestroy = (int) Math.floor(prob * K);
        return Math.min(hparam.getMaxDestroy(), nDestroy);
    }

    /**
     * 사용할 파괴 메서드 선택
     */
    private int getSamplingMethod() {
        double rand = random.nextDouble();
        double sum = 0.0;
        for (int i = 0; i < numSamplingMethod; i++) {
            sum += probabilityDistribution[i];
            if (rand <= sum) {
                return i;
            }
        }
        return numSamplingMethod - 1;
    }

    /**
     * 확률 분포 초기화
     */
    private void initializeProbabilityDistribution() {
        probabilityDistribution = new double[numSamplingMethod];
        double initialProb = 1.0 / numSamplingMethod;
        Arrays.fill(probabilityDistribution, initialProb);
    }

    /**
     * 거리 관계 행렬 초기화
     */
    private double[][] initializeDistRel() {
        double[][] distRel = new double[K][K];
        double dmax = 0;

        // 최대 거리 찾기
        for (int i = 0; i < matrixLength; i++) {
            for (int j = 0; j < matrixLength; j++) {
                if (i != j) {
                    dmax = Math.max(dmax, distMatPtr[i * matrixLength + j]);
                }
            }
        }

        // 관계 행렬 계산
        for (int i = 0; i < K; i++) {
            for (int j = 0; j < K; j++) {
                double dmean = (distMatPtr[i * matrixLength + j] +
                    distMatPtr[i * matrixLength + (j + K)] +
                    distMatPtr[(i + K) * matrixLength + j] +
                    distMatPtr[(i + K) * matrixLength + (j + K)]) / 4.0;
                distRel[i][j] = dmean / dmax;
            }
        }
        return distRel;
    }

    /**
     * 매장 거리 관계 행렬 초기화
     */
    private double[][] initializeShopDistRel() {
        double[][] shopDistRel = new double[K][K];
        for (int i = 0; i < K; i++) {
            for (int j = 0; j < K; j++) {
                shopDistRel[i][j] = distMatPtr[i * matrixLength + j];
            }
        }
        return shopDistRel;
    }

    /**
     * 배달 거리 관계 행렬 초기화
     */
    private double[][] initializeDlvDistRel() {
        double[][] dlvDistRel = new double[K][K];
        for (int i = 0; i < K; i++) {
            for (int j = 0; j < K; j++) {
                dlvDistRel[i][j] = distMatPtr[(i + K) * matrixLength + (j + K)];
            }
        }
        return dlvDistRel;
    }

    /**
     * 시간 관계 행렬 초기화
     */
    private double[][] initializeTimeRel() {
        double[][] timeRel = new double[K][K];
        double[] windowCenter = new double[K];

        // 시간 창 중심점 계산
        for (int i = 0; i < K; i++) {
            windowCenter[i] = (ordersPtr[i * 3] + ordersPtr[i * 3 + 1]) / 2.0;
        }

        // 최대 시간 차이 찾기
        double tmax = 0;
        for (int i = 0; i < K; i++) {
            for (int j = 0; j < K; j++) {
                timeRel[i][j] = Math.abs(windowCenter[i] - windowCenter[j]);
                tmax = Math.max(tmax, timeRel[i][j]);
            }
        }

        // 정규화
        for (int i = 0; i < K; i++) {
            for (int j = 0; j < K; j++) {
                timeRel[i][j] /= tmax;
            }
        }
        return timeRel;
    }

    /**
     * 부하 관계 행렬 초기화
     */
    private double[][] initializeLoadRel() {
        double[][] loadRel = new double[K][K];
        double lmax = 0;

        // 최대 부하 차이 찾기
        for (int i = 0; i < K; i++) {
            for (int j = 0; j < K; j++) {
                loadRel[i][j] = Math.abs(ordersPtr[i * 3 + 2] - ordersPtr[j * 3 + 2]);
                lmax = Math.max(lmax, loadRel[i][j]);
            }
        }

        // 정규화
        for (int i = 0; i < K; i++) {
            for (int j = 0; j < K; j++) {
                loadRel[i][j] /= lmax;
            }
        }
        return loadRel;
    }

    /**
     * 히스토리 행렬 초기화
     */
    private double[][] initializeHistory() {
        double[][] history = new double[matrixLength][matrixLength];
        for (double[] row : history) {
            Arrays.fill(row, INF);
        }
        return history;
    }

    /**
     * 주어진 ID들의 주문 제거
     */
    private void destroyGivenIds(List<Integer> idsToDestroy, Solution sol,
        Map<String, Integer> ridersAvailable) {
        Map<Integer, Integer> idToBundle = new HashMap<>();
        for (int bundleId = 0; bundleId < sol.size(); bundleId++) {
            for (int id : sol.getSource(bundleId)) {
                idToBundle.put(id, bundleId);
            }
        }

        Set<Integer> bundlesToDelete = new HashSet<>();
        for (int id : idsToDestroy) {
            int bundleId = idToBundle.get(id);
            if (bundlesToDelete.contains(bundleId)) continue;

            Bundle bundle = sol.getSolutions().get(bundleId);
            List<Integer> newSourceOrder = new ArrayList<>(bundle.source());
            List<Integer> newDestOrder = new ArrayList<>(bundle.dest());

            newSourceOrder.remove(Integer.valueOf(id));
            newDestOrder.remove(Integer.valueOf(id));

            if (newSourceOrder.isEmpty()) {
                ridersAvailable.merge(bundle.riderType(), 1, Integer::sum);
                bundlesToDelete.add(bundleId);
            } else {
                Map.Entry<int[], List<Integer>> riderData =
                    riderInfo.prepare(bundle.riderType());
                double newCost = BundleUtils.getBundleCost(
                    newSourceOrder, newDestOrder,
                    riderData.getValue(), distMatPtr, matrixLength);

                sol.getSolutions().set(bundleId, new Bundle(
                    bundle.riderType(), newCost, newSourceOrder, newDestOrder));
            }
        }

        sol.remove(new ArrayList<>(bundlesToDelete));
    }

    /**
     * 확률 분포 업데이트
     */
    private void updateProbabilities() {
        double[] newProb = new double[numSamplingMethod];
        double sum = 0.0;

        // 새로운 확률 계산
        for (int i = 0; i < numSamplingMethod; i++) {
            if (numCalled[i] > 0) {
                newProb[i] = (double) numSuccess[i] / numCalled[i];
                sum += newProb[i];
            }
        }

        // 정규화 및 업데이트
        if (sum > 0) {
            for (int i = 0; i < numSamplingMethod; i++) {
                probabilityDistribution[i] = (1 - rho) * probabilityDistribution[i] +
                    rho * (newProb[i] / sum);
            }
        }

        // 스무딩 적용
        sum = 0.0;
        for (int i = 0; i < numSamplingMethod; i++) {
            probabilityDistribution[i] = smoothingRatio +
                (1 - smoothingRatio * numSamplingMethod) * probabilityDistribution[i];
            sum += probabilityDistribution[i];
        }

        // 최종 정규화
        for (int i = 0; i < numSamplingMethod; i++) {
            probabilityDistribution[i] /= sum;
        }

        // 카운터 초기화
        Arrays.fill(numCalled, 0);
        Arrays.fill(numSuccess, 0);
    }

}