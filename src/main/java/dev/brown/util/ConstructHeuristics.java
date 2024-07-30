package dev.brown.util;

import dev.brown.domain.Order;
import dev.brown.domain.Rider;
import dev.brown.domain.Solution;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConstructHeuristics {

    static final Logger log = LoggerFactory.getLogger(ConstructHeuristics.class);

    public static Solution solve(Solution solution) {

        HashMap<Integer, Order> orderMap = solution.orderMap();
        HashMap<Integer, Rider> riderMap = solution.riderMap();

        int totalCost = 0;

        int riderIndex = 0;
        int orderIndex = 0;

        while (riderIndex < riderMap.size() && orderIndex < orderMap.size()) {

            Rider rider = riderMap.get(riderIndex);
            Order order = orderMap.get(orderIndex);

            rider.addOrder(order);
            if (rider.isValid()) {
                order.setRider(rider);
                totalCost += rider.cost();
                orderIndex += 1;
            } else {
                rider.removeOrder(order);
                riderIndex += 1;
            }
        }

//        log.info("totalCost: {}", totalCost);
        return solution;
    }
}
