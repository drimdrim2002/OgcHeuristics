package dev.brown.improved.alns.repair;

import dev.brown.improved.alns.domain.*;
import dev.brown.improved.alns.util.HelperFunctions;
import java.util.*;

/**
 * 솔루션의 라이더 타입을 최적화하는 클래스
 */
public class OptimizeRider {
    private final int[] ordersPtr;
    private final RiderInfo riderInfo;
    private final int[] distMatPtr;
    private final int matrixLength;

    /**
     * OptimizeRider 생성자
     *
     * @param ordersPtr 주문 정보 배열
     * @param riderInfo 라이더 정보
     * @param distMatPtr 거리 행렬
     * @param matrixLength 행렬 크기
     */
    public OptimizeRider(int[] ordersPtr, RiderInfo riderInfo, int[] distMatPtr, int matrixLength) {
        this.ordersPtr = ordersPtr;
        this.riderInfo = riderInfo;
        this.distMatPtr = distMatPtr;
        this.matrixLength = matrixLength;
    }

    /**
     * 인덱스 목록에 해당하는 솔루션의 라이더 타입을 최적화
     *
     * @param indices 처리할 번들 인덱스 목록
     * @param solution 솔루션 객체
     * @param ridersAvailable 사용 가능한 라이더 맵
     */
    public void optimizeRiderType(List<Integer> indices, Solution solution, Map<String, Integer> ridersAvailable) {
        // 항상 세 가지를 업데이트: solution.bundles, solution.cost, ridersAvailable
        for (Integer i : indices) {
            String riderType = solution.getRiderType(i);
            List<Integer> source = solution.getSource(i);
            List<Integer> dest = solution.getDest(i);

            // 최적의 라이더 타입 목록 얻기
            List<Map.Entry<String, Double>> optimalTypes =
                HelperFunctions.getOptimalRiderType(source, dest, riderInfo, ordersPtr, distMatPtr, matrixLength);

            // 더 나은 라이더 타입 찾기
            for (Map.Entry<String, Double> entry : optimalTypes) {
                String newRiderType = entry.getKey();
                Double newCost = entry.getValue();

                // 현재 타입이면 더 이상 조사하지 않음
                if (newRiderType.equals(riderType)) break;

                // 사용 가능한 라이더가 있는지 확인
                if (ridersAvailable.getOrDefault(newRiderType, 0) > 0) {
                    // 새 번들 생성
                    Bundle newBundle = new Bundle(
                        newRiderType,
                        newCost,
                        source,
                        dest
                    );

                    // 솔루션 업데이트
                    solution.updateBundle(i, newBundle);

                    // 라이더 가용성 업데이트
                    ridersAvailable.merge(riderType, 1, Integer::sum);
                    ridersAvailable.merge(newRiderType, -1, Integer::sum);
                    break;
                }
            }
        }

        // 총 비용 업데이트
        solution.updateCost();
    }

    /**
     * 경로 타당성 검사 및 비용 계산을 위한 조사 메서드
     *
     * @param source 출발지 목록
     * @param dest 도착지 목록
     * @return 조사 결과
     */
    public InvestigationResult investigate(List<Integer> source, List<Integer> dest) {
        // 최적의 라이더 타입 얻기
        List<Map.Entry<String, Double>> optimalTypes =
            HelperFunctions.getOptimalRiderType(source, dest, riderInfo, ordersPtr, distMatPtr, matrixLength);

        // InvestigationResult 생성
        Map<String, Boolean> feasibility = new HashMap<>();
        Map<String, Double> costs = new HashMap<>();
        Map<String, List<Integer>> sources = new HashMap<>();
        Map<String, List<Integer>> destinations = new HashMap<>();
        List<String> optimalOrder = new ArrayList<>();

        // 결과 채우기
        for (Map.Entry<String, Double> entry : optimalTypes) {
            String type = entry.getKey();
            Double cost = entry.getValue();

            optimalOrder.add(type);
            feasibility.put(type, true);
            costs.put(type, cost);
            sources.put(type, new ArrayList<>(source));
            destinations.put(type, new ArrayList<>(dest));
        }

        // 불가능한 라이더 타입 처리
        for (String type : Arrays.asList("WALK", "BIKE", "CAR")) {
            if (!feasibility.containsKey(type)) {
                feasibility.put(type, false);
                costs.put(type, Double.MAX_VALUE);
                sources.put(type, new ArrayList<>());
                destinations.put(type, new ArrayList<>());
            }
        }

        return new InvestigationResult(
            feasibility,
            costs,
            sources,
            destinations,
            optimalOrder
        );
    }
}