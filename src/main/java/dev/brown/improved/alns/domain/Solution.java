package dev.brown.improved.alns.domain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Solution {

    private double cost;
    public List<Bundle> bundles;

    public Solution() {
        this.cost = 0.0;
        this.bundles = new ArrayList<>();
    }

    public Solution(List<Bundle> bundles, double cost) {
        this.bundles = bundles;
        this.cost = cost;
    }

    public String getRiderType(int i) {
        return bundles.get(i).riderType();
    }

    public double getCost(int i) {
        return bundles.get(i).cost();
    }

    public List<Integer> getSource(int i) {
        return bundles.get(i).source();
    }

    public List<Integer> getDest(int i) {
        return bundles.get(i).dest();
    }

    /**
     * 주문 ID에 해당하는 번들 ID 찾기
     *
     * @return 번들 ID, 없으면 -1
     */
    public int getBundleId(int orderId) {
        for (int i = 0; i < bundles.size(); i++) {
            if (bundles.get(i).source().contains(orderId)) {
                return i;
            }
        }
        return -1;
    }

    public void updateCost() {
        cost = bundles.stream()
            .mapToDouble(Bundle::cost)
            .sum();
    }

    /**
     * 지정된 번들 ID들 제거
     */
    public void remove(List<Integer> bundleIds) {
        Set<Integer> idsToRemove = new HashSet<>(bundleIds);
        List<Bundle> newSolutions = new ArrayList<>();

        for (int i = 0; i < bundles.size(); i++) {
            if (!idsToRemove.contains(i)) {
                newSolutions.add(bundles.get(i));
            }
        }

        bundles.clear();
        bundles.addAll(newSolutions);
        updateCost();
    }

    public void remove(int i) {
        cost -= bundles.get(i).cost();
        bundles.remove(i);
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
        bundles.set(bundleId, new Bundle(riderType, newCost, source, dest));

        return Map.entry(false, riderType);
    }

    /**
     * 번들 추가
     */
    public void append(Bundle bundle) {
        bundles.add(bundle);
        cost += bundle.cost();
    }

    /**
     * 솔루션 추출
     */
    public Map.Entry<List<Bundle>, Double> getSolution() {
        return Map.entry(new ArrayList<>(bundles), cost);
    }

    /**
     * SolutionFormat 형식으로 솔루션 추출
     * @return SolutionFormat 객체
     */
    public SolutionFormat extract() {
        List<Bundle> bundleInfos = bundles.stream()
            .map(bundle -> new Bundle(
                bundle.riderType(),      // riderType이 첫 번째 매개변수
                bundle.cost(),           // cost가 두 번째 매개변수
                bundle.source(),         // source가 세 번째 매개변수
                bundle.dest()            // dest가 네 번째 매개변수
            ))
            .toList();

        return new SolutionFormat(cost, bundleInfos);
    }

    /**
     * 기존의 extract 메서드는 extractLegacy로 이름 변경
     */
//    public Map.Entry<Double, List<BundleInfo>> extractLegacy() {
//        List<BundleInfo> result = bundles.stream()
//            .map(bundle -> new BundleInfo(
//                bundle.riderType(),
//                bundle.source(),
//                bundle.dest()
//            ))
//            .collect(Collectors.toList());
//
//        return Map.entry(cost, result);
//    }
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
        return bundles.size();
    }

    /**
     * 모든 번들 반환
     */
    public List<Bundle> getBundles() {
        return new ArrayList<>(bundles);
    }

    /**
     * 복사 생성자
     * @param other 복사할 Solution 객체
     */
    public Solution(Solution other) {
        this.cost = other.cost;
        this.bundles = other.bundles.stream()
            .map(bundle -> new Bundle(
                bundle.riderType(),
                bundle.cost(),
                new ArrayList<>(bundle.source()),
                new ArrayList<>(bundle.dest())
            ))
            .collect(Collectors.toList());
    }

    /**
     * 다른 Solution의 내용을 현재 객체로 복사
     * @param other 복사할 Solution 객체
     */
    public void copyFrom(Solution other) {
        this.bundles.clear();
        this.bundles.addAll(other.getBundles());
        this.updateCost();
    }

    /**
     * 특정 인덱스의 번들 업데이트
     * @param index 업데이트할 번들 인덱스
     * @param newBundle 새 번들
     */
    public void updateBundle(int index, Bundle newBundle) {
        // 이전 비용 제거
        cost -= bundles.get(index).cost();
        // 새 번들로 교체
        bundles.set(index, newBundle);
        // 새 비용 추가
        cost += newBundle.cost();
    }
}
