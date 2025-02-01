package dev.brown.improved.alns.domain;

import java.util.List;

public class Res {
    private final RiderTuple walk;
    private final RiderTuple bike;
    private final RiderTuple car;
    private final List<String> optimalOrder;
    private final boolean feasible;

    public Res() {
        this(null, null, null, null, false);
    }

    public Res(RiderTuple walk,
        RiderTuple bike,
        RiderTuple car,
        List<String> optimalOrder,
        boolean feasible) {
        this.walk = walk;
        this.bike = bike;
        this.car = car;
        this.optimalOrder = optimalOrder;
        this.feasible = feasible;
    }

    public RiderTuple getAll(String type) {
        return switch (type) {
            case "WALK" -> walk;
            case "BIKE" -> bike;
            case "CAR" -> car;
            default -> throw new IllegalArgumentException("Unknown rider type: " + type);
        };
    }

    public boolean getFeasibility(String type) {
        return getAll(type).feasible();
    }

    public double getCost(String type) {
        return getAll(type).cost();
    }

    public List<Integer> getSource(String type) {
        return getAll(type).source();
    }

    public List<Integer> getDest(String type) {
        return getAll(type).dest();
    }

    public List<String> getOptimalOrder() {
        return optimalOrder;
    }

    public boolean isFeasible() {
        return feasible;
    }
}