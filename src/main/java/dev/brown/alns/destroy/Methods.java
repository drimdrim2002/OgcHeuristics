package dev.brown.alns.destroy;
import dev.brown.domain.Order;
import dev.brown.domain.Rider;
import dev.brown.domain.Solution;
import dev.brown.alns.parameter.HyperParameter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * ALNS 알고리즘의 Destroy 연산에 사용되는 공통 메서드들
 */
public class Methods {

    /**
     * 무작위 주문 제거
     * @param solution 현재 솔루션
     * @param nDestroy 제거할 주문 수
     * @return 제거된 주문 ID 리스트
     */
    public static List<Integer> randomRemoval(Solution solution, int nDestroy) {
        List<Integer> allOrders = new ArrayList<>(solution.orderMap().keySet());
        Collections.shuffle(allOrders);
        return allOrders.subList(0, Math.min(nDestroy, allOrders.size()));
    }

    /**
     * 주문 제거 시의 비용 변화 계산
     * @param orderId 제거할 주문 ID
     * @param rider 라이더
     * @return 비용 변화량
     */
    public static double calculateRemovalCost(int orderId, Rider rider) {
        // 현재 비용 저장
        int currentCost = rider.cost();

        // 주문 제거
        Order orderToRemove = rider.orderList().stream()
            .filter(o -> o.getId() == orderId)
            .findFirst()
            .orElse(null);

        if (orderToRemove == null) {
            return 0.0;
        }

        rider.removeOrder(orderToRemove);
        int newCost = rider.cost();

        // 주문 복구
        rider.addOrder(orderToRemove);

        return currentCost - newCost;
    }

    /**
     * 두 주문 간의 관련성 계산
     * @param order1 첫 번째 주문
     * @param order2 두 번째 주문
     * @param hyperParameter 하이퍼파라미터
     * @return 관련성 점수
     */
    public static double calculateRelatedness(Order order1, Order order2, HyperParameter hyperParameter) {
        // 거리 차이
        double distanceDiff = calculateDistance(order1, order2);

        // 시간 차이
        double timeDiff = Math.abs(order1.getReadyTime() - order2.getReadyTime());

        // 크기(볼륨) 차이
        double loadDiff = Math.abs(order1.getVolume() - order2.getVolume());

        return hyperParameter.getShawD() * distanceDiff +
            hyperParameter.getShawT() * timeDiff +
            hyperParameter.getShawL() * loadDiff;
    }

    /**
     * 비용에 노이즈 추가
     * @param value 원래 비용
     * @param noiseParam 노이즈 파라미터
     * @return 노이즈가 추가된 비용
     */
    public static double addNoise(double value, double noiseParam) {
        double noise = ThreadLocalRandom.current().nextDouble(-0.5, 0.5);
        return value * (1.0 + noiseParam * noise);
    }

    /**
     * 제거된 주문들의 유효성 검사
     * @param removed 제거된 주문 ID 리스트
     * @param solution 현재 솔루션
     * @return 유효성 여부
     */
    public static boolean validateRemoval(List<Integer> removed, Solution solution) {
        Set<Integer> uniqueRemoved = new HashSet<>(removed);
        return uniqueRemoved.size() == removed.size() &&
            !removed.isEmpty() &&
            removed.size() <= solution.orderMap().size();
    }

    /**
     * 두 주문 간의 거리 계산
     * @param order1 첫 번째 주문
     * @param order2 두 번째 주문
     * @return 두 주문 간의 거리
     */
    private static double calculateDistance(Order order1, Order order2) {
        // 주문의 위치 정보를 이용한 거리 계산
        // 실제 구현에서는 MatrixManager를 사용할 수 있습니다
        double dx = order1.getShopLat() - order2.getDeliveryLat();
        double dy = order1.getShopLon() - order2.getDeliveryLon();
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * 비용 계산 결과를 저장하는 레코드
     */
    public record CostOrderPair(double cost, int orderId)
        implements Comparable<CostOrderPair> {
        @Override
        public int compareTo(CostOrderPair other) {
            return Double.compare(this.cost, other.cost);
        }
    }

    /**
     * 관련성 계산 결과를 저장하는 레코드
     */
    public record RelatednessPair(double relatedness, int orderId)
        implements Comparable<RelatednessPair> {
        @Override
        public int compareTo(RelatednessPair other) {
            return Double.compare(this.relatedness, other.relatedness);
        }
    }

    /**
     * 제거 연산 통계를 관리하는 클래스
     */
    public static class RemovalStats {
        private int totalRemovals = 0;
        private double totalCostImprovement = 0.0;
        private final Map<String, Integer> operatorUsage = new HashMap<>();

        public void update(String opName, double costImprovement) {
            totalRemovals++;
            totalCostImprovement += costImprovement;
            operatorUsage.merge(opName, 1, Integer::sum);
        }

        public void printStats() {
            System.out.println("Total removals: " + totalRemovals);
            System.out.printf("Average cost improvement: %.2f%n",
                totalCostImprovement / totalRemovals);
            System.out.println("Operator usage:");
            operatorUsage.forEach((op, count) ->
                System.out.printf("%s: %d%n", op, count));
        }
    }
}