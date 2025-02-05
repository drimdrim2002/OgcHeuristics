package dev.brown.improved.alns.storage;

import dev.brown.improved.alns.domain.Bundle;
import dev.brown.improved.alns.domain.BundleInfo;
import dev.brown.improved.alns.domain.GurobiInput;
import dev.brown.improved.alns.domain.RiderInfo;
import dev.brown.improved.alns.domain.Solution;
import java.util.*;

/**
 * 번들 저장소 클래스
 * 솔루션의 번들들을 저장하고 관리
 */
public class BundleStorage {
    private final Set<Bundle> bundles;
    private final int capacity;

    /**
     * 생성자
     * @param capacity 저장소 최대 용량
     */
    public BundleStorage(int capacity) {
        this.capacity = capacity;
        this.bundles = new HashSet<>();
    }

    /**
     * 솔루션의 모든 번들을 저장소에 추가
     * @param solution 번들을 추가할 솔루션
     */
    public void append(Solution solution) {
        for (Bundle bundle : solution.getBundles()) {
            if (bundles.size() >= capacity) {
                // 용량 초과 시 가장 오래된 번들 제거
                bundles.iterator().next();
            }
            // Bundle record의 새 인스턴스 생성
            bundles.add(new Bundle(
                bundle.riderType(),
                bundle.cost(),
                bundle.source(),
                bundle.dest()
            ));
        }
    }

    /**
     * 저장된 모든 번들 반환
     * @return 저장된 번들들의 불변 집합
     */
    public Set<Bundle> getBundles() {
        return Collections.unmodifiableSet(bundles);
    }

    /**
     * 저장소 초기화
     */
    public void clear() {
        bundles.clear();
    }

    /**
     * 현재 저장된 번들 수 반환
     * @return 저장된 번들 수
     */
    public int size() {
        return bundles.size();
    }

    /**
     * 저장소가 비어있는지 확인
     * @return 저장소 비어있음 여부
     */
    public boolean isEmpty() {
        return bundles.isEmpty();
    }

    /**
     * 특정 번들이 저장소에 있는지 확인
     * @param bundle 확인할 번들
     * @return 번들 존재 여부
     */
    public boolean contains(Bundle bundle) {
        return bundles.contains(bundle);
    }

    /**
     * Gurobi 입력 형식으로 데이터 추출
     * @param solution 현재 솔루션
     * @param K 주문 수의 절반
     * @param riderInfo 라이더 정보
     * @param distMat 거리 행렬
     * @return Gurobi 입력 데이터
     */
    public GurobiInput extract(
        Solution solution,
        int K,
        RiderInfo riderInfo,
        int[][] distMat) {

        int matrixLength = 2 * K;

        // 엣지 행렬 초기화
        int[][] edgeMatrix = new int[matrixLength][matrixLength];

        // 라이더 타입 배열 초기화
        int[] riderTypes = new int[K];
        Arrays.fill(riderTypes, -1);

        // 각 번들에 대해 처리
        for (Bundle bundle : solution.getBundles()) {
            List<Integer> source = bundle.source();
            List<Integer> dest = bundle.dest();
            String riderType = bundle.riderType();

            // 라이더 타입 설정
            for (int orderId : source) {
                riderTypes[orderId] = getRiderTypeId(riderType);
            }

            // source to source 엣지
            for (int i = 0; i < source.size() - 1; i++) {
                edgeMatrix[source.get(i)][source.get(i + 1)] = 1;
            }

            // source to destination 엣지
            edgeMatrix[source.get(source.size() - 1)][dest.get(0) + K] = 1;

            // destination to destination 엣지
            for (int i = 0; i < dest.size() - 1; i++) {
                edgeMatrix[dest.get(i) + K][dest.get(i + 1) + K] = 1;
            }
        }

        // 저장된 모든 번들의 정보 수집
        List<BundleInfo> storedBundles = new ArrayList<>();
        for (Bundle bundle : bundles) {
            storedBundles.add(new BundleInfo(
                bundle.riderType(),
                bundle.source(),
                bundle.dest()
            ));
        }

        return new GurobiInput(
            edgeMatrix,
            riderTypes,
            storedBundles,
            calculateCosts(storedBundles, riderInfo, distMat, K)
        );
    }

    /**
     * 라이더 타입 문자열을 ID로 변환
     */
    private int getRiderTypeId(String riderType) {
        return switch (riderType) {
            case "WALK" -> 0;
            case "BIKE" -> 1;
            case "CAR" -> 2;
            default -> throw new IllegalArgumentException("Unknown rider type: " + riderType);
        };
    }

    /**
     * 각 번들의 비용 계산
     */
    private List<Double> calculateCosts(
        List<BundleInfo> bundles,
        RiderInfo riderInfo,
        int[][] distMat,
        int K) {

        List<Double> costs = new ArrayList<>();
        int matrixLength = 2 * K;

        for (BundleInfo bundle : bundles) {
            List<Integer> source = bundle.source();
            List<Integer> dest = bundle.dest();
            String riderType = bundle.riderType();

            Map.Entry<int[], List<Integer>> riderData = riderInfo.prepare(riderType);
            List<Integer> riderParams = riderData.getValue();

            double cost = riderParams.get(1); // 기본 비용
            int m = source.size();

            // 출발지 간 이동 비용
            for (int i = 1; i < m; i++) {
                cost += riderParams.get(2) *
                    distMat[source.get(i-1)][source.get(i)] / 100.0;
            }

            // 마지막 출발지에서 첫 도착지까지의 비용
            cost += riderParams.get(2) *
                distMat[source.get(m-1)][dest.get(0) + K] / 100.0;

            // 도착지 간 이동 비용
            for (int i = 1; i < m; i++) {
                cost += riderParams.get(2) *
                    distMat[dest.get(i-1) + K][dest.get(i) + K] / 100.0;
            }

            costs.add(cost);
        }

        return costs;
    }
}