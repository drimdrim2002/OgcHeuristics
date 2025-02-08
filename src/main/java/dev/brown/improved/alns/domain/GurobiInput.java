package dev.brown.improved.alns.domain;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Gurobi 최적화를 위한 입력 데이터
 */
public record GurobiInput(
    int[][] edgeMatrix,      // 엣지 연결 행렬 (2K x 2K)
    int[] riderTypes,        // 각 주문의 라이더 타입 (K)
    List<Bundle> bundles,// 저장된 모든 번들
    List<Double> costs       // 각 번들의 비용
) {
    /**
     * 불변성을 보장하기 위한 생성자
     */
    public GurobiInput {
        // 방어적 복사
        edgeMatrix = deepCopyMatrix(edgeMatrix);
        riderTypes = riderTypes.clone();
        bundles = List.copyOf(bundles);
        costs = List.copyOf(costs);
    }

    /**
     * 2차원 배열의 깊은 복사
     */
    private static int[][] deepCopyMatrix(int[][] matrix) {
        return IntStream.range(0, matrix.length)
            .mapToObj(i -> matrix[i].clone())
            .toArray(int[][]::new);
    }

    /**
     * 문제 크기(K) 반환
     */
    public int getK() {
        return riderTypes.length;
    }

    /**
     * 특정 라이더 타입의 번들 수 반환
     */
    public int getBundleCountByType(String riderType) {
        return (int) bundles.stream()
            .filter(bundle -> bundle.riderType().equals(riderType))
            .count();
    }

    /**
     * 라이더 타입별 번들 수 맵 반환
     */
    public Map<String, Long> getBundleCountsByType() {
        return bundles.stream()
            .collect(Collectors.groupingBy(
                Bundle::riderType,
                Collectors.counting()
            ));
    }

    /**
     * 특정 주문이 포함된 번들 찾기
     */
    public List<Bundle> findBundlesContainingOrder(int orderId) {
        return bundles.stream()
            .filter(bundle ->
                bundle.source().contains(orderId) ||
                    bundle.dest().contains(orderId))
            .collect(Collectors.toList());
    }

    /**
     * 엣지 존재 여부 확인
     */
    public boolean hasEdge(int from, int to) {
        return edgeMatrix[from][to] == 1;
    }

    /**
     * 특정 주문의 라이더 타입 반환
     */
    public String getRiderTypeForOrder(int orderId) {
        return switch (riderTypes[orderId]) {
            case 0 -> "WALK";
            case 1 -> "BIKE";
            case 2 -> "CAR";
            default -> throw new IllegalArgumentException(
                "Invalid rider type ID: " + riderTypes[orderId]);
        };
    }

    /**
     * 번들의 평균 비용 계산
     */
    public double getAverageBundleCost() {
        if (costs.isEmpty()) return 0.0;
        return costs.stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.0);
    }

    /**
     * 총 비용 계산
     */
    public double getTotalCost() {
        return costs.stream()
            .mapToDouble(Double::doubleValue)
            .sum();
    }

    /**
     * 유효성 검사
     */
    public boolean validate() {
        // 기본 크기 검사
        if (edgeMatrix.length != edgeMatrix[0].length) return false;
        if (edgeMatrix.length != 2 * riderTypes.length) return false;
        if (bundles.size() != costs.size()) return false;

        // 엣지 매트릭스 값 검사
        for (int[] row : edgeMatrix) {
            for (int value : row) {
                if (value != 0 && value != 1) return false;
            }
        }

        // 라이더 타입 값 검사
        for (int type : riderTypes) {
            if (type < 0 || type > 2) return false;
        }

        return true;
    }

    /**
     * 문자열 표현
     */
    @Override
    public String toString() {
        return String.format(
            "GurobiInput{K=%d, bundles=%d, totalCost=%.2f, " +
                "riderTypes=%s}",
            getK(),
            bundles.size(),
            getTotalCost(),
            getBundleCountsByType()
        );
    }

    @Override
    public List<Bundle> bundles() {
        return bundles;
    }

    @Override
    public List<Double> costs() {
        return costs;
    }

    @Override
    public int[][] edgeMatrix() {
        return edgeMatrix;
    }

    @Override
    public int[] riderTypes() {
        return riderTypes;
    }
}