package dev.brown.improved.alns.destroy;

import dev.brown.improved.alns.domain.*;
import java.util.*;
import java.util.stream.IntStream;

/**
 * Semi-Worst Removal 전략 구현
 */
public class SemiWorstRemoval implements Destroyer {
    private final int K;
    private final RiderInfo riderInfo;
    private final int[] distMatPtr;
    private final int matrixLength;
    private final double worstNoise;
    private final Random random;

    public SemiWorstRemoval(int K, RiderInfo riderInfo, int[] distMatPtr,
        int matrixLength, double worstNoise, long seed) {
        this.K = K;
        this.riderInfo = riderInfo;
        this.distMatPtr = distMatPtr;
        this.matrixLength = matrixLength;
        this.worstNoise = worstNoise;
        this.random = new Random(seed);
    }

    @Override
    public List<Integer> destroy(Solution originalSol, int nDestroy) {
        Solution sol = new Solution(originalSol);
        List<Integer> toRemove = new ArrayList<>();
        int[] costOfOrder = new int[K];

        // 병렬로 각 번들의 비용 계산
        IntStream.range(0, sol.size()).parallel().forEach(bundleId ->
            updateBundle(bundleId, costOfOrder, sol));

        // 비용이 있는 주문들만 선택하여 정렬
        List<IndexedValue> orderCosts = new ArrayList<>();
        for (int i = 0; i < K; i++) {
            if (costOfOrder[i] > 0) {
                orderCosts.add(new IndexedValue(costOfOrder[i], i));
            }
        }
        orderCosts.sort((a, b) -> Double.compare(b.value(), a.value()));

        List<Integer> erasePlease = new ArrayList<>();
        int worstOrder = -1;

        for (int rep = 0; rep < nDestroy; rep++) {
            if (rep >= 1) {
                erasePlease.add(worstOrder);
                removeElements(orderCosts, erasePlease);
                merge(orderCosts, erasePlease, costOfOrder);
            }

            // 노이즈를 적용한 인덱스 선택
            int noiseIdx = (int) Math.floor(Math.pow(random.nextDouble(), worstNoise)
                * orderCosts.size());
            worstOrder = orderCosts.get(noiseIdx).index();

            int worstBundleId = sol.getBundleId(worstOrder);
            costOfOrder[worstOrder] = -1;

            toRemove.add(worstOrder);

            // 솔루션 업데이트
            Map.Entry<Boolean, String> res = sol.removeOrder(worstOrder, worstBundleId, 0.0);
            if (worstBundleId < sol.size()) {
                erasePlease = updateBundle(worstBundleId, costOfOrder, sol);
            } else {
                erasePlease.clear();
            }
        }

        return toRemove;
    }

    private List<Integer> updateBundle(int bundleId, int[] costOfOrder, Solution sol) {
        List<Integer> source = sol.getSource(bundleId);
        List<Integer> dest = sol.getDest(bundleId);

        // 수정된 부분: prepare 메서드의 반환값을 Map.Entry로 받아서 처리
        Map.Entry<int[], List<Integer>> riderData = riderInfo.prepare(sol.getRiderType(bundleId));
        List<Integer> rider = riderData.getValue();

        int m = source.size();
        if (m == 1) {
            int order = source.get(0);
            double oneSourceCost = rider.get(1) + rider.get(2) *
                distMatPtr[getIndex(order, order + K)] / 100.0;
            costOfOrder[order] = (int) Math.round(100 * oneSourceCost);
            return source;
        }

        // source update
        for (int i = 0; i < m; i++) {
            int order = source.get(i);
            int next = (i == m-1) ? dest.get(0) + K : source.get(i + 1);
            int distIncrement = -distMatPtr[getIndex(order, next)];
            if (i != 0) {
                distIncrement += distMatPtr[getIndex(source.get(i-1), next)] -
                    distMatPtr[getIndex(source.get(i-1), order)];
            }
            costOfOrder[order] -= distIncrement * rider.get(2);
        }

        // dest update
        for (int i = 0; i < m; i++) {
            int order = dest.get(i);
            int befo = (i == 0) ? source.get(m-1) : dest.get(i) + K;
            int distIncrement = -distMatPtr[getIndex(befo, order + K)];
            if (i != m-1) {
                distIncrement += distMatPtr[getIndex(befo, dest.get(i+1) + K)] -
                    distMatPtr[getIndex(order + K, dest.get(i+1) + K)];
            }
            costOfOrder[order] -= distIncrement * rider.get(2);
        }

        // exception case
        if (source.get(m-1).equals(dest.get(0))) {
            int order = dest.get(0);
            int befo = source.get(m-2);
            int next = dest.get(1) + K;
            int distIncrement = distMatPtr[getIndex(befo, next)] -
                distMatPtr[getIndex(befo, order)] -
                distMatPtr[getIndex(order, order + K)] -
                distMatPtr[getIndex(order + K, next)];

            costOfOrder[order] = -distIncrement * rider.get(2);
        }

        return source;
    }

    private int getIndex(int i, int j) {
        return i * matrixLength + j;
    }

    private void removeElements(List<IndexedValue> list, List<Integer> toRemove) {
        Set<Integer> removeSet = new HashSet<>(toRemove);
        list.removeIf(item -> removeSet.contains(item.index()));
    }

    private void merge(List<IndexedValue> target, List<Integer> source, int[] costOfOrder) {
        List<IndexedValue> temp = new ArrayList<>();
        for (int x : source) {
            if (costOfOrder[x] > 0) {
                temp.add(new IndexedValue(costOfOrder[x], x));
            }
        }
        temp.sort((a, b) -> Double.compare(b.value(), a.value()));

        target.addAll(temp);
        target.sort((a, b) -> Double.compare(b.value(), a.value()));
    }

    @Override
    public void update(double score) {
        // Semi-Worst Removal에서는 update가 필요 없음
    }

    @Override
    public String getCurrentMethod() {
        return "SemiWorstRemoval";
    }
}