package dev.brown.improved.alns.repair;

import java.util.ArrayList;
import java.util.List;

/**
 * 번들 솔루션을 나타내는 클래스
 */
public class BundleSolution implements Comparable<BundleSolution> {
    final FeasibleSolution solution;
    final double newCost;
    final List<Integer> newSource;
    final List<Integer> newDest;

    public BundleSolution(FeasibleSolution solution, double newCost,
        List<Integer> newSource, List<Integer> newDest) {
        this.solution = solution;
        this.newCost = newCost;
        this.newSource = new ArrayList<>(newSource); // defensive copy
        this.newDest = new ArrayList<>(newDest);    // defensive copy
    }

    @Override
    public int compareTo(BundleSolution other) {
        return Double.compare(this.solution.cost(), other.solution.cost());
    }
}