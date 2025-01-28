package dev.brown.alns.destroy;
import dev.brown.domain.Order;
import dev.brown.domain.Rider;
import dev.brown.domain.Solution;
import java.util.*;

public class RandomRemoval implements DestroyOperator {
    private final Random random;

    public RandomRemoval(Random random) {
        this.random = random;
    }

    @Override
    public List<Integer> destroy(Solution solution, int numToDestroy) {
        List<Integer> removedOrders = new ArrayList<>();
        List<Integer> candidateOrders = new ArrayList<>(solution.orderMap().keySet());

        // 무작위로 주문 선택 및 제거
        while (removedOrders.size() < numToDestroy && !candidateOrders.isEmpty()) {
            int idx = random.nextInt(candidateOrders.size());
            int orderId = candidateOrders.remove(idx);
            Order order = solution.orderMap().get(orderId);

            // 라이더로부터 주문 제거
            if (order.rider() != null) {
                order.rider().removeOrder(order);
                removedOrders.add(orderId);
            }
        }

        return removedOrders;
    }
}