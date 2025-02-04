package dev.brown.improved.alns.destroy;

import dev.brown.improved.alns.domain.Solution;
import dev.brown.improved.alns.parameter.HyperParameter;
import java.util.*;

/**
 * Shaw 제거 전략 구현
 */
public class ShawRemoval implements Destroyer {
    private final int K;
    private final double shawD;
    private final double shawT;
    private final double shawL;
    private final double shawNoise;
    private final double[][] distRel;
    private final double[][] timeRel;
    private final double[][] loadRel;
    private final Random random;

    public ShawRemoval(int K, HyperParameter hparam, double[][] distRel,
        double[][] timeRel, double[][] loadRel, long seed) {
        this.K = K;
        this.shawD = hparam.getShawD();
        this.shawT = hparam.getShawT();
        this.shawL = hparam.getShawL();
        this.shawNoise = hparam.getShawNoise();
        this.distRel = distRel;
        this.timeRel = timeRel;
        this.loadRel = loadRel;
        this.random = new Random(seed);
    }

    @Override
    public List<Integer> destroy(Solution solution, int nDestroy) {
        return DestroyMethods.shawRemoval(
            nDestroy,
            solution,
            K,
            shawD,
            shawT,
            shawL,
            shawNoise,
            distRel,
            timeRel,
            loadRel,
            random
        );
    }

    @Override
    public void update(double score) {
        // ShawRemoval에서는 update가 필요 없음
    }

    @Override
    public String getCurrentMethod() {
        return "ShawRemoval";
    }
}