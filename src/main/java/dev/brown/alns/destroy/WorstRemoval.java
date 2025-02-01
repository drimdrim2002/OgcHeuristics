package dev.brown.alns.destroy;
import dev.brown.domain.Order;
import dev.brown.domain.Rider;
import dev.brown.domain.Solution;
import dev.brown.alns.parameter.HyperParameter;
import java.util.*;

public class WorstRemoval implements DestroyOperator {
    private final Random random;
    private final HyperParameter hparam;

    public WorstRemoval(HyperParameter hparam, Random random) {
        this.hparam = hparam;
        this.random = random;
    }

    @Override
    public List<Integer> destroy(Solution solution, int numToDestroy) {
        List<OrderCost> orderCosts = new ArrayList<>();

        // 각 주문의 비용 계산
        for (Order order : solution.orderMap().values()) {
//            if (order.getRider() != null) {
//                Rider rider = order.getRider();
//                int currentCost = rider.cost();
//                rider.removeOrder(order);
//                int newCost = rider.cost();
//                int costDiff = currentCost - newCost;
//
//                // 노이즈 추가
//                double noise = 1.0 + hparam.getWorstNoise() * random.nextDouble();
//                orderCosts.add(new OrderCost(order.getId(), costDiff * noise));
//
//                // 원래대로 복구
//                rider.addOrder(order);
//            }
        }

        // 비용이 높은 순으로 정렬
        orderCosts.sort((a, b) -> Double.compare(b.cost, a.cost));

        // 상위 주문들 제거
        List<Integer> removedOrders = new ArrayList<>();
        for (int i = 0; i < Math.min(numToDestroy, orderCosts.size()); i++) {
            int orderId = orderCosts.get(i).orderId;
            Order order = solution.orderMap().get(orderId);
//            if (order.getRider() != null) {
//                order.getRider().removeOrder(order);
//                removedOrders.add(orderId);
//            }
        }

        return removedOrders;
    }

    private static class OrderCost {
        final int orderId;
        final double cost;

        OrderCost(int orderId, double cost) {
            this.orderId = orderId;
            this.cost = cost;
        }
    }
}