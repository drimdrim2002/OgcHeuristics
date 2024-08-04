package dev.brown.domain;

import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Solution {


    private static final Logger log = LoggerFactory.getLogger(Solution.class);
    private int notAssignedOrderCount = 0;
    private int totalCost = 0;

    public int notAssignedOrderCount() {
        return notAssignedOrderCount;
    }

    public int totalCost() {
        return totalCost;
    }

    public void calculateScore() {
        for (Order order : orderMap.values()) {
            if (order.rider() == null) {
                notAssignedOrderCount -= 1;
            }
        }

        int totalCost = 0;
        for (Rider rider : riderMap.values()) {
//            log.info(String.valueOf(rider.cost()));
            totalCost += rider.cost() ;

        }
        this.totalCost = totalCost * -1;
    }


    private HashMap<Integer, Order> orderMap = new HashMap<>();
    private HashMap<Integer, Rider> riderMap = new HashMap<>();
    private HashMap<String, Rider> sampleRiderMapByType = new HashMap<>();

    public Solution() {
    }

    public HashMap<Integer, Order> orderMap() {
        return orderMap;
    }

    public HashMap<Integer, Rider> riderMap() {
        return riderMap;
    }

    public void setOrderMap(HashMap<Integer, Order> orderMap) {
        this.orderMap = orderMap;
    }

    public void setRiderMap(HashMap<Integer, Rider> riderMap) {
        this.riderMap = riderMap;
    }

    public void setSampleRiderMapByType(HashMap<String, Rider> sampleRiderMapByType) {
        this.sampleRiderMapByType = sampleRiderMapByType;
    }

    public HashMap<String, Rider> sampleRiderMapByType() {
        return sampleRiderMapByType;
    }
}
