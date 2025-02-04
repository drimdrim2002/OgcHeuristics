package dev.brown.improved.alns.domain;

/**
 * 인덱스가 있는 값을 저장하는 레코드
 */
public record IndexedValue(double value, int index) implements Comparable<IndexedValue> {

    @Override
    public int compareTo(IndexedValue other) {
        return Double.compare(this.value, other.value);
    }

    /**
     * 값이 특정 범위 내에 있는지 확인
     */
    public boolean isInRange(double min, double max) {
        return value >= min && value <= max;
    }

    /**
     * 인덱스가 유효한지 확인
     */
    public boolean isValidIndex(int maxIndex) {
        return index >= 0 && index < maxIndex;
    }
}