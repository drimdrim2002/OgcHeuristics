package dev.brown.improved.alns.destroy;

import dev.brown.improved.alns.domain.Solution;
import java.util.*;

/**
 * 거리 기반 제거 전략 구현
 */
public class DistanceOrientedRemoval implements Destroyer {
    private final int K;
    private final double[][] distRel;
    private final Random random;

    public DistanceOrientedRemoval(int K, double[][] distRel, long seed) {
        this.K = K;
        this.distRel = distRel;
        this.random = new Random(seed);
    }

    @Override
    public List<Integer> destroy(Solution solution, int nDestroy) {
        return DestroyMethods.distanceOrientedRemoval(nDestroy, K, random, distRel);
    }

    @Override
    public void update(double score) {
        // DistanceOrientedRemoval에서는 update가 필요 없음
    }

    @Override
    public String getCurrentMethod() {
        return "DistanceOrientedRemoval";
    }
}