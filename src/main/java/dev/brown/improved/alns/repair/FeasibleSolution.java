package dev.brown.improved.alns.repair;

public class FeasibleSolution {
    final double cost;
    final int bundleId;
    final String riderType;

    FeasibleSolution(double cost, int bundleId, String riderType) {
        this.cost = cost;
        this.bundleId = bundleId;
        this.riderType = riderType;
    }

    double cost() { return cost; }
    int bundleId() { return bundleId; }
    String riderType() { return riderType; }
}
