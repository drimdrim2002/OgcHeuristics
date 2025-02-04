package dev.brown.improved.alns.repair;

import dev.brown.improved.alns.domain.Solution;
import java.util.List;
import java.util.Map;

/**
 * 복구 전략을 위한 인터페이스
 */
public interface Repairer {
    /**
     * 파괴된 솔루션을 복구
     *
     * @param idsToBuild 재구성해야 할 주문 ID 목록
     * @param solution 현재 솔루션
     * @param ridersAvailable 사용 가능한 라이더 정보
     */
    void repair(List<Integer> idsToBuild, Solution solution, Map<String, Integer> ridersAvailable);

    /**
     * 복구 점수 업데이트 (선택적 구현)
     *
     * @param score 복구 성공 점수
     */
    default void update(double score) {
        // 기본적으로는 아무 동작도 하지 않음
    }

    /**
     * 현재 사용 중인 복구 방법 이름 반환
     *
     * @return 복구 방법 이름
     */
    default String getCurrentMethod() {
        return this.getClass().getSimpleName();
    }
}