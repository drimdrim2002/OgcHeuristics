package dev.brown.improved.alns.util;

import dev.brown.improved.alns.domain.RiderInfo;
import java.util.*;

public class HelperFunctions {
    private static final double INF = Double.MAX_VALUE;

    /**
     * 주문 정보 배열에서 인덱스 계산
     * @param orderId 주문 ID
     * @param infoIndex 정보 인덱스 (0:ready_time, 1:deadline, 2:volume)
     * @param stride 주문당 정보 개수 (일반적으로 3)
     * @return 배열 인덱스
     */
    private static int getOrderInfo(int orderId, int infoIndex, int stride) {
        return orderId * stride + infoIndex;
    }

    /**
     * 매트릭스에서 인덱스 계산 (시간, 거리 매트릭스용)
     * @param i 출발지 인덱스
     * @param j 도착지 인덱스
     * @param matrixLength 매트릭스 행/열 크기
     * @return 1차원 배열에서의 인덱스
     */
    private static int getMatrixIndex(int i, int j, int matrixLength) {
        return i * matrixLength + j;
    }

    /**
     * 주어진 source, dest에 대해 가능한 모든 라이더 타입을 찾고, 비용과 함께 정렬하여 반환
     */
    public static List<Map.Entry<String, Double>> getOptimalRiderType(
        List<Integer> source,
        List<Integer> dest,
        RiderInfo rider,
        int[] ordersPtr,
        int[] distMatPtr,
        int matrixLength) {

        // 총 물량 계산
        int totalVolume = 0;
        for (int id : source) {
            totalVolume += ordersPtr[getOrderInfo(id, 2, 3)];
        }

        // 각 라이더 타입별 정보 준비
        Map.Entry<int[], List<Integer>> walk = rider.prepare("WALK");
        Map.Entry<int[], List<Integer>> bike = rider.prepare("BIKE");
        Map.Entry<int[], List<Integer>> car = rider.prepare("CAR");

        // 각 라이더 타입별 경로 체크
        Map.Entry<Boolean, Double> resWalk = checkPath(source, dest, walk.getKey(),
            walk.getValue(), ordersPtr, distMatPtr, matrixLength);
        Map.Entry<Boolean, Double> resBike = checkPath(source, dest, bike.getKey(),
            bike.getValue(), ordersPtr, distMatPtr, matrixLength);
        Map.Entry<Boolean, Double> resCar = checkPath(source, dest, car.getKey(),
            car.getValue(), ordersPtr, distMatPtr, matrixLength);

        // 실행 가능한 라이더 타입 수집
        List<Map.Entry<Double, String>> feasibleTypes = new ArrayList<>();
        if (resWalk.getKey() && totalVolume <= walk.getValue().getFirst()) {
            feasibleTypes.add(new AbstractMap.SimpleEntry<>(resWalk.getValue(), "WALK"));
        }
        if (resBike.getKey() && totalVolume <= bike.getValue().getFirst()) {
            feasibleTypes.add(new AbstractMap.SimpleEntry<>(resBike.getValue(), "BIKE"));
        }
        if (resCar.getKey() && totalVolume <= car.getValue().getFirst()) {
            feasibleTypes.add(new AbstractMap.SimpleEntry<>(resCar.getValue(), "CAR"));
        }

        // 비용 기준으로 정렬
        feasibleTypes.sort(Map.Entry.comparingByKey());

        // 결과 형식 변환 (cost, type) -> (type, cost)
        List<Map.Entry<String, Double>> sortedRiderType = new ArrayList<>();
        for (Map.Entry<Double, String> entry : feasibleTypes) {
            sortedRiderType.add(new AbstractMap.SimpleEntry<>(
                entry.getValue(), entry.getKey()));
        }

        return sortedRiderType;
    }

    /**
     * 주어진 경로가 실행 가능한지 체크하고 비용 계산
     */
    public static Map.Entry<Boolean, Double> checkPath(
        List<Integer> source,
        List<Integer> dest,
        int[] timePtr,
        List<Integer> rider,
        int[] ordersPtr,
        int[] distMatPtr,
        int matrixLength) {

        int K = matrixLength / 2;
        int n = source.size();

        // 출발지 순회
        int dist = 0;
        int tCurr = ordersPtr[getOrderInfo(source.getFirst(), 0, 3)];

        for (int i = 1; i < n; i++) {
            dist += distMatPtr[getMatrixIndex(source.get(i-1), source.get(i), matrixLength)];
            tCurr = Math.max(
                tCurr + timePtr[getMatrixIndex(source.get(i-1), source.get(i), matrixLength)],
                ordersPtr[getOrderInfo(source.get(i), 0, 3)]
            );
        }

        // 도착지 순회
        int bef = source.get(n-1);
        for (int i = 0; i < n; i++) {
            int aft = dest.get(i) + K;
            dist += distMatPtr[getMatrixIndex(bef, aft, matrixLength)];
            tCurr += timePtr[getMatrixIndex(bef, aft, matrixLength)];
            bef = aft;

            if (tCurr > ordersPtr[getOrderInfo(dest.get(i), 1, 3)]) {
                return new AbstractMap.SimpleEntry<>(false, INF);
            }
        }

        // 비용 계산: 기본 비용 + 거리 비례 비용
        double cost = rider.get(1) + rider.get(2) * (double)dist / 100;
        return new AbstractMap.SimpleEntry<>(true, cost);
    }
}
