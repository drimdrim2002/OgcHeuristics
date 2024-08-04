package dev.brown.domain;

import dev.brown.Constants;
import dev.brown.util.CalculationUtils;
import dev.brown.util.MatrixManager;
import dev.brown.util.Permutation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Rider {

    private static final Logger log = LoggerFactory.getLogger(Rider.class);
    private final int id;
    private final String type;
    private final double speed;
    private final int capa;
    private final int varCost;
    private final int fixedCost;
    private final int serviceTime;
    private List<Order> orderList;

    public void setShopIndexList(List<Integer> shopIndexList) {
        this.shopIndexList = shopIndexList;
    }

    public void setDeliveryIndexList(List<Integer> deliveryIndexList) {
        this.deliveryIndexList = deliveryIndexList;
    }

    public void setOrderList(List<Order> orderList) {
        this.orderList = orderList;
    }

    private List<Integer> shopIndexList = new ArrayList<>();
    private List<Integer> deliveryIndexList = new ArrayList<>();
    private Solution solution;

    public void setCost(int cost) {
        this.cost = cost;
    }

    private int cost;
    private final int priority;
//    private int extraTime;

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
        this.cost = 0;
        this.isValid = true;
        if (type.equals("CAR")) {
            priority = -1;
        } else if (type.equals("BIKE")) {
            priority = -2;
        } else {
            priority = -3;
        }

        Constants.bestShopIndexMap.put(this.type(), new HashMap<>());
        Constants.bestDeliveryIndexMap.put(this.type(), new HashMap<>());
        Constants.bestScore.put(this.type(), new HashMap<>());
//        this.extraTime = 0;
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
        return defaultInformation() + additionalInformation();
    }

    private String defaultInformation() {
        return "Rider{" +
            "capa=" + capa +
            ", id=" + id +
            ", type='" + type + '\'' +
            ", varCost=" + varCost +
            ", fixedCost=" + fixedCost +
            ", serviceTime=" + serviceTime +
            '}';
    }

    private String additionalInformation() {
        StringBuilder sb = new StringBuilder();
        for (Integer shopIndex : this.shopIndexList) {
            sb.append(shopIndex)
                .append("->");
        }
        for (Integer deliveryIndex : this.deliveryIndexList) {
            sb.append(deliveryIndex)
                .append("->");
        }
        return sb.substring(0, sb.length() - 2);

    }

    public int calculateCost(int totalDistance) {
        return this.fixedCost + (int) (totalDistance / 100.0 * this.varCost);
    }


    public void addOrder(Order order) {
        this.orderList.add(order);

        shopIndexList.add(order.id());
        deliveryIndexList.add(order.id());

        List<List<Integer>> shopPermuationList = Permutation.generatePermutations(shopIndexList);
        List<List<Integer>> deliveryPermutationList = Permutation.generatePermutations(deliveryIndexList);

        int minCost = Integer.MAX_VALUE;

        TreeMap<Integer, HashMap<String, List<Integer>>> bestScoreRecord = new TreeMap<>();

        for (List<Integer> shopSeqListCandidate : shopPermuationList) {
            for (List<Integer> deliveryShopSeqListCandidate : deliveryPermutationList) {

                if (shopSeqListCandidate.size() == 3
                    && shopSeqListCandidate.get(0) == 0
                    && shopSeqListCandidate.get(1) == 18
                    && shopSeqListCandidate.get(2) == 3
                    && deliveryShopSeqListCandidate.get(0) == 3
                    && deliveryShopSeqListCandidate.get(1) == 0
                    && deliveryShopSeqListCandidate.get(2) == 18
                    && this.type().equals("CAR")
                ) {
                    int t = 1;
                    t = 2;
                }

                resetRecord();
                CalculationResult calculationResult = calculate(shopSeqListCandidate, deliveryShopSeqListCandidate);

                int newScore = calculationResult.cost();
                if (calculationResult.isFeasible() && newScore < minCost) {
                    bestScoreRecord.put(newScore, new HashMap<>());
                    bestScoreRecord.get(newScore).put("shopIndexList", shopSeqListCandidate);
                    bestScoreRecord.get(newScore).put("deliveryIndexList", deliveryShopSeqListCandidate);
                    minCost = calculationResult.cost();
                }
            }
        }

        if (!bestScoreRecord.isEmpty()) {

            List<Integer> idCollect = getSortedSequence();
            String orderKey = CalculationUtils.getKeyFromList(idCollect);
            HashMap<String, List<Integer>> indexListMap = bestScoreRecord.firstEntry().getValue();
            List<Integer> bestShopIndexList = indexListMap.get("shopIndexList");
            List<Integer> bestDeliveryIndexList = indexListMap.get("deliveryIndexList");

            String shopIndexKey = CalculationUtils.getKeyFromList(bestShopIndexList);
            String deliveryIndexKey = CalculationUtils.getKeyFromList(bestDeliveryIndexList);

            shopIndexList.clear();
            deliveryIndexList.clear();

            shopIndexList.addAll(bestShopIndexList);
            deliveryIndexList.addAll(bestDeliveryIndexList);

            if (shopIndexList.size() == 3
                && shopIndexList.get(0) == 0
                && shopIndexList.get(1) == 18
                && shopIndexList.get(2) == 3
                && deliveryIndexList.get(0) == 3
                && deliveryIndexList.get(1) == 0
                && deliveryIndexList.get(2) == 18
            ) {
                int t = 1;
                t = 2;
            }

            Constants.bestShopIndexMap.get(this.type()).put(orderKey, shopIndexKey);
            Constants.bestDeliveryIndexMap.get(this.type()).put(orderKey, deliveryIndexKey);
            Constants.bestScore.get(this.type()).put(orderKey, minCost);

            this.isValid = true;
            this.cost = minCost;


        } else {
            this.isValid = false;
            this.cost = -1;
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
            String backupShopIndexStr = Constants.bestShopIndexMap.get(type()).get(orderKey);
            String backupDeliveryIndexStr = Constants.bestDeliveryIndexMap.get(type()).get(orderKey);
            this.shopIndexList.addAll(Arrays.stream(backupShopIndexStr.split(",")).map(Integer::parseInt).toList());
            this.deliveryIndexList.addAll(
                Arrays.stream(backupDeliveryIndexStr.split(",")).map(Integer::parseInt).toList());
//            if (orderKey.equals("0,3,18")) {
//                log.info("==========");
//                for (Integer i : shopIndexList) {
//                    log.info(String.valueOf(i));
//                }
//                log.info("--------");
//                for (Integer i : deliveryIndexList) {
//                    log.info(String.valueOf(i));
//                }
//
//                log.info("==========");
//
//
//            }
            this.cost = Constants.bestScore.get(this.type()).get(orderKey);
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
//        this.extraTime = 0;
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
//                this.extraTime = order.deadline() - deliveryTime;
            }
            volSum += order.volume();
        }

        boolean isFeasible = volSum <= this.capa && !deadlineViolated;

        CalculationResult calculationResult = new CalculationResult(isFeasible);
        if (isFeasible) {
            calculationResult.setCost(calculateCost(totalDistance));
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
