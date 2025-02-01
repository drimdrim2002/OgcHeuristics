package dev.brown.improved.alns.domain;

import java.util.List;

public record RiderTuple(
    boolean feasible,
    double cost,
    List<Integer> source,
    List<Integer> dest
) {
    public static RiderTuple of(boolean feasible, double cost, List<Integer> source, List<Integer> dest) {
        return new RiderTuple(feasible, cost, source, dest);
    }
}