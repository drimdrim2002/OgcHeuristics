package dev.brown.improved.alns.repair;

import dev.brown.improved.alns.domain.*;
import java.util.*;

/**
 * 라이더 타입 최적화 유틸리티
 */
public class RiderOptimizer {

    /**
     * 주어진 번들들의 라이더 타입을 최적화
     */
    public static void optimizeRiderType(
        List<Integer> indices,
        Solution solution,
        Map<String, Integer> ridersAvailable,
        RiderInfo riderInfo,
        int[] ordersPtr,
        int[] distMatPtr,
        int matrixLength) {

        for (int bundleIndex : indices) {
            String currentRiderType = solution.getRiderType(bundleIndex);
            List<Integer> source = solution.getSource(bundleIndex);
            List<Integer> dest = solution.getDest(bundleIndex);

            // 최적의 라이더 타입과 비용 계산
            List<RiderTypeResult> results = getOptimalRiderType(
                source, dest, riderInfo, ordersPtr, distMatPtr, matrixLength);

            // 더 나은 라이더 타입 찾기
            for (RiderTypeResult result : results) {
                if (result.riderType.equals(currentRiderType)) {
                    break;
                }

                if (ridersAvailable.getOrDefault(result.riderType, 0) > 0) {
                    // 번들의 라이더 타입과 비용만 업데이트
                    solution.bundles.set(
                        bundleIndex,
                        new Bundle(
                            result.riderType,  // 새로운 라이더 타입
                            result.cost,       // 새로운 비용
                            source,            // 기존 출발지 유지
                            dest              // 기존 도착지 유지
                        )
                    );

                    // 라이더 가용성 업데이트
                    ridersAvailable.merge(currentRiderType, 1, Integer::sum);
                    ridersAvailable.merge(result.riderType, -1, Integer::sum);
                    break;
                }
            }
        }

        solution.updateCost();
    }


    /**
     * 최적의 라이더 타입과 비용을 계산
     */
    private static List<RiderTypeResult> getOptimalRiderType(
        List<Integer> source,
        List<Integer> dest,
        RiderInfo riderInfo,
        int[] ordersPtr,
        int[] distMatPtr,
        int matrixLength) {

        List<RiderTypeResult> results = new ArrayList<>();

        // 각 라이더 타입에 대해 비용 계산
        for (String riderType : Arrays.asList("WALK", "BIKE", "CAR")) {
            InvestigationResult res = InvestigationUtils.investigate(
                -1, source, dest, riderInfo, ordersPtr, distMatPtr, matrixLength);

            if (res.getFeasibility(riderType)) {
                results.add(new RiderTypeResult(riderType, res.getCost(riderType)));
            }
        }

        // 비용 기준으로 정렬
        Collections.sort(results);

        return results;
    }

    /**
     * 라이더 타입 결과를 저장하는 보조 클래스
     */
    private static class RiderTypeResult implements Comparable<RiderTypeResult> {
        final String riderType;
        final double cost;

        RiderTypeResult(String riderType, double cost) {
            this.riderType = riderType;
            this.cost = cost;
        }

        @Override
        public int compareTo(RiderTypeResult other) {
            return Double.compare(this.cost, other.cost);
        }
    }
}