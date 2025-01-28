package dev.brown.alns.destroy;

import dev.brown.domain.Solution;
import java.util.List;

public interface DestroyOperator {
    /**
     * 주어진 솔루션에서 주문들을 제거
     * @param solution 현재 솔루션
     * @param numToDestroy 제거할 주문 수
     * @return 제거된 주문 ID 리스트
     */
    List<Integer> destroy(Solution solution, int numToDestroy);
}