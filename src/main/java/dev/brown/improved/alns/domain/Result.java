package dev.brown.improved.alns.domain;

import java.util.List;

public class Result {
    private final PathResult walk;
    private final PathResult bike;
    private final PathResult car;
    private final List<String> optimalOrder;
    private final boolean feasible;

    public Result(PathResult walk, PathResult bike, PathResult car,
        List<String> optimalOrder, boolean feasible) {
        this.walk = walk;
        this.bike = bike;
        this.car = car;
        this.optimalOrder = List.copyOf(optimalOrder);
        this.feasible = feasible;
    }

    public PathResult getAll(String type) {
        return switch (type) {
            case "WALK" -> walk;
            case "BIKE" -> bike;
            case "CAR" -> car;
            default -> throw new IllegalArgumentException("Unknown rider type: " + type);
        };
    }

    public boolean getFeasibility(String type) {
        return getAll(type).isFeasible();
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