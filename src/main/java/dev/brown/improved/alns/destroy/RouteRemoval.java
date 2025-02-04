package dev.brown.improved.alns.destroy;

import dev.brown.improved.alns.domain.Solution;
import java.util.*;

/**
 * 경로 기반 제거 전략 구현
 */
public class RouteRemoval implements Destroyer {
    private final double routeRemovalRatio;
    private final Random random;

    public RouteRemoval(double routeRemovalRatio, long seed) {
        this.routeRemovalRatio = routeRemovalRatio;
        this.random = new Random(seed);
    }

    @Override
    public List<Integer> destroy(Solution solution, int nDestroy) {
        return DestroyMethods.routeRemoval(nDestroy, solution, routeRemovalRatio, random);
    }

    @Override
    public void update(double score) {
        // RouteRemoval에서는 update가 필요 없음
    }

    @Override
    public String getCurrentMethod() {
        return "RouteRemoval";
    }
}