package dev.brown.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Solution {


    private static final Logger log = LoggerFactory.getLogger(Solution.class);
    private HashMap<Integer, Order> orderMap = new HashMap<>();
    private HashMap<Integer, Rider> riderMap = new HashMap<>();
    private HashMap<String, Rider> sampleRiderMapByType = new HashMap<>();
    private Set<Integer> removedOrderIds;        // 제거된 주문 ID 집합

    private int notAssignedOrderCount = 0;
    private int totalCost = 0;

    public int notAssignedOrderCount() {
        return notAssignedOrderCount;
    }

    public int totalCost() {
        return totalCost;
    }

    public void calculateScore() {
        for (Order order : orderMap.values()) {
            if (order.rider() == null) {
                notAssignedOrderCount -= 1;
            }
        }

        int totalCost = 0;
        for (Rider rider : riderMap.values()) {
//            log.info(String.valueOf(rider.cost()));
            totalCost += rider.cost() ;

        }
        this.totalCost = totalCost * -1;
    }

    public Solution() {
        this.orderMap = new HashMap<>();
        this.riderMap = new HashMap<>();
        this.removedOrderIds = new LinkedHashSet<>();
    }

    public HashMap<Integer, Order> orderMap() {
        return orderMap;
    }

    public HashMap<Integer, Rider> riderMap() {
        return riderMap;
    }

    public void setOrderMap(HashMap<Integer, Order> orderMap) {
        this.orderMap = orderMap;
    }

    public void setRiderMap(HashMap<Integer, Rider> riderMap) {
        this.riderMap = riderMap;
    }

    public void setSampleRiderMapByType(HashMap<String, Rider> sampleRiderMapByType) {
        this.sampleRiderMapByType = sampleRiderMapByType;
    }

    public HashMap<String, Rider> sampleRiderMapByType() {
        return sampleRiderMapByType;
    }

    /**
     * Solution 객체의 깊은 복사본을 생성
     * @return 새로운 Solution 객체
     */
    public Solution copy() {
        Solution newSolution = new Solution();

        // riderMap 복사
        HashMap<Integer, Rider> newRiderMap = new HashMap<>();
        HashMap<Integer, Order> newOrderMap = new HashMap<>();

        for (var entry : riderMap.entrySet()) {
            Integer riderId = entry.getKey();
            Rider copiedRider = entry.getValue().copy();
            for (Order copiedOrder : copiedRider.orderList()) {
                copiedOrder.setRider(copiedRider);
                newOrderMap.put(copiedOrder.id(), copiedOrder);
            }
            newRiderMap.put(riderId, copiedRider);
        }
        newSolution.setRiderMap(newRiderMap);


        // orderMap 복사
        for (Order order : orderMap.values()) {
            if(!newOrderMap.containsKey(order.id())) {
                newOrderMap.put(order.id(), order.copy());
            }
        }
        newSolution.setOrderMap(newOrderMap);



        // sampleRiderMapByType 복사
        HashMap<String, Rider> newSampleRiderMap = new HashMap<>();
        for (var entry : sampleRiderMapByType.entrySet()) {
            newSampleRiderMap.put(entry.getKey(), entry.getValue().copy());
        }
        newSolution.setSampleRiderMapByType(newSampleRiderMap);

        for (Rider rider : newSolution.riderMap().values()) {
            for (Order order : rider.orderList()) {
                order.setRider(rider);
            }
        }

        // 점수 관련 필드 복사
        newSolution.notAssignedOrderCount = this.notAssignedOrderCount;
        newSolution.totalCost = this.totalCost;

        return newSolution;
    }

    /**
     * 현재 솔루션의 총 주문 수를 반환
     * @return 총 주문 수
     */
    public int getNumOrders() {
        return orderMap.size();
    }

    /**
     * 현재 솔루션의 총 비용을 계산하여 반환
     * @return 총 비용
     */
    public double calculateTotalCost() {
        calculateScore();  // 기존 메서드 활용
        return totalCost;
    }

    /**
     * 활성화된 주문들을 반환
     * @return 제거되지 않은 주문들의 리스트
     */
    public List<Order> getActiveOrders() {
        List<Order> activeOrders = new ArrayList<>();
        for (Order order : orderMap.values()) {
            if (!removedOrderIds.contains(order.id())) {
                activeOrders.add(order);
            }
        }
        return activeOrders;
    }

    /**
     * 활성화된 주문 ID들을 반환
     * @return 제거되지 않은 주문들의 ID 리스트
     */
    public List<Integer> getActiveOrderIds() {
        List<Integer> activeOrderIds = new ArrayList<>();
        for (Integer orderId : orderMap.keySet()) {
            if (!removedOrderIds.contains(orderId)) {
                activeOrderIds.add(orderId);
            }
        }
        return activeOrderIds;
    }

    /**
     * 주문을 제거 상태로 표시
     * @param orderId 제거할 주문 ID
     */
    public void markOrderAsRemoved(int orderId) {
        removedOrderIds.add(orderId);
    }

    /**
     * 주문의 제거 상태를 해제
     * @param orderId 복원할 주문 ID
     */
    public void markOrderAsActive(int orderId) {
        removedOrderIds.remove(orderId);
    }

    /**
     * 주문이 활성 상태인지 확인
     * @param orderId 확인할 주문 ID
     * @return 활성 상태이면 true
     */
    public boolean isOrderActive(int orderId) {
        return !removedOrderIds.contains(orderId);
    }

    /**
     * ID로 주문 객체 조회
     * @param orderId 주문 ID
     * @return 주문 객체
     */
    public Order getOrder(int orderId) {
        return orderMap.get(orderId);
    }
}
