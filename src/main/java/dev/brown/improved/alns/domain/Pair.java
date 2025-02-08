package dev.brown.improved.alns.domain;
/**
 * 두 개의 값을 담는 불변(immutable) 제네릭 클래스
 *
 * @param <T> 첫 번째 값의 타입
 * @param <U> 두 번째 값의 타입
 */
public class Pair<T, U> {
    private final T first;
    private final U second;

    /**
     * Pair 객체를 생성합니다.
     *
     * @param first 첫 번째 값
     * @param second 두 번째 값
     */
    public Pair(T first, U second) {
        this.first = first;
        this.second = second;
    }

    /**
     * 첫 번째 값을 반환합니다.
     *
     * @return 첫 번째 값
     */
    public T getFirst() {
        return first;
    }

    /**
     * 두 번째 값을 반환합니다.
     *
     * @return 두 번째 값
     */
    public U getSecond() {
        return second;
    }

    /**
     * 두 Pair 객체가 동일한지 비교합니다.
     *
     * @param obj 비교할 객체
     * @return 두 객체가 동일하면 true, 그렇지 않으면 false
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Pair<?, ?> pair = (Pair<?, ?>) obj;

        if (first != null ? !first.equals(pair.first) : pair.first != null) return false;
        return second != null ? second.equals(pair.second) : pair.second == null;
    }

    /**
     * 객체의 해시코드를 반환합니다.
     *
     * @return 해시코드
     */
    @Override
    public int hashCode() {
        int result = first != null ? first.hashCode() : 0;
        result = 31 * result + (second != null ? second.hashCode() : 0);
        return result;
    }

    /**
     * 객체를 문자열로 표현합니다.
     *
     * @return 객체의 문자열 표현
     */
    @Override
    public String toString() {
        return "Pair{" +
            "first=" + first +
            ", second=" + second +
            '}';
    }

    /**
     * 새로운 Pair 객체를 생성하는 편의 메서드
     *
     * @param a 첫 번째 값
     * @param b 두 번째 값
     * @return 새로운 Pair 객체
     * @param <T> 첫 번째 값의 타입
     * @param <U> 두 번째 값의 타입
     */
    public static <T, U> Pair<T, U> of(T a, U b) {
        return new Pair<>(a, b);
    }
}