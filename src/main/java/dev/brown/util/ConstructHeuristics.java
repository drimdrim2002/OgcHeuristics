package dev.brown.util;

import dev.brown.Constants.RIDER_TYPE;
import dev.brown.domain.Order;
import dev.brown.domain.OrderPool;
import dev.brown.domain.Rider;
import dev.brown.domain.RiderPool;
import dev.brown.domain.SimulationResult;
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


        brownSolution(solution);

        return solution;
    }

    private static void simpleSolution(HashMap<Integer, Rider> riderMap, HashMap<Integer, Order> orderMap) {
        int riderIndex = 0;
        int orderIndex = 0;

        while (riderIndex < riderMap.size() && orderIndex < orderMap.size()) {

            Rider rider = riderMap.get(riderIndex);
            Order order = orderMap.get(orderIndex);

            rider.addOrder(order);
            if (rider.isValid()) {
                order.setRiderId(rider.id());
                orderIndex += 1;
//                log.info("order {}-> rider{}", order.id(), rider.id());
            } else {
                rider.removeOrder(order);
                riderIndex += 1;
            }

        }
    }

    private static void brownSolution(Solution solution) {
        OrderPool orderPool = new OrderPool(solution.orderMap());
        RiderPool riderPool = new RiderPool(solution.riderMap());
        while (orderPool.orderRemains() && riderPool.vehicleRemains()) {
//            ORDER_CRITERIA criteria = trial % 2 == 0 ? ORDER_CRITERIA.DEADLINE : ORDER_CRITERIA.CAPACITY;

            Order nextOrder = orderPool.getNextOrder();
            if (nextOrder == null) {
                break;
            }
            TreeMap<Double, List<SimulationResult>> availableScenarioMap = new TreeMap<>();
            for (RIDER_TYPE riderType : RIDER_TYPE.values()) {
                HashSet<Integer> triedOrderIndex = new HashSet<>();

                Rider nextRider = riderPool.getNextRider(riderType.value());
                if (nextRider == null) {
                    continue;
                }

                List<Order> pickOrder = new ArrayList<>();
                while (nextOrder != null) {
                    int totalCost = 0;
                    nextRider.addOrder(nextOrder);
                    if (nextRider.isValid()) {
                        int cost = nextRider.cost();
                        totalCost += cost;
                        pickOrder.add(nextOrder);

                    } else {
                        totalCost = -1;
                        nextRider.removeOrder(nextOrder);
                        pickOrder.remove(nextOrder);
                    }

                    triedOrderIndex.add(nextOrder.getId());
                    int nearestIndex = orderPool.findNearestIndex(nextOrder, triedOrderIndex);
                    nextOrder = solution.orderMap().get(nearestIndex);


                    if (totalCost > 0) {
                        double averageCost = totalCost * 1.0 / pickOrder.size();
                        availableScenarioMap.put(averageCost, new ArrayList<>());
                        availableScenarioMap.get(averageCost).add(new SimulationResult(nextRider));
                    }


                }

                nextRider.resetAll();
                for (Order order : pickOrder) {
                    order.setRiderId(null);
                }

                nextOrder = orderPool.getNextOrder();
            }

            if (!availableScenarioMap.isEmpty()) {
                List<SimulationResult> simulationResultList = availableScenarioMap.firstEntry().getValue();

                SimulationResult simulationResult = simulationResultList.get(0);
                Rider bestPickRider = solution.riderMap().get(simulationResult.riderIndex());
                bestPickRider.setCost(simulationResult.cost());
                bestPickRider.setShopIndexList(simulationResult.shopIndexList());
                bestPickRider.setDeliveryIndexList(simulationResult.deliveryIndexList());
                bestPickRider.setOrderList(simulationResult.orderList());

//                log.info("best pick rider : {}", bestPickRider );

                for (Order order : simulationResult.orderList()) {
                    orderPool.consume(order);
                    order.setRiderId(bestPickRider.id());
                }
                riderPool.consume(bestPickRider);


            }

//            orderPool.getNextOrder()
        }

        int t = 1;
        t = 2;

//        for (Rider rider : solution.riderMap().values()) {
//            log.info("rider: {}", );
//        }

    }
}
