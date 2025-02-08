package dev.brown.improved.alns.domain;

import java.util.List;

/**
 * 솔루션 결과를 저장하는 포맷
 */
public record SolutionFormat(
    double cost,                  // 전체 솔루션 비용
    List<Bundle> bundles     // 번들 정보 리스트
) {
    /**
     * 불변성을 보장하기 위한 생성자
     */
    public SolutionFormat {
        bundles = List.copyOf(bundles);  // 불변 리스트로 변환
    }

    /**
     * 번들 수 반환
     */
    public int size() {
        return bundles.size();
    }

    /**
     * 특정 인덱스의 번들 정보 반환
     */
    public Bundle getBundle(int index) {
        return bundles.get(index);
    }

    /**
     * 모든 번들의 총 주문 수 반환
     */
    public int getTotalOrders() {
        return bundles.stream()
            .mapToInt(bundle -> bundle.source().size())
            .sum();
    }

    /**
     * 평균 번들 크기 반환
     */
    public double getAverageBundleSize() {
        if (bundles.isEmpty()) return 0.0;
        return (double) getTotalOrders() / bundles.size();
    }

    /**
     * 특정 라이더 타입의 번들 수 반환
     */
    public long countByRiderType(String riderType) {
        return bundles.stream()
            .filter(bundle -> bundle.riderType().equals(riderType))
            .count();
    }

    /**
     * 솔루션의 간단한 문자열 표현 반환
     */
    @Override
    public String toString() {
        return String.format(
            "SolutionFormat{cost=%.2f, bundles=%d, totalOrders=%d}",
            cost,
            bundles.size(),
            getTotalOrders()
        );
    }
}