package dev.brown.alns.repair;

import dev.brown.domain.Order;
import dev.brown.domain.Rider;
import dev.brown.domain.Solution;
import dev.brown.domain.CalculationResult;
import java.util.*;

public class GreedyRepair implements RepairOperator {
    private final Random random;

    public GreedyRepair(Random random) {
        this.random = random;
    }

    @Override
    public boolean repair(Solution solution, List<Integer> removedOrders) {
        // 제거된 주문들을 무작위 순서로 처리
        List<Integer> shuffledOrders = new ArrayList<>(removedOrders);
        Collections.shuffle(shuffledOrders, random);

        for (Integer orderId : shuffledOrders) {
            Order order = solution.orderMap().get(orderId);
            if (!insertOrder(order, solution)) {
                return false;  // 하나라도 삽입 실패하면 전체 실패
            }
        }

        return true;
    }

    private boolean insertOrder(Order order, Solution solution) {
        double bestCost = Double.MAX_VALUE;
        Rider bestRider = null;
        int bestShopPos = -1;
        int bestDeliveryPos = -1;

        // 각 라이더에 대해 최적의 삽입 위치 탐색
        for (Rider rider : solution.riderMap().values()) {
            // 각 가능한 삽입 위치 시도
            for (int shopPos = 0; shopPos <= rider.shopIndexList().size(); shopPos++) {
                for (int deliveryPos = shopPos; deliveryPos <= rider.deliveryIndexList().size(); deliveryPos++) {
                    // 임시로 주문 삽입
                    List<Integer> tempShopList = new ArrayList<>(rider.shopIndexList());
                    List<Integer> tempDeliveryList = new ArrayList<>(rider.deliveryIndexList());

                    tempShopList.add(shopPos, order.id());
                    tempDeliveryList.add(deliveryPos, order.id());

                    // 임시 상태로 비용 계산
                    rider.setShopIndexList(tempShopList);
                    rider.setDeliveryIndexList(tempDeliveryList);

                    CalculationResult result = rider.calculate();

                    // 원래 상태로 복구
                    rider.setShopIndexList(new ArrayList<>(rider.shopIndexList()));
                    rider.setDeliveryIndexList(new ArrayList<>(rider.deliveryIndexList()));

                    // 더 나은 해를 찾은 경우 업데이트
                    if (result.isFeasible() && result.cost() < bestCost) {
                        bestCost = result.cost();
                        bestRider = rider;
                        bestShopPos = shopPos;
                        bestDeliveryPos = deliveryPos;
                    }
                }
            }
        }

        // 실행 가능한 삽입 위치를 찾은 경우
        if (bestRider != null) {
            // 최적의 위치에 주문 삽입
            List<Integer> newShopList = new ArrayList<>(bestRider.shopIndexList());
            List<Integer> newDeliveryList = new ArrayList<>(bestRider.deliveryIndexList());

            newShopList.add(bestShopPos, order.id());
            newDeliveryList.add(bestDeliveryPos, order.id());

            bestRider.setShopIndexList(newShopList);
            bestRider.setDeliveryIndexList(newDeliveryList);

            // 주문-라이더 연결 설정
            order.setRider(bestRider);
            bestRider.addOrder(order);

            return true;
        }

        return false;  // 실행 가능한 삽입 위치를 찾지 못함
    }
}