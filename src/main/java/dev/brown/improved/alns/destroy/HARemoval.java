package dev.brown.improved.alns.destroy;

import dev.brown.improved.alns.domain.Solution;
import java.util.*;
import java.util.stream.IntStream;

/**
 * Historical Action Pair Removal 구현
 */
public class HARemoval implements Destroyer {
    private final int K;
    private final double[][] history;

    public HARemoval(int K, double[][] history) {
        this.K = K;
        this.history = history;
    }

    @Override
    public List<Integer> destroy(Solution solution, int nDestroy) {
        // 각 주문의 히스토리 값과 인덱스를 저장할 리스트
        List<IndexedValue> historicalValues = new ArrayList<>(K);
        double[] values = new double[K];

        // 병렬로 각 번들의 히스토리 값 계산
        IntStream.range(0, solution.size()).parallel().forEach(bundleId -> {
            List<Integer> source = solution.getSource(bundleId);
            List<Integer> dest = solution.getDest(bundleId);

            int n = source.size();

            // source 순서 간의 히스토리 값
            for (int i = 0; i < n - 1; i++) {
                synchronized (values) {
                    values[source.get(i)] += history[source.get(i)][source.get(i + 1)];
                    values[source.get(i + 1)] += history[source.get(i)][source.get(i + 1)];
                }
            }

            // source의 마지막과 dest의 첫 번째 사이의 히스토리 값
            synchronized (values) {
                values[source.get(n - 1)] += history[source.get(n - 1)][dest.get(0) + K];
                values[dest.get(0)] += history[source.get(n - 1)][dest.get(0) + K];
            }

            // dest 순서 간의 히스토리 값
            for (int i = 0; i < n - 1; i++) {
                synchronized (values) {
                    values[dest.get(i)] += history[dest.get(i) + K][dest.get(i + 1) + K];
                    values[dest.get(i + 1)] += history[dest.get(i) + K][dest.get(i + 1) + K];
                }
            }
        });

        // 히스토리 값과 인덱스를 함께 저장
        for (int i = 0; i < K; i++) {
            historicalValues.add(new IndexedValue(values[i], i));
        }

        // 히스토리 값이 큰 순서대로 정렬
        historicalValues.sort((a, b) -> Double.compare(b.value(), a.value()));

        // nDestroy 개수만큼 선택하여 반환
        return historicalValues.subList(0, nDestroy).stream()
            .map(IndexedValue::index)
            .toList();
    }

    @Override
    public void update(double score) {
        // HARemoval에서는 update가 필요 없음
    }

    @Override
    public String getCurrentMethod() {
        return "HARemoval";
    }
}