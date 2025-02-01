package dev.brown.alns.repair;

import dev.brown.domain.Order;
import dev.brown.domain.Rider;
import dev.brown.domain.Solution;
import dev.brown.domain.CalculationResult;
import java.util.*;

public class RandomRepair implements RepairOperator {
    private final Random random;
    private static final int MAX_ATTEMPTS = 50;  // 각 주문당 최대 시도 횟수

    public RandomRepair(Random random) {
        this.random = random;
    }

    @Override
    public boolean repair(Solution solution, List<Integer> removedOrders) {
        List<Integer> unassignedOrders = new ArrayList<>(removedOrders);
        Collections.shuffle(unassignedOrders, random);  // 무작위 순서로 처리

        for (Integer orderId : unassignedOrders) {
            Order order = solution.orderMap().get(orderId);
            if (!insertOrderRandomly(order, solution)) {
                return false;  // 하나라도 삽입 실패하면 전체 실패
            }
        }

        return true;
    }

    private boolean insertOrderRandomly(Order order, Solution solution) {
        List<Rider> riders = new ArrayList<>(solution.riderMap().values());
        Collections.shuffle(riders, random);  // 라이더 순서도 무작위화

        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            // 무작위 라이더 선택
            Rider rider = riders.get(random.nextInt(riders.size()));

            // 가능한 삽입 위치 범위 결정
            int maxShopPos = rider.shopIndexList().size() + 1;
            int shopPos = random.nextInt(maxShopPos);

            // 배달 위치는 픽업 위치 이후여야 함
            int maxDeliveryPos = rider.deliveryIndexList().size() + 1;
            int deliveryPos = shopPos + random.nextInt(maxDeliveryPos - shopPos);

            // 임시로 주문 삽입
            List<Integer> tempShopList = new ArrayList<>(rider.shopIndexList());
            List<Integer> tempDeliveryList = new ArrayList<>(rider.deliveryIndexList());

            tempShopList.add(shopPos, order.getId());
            tempDeliveryList.add(deliveryPos, order.getId());

            rider.setShopIndexList(tempShopList);
            rider.setDeliveryIndexList(tempDeliveryList);

            CalculationResult result = rider.calculate();

            if (result.isFeasible()) {
                // 실행 가능한 위치를 찾았으면 주문 할당
//                order.setRider(rider);
                rider.addOrder(order);
                return true;
            }

            // 원래 상태로 복구
            rider.setShopIndexList(new ArrayList<>(rider.shopIndexList()));
            rider.setDeliveryIndexList(new ArrayList<>(rider.deliveryIndexList()));
        }

        return false;  // MAX_ATTEMPTS 시도 후에도 실행 가능한 위치를 찾지 못함
    }

    /**
     * 실행 가능한 삽입 위치를 찾을 확률을 높이기 위한 휴리스틱 메서드
     */
    private int getSmartRandomDeliveryPos(int shopPos, int maxDeliveryPos) {
        // 배달 위치가 픽업 위치와 너무 멀어지지 않도록 제한
        int maxDistance = Math.min(5, maxDeliveryPos - shopPos);  // 최대 거리 제한
        return shopPos + random.nextInt(maxDistance + 1);
    }

    /**
     * 주문의 시간 제약을 고려한 삽입 위치 선택
     */
    private boolean isTimeCompatible(Order order, Rider rider, int shopPos, int deliveryPos) {
        // 주문의 준비 시간과 마감 시간을 고려한 간단한 검사
        int currentTime = rider.getCurrentTime();
        int estimatedPickupTime = currentTime + (shopPos * 5);  // 예상 픽업 시간
        int estimatedDeliveryTime = estimatedPickupTime + ((deliveryPos - shopPos) * 5);  // 예상 배달 시간

        return estimatedPickupTime >= order.getReadyTime() &&
            estimatedDeliveryTime <= order.getDeadline();
    }

    /**
     * 라이더의 현재 적재량을 고려한 실행 가능성 검사
     */
    private boolean isCapacityCompatible(Order order, Rider rider) {
        int currentLoad = rider.getCurrentLoad();
        return currentLoad + order.getVolume() <= rider.getCapacity();
    }
}