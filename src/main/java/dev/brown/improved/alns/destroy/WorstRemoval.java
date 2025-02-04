package dev.brown.improved.alns.destroy;

import dev.brown.improved.alns.domain.*;
import java.util.*;
import java.util.stream.IntStream;

public class WorstRemoval implements Destroyer {
    private final int K;
    private final RiderInfo riderInfo;
    private final int[] distMatPtr;
    private final int matrixLength;
    private final double worstNoise;
    private final Random random;

    public WorstRemoval(int K, RiderInfo riderInfo, int[] distMatPtr,
        int matrixLength, double worstNoise, long seed) {
        this.K = K;
        this.riderInfo = riderInfo;
        this.distMatPtr = distMatPtr;
        this.matrixLength = matrixLength;
        this.worstNoise = worstNoise;
        this.random = new Random(seed);
    }

    @Override
    public List<Integer> destroy(Solution sol, int nDestroy) {
        Map<String, Integer> ridersAvailable = new HashMap<>();
        List<Double> samples = IntStream.range(0, nDestroy)
            .mapToObj(i -> random.nextDouble())
            .toList();

        return worstRemoval(nDestroy, sol, ridersAvailable, samples);
    }

    private List<Integer> worstRemoval(int nDestroy, Solution sol,
        Map<String, Integer> ridersAvailable, List<Double> samples) {
        List<Integer> toRemove = new ArrayList<>();
        List<OrderCost> costOfOrder = new ArrayList<>(Collections.nCopies(K, new OrderCost(0, 0.0)));

        // 병렬로 각 번들의 비용 계산
        IntStream.range(0, sol.size()).parallel()
            .forEach(bundleId -> updateBundle(bundleId, costOfOrder, sol));

        boolean needUpdate = false;
        List<Integer> erasePlease = new ArrayList<>();

        // 초기 비용 순서 설정
        List<IndexedValue> orderValues = new ArrayList<>();
        for (int i = 0; i < K; i++) {
            if (costOfOrder.get(i).cost > 0) {
                orderValues.add(new IndexedValue(costOfOrder.get(i).cost, i));
            }
        }
        orderValues.sort((a, b) -> Double.compare(b.value(), a.value()));

        for (int rep = 0; rep < nDestroy; rep++) {
            if (needUpdate) {
                orderValues = merge(orderValues, erasePlease, costOfOrder);
            }

            int noiseIdx = (int) Math.floor(Math.pow(samples.get(rep), worstNoise)
                * orderValues.size());
            int worstOrder = orderValues.get(noiseIdx).index();
            toRemove.add(worstOrder);

            int worstBundleId = sol.getBundleId(worstOrder);
            costOfOrder.set(worstOrder, new OrderCost(-1, 0.0));

            // 솔루션과 라이더 가용성 업데이트
            Map.Entry<Boolean, String> res = sol.removeOrder(worstOrder, worstBundleId,
                costOfOrder.get(worstOrder).newCost);

            if (res.getKey()) {
                ridersAvailable.merge(res.getValue(), 1, Integer::sum);
                removeOrder(orderValues, worstOrder);
                needUpdate = false;
            } else {
                erasePlease = updateBundle(worstBundleId, costOfOrder, sol);
                erasePlease.add(worstOrder);
                removeElements(orderValues, erasePlease);
                needUpdate = true;
            }
        }

        return toRemove;
    }

    private List<Integer> updateBundle(int bundleId, List<OrderCost> costOfOrder,
        Solution sol) {
        double orgCost = sol.getCost(bundleId);
        List<Integer> source = sol.getSource(bundleId);
        List<Integer> dest = sol.getDest(bundleId);
        List<Integer> rider = riderInfo.prepare(sol.getRiderType(bundleId)).getValue();

        int m = source.size();
        if (m == 1) {
            int order = source.getFirst();
            costOfOrder.set(order, new OrderCost((int) Math.round(100 * orgCost), 0.0));
            return source;
        }

        // source update
        for (int i = 0; i < m; i++) {
            int order = source.get(i);
            int next = (i == m-1) ? dest.getFirst() + K : source.get(i + 1);
            int distIncrement = -distMatPtr[getIndex(order, next)];
            if (i != 0) {
                distIncrement += distMatPtr[getIndex(source.get(i-1), next)] -
                    distMatPtr[getIndex(source.get(i-1), order)];
            }
            costOfOrder.get(order).cost = -distIncrement * rider.get(2);
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
            costOfOrder.get(order).cost -= distIncrement * rider.get(2);
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

            costOfOrder.get(order).cost = -distIncrement * rider.get(2);
        }

        // Update new costs
        for (int order : source) {
            costOfOrder.get(order).newCost = orgCost -
                costOfOrder.get(order).cost / 100.0;
        }

        return source;
    }

    private int getIndex(int i, int j) {
        return i * matrixLength + j;
    }

    private void removeElements(List<IndexedValue> values, List<Integer> toRemove) {
        Set<Integer> removeSet = new HashSet<>(toRemove);
        values.removeIf(item -> removeSet.contains(item.index()));
    }

    private void removeOrder(List<IndexedValue> values, int order) {
        values.removeIf(item -> item.index() == order);
    }

    private List<IndexedValue> merge(List<IndexedValue> values, List<Integer> considerPlease,
        List<OrderCost> costOfOrder) {
        List<IndexedValue> temp = new ArrayList<>();
        for (int x : considerPlease) {
            if (costOfOrder.get(x).cost > 0) {
                temp.add(new IndexedValue(costOfOrder.get(x).cost, x));
            }
        }
        temp.sort((a, b) -> Double.compare(b.value(), a.value()));

        List<IndexedValue> result = new ArrayList<>();
        int i = 0, j = 0;
        while (i < values.size() && j < temp.size()) {
            if (values.get(i).value() > temp.get(j).value()) {
                result.add(values.get(i++));
            } else {
                result.add(temp.get(j++));
            }
        }
        while (i < values.size()) result.add(values.get(i++));
        while (j < temp.size()) result.add(temp.get(j++));

        return result;
    }

    @Override
    public void update(double score) {
        // WorstRemoval에서는 update가 필요 없음
    }

    @Override
    public String getCurrentMethod() {
        return "WorstRemoval";
    }
}

/**
 * 주문의 비용 정보를 저장하는 클래스
 */
class OrderCost {
    int cost;
    double newCost;

    OrderCost(int cost, double newCost) {
        this.cost = cost;
        this.newCost = newCost;
    }
}