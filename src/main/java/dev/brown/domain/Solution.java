package dev.brown.domain;

import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Solution {


    private static final Logger log = LoggerFactory.getLogger(Solution.class);
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


    private HashMap<Integer, Order> orderMap = new HashMap<>();
    private HashMap<Integer, Rider> riderMap = new HashMap<>();
    private HashMap<String, Rider> sampleRiderMapByType = new HashMap<>();

    public Solution() {
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

        // orderMap 복사
        HashMap<Integer, Order> newOrderMap = new HashMap<>();
        for (var entry : orderMap.entrySet()) {
            newOrderMap.put(entry.getKey(), entry.getValue().copy());
        }
        newSolution.setOrderMap(newOrderMap);

        // riderMap 복사
        HashMap<Integer, Rider> newRiderMap = new HashMap<>();
        for (var entry : riderMap.entrySet()) {
            newRiderMap.put(entry.getKey(), entry.getValue().copy());
        }
        newSolution.setRiderMap(newRiderMap);

        // sampleRiderMapByType 복사
        HashMap<String, Rider> newSampleRiderMap = new HashMap<>();
        for (var entry : sampleRiderMapByType.entrySet()) {
            newSampleRiderMap.put(entry.getKey(), entry.getValue().copy());
        }
        newSolution.setSampleRiderMapByType(newSampleRiderMap);

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

}
