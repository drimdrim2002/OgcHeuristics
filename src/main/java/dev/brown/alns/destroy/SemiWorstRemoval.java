package dev.brown.alns.destroy;

import dev.brown.domain.CalculationResult;
import dev.brown.domain.Order;
import dev.brown.domain.Rider;
import dev.brown.domain.Solution;
import dev.brown.alns.parameter.HyperParameter;
import java.util.*;

public class SemiWorstRemoval implements DestroyOperator {
    private final Random random;
    private final HyperParameter params;

    public SemiWorstRemoval(HyperParameter params, Random random) {
        this.params = params;
        this.random = random;
    }

    @Override
    public List<Integer> destroy(Solution solution, int numToDestroy) {
        // 최대 제거 수 제한
        int actualNumToDestroy = Math.min(numToDestroy, params.getMaxDestroy());
        List<Integer> removedOrders = new ArrayList<>();
        Map<Integer, Double> orderCosts = calculateOrderCosts(solution);

        while (removedOrders.size() < actualNumToDestroy) {
            int selectedOrder = selectSemiWorstOrder(orderCosts, removedOrders);
            removedOrders.add(selectedOrder);
            updateCosts(solution, orderCosts, selectedOrder);
        }

        return removedOrders;
    }

    /**
     * 각 주문의 제거 비용 계산
     */
    private Map<Integer, Double> calculateOrderCosts(Solution solution) {
        Map<Integer, Double> orderCosts = new HashMap<>();

//        for (Order order : solution.orderMap().values()) {
//            if (order.getRider() != null) {
//                double removalCost = calculateRemovalCost(order, solution);
//                orderCosts.put(order.getId(), removalCost);
//            }
//        }

        return orderCosts;
    }

    /**
     * 주문 제거 시의 비용 계산
     */
    private double calculateRemovalCost(Order order, Solution solution) {
//        Rider rider = order.getRider();
//
//        // 1. 현재 상태 저장
//        int originalCost = rider.cost();
//        boolean originalValid = rider.isValid();
//
//        // 2. 주문 임시 제거
//        rider.removeOrder(order);
//
//        // 3. 새로운 비용 계산
//        CalculationResult result = rider.calculate();
//        int newCost = result != null && result.isFeasible() ? result.cost() : Integer.MAX_VALUE;
//
//        // 4. 원래 상태로 복원
//        rider.addOrder(order);
//
//        // 5. 비용 차이 및 페널티 계산
//        double costDiff = originalCost - newCost;
//
//        // 시간 제약 위반 페널티
//        if (!originalValid) {
//            costDiff += calculateTimeWindowPenalty(order);
//        }
//
//        return costDiff;
        return 0.0;
    }

    /**
     * 시간 제약 위반 페널티 계산
     */
    private double calculateTimeWindowPenalty(Order order) {
//        int currentTime = order.getRider().getCurrentTime();
//        int deadline = order.getDeadline();
//
//        if (currentTime > deadline) {
//            return (currentTime - deadline) * params.getTwWeight();
//        }
        return 0.0;
    }

    /**
     * Semi-worst 주문 선택
     */
    private int selectSemiWorstOrder(Map<Integer, Double> orderCosts,
        List<Integer> excludeOrders) {
        List<Map.Entry<Integer, Double>> sortedCosts = new ArrayList<>(
            orderCosts.entrySet().stream()
                .filter(e -> !excludeOrders.contains(e.getKey()))
                .toList()
        );

        sortedCosts.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        // worstNoise 파라미터를 사용한 확률적 선택
        double r = Math.pow(random.nextDouble(), params.getWorstNoise());
        int index = (int)(r * Math.min(sortedCosts.size(), params.getConsiderSize()));

        return sortedCosts.get(Math.min(index, sortedCosts.size() - 1)).getKey();
    }

    /**
     * 주문 제거 후 비용 업데이트
     */
    private void updateCosts(Solution solution,
        Map<Integer, Double> orderCosts,
        int removedOrderId) {
        Order removedOrder = solution.orderMap().get(removedOrderId);
//        Rider affectedRider = removedOrder.getRider();
//
//         같은 라이더의 주문들 업데이트
//        for (Order order : affectedRider.orderList()) {
//            if (order.getId() != removedOrderId) {
//                double newCost = calculateRemovalCost(order, solution);
//                orderCosts.put(order.getId(), newCost);
//            }
//        }

        // 제거된 주문의 비용 제거
        orderCosts.remove(removedOrderId);

        // 시간 윈도우가 겹치는 주문들 업데이트
        if (params.isUseOld()) {
            updateTimeWindowOverlappingOrders(solution, orderCosts, removedOrder);
        }
    }

    /**
     * 시간 윈도우가 겹치는 주문들의 비용 업데이트
     */
    private void updateTimeWindowOverlappingOrders(Solution solution,
        Map<Integer, Double> orderCosts,
        Order removedOrder) {
        for (Order order : solution.orderMap().values()) {
//            if (order.getRider() != null &&
//                order.getId() != removedOrder.getId() &&
//                isTimeWindowOverlapping(order, removedOrder)) {
//
//                double newCost = calculateRemovalCost(order, solution);
//                orderCosts.put(order.getId(), newCost);
//            }
        }
    }

    /**
     * 시간 윈도우 겹침 여부 확인
     */
    private boolean isTimeWindowOverlapping(Order order1, Order order2) {
        // timeMargin을 고려한 시간 윈도우 체크
        double timeMargin = params.getTimeMargin();

        int start1 = order1.getReadyTime();
        int end1 = (int)(order1.getDeadline() * (1 + timeMargin));
        int start2 = order2.getReadyTime();
        int end2 = (int)(order2.getDeadline() * (1 + timeMargin));

        return !(end1 < start2 || end2 < start1);
    }
}