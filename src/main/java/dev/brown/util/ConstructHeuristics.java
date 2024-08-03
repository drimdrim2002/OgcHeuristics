package dev.brown.util;

import dev.brown.domain.Order;
import dev.brown.domain.Rider;
import dev.brown.domain.Solution;
import java.util.HashMap;

public class ConstructHeuristics {

//    static final Logger log = LoggerFactory.getLogger(ConstructHeuristics.class);

    public static Solution solve(Solution solution) {

        HashMap<Integer, Order> orderMap = solution.orderMap();
        HashMap<Integer, Rider> riderMap = solution.riderMap();

//        for (Order order : orderMap.values()) {
//            solution.sam
//        }

        // 무엇이 풀기 어려운 order일까?

        int riderIndex = 0;
        int orderIndex = 0;

        while (riderIndex < riderMap.size() && orderIndex < orderMap.size()) {

            Rider rider = riderMap.get(riderIndex);
            Order order = orderMap.get(orderIndex);


            rider.addOrder(order);
            if (rider.isValid()) {
                order.setRider(rider);
                orderIndex += 1;
//                log.info("order {}-> rider{}", order.id(), rider.id());
            } else {
                rider.removeOrder(order);
                riderIndex += 1;
            }

        }


//        log.info("totalCost: {}", totalCost);
        return solution;
    }
}
