package dev.brown.improved.alns.domain;

import java.util.List;

/**
 * 번들 저장소의 키로 사용되는 클래스 C++의 pair<string, vint>를 Java record로 변환
 */
public record BundleKey(
    String riderType,
    List<Integer> orders
) {

    /**
     * 불변성을 보장하기 위한 생성자
     */
    public BundleKey {
        orders = List.copyOf(orders); // 불변 리스트로 변환
    }
}