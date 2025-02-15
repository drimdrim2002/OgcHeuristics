package dev.brown.util;

import static dev.brown.util.MatrixManager.getDeliveryDuration;
import static dev.brown.util.MatrixManager.getShopDuration;

import dev.brown.domain.Order;
import dev.brown.domain.Rider;
import dev.brown.improved.alns.domain.Bundle;
import dev.brown.improved.alns.domain.Solution;
import java.util.HashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Validator {

    private static final Logger log = LoggerFactory.getLogger(Validator.class);

    public static void validateAll(Solution solution,
        HashMap<Integer, Order> orderMap, HashMap<String, Rider> riderMapByType) throws Exception {

        int totalDistance = 0;
        int totalCost = 0;

        HashMap<Bundle, String> errorMap = new HashMap<>();
        for (Bundle bundle : solution.getBundles()) {

            Rider rider = riderMapByType.get(bundle.riderType());
            int distance = 0;
            int cost = 0;
            int deliveryTime = 0;

            String riderType = bundle.riderType();
            List<Integer> shopIndexList = bundle.source();
            for (int shopVisitOrder = 0; shopVisitOrder < shopIndexList.size(); shopVisitOrder++) {
                Integer currShopIndex = shopIndexList.get(shopVisitOrder);
                if (shopVisitOrder > 0) {
                    Integer prevShopIndex = shopIndexList.get(shopVisitOrder - 1);
                    int shopDistance = MatrixManager.getShopDistance(prevShopIndex, currShopIndex);
                    distance += shopDistance;
                    deliveryTime += getShopDuration(riderType, prevShopIndex, currShopIndex);
                }
                Order order = orderMap.get(currShopIndex);
                deliveryTime = Math.max(deliveryTime, order.getReadyTime());
            }

            List<Integer> deliveryIndexList = bundle.dest();

            int lastVisitShopIndex = shopIndexList.getLast();
            int firstVisitShopIndex = deliveryIndexList.getFirst();
            int shopToDeliveryDistance = MatrixManager.getShopToDeliveryDistance(
                lastVisitShopIndex, firstVisitShopIndex);
            distance += shopToDeliveryDistance;

            deliveryTime += MatrixManager.getShopToDeliveryDuration(riderType, lastVisitShopIndex, firstVisitShopIndex);

            int volSum = 0;
            boolean deadlineViolated = false;
            for (int deliveryVisitOrder = 0; deliveryVisitOrder < deliveryIndexList.size(); deliveryVisitOrder++) {
                Integer currDeliveryIndex = deliveryIndexList.get(deliveryVisitOrder);
                if (deliveryVisitOrder > 0) {
                    Integer prevShopIndex = deliveryIndexList.get(deliveryVisitOrder - 1);
                    int deliveryDistance = MatrixManager.getDeliveryDistance(prevShopIndex, currDeliveryIndex);
                    distance += deliveryDistance;
                    deliveryTime += getDeliveryDuration(riderType, prevShopIndex, currDeliveryIndex);
                }
                Order order = orderMap.get(currDeliveryIndex);
                if (order.getDeadline() < deliveryTime) {
                    deadlineViolated = true;
                    break;
                }
                volSum += order.getVolume();
            }
            boolean capaSafe = volSum <= riderMapByType.get(riderType).capa();
            boolean isFeasible = capaSafe && !deadlineViolated;

            if (isFeasible) {
                totalDistance += distance;
                cost = rider.getFixedCost() + (int) (distance / 100.0 * rider.getVarCost());
                totalCost += cost;
            } else {
                if (!capaSafe) {
                    errorMap.put(bundle, "Rider (" + rider.id() + ") is overloaded");
                } else {
                    errorMap.put(bundle, "Rider (" + rider.id() + ") violates deadline");
                }
            }
        }
        if (!errorMap.isEmpty()) {

            throw new Exception("Error: " + errorMap);
        } else {
            log.info("Total Distance: {}, Total Cost: {}", totalDistance, totalCost);
        }
    }
}
