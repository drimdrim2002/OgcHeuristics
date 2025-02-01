package dev.brown.improved.alns.domain;

import java.util.List;

public record PathResult(
    boolean feasible,
    double cost,
    List<Integer> source,
    List<Integer> dest
) {
    public static PathResult infeasible() {
        return new PathResult(false, Double.MAX_VALUE, List.of(), List.of());
    }
}