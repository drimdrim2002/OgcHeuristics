package dev.brown.alns.repair;

import dev.brown.domain.Solution;
import java.util.List;

public interface RepairOperator {
    /**
     * 제거된 주문들을 솔루션에 다시 삽입
     * @param solution 현재 솔루션
     * @param removedOrders 제거된 주문 ID 리스트
     * @return 삽입 성공 여부
     */
    boolean repair(Solution solution, List<Integer> removedOrders);
}