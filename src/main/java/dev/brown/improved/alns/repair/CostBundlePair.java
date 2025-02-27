package dev.brown.improved.alns.repair;

/**
 * 비용과 번들 ID를 저장하는 클래스
 * C++의 pair<float, int>를 대체
 */
public class CostBundlePair implements Comparable<CostBundlePair>{
    private final float cost;
    private final int bundleId;

    public CostBundlePair(float cost, int bundleId) {
        this.cost = cost;
        this.bundleId = bundleId;
    }

    public float getCost() {
        return cost;
    }

    public int getBundleId() {
        return bundleId;
    }

    @Override
    public int compareTo(CostBundlePair other) {
        return Float.compare(this.cost, other.cost);
    }

    @Override
    public String toString() {
        return String.format("CostBundlePair{cost=%.2f, bundleId=%d}", cost, bundleId);
    }
}
