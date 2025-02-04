package dev.brown.improved.alns.destroy;

import dev.brown.improved.alns.domain.Solution;
import java.util.*;

/**
 * 배달지 기반 제거 전략 구현
 */
public class DeliveryOrientedRemoval implements Destroyer {
    private final int K;
    private final double[][] dlvDistRel;
    private final Random random;

    public DeliveryOrientedRemoval(int K, double[][] dlvDistRel, long seed) {
        this.K = K;
        this.dlvDistRel = dlvDistRel;
        this.random = new Random(seed);
    }

    @Override
    public List<Integer> destroy(Solution solution, int nDestroy) {
        return DestroyMethods.deliveryOrientedRemoval(nDestroy, K, random, dlvDistRel);
    }

    @Override
    public void update(double score) {
        // DeliveryOrientedRemoval에서는 update가 필요 없음
    }

    @Override
    public String getCurrentMethod() {
        return "DeliveryOrientedRemoval";
    }
}