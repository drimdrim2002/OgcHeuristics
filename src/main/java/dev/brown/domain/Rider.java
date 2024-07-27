package dev.brown.domain;

import dev.brown.util.MatrixManager;
import java.util.ArrayList;
import java.util.List;

public class Rider {

    private final int id;
    private final String type;
    private final int speed;
    private final int capa;
    private final int varCost;
    private final int fixedCost;
    private final int serviceTime;
    //    private final List<Order> orderList;
    private final List<Integer> shopIndexList;
    private final List<Integer> deliveryIndexList;
    private Solution solution;
    private int cost;
    private boolean isValid;

    public Rider(int id, String type, int speed, int capa, int varCost, int fixedCost, int serviceTime) {
        this.id = id;
        this.type = type;
        this.speed = speed;
        this.capa = capa;
        this.varCost = varCost;
        this.fixedCost = fixedCost;
        this.serviceTime = serviceTime;
//        this.orderList = new ArrayList<>();
        this.shopIndexList = new ArrayList<>();
        this.deliveryIndexList = new ArrayList<>();
        this.cost = 0;
        this.isValid = true;
    }

    public int capa() {
        return capa;
    }

    public int fixedCost() {
        return fixedCost;
    }

    public int id() {
        return id;
    }

    public int serviceTime() {
        return serviceTime;
    }

    public String type() {
        return type;
    }

    public int varCost() {
        return varCost;
    }

    public Solution solution() {
        return solution;
    }

    public void setSolution(Solution solution) {
        this.solution = solution;
    }

    @Override
    public String toString() {
        return "Rider{" +
            "capa=" + capa +
            ", id=" + id +
            ", type='" + type + '\'' +
            ", varCost=" + varCost +
            ", fixedCost=" + fixedCost +
            ", serviceTime=" + serviceTime +
            '}';
    }

//    public int calculateDistance() {
//        int totalDistance = 0;
//        if (shopIndexList.isEmpty()) {
//            return 0;
//        }
//
//        if (shopIndexList.size() > 1) {
//            for (int shopSeq = 1; shopSeq < shopIndexList.size(); shopSeq++) {
//
//                // calculate distance between shops
//                Integer prevShopIndex = shopIndexList.get(shopSeq - 1);
//                Integer currShopIndex = shopIndexList.get(shopSeq);
//                totalDistance += MatrixManager.getShopDistance(prevShopIndex, currShopIndex);
//
//                // calculate distance between deliverys
//                Integer prevDeliveryIndex = deliveryIndexList.get(shopSeq - 1);
//                Integer currDeliveryIndex = deliveryIndexList.get(shopSeq);
//                totalDistance += MatrixManager.getDeliveryDistance(prevDeliveryIndex, currDeliveryIndex);
//            }
//        }
//        totalDistance += MatrixManager.getShopToDeliveryDistance(
//            shopIndexList.get(shopIndexList.size() - 1), deliveryIndexList.get(0)
//        );
//        return totalDistance;
//    }

    // todo : int로 변환하는 것이 맞는 것인가 고민해야 한다.
    public int calculateCost(int totalDistance) {
        if (shopIndexList.isEmpty()) {
            return 0;
        }
        return this.fixedCost + (int) Math.floor(totalDistance / 100.0) * this.varCost;
    }

    public int calculateDuration(int distance) {
        return (int) Math.round(distance * 1.0 / this.speed) + this.serviceTime;
    }

    public void reset() {
        this.shopIndexList.clear();
        this.deliveryIndexList.clear();
        ;
        this.cost = 0;
        this.isValid = true;
    }

    public void calculateAll() {

        int deliveryTime = 0;
        int totalDistance = 0;
        for (int shopVisitOrder = 0; shopVisitOrder < shopIndexList.size(); shopVisitOrder++) {
            Integer currShopIndex = shopIndexList.get(shopVisitOrder);
            if (shopVisitOrder > 0) {
                Integer prevShopIndex = shopIndexList.get(shopVisitOrder - 1);
                int distance = MatrixManager.getShopDistance(prevShopIndex, currShopIndex);
                totalDistance += distance;
                deliveryTime += calculateDuration(distance);
            }
            Order order = this.solution.orderMap().get(currShopIndex);
            deliveryTime = Math.max(deliveryTime, order.readyTime());
        }

        int shopToDeliveryDistance = MatrixManager.getShopToDeliveryDistance(
            shopIndexList.get(shopIndexList.size() - 1), deliveryIndexList.get(0));
        totalDistance += shopToDeliveryDistance;
        deliveryTime += calculateDuration(shopToDeliveryDistance);

        int volSum = 0;
        boolean deadlineViolated = false;
        for (int deliveryVisitOrder = 0; deliveryVisitOrder < deliveryIndexList.size(); deliveryVisitOrder++) {
            Integer currDeliveryIndex = deliveryIndexList.get(deliveryVisitOrder);
            if (deliveryVisitOrder > 0) {
                Integer prevShopIndex = deliveryIndexList.get(deliveryVisitOrder - 1);
                int distance = MatrixManager.getShopDistance(prevShopIndex, currDeliveryIndex);
                totalDistance += shopToDeliveryDistance;
                deliveryTime += calculateDuration(distance);
            }
            Order order = this.solution.orderMap().get(currDeliveryIndex);
            if (order.deadline() < deliveryTime) {
                deadlineViolated = true;
                break;
            }
            volSum += order.volume();

        }

        if (volSum > this.capa || deadlineViolated) {
            isValid = false;
        } else {
            cost = calculateCost(totalDistance);
        }
    }

}
