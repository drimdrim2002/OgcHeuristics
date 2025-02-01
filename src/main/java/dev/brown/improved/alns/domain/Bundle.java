package dev.brown.improved.alns.domain;

import java.util.List;

public record Bundle(
    String riderType,
    double cost,
    List<Integer> source,
    List<Integer> dest
) {
    public static Bundle of(String riderType, double cost, List<Integer> source, List<Integer> dest) {
        return new Bundle(riderType, cost, source, dest);
    }
}