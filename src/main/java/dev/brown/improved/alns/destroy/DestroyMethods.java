package dev.brown.improved.alns.destroy;

import dev.brown.improved.alns.domain.Solution;
import java.util.*;

/**
 * Destroy 메서드들을 모아둔 유틸리티 클래스
 */
//todo: OpenMIP 병렬 처리 제거 로직을 반영해야 함
public class DestroyMethods {
    private DestroyMethods() {} // 인스턴스화 방지

    /**
     * 무작위 제거
     */
    public static List<Integer> randomRemoval(int nDestroy, int K, Random rng) {
        List<Integer> indices = new ArrayList<>(K);
        for (int i = 0; i < K; i++) {
            indices.add(i);
        }
        Collections.shuffle(indices, rng);
        return indices.subList(0, nDestroy);
    }

    /**
     * 거리 기반 제거
     */
    public static List<Integer> distanceOrientedRemoval(int nDestroy, int K, Random rng,
        double[][] distRel) {
        int id1 = rng.nextInt(K);

        List<IndexedValue> values = new ArrayList<>(K);
        for (int id2 = 0; id2 < K; id2++) {
            values.add(new IndexedValue(distRel[id1][id2], id2));
        }
        Collections.sort(values);

        return values.subList(0, nDestroy).stream()
            .map(IndexedValue::index)
            .toList();
    }

    /**
     * 매장 기반 제거
     */
    public static List<Integer> shopOrientedRemoval(int nDestroy, int K, Random rng,
        double[][] shopDistRel) {
        int id1 = rng.nextInt(K);

        List<IndexedValue> values = new ArrayList<>(K);
        for (int id2 = 0; id2 < K; id2++) {
            values.add(new IndexedValue(shopDistRel[id1][id2], id2));
        }
        Collections.sort(values);

        return values.subList(0, nDestroy).stream()
            .map(IndexedValue::index)
            .toList();
    }

    /**
     * 배달 기반 제거
     */
    public static List<Integer> deliveryOrientedRemoval(int nDestroy, int K, Random rng,
        double[][] dlvDistRel) {
        int id1 = rng.nextInt(K);

        List<IndexedValue> values = new ArrayList<>(K);
        for (int id2 = 0; id2 < K; id2++) {
            values.add(new IndexedValue(dlvDistRel[id1][id2], id2));
        }
        Collections.sort(values);

        return values.subList(0, nDestroy).stream()
            .map(IndexedValue::index)
            .toList();
    }

    /**
     * 경로 기반 제거
     */
    public static List<Integer> routeRemoval(int nDestroy, Solution sol,
        double routeRemovalRatio, Random rng) {
        int nBundle = sol.size();
        List<Integer> indices = new ArrayList<>(nBundle);
        for (int i = 0; i < nBundle; i++) {
            indices.add(i);
        }
        Collections.shuffle(indices, rng);

        int nDestroyBundle = (int) Math.floor(nBundle * routeRemovalRatio);
        List<Integer> toRemove = new ArrayList<>();
        for (int i = 0; i < nDestroyBundle; i++) {
            toRemove.addAll(sol.getSource(indices.get(i)));
        }
        return toRemove;
    }

    /**
     * Shaw 제거
     */
    public static List<Integer> shawRemoval(int nDestroy, Solution sol, int K,
        double shawD, double shawT, double shawL, double shawNoise,
        double[][] distRel, double[][] timeRel, double[][] loadRel, Random rng) {
        int initOrder = rng.nextInt(K);
        List<Integer> toRemove = new ArrayList<>();
        toRemove.add(initOrder);

        boolean[] checked = new boolean[K];
        checked[initOrder] = true;

        while (toRemove.size() < nDestroy) {
            int id1 = toRemove.get(rng.nextInt(toRemove.size()));

            List<IndexedValue> values = new ArrayList<>();
            for (int id2 = 0; id2 < K; id2++) {
                if (!checked[id2]) {
                    double rel = shawD * distRel[id1][id2] +
                        shawT * timeRel[id1][id2] +
                        shawL * loadRel[id1][id2];
                    values.add(new IndexedValue(rel, id2));
                }
            }
            Collections.sort(values);

            int noiseIdx = (int) Math.floor(Math.pow(rng.nextDouble(), shawNoise) * values.size());
            int shawOrder = values.get(noiseIdx).index();

            toRemove.add(shawOrder);
            checked[shawOrder] = true;
        }
        return toRemove;
    }

    /**
     * 히스토리 업데이트
     */
    public static void historyUpdate(Solution sol, int K, double[][] history) {
        double cost = sol.getCost() / K;
        for (int bundleId = 0; bundleId < sol.size(); bundleId++) {
            List<Integer> s = sol.getSource(bundleId);
            List<Integer> d = sol.getDest(bundleId);

            int n = s.size();
            for (int i = 0; i < n - 1; i++) {
                history[s.get(i)][s.get(i + 1)] =
                    Math.min(history[s.get(i)][s.get(i + 1)], cost);
            }
            history[s.get(n-1)][d.get(0) + K] =
                Math.min(history[s.get(n-1)][d.get(0) + K], cost);
            for (int i = 0; i < n - 1; i++) {
                history[d.get(i) + K][d.get(i + 1) + K] =
                    Math.min(history[d.get(i) + K][d.get(i + 1) + K], cost);
            }
        }
    }
}