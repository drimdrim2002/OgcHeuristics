package dev.brown.improved.alns.repair;

import java.util.*;

/**
 * 주문 조사 결과를 나타내는 클래스
 */
public class InvestigationResult {
    private final Map<String, Boolean> feasibility;
    private final Map<String, Double> costs;
    private final Map<String, List<Integer>> sources;
    private final Map<String, List<Integer>> destinations;
    private final List<String> optimalOrder;

    public InvestigationResult() {
        this.feasibility = new HashMap<>();
        this.costs = new HashMap<>();
        this.sources = new HashMap<>();
        this.destinations = new HashMap<>();
        this.optimalOrder = new ArrayList<>();
    }

    public InvestigationResult(
        Map<String, Boolean> feasibility,
        Map<String, Double> costs,
        Map<String, List<Integer>> sources,
        Map<String, List<Integer>> destinations,
        List<String> optimalOrder
    ) {
        this.feasibility = feasibility;
        this.costs = costs;
        this.sources = sources;
        this.destinations = destinations;
        this.optimalOrder = optimalOrder;
    }

    /**
     * 특정 라이더 타입에 대한 실현 가능성 설정
     */
    public void setFeasibility(String riderType, boolean isFeasible) {
        feasibility.put(riderType, isFeasible);
    }

    /**
     * 특정 라이더 타입에 대한 비용 설정
     */
    public void setCost(String riderType, double cost) {
        costs.put(riderType, cost);
    }

    /**
     * 특정 라이더 타입에 대한 출발지 목록 설정
     */
    public void setSource(String riderType, List<Integer> source) {
        sources.put(riderType, new ArrayList<>(source));
    }

    /**
     * 특정 라이더 타입에 대한 도착지 목록 설정
     */
    public void setDest(String riderType, List<Integer> dest) {
        destinations.put(riderType, new ArrayList<>(dest));
    }

    /**
     * 최적 라이더 순서 설정
     */
    public void setOptimalOrder(List<String> order) {
        optimalOrder.clear();
        optimalOrder.addAll(order);
    }

    /**
     * 특정 라이더 타입의 실현 가능성 반환
     */
    public boolean getFeasibility(String riderType) {
        return feasibility.getOrDefault(riderType, false);
    }

    /**
     * 특정 라이더 타입의 비용 반환
     */
    public double getCost(String riderType) {
        return costs.getOrDefault(riderType, Double.MAX_VALUE);
    }

    /**
     * 특정 라이더 타입의 출발지 목록 반환
     */
    public List<Integer> getSource(String riderType) {
        return sources.getOrDefault(riderType, new ArrayList<>());
    }

    /**
     * 특정 라이더 타입의 도착지 목록 반환
     */
    public List<Integer> getDest(String riderType) {
        return destinations.getOrDefault(riderType, new ArrayList<>());
    }

    /**
     * 최적 라이더 순서 반환
     */
    public List<String> getOptimalOrder() {
        return new ArrayList<>(optimalOrder);
    }

    /**
     * 결과가 비어있는지 확인
     */
    public boolean isEmpty() {
        return feasibility.isEmpty() && costs.isEmpty() &&
            sources.isEmpty() && destinations.isEmpty() &&
            optimalOrder.isEmpty();
    }
}