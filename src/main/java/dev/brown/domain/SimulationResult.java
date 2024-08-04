package dev.brown.domain;

import java.util.ArrayList;
import java.util.List;

public class SimulationResult {

    private final int riderIndex;
    private final boolean isFeasible;
    private final int cost;
    private final List<Order> orderList;
    private final List<Integer> shopIndexList;
    private final List<Integer> deliveryIndexList;

    public SimulationResult(Rider rider) {
        this.riderIndex = rider.id();
        this.cost = rider.cost();
        this.isFeasible = rider.isValid();
        this.orderList = new ArrayList<>(rider.orderList());
        this.shopIndexList = new ArrayList<>(rider.shopIndexList());
        this.deliveryIndexList = new ArrayList<>(rider.deliveryIndexList());
    }

    public int cost() {
        return cost;
    }

    public List<Integer> deliveryIndexList() {
        return deliveryIndexList;
    }

    public boolean isFeasible() {
        return isFeasible;
    }

    public List<Order> orderList() {
        return orderList;
    }

    public List<Integer> shopIndexList() {
        return shopIndexList;
    }

    public int riderIndex() {
        return riderIndex;
    }


}
