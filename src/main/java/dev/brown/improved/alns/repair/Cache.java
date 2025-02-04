package dev.brown.improved.alns.repair;

import java.util.*;

/**
 * 조사 결과를 캐싱하는 클래스
 */
public class Cache {
    private final Map<String, InvestigationResult> cache;

    public Cache() {
        this.cache = new HashMap<>();
    }

    /**
     * 주문 ID 목록에 대한 캐시 키 생성
     */
    private String createKey(List<Integer> idList) {
        List<Integer> sortedList = new ArrayList<>(idList);
        Collections.sort(sortedList);
        return sortedList.toString();
    }

    /**
     * 주어진 ID 목록에 대한 결과가 캐시에 있는지 확인
     */
    public boolean check(List<Integer> idList) {
        return cache.containsKey(createKey(idList));
    }

    /**
     * 주어진 ID 목록에 대한 결과를 캐시에서 검색
     */
    public InvestigationResult retrieve(List<Integer> idList) {
        return cache.get(createKey(idList));
    }

    /**
     * 새로운 결과를 캐시에 추가
     */
    public void append(List<Integer> idList, InvestigationResult result) {
        if (result != null && !result.isEmpty()) {
            cache.put(createKey(idList), result);
        }
    }

    /**
     * 캐시 크기 반환
     */
    public int size() {
        return cache.size();
    }

    /**
     * 캐시 초기화
     */
    public void clear() {
        cache.clear();
    }

    /**
     * 특정 ID 목록에 대한 캐시 항목 제거
     */
    public void remove(List<Integer> idList) {
        cache.remove(createKey(idList));
    }

    /**
     * 캐시된 모든 키 반환
     */
    public Set<String> getCachedKeys() {
        return new HashSet<>(cache.keySet());
    }

    /**
     * 캐시가 비어있는지 확인
     */
    public boolean isEmpty() {
        return cache.isEmpty();
    }

    /**
     * 캐시 내용을 문자열로 반환
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Cache contents:\n");
        for (Map.Entry<String, InvestigationResult> entry : cache.entrySet()) {
            sb.append(entry.getKey()).append(" -> ")
                .append(entry.getValue()).append("\n");
        }
        return sb.toString();
    }
}