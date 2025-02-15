package dev.brown.improved.alns;

import dev.brown.improved.alns.algorithm.Annealer;
import dev.brown.improved.alns.domain.*;
import dev.brown.improved.alns.destroy.AdaptiveDestroyer;
import dev.brown.improved.alns.parameter.HyperParameter;
import dev.brown.improved.alns.repair.RepairBuilder;
import dev.brown.improved.alns.storage.BundleStorage;
import java.time.Instant;
import java.util.*;

/**
 * ALNS 알고리즘의 실행을 관리하는 클래스
 */
public class Runner {
    private static final int DEFAULT_BUNDLE_STORAGE_CAPACITY = 10000;  // 적절한 값으로 조정 필요

    /**
     * ALNS 알고리즘 실행
     */
    public static Map.Entry<SolutionFormat, GurobiInput> run(
        double totalTimeLimit,
        float annealerModifyRatio,
        long seed,
        int[][] solEdge,      // (2*K, 2*K)
        int[] solRider,       // (K,)
        List<Bundle> initBundle,
        int[][] orders,       // K by 3
        int[][] walkT,        // 2K by 2K
        List<Integer> walkInfo,
        int[][] bikeT,
        List<Integer> bikeInfo,
        int[][] carT,
        List<Integer> carInfo,
        Map<String, Integer> ridersAvailable,
        int[][] distMat,
        Map<String, Double> pyHparam,
        boolean usePower,
        boolean verbose) {

        Instant startTime = Instant.now();

        int l = carT.length;     // this is equal to 2K
        int K = l / 2;

        if (verbose) {
            System.out.println("size_t l: " + l);
            System.out.println("K: " + K);
        }

        // 하이퍼파라미터 초기화
        HyperParameter hparam = new HyperParameter(pyHparam);

        // 초기 설정
        boolean firstAlns = initBundle.isEmpty();
        Solution bestSolution = new Solution();
        RiderInfo riderInfo = new RiderInfo(
            flattenMatrix(walkT),
            walkInfo,
            flattenMatrix(bikeT),
            bikeInfo,
            flattenMatrix(carT),
            carInfo
        );
        BundleStorage bundleStorage = new BundleStorage(DEFAULT_BUNDLE_STORAGE_CAPACITY);

        // 초기 번들이 있다면 추가
        if (!firstAlns) {
            Solution initialSolution = new Solution();
            for (Bundle bundle : initBundle) {
                initialSolution.append(bundle);
            }
            bundleStorage.append(initialSolution);
        }

        // 파괴자와 빌더 초기화
        AdaptiveDestroyer destroyer = new AdaptiveDestroyer(
            K, flattenOrders(orders), riderInfo, flattenMatrix(distMat),
            hparam, seed
        );

        RepairBuilder builder = new RepairBuilder(
            K, flattenOrders(orders), riderInfo, flattenMatrix(distMat),
            usePower, hparam, seed
        );

        // 초기 솔루션 생성
        if (!firstAlns) {
            bestSolution = inputToSolution(
                solEdge, solRider, riderInfo, distMat, ridersAvailable, K);
        } else {
            List<Integer> allIds = new ArrayList<>();
            for (int i = 0; i < K; i++) {
                allIds.add(i);
            }
            builder.build(allIds, bestSolution, ridersAvailable);
        }
        bundleStorage.append(bestSolution);

        if (verbose) {
            System.out.printf("Initial solution built. Time taken: %.2f seconds%n",
                getTimeElapsed(startTime));
            System.out.printf("Initial cost: %.2f, Problem size (K): %d%n",
                bestSolution.getCost(), K);
        }

        // SA 초기화
        double temperature = firstAlns ? bestSolution.getCost() * 0.003 : 0;
        Annealer annealer = new Annealer(
            temperature, annealerModifyRatio, K, seed);

        // 반복적 업데이트
        int iterations = MainIteration.sequentialDestroyInsert(
            totalTimeLimit,
            startTime,
            bestSolution,
            ridersAvailable,
            annealer,
            destroyer,
            builder,
            bundleStorage,
            hparam,
            verbose
        );

        if (verbose) {
            printFinalStats(K, iterations, annealer, destroyer, bestSolution);
        }

        return Map.entry(
            bestSolution.extract(),
            bundleStorage.extract(bestSolution, K, riderInfo, distMat)
        );
    }

    /**
     * 2차원 배열을 1차원으로 변환
     */
    private static int[] flattenMatrix(int[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        int[] flattened = new int[rows * cols];

        for (int i = 0; i < rows; i++) {
            System.arraycopy(matrix[i], 0, flattened, i * cols, cols);
        }
        return flattened;
    }

    /**
     * orders 배열을 1차원으로 변환
     */
    private static int[] flattenOrders(int[][] orders) {
        return flattenMatrix(orders);
    }

    /**
     * 입력 데이터를 Solution 객체로 변환
     */
    private static Solution inputToSolution(
        int[][] solEdge,
        int[] solRider,
        RiderInfo riderInfo,
        int[][] distMat,
        Map<String, Integer> ridersAvailable,
        int K) {

        Solution solution = new Solution();
        boolean[] visited = new boolean[K];
        int matrixLength = 2 * K;

        // 각 주문에 대해 처리
        for (int startOrder = 0; startOrder < K; startOrder++) {
            // 이미 처리된 주문은 건너뜜기
            if (visited[startOrder]) {
                continue;
            }

            // 새로운 번들 생성을 위한 데이터 수집
            List<Integer> sourceOrder = new ArrayList<>();
            List<Integer> destOrder = new ArrayList<>();
            String riderType = getRiderType(solRider[startOrder]);

            // 첫 주문 추가
            sourceOrder.add(startOrder);
            destOrder.add(startOrder);
            visited[startOrder] = true;

            // 연결된 주문들 찾기
            int currentOrder = startOrder;
            while (true) {
                int nextOrder = -1;

                // source to source
                for (int j = 0; j < K; j++) {
                    if (!visited[j] && solEdge[currentOrder][j] == 1) {
                        nextOrder = j;
                        break;
                    }
                }

                // source to destination
                if (nextOrder == -1) {
                    for (int j = K; j < matrixLength; j++) {
                        if (solEdge[currentOrder][j] == 1) {
                            nextOrder = j - K;
                            break;
                        }
                    }
                }

                if (nextOrder == -1) {
                    break;
                }

                // 새로운 주문 추가
                if (!visited[nextOrder]) {
                    sourceOrder.add(nextOrder);
                    destOrder.add(nextOrder);
                    visited[nextOrder] = true;
                }
                currentOrder = nextOrder;
            }

            // 번들 비용 계산
            Map.Entry<int[], List<Integer>> riderData = riderInfo.prepare(riderType);
            double bundleCost = calculateBundleCost(
                sourceOrder,
                destOrder,
                riderData.getValue(),
                distMat,
                matrixLength
            );

            // 번들 생성 및 솔루션에 추가
            Bundle bundle = new Bundle(riderType, bundleCost, sourceOrder, destOrder);
            solution.append(bundle);

            // 사용 가능한 라이더 수 업데이트
            ridersAvailable.merge(riderType, -1, Integer::sum);
        }

        return solution;
    }

    /**
     * 라이더 타입 ID를 문자열로 변환
     */
    private static String getRiderType(int riderId) {
        return switch (riderId) {
            case 0 -> "WALK";
            case 1 -> "BIKE";
            case 2 -> "CAR";
            default -> throw new IllegalArgumentException("Invalid rider type ID: " + riderId);
        };
    }

    /**
     * 번들 비용 계산
     */
    private static double calculateBundleCost(
        List<Integer> sourceOrder,
        List<Integer> destOrder,
        List<Integer> riderParams,
        int[][] distMat,
        int matrixLength) {

        if (sourceOrder.isEmpty()) {
            return 0.0;
        }

        double cost = riderParams.get(1); // 기본 비용
        int m = sourceOrder.size();

        // 출발지 간 이동 비용
        for (int i = 1; i < m; i++) {
            cost += riderParams.get(2) *
                distMat[sourceOrder.get(i-1)][sourceOrder.get(i)] / 100.0;
        }

        // 마지막 출발지에서 첫 도착지까지의 비용
        cost += riderParams.get(2) *
            distMat[sourceOrder.get(m-1)][destOrder.get(0) + matrixLength/2] / 100.0;

        // 도착지 간 이동 비용
        for (int i = 1; i < m; i++) {
            cost += riderParams.get(2) *
                distMat[destOrder.get(i-1) + matrixLength/2]
                    [destOrder.get(i) + matrixLength/2] / 100.0;
        }

        return cost;
    }

    /**
     * 최종 통계 출력
     */
    private static void printFinalStats(
        int K,
        int iterations,
        Annealer annealer,
        AdaptiveDestroyer destroyer,
        Solution bestSolution) {

        System.out.printf("size of problem: %d, total iter: %d%n", K, iterations);
        System.out.printf("Current temperature: %.2f%n", annealer.getTemperature());
        System.out.print("Destroyer probabilities: ");
        // destroyer의 확률 분포 출력
        System.out.println();
        System.out.println("Optimization complete.");
        System.out.printf("Final cost: %.2f, Total iterations: %d%n",
            bestSolution.getCost(), iterations);
    }

    /**
     * 경과 시간 계산
     */
    private static double getTimeElapsed(Instant startTime) {
        return (Instant.now().toEpochMilli() - startTime.toEpochMilli()) / 1000.0;
    }
}