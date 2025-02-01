package dev.brown.alns.destroy;

import dev.brown.domain.Order;
import dev.brown.domain.Solution;
import dev.brown.alns.parameter.HyperParameter;
import java.util.*;

public class DistanceOrientedRemoval implements DestroyOperator {
    private final Random random;
    private final HyperParameter params;

    public DistanceOrientedRemoval(HyperParameter params, Random random) {
        this.params = params;
        this.random = random;
    }

    @Override
    public List<Integer> destroy(Solution solution, int numToDestroy) {
        List<Integer> removedOrders = new ArrayList<>();

        // 1. 첫 번째 주문을 무작위로 선택
        int firstOrder = selectRandomOrder(solution);
        removedOrders.add(firstOrder);

        // 2. 거리 기반으로 관련된 주문들 선택
        while (removedOrders.size() < numToDestroy) {
            int selectedOrder = selectRelatedOrder(solution, removedOrders);
            removedOrders.add(selectedOrder);
        }

        return removedOrders;
    }

    /**
     * 거리 기반 관련성을 계산하고 관련된 주문 선택
     */
    private int selectRelatedOrder(Solution solution, List<Integer> removedOrders) {
        Map<Integer, Double> orderScores = new HashMap<>();

        // 아직 제거되지 않은 모든 주문에 대해
        for (Order order : solution.getActiveOrders()) {
            if (!removedOrders.contains(order.id())) {
                // 이미 제거된 주문들과의 평균 거리 계산
                double avgDistance = calculateAverageDistance(order, removedOrders, solution);
                // 거리가 가까울수록 높은 점수
                orderScores.put(order.id(), 1.0 / (avgDistance + 1));
            }
        }

        // 확률적 선택 (거리가 가까운 주문이 선택될 확률이 높음)
        return selectOrderProbabilistically(orderScores);
    }

    /**
     * 주문 간의 평균 거리 계산
     */
    private double calculateAverageDistance(Order order, List<Integer> removedOrders, Solution solution) {
        double totalDistance = 0.0;

        for (int removedOrderId : removedOrders) {
            Order removedOrder = solution.orderMap().get(removedOrderId);

            // 픽업 지점 간 거리
            double pickupDistance = calculateDistance(
                order.shopLat(), order.shopLon(),
                removedOrder.shopLat(), removedOrder.shopLon()
            );

            // 배달 지점 간 거리
            double deliveryDistance = calculateDistance(
                order.dlvryLat(), order.dlvryLon(),
                removedOrder.dlvryLat(), removedOrder.dlvryLon()
            );

            // 평균 거리에 가중치 적용
            totalDistance += (pickupDistance + deliveryDistance) / 2.0;
        }

        return totalDistance / removedOrders.size();
    }

    /**
     * 두 지점 간의 거리 계산 (Haversine 공식 사용)
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
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
     * 점수에 기반한 확률적 선택
     */
    private int selectOrderProbabilistically(Map<Integer, Double> orderScores) {
        double total = orderScores.values().stream().mapToDouble(d -> d).sum();
        double r = random.nextDouble() * total;
        double sum = 0.0;

        for (Map.Entry<Integer, Double> entry : orderScores.entrySet()) {
            sum += entry.getValue();
            if (r <= sum) {
                return entry.getKey();
            }
        }

        // 예외 처리
        return orderScores.keySet().iterator().next();
    }

    /**
     * 무작위 주문 선택
     */
    private int selectRandomOrder(Solution solution) {
        List<Integer> activeOrders = new ArrayList<>(solution.getActiveOrderIds());
        return activeOrders.get(random.nextInt(activeOrders.size()));
    }
}