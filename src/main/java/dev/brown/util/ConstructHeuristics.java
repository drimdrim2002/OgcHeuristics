package dev.brown.util;

import dev.brown.Constants.ORDER_CRITERIA;
import dev.brown.Constants.RIDER_TYPE;
import dev.brown.domain.CalculationResult;
import dev.brown.domain.Order;
import dev.brown.domain.OrderPool;
import dev.brown.domain.Rider;
import dev.brown.domain.RiderPool;
import dev.brown.domain.Solution;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConstructHeuristics {

    private static final Logger log = LoggerFactory.getLogger(ConstructHeuristics.class);

//    static final Logger log = LoggerFactory.getLogger(ConstructHeuristics.class);

    public static Solution solve(Solution solution) {

        HashMap<Integer, Order> orderMap = solution.orderMap();
        HashMap<Integer, Rider> riderMap = solution.riderMap();

//        brownSolution(solution, orderMap);
//        orderPool.sortOrderByDifficulties();


//        for (Order order : orderMap.values()) {
//            solution.sam
//        }

        // 무엇이 풀기 어려운 order일까?

        int riderIndex = 0;
        int orderIndex = 0;

        while (riderIndex < riderMap.size() && orderIndex < orderMap.size()) {

            Rider rider = riderMap.get(riderIndex);
            Order order = orderMap.get(orderIndex);

            CalculationResult calculationResult = rider.addOrder(order);
            if (calculationResult.isFeasible()) {
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

    private static void brownSolution(Solution solution, HashMap<Integer, Order> orderMap) {
        OrderPool orderPool = new OrderPool(solution.orderMap(), solution.sampleRiderMapByType());
        RiderPool riderPool = new RiderPool(solution.riderMap());
        int trial = 0;
        while (orderPool.consumedRecord().size() < orderMap.size()) {
            ORDER_CRITERIA criteria = trial % 2 == 0 ? ORDER_CRITERIA.DEADLINE : ORDER_CRITERIA.CAPACITY;

            Order nextOrder = orderPool.getNextOrder(criteria);


            TreeMap<Double, HashMap<Rider, List<Order>>> availableScenarioMap = new TreeMap<>();
            for (RIDER_TYPE riderType : RIDER_TYPE.values()) {
                HashSet<Integer> triedOrderIndex = new HashSet<>();

                Rider nextRider = riderPool.getNextRider(riderType.value());
                if (nextRider == null) {
                    continue;
                }


                int totalCost =0;
                List<Order> pickOrder = new ArrayList<>();
                while (nextOrder != null) {
                    CalculationResult calculationResult = nextRider.addOrder(nextOrder);
                    if (calculationResult.isFeasible()) {
                        int cost = calculationResult.cost();
                        totalCost += cost;
                        pickOrder.add(nextOrder);
                        triedOrderIndex.add(nextOrder.id());
                        int nearestIndex = MatrixManager.findNearestIndex(nextOrder, triedOrderIndex);
                        nextOrder = solution.orderMap().get(nearestIndex);
                    }
                }

                double averageCost = totalCost * 1.0 / pickOrder.size();
                availableScenarioMap.put(averageCost, new HashMap<>());
                availableScenarioMap.get(averageCost).put(nextRider, pickOrder);


                nextRider.resetAll();
                for (Order order : pickOrder) {
                    order.setRider(null);
                }

            }

            if (!availableScenarioMap.isEmpty()) {
                HashMap<Rider, List<Order>> bestScenario = availableScenarioMap.firstEntry().getValue();
                for (Rider rider : bestScenario.keySet()) {
                    riderPool.consume(rider);
                    for (Order order : bestScenario.get(rider)) {
                        orderPool.consume(order, criteria);
                    }
                }

            }

            trial += 1;
//            orderPool.getNextOrder()
        }
    }
}
