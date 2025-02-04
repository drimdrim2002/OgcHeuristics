package dev.brown.improved.alns.destroy;

import dev.brown.improved.alns.domain.Solution;
import java.util.*;

/**
 * 무작위 제거 전략 구현
 */
public class RandomRemoval implements Destroyer {
    private final int K;
    private final Random random;

    public RandomRemoval(int K, long seed) {
        this.K = K;
        this.random = new Random(seed);
    }

    @Override
    public List<Integer> destroy(Solution solution, int nDestroy) {
        return DestroyMethods.randomRemoval(nDestroy, K, random);
    }

    @Override
    public void update(double score) {
        // RandomRemoval에서는 update가 필요 없음
    }

    @Override
    public String getCurrentMethod() {
        return "RandomRemoval";
    }
}