package dev.brown.improved.alns.destroy;

import dev.brown.improved.alns.domain.Solution;
import java.util.List;

/**
 * Destroy 전략을 정의하는 인터페이스
 */
public interface Destroyer {
    /**
     * 솔루션에서 주문들을 제거
     * @param solution 현재 솔루션
     * @param nDestroy 제거할 주문 수
     * @return 제거된 주문 ID 리스트
     */
    List<Integer> destroy(Solution solution, int nDestroy);

    /**
     * 전략의 점수 업데이트
     * @param score 새로운 점수
     */
    void update(double score);

    /**
     * 현재 사용 중인 메서드 이름 반환
     * @return 메서드 이름
     */
    String getCurrentMethod();
}