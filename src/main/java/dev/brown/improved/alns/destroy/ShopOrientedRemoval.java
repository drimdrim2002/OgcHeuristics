package dev.brown.improved.alns.destroy;

import dev.brown.improved.alns.domain.Solution;
import java.util.*;

/**
 * 매장 기반 제거 전략 구현
 */
public class ShopOrientedRemoval implements Destroyer {
    private final int K;
    private final double[][] shopDistRel;
    private final Random random;

    public ShopOrientedRemoval(int K, double[][] shopDistRel, long seed) {
        this.K = K;
        this.shopDistRel = shopDistRel;
        this.random = new Random(seed);
    }

    @Override
    public List<Integer> destroy(Solution solution, int nDestroy) {
        return DestroyMethods.shopOrientedRemoval(nDestroy, K, random, shopDistRel);
    }

    @Override
    public void update(double score) {
        // ShopOrientedRemoval에서는 update가 필요 없음
    }

    @Override
    public String getCurrentMethod() {
        return "ShopOrientedRemoval";
    }
}