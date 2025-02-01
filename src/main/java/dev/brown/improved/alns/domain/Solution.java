package dev.brown.improved.alns.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class Solution {
    private double cost;
    private List<Bundle> solutions;

    public Solution() {
        this.cost = 0.0;
        this.solutions = new ArrayList<>();
    }

    public Solution(List<Bundle> solutions, double cost) {
        this.solutions = solutions;
        this.cost = cost;
    }

    public String getRiderType(int i) {
        return solutions.get(i).riderType();
    }

    public double getCost(int i) {
        return solutions.get(i).cost();
    }

    public List<Integer> getSource(int i) {
        return solutions.get(i).source();
    }

    public List<Integer> getDest(int i) {
        return solutions.get(i).dest();
    }

    public int getBundleId(int orderId) {
        for (int i = 0; i < solutions.size(); i++) {
            List<Integer> orders = getSource(i);
            if (orders.contains(orderId)) {
                return i;
            }
        }
        return -1;
    }

    public void updateCost() {
        cost = solutions.stream()
            .mapToDouble(Bundle::cost)
            .sum();
    }

    public void remove(List<Integer> ids) {
        Set<Integer> idSet = new HashSet<>(ids);
        solutions = solutions.stream()
            .filter(bundle -> !idSet.contains(solutions.indexOf(bundle)))
            .toList();
        updateCost();
    }

    public void remove(int i) {
        cost -= solutions.get(i).cost();
        solutions.remove(i);
    }

    public RemoveOrderResult removeOrder(int orderId, int bundleId, double newCost) {
        String riderType = getRiderType(bundleId);
        List<Integer> source = new ArrayList<>(getSource(bundleId));

        if (source.size() == 1) {
            remove(bundleId);
            return new RemoveOrderResult(true, riderType);
        }

        List<Integer> dest = new ArrayList<>(getDest(bundleId));
        source.remove(Integer.valueOf(orderId));
        dest.remove(Integer.valueOf(orderId));
        cost += (newCost - getCost(bundleId));

        solutions.set(bundleId, new Bundle(riderType, newCost, source, dest));
        return new RemoveOrderResult(false, riderType);
    }

    public void append(Bundle bundle) {
        solutions.add(bundle);
        cost += bundle.cost();
    }

    public SolutionResult getSol() {
        return new SolutionResult(solutions, cost);
    }

    public ExtractResult extract() {
        List<ExtractedBundle> extracted = solutions.stream()
            .map(bundle -> new ExtractedBundle(
                bundle.riderType(),
                bundle.source(),
                bundle.dest()))
            .toList();
        return new ExtractResult(cost, extracted);
    }

    // Inner record classes
    public record RemoveOrderResult(boolean removed, String riderType) {}
    public record SolutionResult(List<Bundle> bundles, double cost) {}
    public record ExtractedBundle(String riderType, List<Integer> source, List<Integer> dest) {}
    public record ExtractResult(double cost, List<ExtractedBundle> bundles) {}
}