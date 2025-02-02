package dev.brown.improved.alns.domain;

import java.util.List;

/**
 * 라이더 정보를 저장하는 클래스 C++의 pair<int*, vint>를 Java record로 변환
 */
public record RiderTuple(
    int[] timeMatrix,     // 시간 행렬
    List<Integer> info    // 라이더 정보
) {

    /**
     * 불변성을 보장하기 위한 생성자
     */
    public RiderTuple {
        timeMatrix = timeMatrix.clone(); // 배열 복사
        info = List.copyOf(info);        // 불변 리스트로 변환
    }
}