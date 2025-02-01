package dev.brown.alns.destroy;
import dev.brown.domain.Order;
import dev.brown.domain.Solution;
import dev.brown.alns.parameter.HyperParameter;
import dev.brown.util.RelatenessCalculator;
import java.util.*;

public class ShawRemoval implements DestroyOperator {
    private final Random random;
    private final HyperParameter hparam;

    public ShawRemoval(HyperParameter hparam, Random random) {
        this.hparam = hparam;
        this.random = random;
    }

    @Override
    public List<Integer> destroy(Solution solution, int numToDestroy) {
        List<Integer> removedOrders = new ArrayList<>();

        // 1. 초기 주문 무작위 선택
        List<Order> assignedOrders = getAssignedOrders(solution);
        if (assignedOrders.isEmpty()) {
            return removedOrders;
        }

        Order randomOrder = assignedOrders.get(random.nextInt(assignedOrders.size()));
        removedOrders.add(randomOrder.getId());
//        randomOrder.getRider().removeOrder(randomOrder);

        // 2. 관련성이 높은 주문들 선택
        while (removedOrders.size() < numToDestroy && !assignedOrders.isEmpty()) {
            Order selectedOrder = selectRelatedOrder(
                randomOrder,
                assignedOrders,
                removedOrders
            );

            if (selectedOrder == null) break;

            removedOrders.add(selectedOrder.getId());
//            selectedOrder.getRider().removeOrder(selectedOrder);
            assignedOrders.remove(selectedOrder);
        }

        return removedOrders;
    }

    private List<Order> getAssignedOrders(Solution solution) {
        List<Order> assignedOrders = new ArrayList<>();
        for (Order order : solution.orderMap().values()) {
//            if (order.getRider() != null) {
//                assignedOrders.add(order);
//            }
        }
        return assignedOrders;
    }

    private Order selectRelatedOrder(Order referenceOrder,
        List<Order> candidates,
        List<Integer> excludeIds) {
        if (candidates.isEmpty()) return null;

        // 관련성에 따른 주문 정렬
        List<OrderRelatedness> orderRelatednessList = new ArrayList<>();

        for (Order candidate : candidates) {
            if (excludeIds.contains(candidate.getId())) continue;

            double relatedness = RelatenessCalculator.calculateRelatedness(
                referenceOrder,
                candidate,
                hparam.getShawD(),
                hparam.getShawT(),
                hparam.getShawL()
            );

            // 노이즈 추가
            double noise = 1.0 + hparam.getShawNoise() * random.nextDouble();
            relatedness *= noise;

            orderRelatednessList.add(new OrderRelatedness(candidate, relatedness));
        }

        if (orderRelatednessList.isEmpty()) return null;

        // 관련성이 높은 순으로 정렬
        orderRelatednessList.sort((a, b) ->
            Double.compare(b.relatedness, a.relatedness));

        // y^p 선택 방식 구현 (p = 3)
        int selectedIndex = (int) (Math.pow(random.nextDouble(), 3)
            * orderRelatednessList.size());
        return orderRelatednessList.get(selectedIndex).order;
    }

    private static class OrderRelatedness {
        final Order order;
        final double relatedness;

        OrderRelatedness(Order order, double relatedness) {
            this.order = order;
            this.relatedness = relatedness;
        }
    }
}