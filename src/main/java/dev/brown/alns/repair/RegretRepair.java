package dev.brown.alns.repair;

import dev.brown.domain.Order;
import dev.brown.domain.Rider;
import dev.brown.domain.Solution;
import dev.brown.domain.CalculationResult;
import dev.brown.alns.parameter.HyperParameter;
import java.util.*;

public class RegretRepair implements RepairOperator {
    private final Random random;
    private final HyperParameter hparam;
    private final int k;  // regret-k 값

    public RegretRepair(int k, HyperParameter hparam, Random random) {
        this.k = k;
        this.hparam = hparam;
        this.random = random;
    }

    @Override
    public boolean repair(Solution solution, List<Integer> removedOrders) {
        List<Integer> unassignedOrders = new ArrayList<>(removedOrders);

        while (!unassignedOrders.isEmpty()) {
            // 각 미할당 주문의 regret 값 계산
            OrderRegret bestOrder = findOrderWithHighestRegret(unassignedOrders, solution);

            if (bestOrder == null) {
                return false;  // 실행 가능한 삽입 위치를 찾지 못함
            }

            // 선택된 주문을 최적 위치에 삽입
            Order order = solution.orderMap().get(bestOrder.orderId);
            insertOrder(order, bestOrder.bestRider, bestOrder.bestShopPos, bestOrder.bestDeliveryPos);
            unassignedOrders.remove(Integer.valueOf(bestOrder.orderId));
        }

        return true;
    }

    private OrderRegret findOrderWithHighestRegret(List<Integer> unassignedOrders, Solution solution) {
        OrderRegret bestRegret = null;

        for (Integer orderId : unassignedOrders) {
            Order order = solution.orderMap().get(orderId);
            List<InsertionCost> insertionCosts = findKBestInsertions(order, solution);

            if (insertionCosts.isEmpty()) {
                continue;  // 실행 가능한 삽입 위치가 없음
            }

            // regret 값 계산
            double regretValue = calculateRegretValue(insertionCosts);

            // 최선의 삽입 위치 정보
            InsertionCost bestInsertion = insertionCosts.get(0);

            OrderRegret orderRegret = new OrderRegret(
                orderId,
                regretValue,
                bestInsertion.cost,
                bestInsertion.rider,
                bestInsertion.shopPos,
                bestInsertion.deliveryPos
            );

            if (bestRegret == null || orderRegret.regretValue > bestRegret.regretValue) {
                bestRegret = orderRegret;
            }
        }

        return bestRegret;
    }

    private List<InsertionCost> findKBestInsertions(Order order, Solution solution) {
        List<InsertionCost> allInsertions = new ArrayList<>();

        // 각 라이더에 대해 가능한 모든 삽입 위치 시도
        for (Rider rider : solution.riderMap().values()) {
            for (int shopPos = 0; shopPos <= rider.shopIndexList().size(); shopPos++) {
                for (int deliveryPos = shopPos; deliveryPos <= rider.deliveryIndexList().size(); deliveryPos++) {
                    // 임시로 주문 삽입
                    List<Integer> tempShopList = new ArrayList<>(rider.shopIndexList());
                    List<Integer> tempDeliveryList = new ArrayList<>(rider.deliveryIndexList());

                    tempShopList.add(shopPos, order.getId());
                    tempDeliveryList.add(deliveryPos, order.getId());

                    rider.setShopIndexList(tempShopList);
                    rider.setDeliveryIndexList(tempDeliveryList);

                    CalculationResult result = rider.calculate();

                    // 원래 상태로 복구
                    rider.setShopIndexList(new ArrayList<>(rider.shopIndexList()));
                    rider.setDeliveryIndexList(new ArrayList<>(rider.deliveryIndexList()));

                    if (result.isFeasible()) {
                        allInsertions.add(new InsertionCost(
                            result.cost(),
                            rider,
                            shopPos,
                            deliveryPos
                        ));
                    }
                }
            }
        }

        // 비용 기준으로 정렬
        Collections.sort(allInsertions);

        // k개의 최선의 삽입 위치 반환
        return allInsertions.subList(0, Math.min(k, allInsertions.size()));
    }

    private double calculateRegretValue(List<InsertionCost> insertionCosts) {
        double regret = 0;
        double bestCost = insertionCosts.get(0).cost;

        // k-regret 값 계산
        for (int i = 1; i < Math.min(k, insertionCosts.size()); i++) {
            regret += (insertionCosts.get(i).cost - bestCost);
        }

        return regret;
    }

    private void insertOrder(Order order, Rider rider, int shopPos, int deliveryPos) {
        List<Integer> newShopList = new ArrayList<>(rider.shopIndexList());
        List<Integer> newDeliveryList = new ArrayList<>(rider.deliveryIndexList());

        newShopList.add(shopPos, order.getId());
        newDeliveryList.add(deliveryPos, order.getId());

        rider.setShopIndexList(newShopList);
        rider.setDeliveryIndexList(newDeliveryList);

//        order.setRider(rider);
        rider.addOrder(order);
    }

    private static class OrderRegret {
        final int orderId;
        final double regretValue;
        final double bestCost;
        final Rider bestRider;
        final int bestShopPos;
        final int bestDeliveryPos;

        OrderRegret(int orderId, double regretValue, double bestCost,
            Rider bestRider, int bestShopPos, int bestDeliveryPos) {
            this.orderId = orderId;
            this.regretValue = regretValue;
            this.bestCost = bestCost;
            this.bestRider = bestRider;
            this.bestShopPos = bestShopPos;
            this.bestDeliveryPos = bestDeliveryPos;
        }
    }

    private static class InsertionCost implements Comparable<InsertionCost> {
        final double cost;
        final Rider rider;
        final int shopPos;
        final int deliveryPos;

        InsertionCost(double cost, Rider rider, int shopPos, int deliveryPos) {
            this.cost = cost;
            this.rider = rider;
            this.shopPos = shopPos;
            this.deliveryPos = deliveryPos;
        }

        @Override
        public int compareTo(InsertionCost other) {
            return Double.compare(this.cost, other.cost);
        }
    }
}