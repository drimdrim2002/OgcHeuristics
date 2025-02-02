package dev.brown.improved.alns.domain;

import java.util.List;

/**
 * 배달 번들 정보를 저장하는 클래스 C++의 tuple<string, float, vint, vint>를 Java record로 변환
 */
public record Bundle(
    String riderType,     // 라이더 타입
    double cost,          // 비용
    List<Integer> source, // 픽업 순서
    List<Integer> dest    // 배달 순서
) {

    /**
     * 불변성을 보장하기 위한 생성자
     */
    public Bundle {
        source = List.copyOf(source); // 불변 리스트로 변환
        dest = List.copyOf(dest);     // 불변 리스트로 변환
    }

    /**
     * 번들의 총 주문 수를 반환
     */
    public int size() {
        return source.size();
    }

    /**
     * 새로운 비용으로 번들을 복사
     */
    public Bundle withCost(double newCost) {
        return new Bundle(riderType, newCost, source, dest);
    }
}