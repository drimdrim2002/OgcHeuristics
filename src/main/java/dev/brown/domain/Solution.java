package dev.brown.domain;

import java.util.HashMap;

public class Solution {


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
            } else {
                totalCost -= order.rider().cost();
            }
        }
    }


    private HashMap<Integer, Order> orderMap = new HashMap<>();
    private HashMap<Integer, Rider> riderMap = new HashMap<>();

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
}
