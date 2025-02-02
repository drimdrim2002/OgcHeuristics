package dev.brown.improved.alns.domain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Solution {

    private double cost;
    private List<Bundle> solutions;

    public Solution() {
        this.cost = 0.0;
        this.solutions = new ArrayList<>();
    }

    public Solution(List<Bundle> solutions, double cost) {
        this.solutions = solutions;
        this.cost = cost;
    }

    public String getRiderType(int i) {
        return solutions.get(i).riderType();
    }

    public double getCost(int i) {
        return solutions.get(i).cost();
    }

    public List<Integer> getSource(int i) {
        return solutions.get(i).source();
    }

    public List<Integer> getDest(int i) {
        return solutions.get(i).dest();
    }

    /**
     * 주문 ID에 해당하는 번들 ID 찾기
     *
     * @return 번들 ID, 없으면 -1
     */
    public int getBundleId(int orderId) {
        for (int i = 0; i < solutions.size(); i++) {
            if (solutions.get(i).source().contains(orderId)) {
                return i;
            }
        }
        return -1;
    }

    public void updateCost() {
        cost = solutions.stream()
            .mapToDouble(Bundle::cost)
            .sum();
    }

    /**
     * 지정된 번들 ID들 제거
     */
    public void remove(List<Integer> bundleIds) {
        Set<Integer> idsToRemove = new HashSet<>(bundleIds);
        List<Bundle> newSolutions = new ArrayList<>();

        for (int i = 0; i < solutions.size(); i++) {
            if (!idsToRemove.contains(i)) {
                newSolutions.add(solutions.get(i));
            }
        }

        solutions.clear();
        solutions.addAll(newSolutions);
        updateCost();
    }

    public void remove(int i) {
        cost -= solutions.get(i).cost();
        solutions.remove(i);
    }


    /**
     * 주문 제거
     *
     * @return Pair<Boolean, String> - (번들이 완전히 제거되었는지 여부, 라이더 타입)
     */
    public Map.Entry<Boolean, String> removeOrder(int orderId, int bundleId, double newCost) {
        String riderType = getRiderType(bundleId);
        List<Integer> source = new ArrayList<>(getSource(bundleId));

        // 번들에 주문이 하나만 있는 경우
        if (source.size() == 1) {
            remove(bundleId);
            return Map.entry(true, riderType);
        }

        // 주문 제거 및 번들 업데이트
        List<Integer> dest = new ArrayList<>(getDest(bundleId));
        source.remove(Integer.valueOf(orderId));
        dest.remove(Integer.valueOf(orderId));

        cost += (newCost - getCost(bundleId));
        solutions.set(bundleId, new Bundle(riderType, newCost, source, dest));

        return Map.entry(false, riderType);
    }

    /**
     * 번들 추가
     */
    public void append(Bundle bundle) {
        solutions.add(bundle);
        cost += bundle.cost();
    }

    /**
     * 솔루션 추출
     */
    public Map.Entry<List<Bundle>, Double> getSolution() {
        return Map.entry(new ArrayList<>(solutions), cost);
    }

    /**
     * 간소화된 형태로 솔루션 추출
     */
    public Map.Entry<Double, List<BundleInfo>> extract() {
        List<BundleInfo> result = solutions.stream()
            .map(bundle -> new BundleInfo(
                bundle.riderType(),
                bundle.source(),
                bundle.dest()
            ))
            .collect(Collectors.toList());

        return Map.entry(cost, result);
    }

    /**
     * 현재 비용 반환
     */
    public double getCost() {
        return cost;
    }

    /**
     * 솔루션 크기 반환
     */
    public int size() {
        return solutions.size();
    }

    /**
     * 모든 번들 반환
     */
    public List<Bundle> getSolutions() {
        return new ArrayList<>(solutions);
    }

    /**
     * 복사 생성자
     * @param other 복사할 Solution 객체
     */
    public Solution(Solution other) {
        this.cost = other.cost;
        this.solutions = other.solutions.stream()
            .map(bundle -> new Bundle(
                bundle.riderType(),
                bundle.cost(),
                new ArrayList<>(bundle.source()),
                new ArrayList<>(bundle.dest())
            ))
            .collect(Collectors.toList());
    }
}

/**
 * 번들 정보를 저장하는 보조 레코드
 */
record BundleInfo(
    String riderType,
    List<Integer> source,
    List<Integer> dest
) {

    public BundleInfo {
        source = List.copyOf(source);
        dest = List.copyOf(dest);
    }
}