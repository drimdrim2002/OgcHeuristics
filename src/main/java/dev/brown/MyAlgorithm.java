package dev.brown;

import dev.brown.domain.Order;
import dev.brown.domain.Rider;
import dev.brown.improved.alns.Runner;
import dev.brown.improved.alns.domain.*;
import dev.brown.improved.alns.parameter.TimeConfig;
import java.time.Instant;
import java.util.*;

public class MyAlgorithm {
    // ... TimeConfig 관련 코드는 동일 ...

    /**
     * 주문 정보를 담는 내부 클래스
     */
    private static class OrderInfo {
        final int start;      // readyTime
        final int end;        // deadline
        final int volume;     // volume

        OrderInfo(Order order) {
            this.start = order.getReadyTime();
            this.end = order.getDeadline();
            this.volume = order.getVolume();
        }

        int[] toArray() {
            return new int[]{start, end, volume};
        }
    }

    /**
     * 데이터 전처리 결과를 담는 클래스
     */
    private static class ProcessedData {
        final int[][] orders;
        final int[][] walkT;
        final List<Integer> walkInfo;
        final int[][] bikeT;
        final List<Integer> bikeInfo;
        final int[][] carT;
        final List<Integer> carInfo;
        final Map<String, Integer> ridersAvailable;

        ProcessedData(
            int[][] orders,
            int[][] walkT, List<Integer> walkInfo,
            int[][] bikeT, List<Integer> bikeInfo,
            int[][] carT, List<Integer> carInfo,
            Map<String, Integer> ridersAvailable
        ) {
            this.orders = orders;
            this.walkT = walkT;
            this.walkInfo = walkInfo;
            this.bikeT = bikeT;
            this.bikeInfo = bikeInfo;
            this.carT = carT;
            this.carInfo = carInfo;
            this.ridersAvailable = ridersAvailable;
        }
    }

    /**
     * 데이터 전처리
     */
    private static ProcessedData preprocess(
        List<Order> allOrders,
        List<Rider> allRiders,
        int[][] distMat) {

        // 주문 정보 처리
        int[][] ordersArray = new int[allOrders.size()][3];
        for (int i = 0; i < allOrders.size(); i++) {
            Order order = allOrders.get(i);
            OrderInfo info = new OrderInfo(order);
            ordersArray[i] = info.toArray();
        }

        // 라이더 타입별 정보 및 가용 수 처리
        Map<String, Integer> ridersAvailable = new HashMap<>();
        Map<String, Rider> riderByType = new HashMap<>();

        for (Rider rider : allRiders) {
            String type = rider.type();
            ridersAvailable.put(type, rider.getCapacity());
            riderByType.put(type, rider);
        }

        // 각 타입별 시간 행렬 및 정보 생성
        int matrixSize = distMat.length;
        int[][] walkT = new int[matrixSize][matrixSize];
        int[][] bikeT = new int[matrixSize][matrixSize];
        int[][] carT = new int[matrixSize][matrixSize];

        // 각 타입별 시간 행렬 계산
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                Rider walkRider = riderByType.get("WALK");
                Rider bikeRider = riderByType.get("BIKE");
                Rider carRider = riderByType.get("CAR");

                walkT[i][j] = calculateTime(distMat[i][j], walkRider);
                bikeT[i][j] = calculateTime(distMat[i][j], bikeRider);
                carT[i][j] = calculateTime(distMat[i][j], carRider);
            }
        }

        // 각 타입별 정보 리스트 생성
        List<Integer> walkInfo = createRiderInfo(riderByType.get("WALK"));
        List<Integer> bikeInfo = createRiderInfo(riderByType.get("BIKE"));
        List<Integer> carInfo = createRiderInfo(riderByType.get("CAR"));

        return new ProcessedData(
            ordersArray,
            walkT, walkInfo,
            bikeT, bikeInfo,
            carT, carInfo,
            ridersAvailable
        );
    }

    /**
     * 거리와 라이더 정보로 이동 시간 계산
     */
    private static int calculateTime(int distance, Rider rider) {
        return (int) Math.round(distance / rider.speed() + rider.serviceTime());
    }

    /**
     * 라이더 정보 리스트 생성
     */
    private static List<Integer> createRiderInfo(Rider rider) {
        return Arrays.asList(
            rider.capa(),          // 용량
            rider.calculateCost(), // 기본 비용
            rider.getVarCost()         // 거리당 비용
        );
    }
    /**
     * 문제 번호별 가중치 반환
     * @param problemNumber 문제 번호
     * @return [wt_weight, tw_weight] 가중치 배열
     */
    private static double[] getWeights(int problemNumber) {
        return switch (problemNumber) {
            case 3, 10 -> new double[]{0.75, 0.75};  // 특수 케이스
            case 0 -> new double[]{0.5, 0.5};        // 기본 케이스
            default -> new double[]{0.2, 1.0};       // 일반 케이스
        };
    }

    /**
     * 문제 번호별 Worst 사용 여부 반환
     */
    private static boolean getUseWorst(int problemNumber) {
        return problemNumber != 5;
    }

    /**
     * 문제 번호별 이전 버전 사용 여부 반환
     */
    private static boolean getUseOldVersion(int problemNumber) {
        return problemNumber == 9;
    }

    /**
     * 하이퍼파라미터 설정
     */
    private static void configureHyperParameters(Map<String, Float> hParam, int problemNumber) {
        double[] weights = getWeights(problemNumber);
        hParam.put("wt_weight", (float) weights[0]);
        hParam.put("tw_weight", (float) weights[1]);
        hParam.put("use_worst", getUseWorst(problemNumber) ? 1.0f : 0.0f);
        hParam.put("use_old", getUseOldVersion(problemNumber) ? 1.0f : 0.0f);

        // 기본값 설정
        hParam.putIfAbsent("destroy_ratio", 0.15f);
        hParam.putIfAbsent("noise_param", 0.1f);
        hParam.putIfAbsent("initial_temperature", 100.0f);
    }

    /**
     * ALNS와 SP 반복 실행
     */
    private static Solution runIterations(
        int K,
        double timeLimit,
        TimeConfig timeConfig,
        ProcessedData data,
        int[][] solEdge,
        int[] solRider,
        List<Bundle> initBundle,
        int[][] distMat,
        Map<String, Double> hParam,
        long seed,
        int verbose,
        Instant startTime) {

        Solution bestSolution = null;
        double bestCost = Double.MAX_VALUE;

        for (int i = 0; i < timeConfig.timeList.length; i++) {
            if (verbose >= 1) {
                System.out.printf("Iteration %d/%d%n", i + 1, timeConfig.timeList.length);
            }

            // 시간 계산
            double alnsTime = timeConfig.timeList[i][0];
            double spTime = timeConfig.timeList[i][1];

            if (i == timeConfig.timeList.length - 1) {
                double leftTime = timeLimit - getElapsedTime(startTime);
                spTime = timeConfig.timeList[i][1];
                alnsTime = leftTime - spTime - timeConfig.spareTime;
            }

            // ALNS 실행
            if (verbose >= 1) {
                System.out.printf("Running ALNS (%.2f seconds)%n", alnsTime);
            }

            Map.Entry<SolutionFormat, GurobiInput> result = Runner.run(
                alnsTime,
                (float) timeConfig.annealerModifyRatio,
                seed,
                solEdge,
                solRider,
                initBundle,
                data.orders,
                data.walkT,
                data.walkInfo,
                data.bikeT,
                data.bikeInfo,
                data.carT,
                data.carInfo,
                data.ridersAvailable,
                distMat,
                hParam,
                getUsePower(i),
                verbose == 2
            );

            // Gurobi SP 실행
            if (verbose >= 1) {
                System.out.printf("Running SP (%.2f seconds)%n", spTime);
            }

            Solution currentSolution = runGurobiSP(
                result.getValue(),
                data.ridersAvailable,
                spTime,
                verbose,
                distMat,
                getFirstHeurTime(i)
            );

            // 최선해 업데이트
            if (currentSolution != null && currentSolution.getCost() < bestCost) {
                bestSolution = currentSolution;
                bestCost = currentSolution.getCost();

                // 다음 반복을 위한 초기해 업데이트
                updateInitialSolution(currentSolution, solEdge, solRider, initBundle);
            }
        }

        return bestSolution;
    }

    /**
     * Gurobi SP 실행
     */
    private static Solution runGurobiSP(
        GurobiInput input,
        Map<String, Integer> ridersAvailable,
        double timeLimit,
        int verbose,
        int[][] distMat,
        double heurTime) {
        // Gurobi SP 솔버 구현 필요
        // 현재는 임시로 null 반환
        return null;
    }

    /**
     * 엣지 행렬과 라이더 배정 업데이트
     */
    private static void updateEdgeMatrixAndRiders(
        Solution solution,
        int[][] solEdge,
        int[] solRider) {

        // 행렬 초기화
        for (int[] row : solEdge) {
            Arrays.fill(row, 0);
        }
        Arrays.fill(solRider, 0);

        // 각 번들에 대해
        for (int i = 0; i < solution.size(); i++) {
            List<Integer> source = solution.getSource(i);
            List<Integer> dest = solution.getDest(i);
            String riderType = solution.getRiderType(i);

            // 라이더 타입 설정
            for (int orderId : source) {
                solRider[orderId] = getRiderTypeIndex(riderType);
            }

            // 엣지 설정
            // 픽업 순서대로 엣지 설정
            for (int j = 0; j < source.size() - 1; j++) {
                solEdge[source.get(j)][source.get(j + 1)] = 1;
            }

            // 마지막 픽업에서 첫 배달로 연결
            if (!source.isEmpty() && !dest.isEmpty()) {
                solEdge[source.get(source.size() - 1)][dest.get(0)] = 1;
            }

            // 배달 순서대로 엣지 설정
            for (int j = 0; j < dest.size() - 1; j++) {
                solEdge[dest.get(j)][dest.get(j + 1)] = 1;
            }
        }
    }

    /**
     * 라이더 타입을 인덱스로 변환
     */
    private static int getRiderTypeIndex(String riderType) {
        return switch (riderType) {
            case "WALK" -> 0;
            case "BIKE" -> 1;
            case "CAR" -> 2;
            default -> throw new IllegalArgumentException("Unknown rider type: " + riderType);
        };
    }

    /**
     * 결과를 문자열 리스트로 변환
     */
    private static List<String> convertToResult(Solution solution) {
        if (solution == null) {
            return Collections.emptyList();
        }

        List<String> result = new ArrayList<>();

        // 전체 비용 추가
        result.add(String.format("Total Cost: %.2f", solution.getCost()));

        // 각 번들 정보 추가
        for (int i = 0; i < solution.size(); i++) {
            result.add(String.format(
                "Bundle[%d] Type=%s, Cost=%.2f, Orders=%s -> %s",
                i,
                solution.getRiderType(i),
                solution.getCost(i),
                solution.getSource(i).toString(),
                solution.getDest(i).toString()
            ));
        }

        return result;
    }

    /**
     * Gurobi SP 실행 결과를 Solution으로 변환
     */
    private static Solution convertGurobiResult(
        List<Bundle> bundles,
        double totalCost) {
        return new Solution(bundles, totalCost);
    }
    /**
     * 초기해 업데이트
     */
    private static void updateInitialSolution(
        Solution solution,
        int[][] solEdge,
        int[] solRider,
        List<Bundle> initBundle) {

        // 엣지 행렬과 라이더 배정 업데이트
        updateEdgeMatrixAndRiders(solution, solEdge, solRider);

        // 번들 리스트 업데이트
        initBundle.clear();
        initBundle.addAll(solution.getBundles());
    }


    /**
     * 경과 시간 계산 (초 단위)
     */
    private static double getElapsedTime(Instant startTime) {
        return (Instant.now().toEpochMilli() - startTime.toEpochMilli()) / 1000.0;
    }

    /**
     * Power 사용 여부 결정
     */
    private static boolean getUsePower(int iteration) {
        return iteration > 0;  // 첫 반복에서는 사용하지 않음
    }

    /**
     * 첫 휴리스틱 시간 설정
     */
    private static double getFirstHeurTime(int iteration) {
        return iteration == 0 ? 3.0 : -1.0;
    }

    /**
     * 일반적인 시간 설정 반환
     */
    private static TimeConfig getGeneralTimeConfig(int K, double timeLimit) {
        return TimeConfig.createGeneralConfig(K, timeLimit);
    }


}