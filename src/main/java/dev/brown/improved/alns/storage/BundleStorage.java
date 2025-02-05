package dev.brown.improved.alns.storage;

import dev.brown.improved.alns.domain.Bundle;
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
}