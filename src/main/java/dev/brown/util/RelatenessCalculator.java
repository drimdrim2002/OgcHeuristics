package dev.brown.util;
import dev.brown.domain.Order;

public class RelatenessCalculator {

    /**
     * 두 주문 간의 관련성을 계산
     */
    public static double calculateRelatedness(Order order1, Order order2,
        float shawD, float shawT, float shawL) {
        double distanceRelated = calculateDistanceRelatedness(order1, order2);
        double timeRelated = calculateTimeRelatedness(order1, order2);
        double loadRelated = calculateLoadRelatedness(order1, order2);

        return shawD * distanceRelated +
            shawT * timeRelated +
            shawL * loadRelated;
    }

    /**
     * 거리 기반 관련성 계산
     */
    private static double calculateDistanceRelatedness(Order order1, Order order2) {
        // 픽업 지점 간의 거리
        double pickupDistance = calculateDistance(
            order1.shopLat(), order1.shopLon(),
            order2.shopLat(), order2.shopLon()
        );

        // 배달 지점 간의 거리
        double deliveryDistance = calculateDistance(
            order1.dlvryLat(), order1.dlvryLon(),
            order2.dlvryLat(), order2.dlvryLon()
        );

        return normalizeDistance(pickupDistance + deliveryDistance);
    }

    /**
     * 시간 기반 관련성 계산
     */
    private static double calculateTimeRelatedness(Order order1, Order order2) {
        int readyTimeDiff = Math.abs(order1.readyTime() - order2.readyTime());
        int deadlineDiff = Math.abs(order1.deadline() - order2.deadline());

        return normalizeTime(readyTimeDiff + deadlineDiff);
    }

    /**
     * 적재량 기반 관련성 계산
     */
    private static double calculateLoadRelatedness(Order order1, Order order2) {
        return normalizeLoad(Math.abs(order1.volume() - order2.volume()));
    }

    /**
     * 두 지점 간의 거리 계산 (Haversine formula)
     */
    private static double calculateDistance(double lat1, double lon1,
        double lat2, double lon2) {
        final int R = 6371; // 지구의 반경 (km)

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
            + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
            * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    /**
     * 거리값 정규화 (0~1 사이 값으로)
     */
    private static double normalizeDistance(double distance) {
        final double MAX_DISTANCE = 50.0; // 최대 예상 거리 (km)
        return Math.min(1.0, distance / MAX_DISTANCE);
    }

    /**
     * 시간값 정규화 (0~1 사이 값으로)
     */
    private static double normalizeTime(int timeDiff) {
        final int MAX_TIME_DIFF = 7200; // 최대 2시간
        return Math.min(1.0, timeDiff / (double)MAX_TIME_DIFF);
    }

    /**
     * 적재량값 정규화 (0~1 사이 값으로)
     */
    private static double normalizeLoad(int loadDiff) {
        final int MAX_LOAD_DIFF = 100; // 최대 적재량 차이
        return Math.min(1.0, loadDiff / (double)MAX_LOAD_DIFF);
    }
}