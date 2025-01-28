package dev.brown.alns.destroy;

import dev.brown.domain.Order;
import dev.brown.domain.Solution;
import dev.brown.alns.parameter.HyperParameter;
import java.util.*;

public class HistoricalRemoval implements DestroyOperator {
    private final Random random;
    private final HyperParameter hparam;
    private final Map<String, Double> historicalPairs;  // 주문 쌍의 히스토리 점수
    private static final double INITIAL_SCORE = 1.0;
    private static final double LEARNING_RATE = 0.1;

    public HistoricalRemoval(HyperParameter hparam, Random random) {
        this.hparam = hparam;
        this.random = random;
        this.historicalPairs = new HashMap<>();
    }

    @Override
    public List<Integer> destroy(Solution solution, int numToDestroy) {
        List<Integer> removedOrders = new ArrayList<>();

        // 1. 초기 주문 선택
        List<Order> assignedOrders = getAssignedOrders(solution);
        if (assignedOrders.isEmpty()) {
            return removedOrders;
        }

        Order randomOrder = assignedOrders.get(random.nextInt(assignedOrders.size()));
        removedOrders.add(randomOrder.id());
        randomOrder.rider().removeOrder(randomOrder);
        assignedOrders.remove(randomOrder);

        // 2. 히스토리 기반으로 관련 주문 선택
        while (removedOrders.size() < numToDestroy && !assignedOrders.isEmpty()) {
            Order selectedOrder = selectHistoricalOrder(
                randomOrder,
                assignedOrders,
                removedOrders
            );

            if (selectedOrder == null) break;

            removedOrders.add(selectedOrder.id());
            selectedOrder.rider().removeOrder(selectedOrder);
            assignedOrders.remove(selectedOrder);
        }

        return removedOrders;
    }

    private List<Order> getAssignedOrders(Solution solution) {
        List<Order> assignedOrders = new ArrayList<>();
        for (Order order : solution.orderMap().values()) {
            if (order.rider() != null) {
                assignedOrders.add(order);
            }
        }
        return assignedOrders;
    }

    private Order selectHistoricalOrder(Order referenceOrder,
        List<Order> candidates,
        List<Integer> excludeIds) {
        if (candidates.isEmpty()) return null;

        List<OrderScore> orderScores = new ArrayList<>();

        for (Order candidate : candidates) {
            if (excludeIds.contains(candidate.id())) continue;

            String pairKey = getPairKey(referenceOrder.id(), candidate.id());
            double historicalScore = historicalPairs.getOrDefault(pairKey, INITIAL_SCORE);

            // 노이즈 추가
            double noise = 1.0 + hparam.getShawNoise() * random.nextDouble();
            double finalScore = historicalScore * noise;

            orderScores.add(new OrderScore(candidate, finalScore));
        }

        if (orderScores.isEmpty()) return null;

        // 점수가 높은 순으로 정렬
        orderScores.sort((a, b) -> Double.compare(b.score, a.score));

        // y^p 선택 방식 구현 (p = 3)
        int selectedIndex = (int) (Math.pow(random.nextDouble(), 3)
            * orderScores.size());
        return orderScores.get(selectedIndex).order;
    }

    /**
     * 좋은 해를 발견했을 때 히스토리 정보 업데이트
     */
    public void updateHistory(Solution solution, double improvement) {
        // 현재 해의 각 라이더에 대해
        for (var rider : solution.riderMap().values()) {
            List<Order> orders = rider.orderList();
            // 같은 라이더에 할당된 주문 쌍들의 점수 업데이트
            for (int i = 0; i < orders.size(); i++) {
                for (int j = i + 1; j < orders.size(); j++) {
                    String pairKey = getPairKey(
                        orders.get(i).id(),
                        orders.get(j).id()
                    );
                    double currentScore = historicalPairs.getOrDefault(pairKey, INITIAL_SCORE);
                    double newScore = currentScore * (1 - LEARNING_RATE) +
                        improvement * LEARNING_RATE;
                    historicalPairs.put(pairKey, newScore);
                }
            }
        }
    }

    /**
     * 주문 쌍의 고유 키 생성
     */
    private String getPairKey(int id1, int id2) {
        // 항상 작은 ID가 앞에 오도록 정렬
        return Math.min(id1, id2) + "," + Math.max(id1, id2);
    }

    private static class OrderScore {
        final Order order;
        final double score;

        OrderScore(Order order, double score) {
            this.order = order;
            this.score = score;
        }
    }
}