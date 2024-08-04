package dev.brown.domain;

import dev.brown.Constants;
import dev.brown.util.CalculationUtils;
import dev.brown.util.MatrixManager;
import dev.brown.util.Permutation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Rider {

    private final int id;
    private final String type;
    private final double speed;
    private final int capa;
    private final int varCost;
    private final int fixedCost;
    private final int serviceTime;
    private final List<Order> orderList;
    private final List<Integer> shopIndexList;
    private final List<Integer> deliveryIndexList;
    private Solution solution;
    private int cost;
    private final int priority;
    private int extraTime;

    public boolean isValid() {
        return isValid;
    }

    public int cost() {
        return cost;
    }

    private boolean isValid;

    public Rider(int id, String type, double speed, int capa, int varCost, int fixedCost, int serviceTime) {
        this.id = id;
        this.type = type;
        this.speed = speed;
        this.capa = capa;
        this.varCost = varCost;
        this.fixedCost = fixedCost;
        this.serviceTime = serviceTime;
        this.orderList = new ArrayList<>();
        this.shopIndexList = new ArrayList<>();
        this.deliveryIndexList = new ArrayList<>();
        this.cost = 0;
        this.isValid = true;
        if (type.equals("CAR")) {
            priority = -1;
        } else if (type.equals("BIKE")) {
            priority = -2;
        } else {
            priority = -3;
        }
        this.extraTime = 0;
    }

    public double speed() {
        return speed;
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

    public int calculateCost(int totalDistance) {
        return this.fixedCost + (int) (totalDistance / 100.0 * this.varCost);
    }




    public CalculationResult addOrder(Order order) {
        this.orderList.add(order);
        shopIndexList.add(order.id());
        deliveryIndexList.add(order.id());

        List<List<Integer>> shopPermuationList = Permutation.generatePermutations(shopIndexList);
        List<List<Integer>> deliveryPermutationList = Permutation.generatePermutations(deliveryIndexList);

        int minCost = Integer.MAX_VALUE;
        boolean addPossible = false;
        for (List<Integer> shopSeqListCandidate : shopPermuationList) {
            for (List<Integer> deliveryShopSeqListCandidate : deliveryPermutationList) {
                CalculationResult calculationResult = calculate(shopSeqListCandidate, deliveryShopSeqListCandidate);
                if (calculationResult.isFeasible() && calculationResult.cost() < minCost) {
                    this.shopIndexList.clear();
                    this.shopIndexList.addAll(shopSeqListCandidate);
                    this.deliveryIndexList.clear();
                    this.deliveryIndexList.addAll(deliveryShopSeqListCandidate);
                    minCost = calculationResult.cost();
                    this.extraTime = calculationResult.extraTime();
                    addPossible = true;
                }
            }
        }

        if (addPossible) {

            List<Integer> idCollect = getSortedSequence();

            String orderKey = CalculationUtils.getKeyFromList(idCollect);
            String shopIndexKey = CalculationUtils.getKeyFromList(this.shopIndexList);
            String deliveryIndexKey = CalculationUtils.getKeyFromList(this.deliveryIndexList);

            Constants.bestShopIndexMap.put(orderKey, shopIndexKey);
            Constants.bestDeliveryIndexMap.put(orderKey, deliveryIndexKey);
            Constants.bestScore.put(orderKey, minCost);

            this.isValid = true;
            this.cost = minCost;

            CalculationResult calculationResult = new CalculationResult(true);
            calculationResult.setCost(minCost);
            calculationResult.setExtraTime(extraTime);
            return calculationResult;

        } else {
            this.isValid = false;

            return new CalculationResult(false);
        }
    }

    private List<Integer> getSortedSequence() {
        return this.orderList.stream()
            .map(Order::id)
            .sorted()
            .toList();
    }

    public void removeOrder(Order order) {
        this.orderList.remove(order);

        String orderKey = CalculationUtils.getKeyFromList(this.getSortedSequence());
        this.resetRecord();
        if (!orderList.isEmpty()) {
            String backupShopIndexStr = Constants.bestShopIndexMap.get(orderKey);
            String backupDeliveryIndexStr = Constants.bestDeliveryIndexMap.get(orderKey);
            this.shopIndexList.addAll(Arrays.stream(backupShopIndexStr.split(",")).map(Integer::parseInt).toList());
            this.deliveryIndexList.addAll(
                Arrays.stream(backupDeliveryIndexStr.split(",")).map(Integer::parseInt).toList());
            this.cost = Constants.bestScore.get(orderKey);
        }
    }

    public void resetAll() {
        this.orderList.clear();
        resetRecord();
    }

    public void resetRecord() {
        this.shopIndexList.clear();
        this.deliveryIndexList.clear();
        this.cost = 0;
        this.extraTime = 0;
        this.isValid = true;
    }

    public CalculationResult calculate(List<Integer> shopIndexList, List<Integer> deliveryIndexList) {

        int deliveryTime = 0;
        int totalDistance = 0;

        for (int shopVisitOrder = 0; shopVisitOrder < shopIndexList.size(); shopVisitOrder++) {
            Integer currShopIndex = shopIndexList.get(shopVisitOrder);
            if (shopVisitOrder > 0) {
                Integer prevShopIndex = shopIndexList.get(shopVisitOrder - 1);
                int shopDistance = MatrixManager.getShopDistance(prevShopIndex, currShopIndex);
                totalDistance += shopDistance;
                deliveryTime += getShopDuration(prevShopIndex, currShopIndex);
            }
            Order order = this.solution.orderMap().get(currShopIndex);
            deliveryTime = Math.max(deliveryTime, order.readyTime());
        }

        int lastVisitShopIndex = shopIndexList.get(shopIndexList.size() - 1);
        int firstVisitShopIndex = deliveryIndexList.get(0);
        int shopToDeliveryDistance = MatrixManager.getShopToDeliveryDistance(
            lastVisitShopIndex, firstVisitShopIndex);
        totalDistance += shopToDeliveryDistance;

        deliveryTime += getShopToDeliveryDuration(lastVisitShopIndex, firstVisitShopIndex);

        int volSum = 0;
        boolean deadlineViolated = false;
        for (int deliveryVisitOrder = 0; deliveryVisitOrder < deliveryIndexList.size(); deliveryVisitOrder++) {
            Integer currDeliveryIndex = deliveryIndexList.get(deliveryVisitOrder);
            if (deliveryVisitOrder > 0) {
                Integer prevShopIndex = deliveryIndexList.get(deliveryVisitOrder - 1);
                int deliveryDistance = MatrixManager.getDeliveryDistance(prevShopIndex, currDeliveryIndex);
                totalDistance += deliveryDistance;
                deliveryTime += getDeliveryDuration(prevShopIndex, currDeliveryIndex);
            }
            Order order = this.solution.orderMap().get(currDeliveryIndex);
            if (order.deadline() < deliveryTime) {
                deadlineViolated = true;
                break;
            } else {
                this.extraTime = order.deadline() - deliveryTime;
            }
            volSum += order.volume();
        }

        boolean isFeasible = volSum <= this.capa && !deadlineViolated;

        CalculationResult calculationResult = new CalculationResult(isFeasible);
        if (isFeasible) {

            calculationResult.setCost(calculateCost(totalDistance));
            calculationResult.setExtraTime(this.extraTime);
        }
        return calculationResult;
    }

//    public void calculateAll() {
//
//        int deliveryTime = 0;
//        int totalDistance = 0;
//        for (int shopVisitOrder = 0; shopVisitOrder < shopIndexList.size(); shopVisitOrder++) {
//            Integer currShopIndex = shopIndexList.get(shopVisitOrder);
//            if (shopVisitOrder > 0) {
//                Integer prevShopIndex = shopIndexList.get(shopVisitOrder - 1);
//                int distance = MatrixManager.getShopDistance(prevShopIndex, currShopIndex);
//                totalDistance += distance;
//                deliveryTime += calculateDuration(distance);
//            }
//            Order order = this.solution.orderMap().get(currShopIndex);
//            deliveryTime = Math.max(deliveryTime, order.readyTime());
//        }
//
//        int shopToDeliveryDistance = MatrixManager.getShopToDeliveryDistance(
//            shopIndexList.get(shopIndexList.size() - 1), deliveryIndexList.get(0));
//        totalDistance += shopToDeliveryDistance;
//        deliveryTime += calculateDuration(shopToDeliveryDistance);
//
//        int volSum = 0;
//        boolean deadlineViolated = false;
//        for (int deliveryVisitOrder = 0; deliveryVisitOrder < deliveryIndexList.size(); deliveryVisitOrder++) {
//            Integer currDeliveryIndex = deliveryIndexList.get(deliveryVisitOrder);
//            if (deliveryVisitOrder > 0) {
//                Integer prevShopIndex = deliveryIndexList.get(deliveryVisitOrder - 1);
//                int distance = MatrixManager.getShopDistance(prevShopIndex, currDeliveryIndex)
//                totalDistance += shopToDeliveryDistance;
//                deliveryTime += calculateDuration(distance);
//            }
//            Order order = this.solution.orderMap().get(currDeliveryIndex);
//            if (order.deadline() < deliveryTime) {
//                deadlineViolated = true;
//                break;
//            }
//            volSum += order.volume();
//
//        }
//
//        if (volSum > this.capa || deadlineViolated) {
//            isValid = false;
//        } else {
//            cost = calculateCost(totalDistance);
//        }
//    }

    public List<Integer> deliveryIndexList() {
        return deliveryIndexList;
    }

    public List<Order> orderList() {
        return orderList;
    }

    public List<Integer> shopIndexList() {
        return shopIndexList;
    }

    public int getShopDuration(int originIndex, int destinationIndex) {
        return MatrixManager.getShopDuration(this.type, originIndex, destinationIndex);
    }

    public int getDeliveryDuration(int originIndex, int destinationIndex) {
        return MatrixManager.getDeliveryDuration(this.type, originIndex, destinationIndex);
    }

    public int getShopToDeliveryDuration(int originIndex, int destinationIndex) {
        return MatrixManager.getShopToDeliveryDuration(this.type, originIndex, destinationIndex);
    }

    public int priority() {
        return priority;
    }
}
