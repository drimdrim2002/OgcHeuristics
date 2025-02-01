package dev.brown.improved.alns.domain;

import java.util.List;

public record Result(
    PathResult walk,
    PathResult bike,
    PathResult car,
    List<String> optimalOrder,
    boolean feasible
) {
}