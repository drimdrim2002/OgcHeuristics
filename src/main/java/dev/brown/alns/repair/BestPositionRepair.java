package dev.brown.alns.repair;

import dev.brown.domain.Order;
import dev.brown.domain.Rider;
import dev.brown.domain.Solution;
import dev.brown.domain.CalculationResult;
import dev.brown.alns.parameter.HyperParameter;
import java.util.*;

public class BestPositionRepair implements RepairOperator {
    private final Random random;
    private final HyperParameter hparam;
    private static final int MAX_ITERATIONS = 100;  // 최대 반복 횟수

    public BestPositionRepair(HyperParameter hparam, Random random) {
        this.hparam = hparam;
        this.random = random;
    }

    @Override
    public boolean repair(Solution solution, List<Integer> removedOrders) {
        List<Integer> unassignedOrders = new ArrayList<>(removedOrders);
        double bestTotalCost = Double.MAX_VALUE;
        Map<Integer, InsertionPosition> bestPositions = new HashMap<>();

        // 여러 번의 시도를 통해 최적의 전체 해 탐색
        for (int iter = 0; iter < MAX_ITERATIONS; iter++) {
            Map<Integer, InsertionPosition> currentPositions = new HashMap<>();
            double currentTotalCost = findBestConfiguration(
                new ArrayList<>(unassignedOrders),
                solution,
                currentPositions
            );

            if (currentTotalCost < bestTotalCost) {
                bestTotalCost = currentTotalCost;
                bestPositions = new HashMap<>(currentPositions);
            }
        }

        // 최적의 위치에 주문들 삽입
        if (!bestPositions.isEmpty()) {
            for (Integer orderId : unassignedOrders) {
                InsertionPosition pos = bestPositions.get(orderId);
                if (pos == null) return false;

                Order order = solution.orderMap().get(orderId);
                insertOrder(order, pos.rider, pos.shopPos, pos.deliveryPos);
            }
            return true;
        }

        return false;
    }

    private double findBestConfiguration(List<Integer> orders, Solution solution,
        Map<Integer, InsertionPosition> positions) {
        Collections.shuffle(orders, random);  // 무작위 순서로 시도
        double totalCost = 0;

        // 각 주문에 대해
        for (Integer orderId : orders) {
            Order order = solution.orderMap().get(orderId);
            InsertionPosition bestPos = null;
            double bestCost = Double.MAX_VALUE;

            // 모든 라이더와 위치 조합 시도
            for (Rider rider : solution.riderMap().values()) {
                for (int shopPos = 0; shopPos <= rider.shopIndexList().size(); shopPos++) {
                    for (int deliveryPos = shopPos;
                        deliveryPos <= rider.deliveryIndexList().size();
                        deliveryPos++) {

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

                        if (result.isFeasible() && result.cost() < bestCost) {
                            bestCost = result.cost();
                            bestPos = new InsertionPosition(rider, shopPos, deliveryPos);
                        }
                    }
                }
            }

            if (bestPos != null) {
                positions.put(orderId, bestPos);
                totalCost += bestCost;
            } else {
                return Double.MAX_VALUE;  // 실행 불가능한 구성
            }
        }

        return totalCost;
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

    /**
     * 주문 삽입 위치 정보를 저장하는 내부 클래스
     */
    private static class InsertionPosition {
        final Rider rider;
        final int shopPos;
        final int deliveryPos;

        InsertionPosition(Rider rider, int shopPos, int deliveryPos) {
            this.rider = rider;
            this.shopPos = shopPos;
            this.deliveryPos = deliveryPos;
        }
    }

    /**
     * 전체 삽입 구성의 비용과 위치 정보를 저장하는 내부 클래스
     */
    private static class Configuration implements Comparable<Configuration> {
        final double totalCost;
        final Map<Integer, InsertionPosition> positions;

        Configuration(double totalCost, Map<Integer, InsertionPosition> positions) {
            this.totalCost = totalCost;
            this.positions = positions;
        }

        @Override
        public int compareTo(Configuration other) {
            return Double.compare(this.totalCost, other.totalCost);
        }
    }
}