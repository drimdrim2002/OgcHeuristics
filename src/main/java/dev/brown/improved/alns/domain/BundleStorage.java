package dev.brown.improved.alns.domain;

import java.util.*;

public class BundleStorage {
    private final Map<BundleKey, Bundle> storage;

    public BundleStorage() {
        this.storage = new HashMap<>();
    }

    public BundleStorage(List<Bundle> initStorage) {
        this.storage = new HashMap<>();
        for (Bundle bundle : initStorage) {
            BundleKey key = getKey(bundle);
            storage.put(key, bundle);
        }
    }

    private BundleKey getKey(Bundle bundle) {
        List<Integer> orders = new ArrayList<>(bundle.source());
        Collections.sort(orders);
        return new BundleKey(bundle.riderType(), orders);
    }

    public void append(Bundle bundle) {
        BundleKey key = getKey(bundle);
        Bundle existingBundle = storage.get(key);

        if (existingBundle == null || bundle.cost() < existingBundle.cost()) {
            storage.put(key, bundle);
        }
    }

    public void append(Solution sol, double prob) {
        for (Bundle bundle : sol.getSolutions()) {
            append(bundle);
        }
    }

    public ExtractResult extract(Solution bestSol, int K, RiderInfo riderInfo, int[] distMatrix, int l) {
        Map<BundleKey, Integer> keysToIndex = new HashMap<>();
        List<Bundle> bundles = new ArrayList<>();

        int idx = 0;
        for (Bundle bundle : storage.values()) {
            bundles.add(bundle);
            keysToIndex.put(getKey(bundle), idx++);
        }

        List<Integer> gurobiInitialSolution = new ArrayList<>();
        for (Bundle bundle : bestSol.getSolutions()) {
            BundleKey key = getKey(bundle);
            gurobiInitialSolution.add(keysToIndex.get(key));
        }

        List<List<Integer>> constraintHelperMatrix = new ArrayList<>(K);
        for (int i = 0; i < K; i++) {
            constraintHelperMatrix.add(new ArrayList<>());
        }

        for (int i = 0; i < bundles.size(); i++) {
            Bundle bundle = bundles.get(i);
            for (int order : bundle.source()) {
                constraintHelperMatrix.get(order).add(i);
            }
        }

        return new ExtractResult(gurobiInitialSolution, constraintHelperMatrix, bundles);
    }

    // Inner record classes
    public record BundleKey(String riderType, List<Integer> orders) {}
    public record ExtractResult(
        List<Integer> gurobiInitialSolution,
        List<List<Integer>> constraintHelperMatrix,
        List<Bundle> bundles
    ) {}
}