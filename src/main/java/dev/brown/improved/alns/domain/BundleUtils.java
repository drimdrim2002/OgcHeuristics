package dev.brown.improved.alns.domain;

import java.util.List;

/**
 * 번들 관련 유틸리티 메서드들을 모아둔 클래스
 */
public class BundleUtils {
    private BundleUtils() {} // 인스턴스화 방지

    /**
     * 번들의 비용 계산
     * @param source 출발지 주문 목록
     * @param dest 도착지 주문 목록
     * @param riderInfo 라이더 정보
     * @param distMatPtr 거리 행렬
     * @param matrixLength 행렬 길이
     * @return 계산된 비용
     */
    public static double getBundleCost(List<Integer> source, List<Integer> dest,
        List<Integer> riderInfo, int[] distMatPtr, int matrixLength) {
        if (source.isEmpty()) {
            return 0.0;
        }

        double cost = riderInfo.get(1); // 기본 비용
        int m = source.size();

        // 출발지 간 이동 비용
        for (int i = 0; i < m - 1; i++) {
            cost += riderInfo.get(2) * distMatPtr[getIndex(source.get(i), source.get(i + 1), matrixLength)] / 100.0;
        }

        // 마지막 출발지에서 첫 도착지까지의 비용
        cost += riderInfo.get(2) * distMatPtr[getIndex(source.get(m - 1), dest.getFirst() + matrixLength, matrixLength)] / 100.0;

        // 도착지 간 이동 비용
        for (int i = 0; i < m - 1; i++) {
            cost += riderInfo.get(2) * distMatPtr[getIndex(dest.get(i) + matrixLength, dest.get(i + 1) + matrixLength, matrixLength)] / 100.0;
        }

        return cost;
    }

    /**
     * 2차원 인덱스를 1차원 인덱스로 변환
     */
    private static int getIndex(int i, int j, int matrixLength) {
        return i * matrixLength + j;
    }
}