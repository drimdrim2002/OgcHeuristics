package dev.brown.domain;

import java.util.HashMap;

public class Solution {

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
