package dev.brown.improved.alns.destroy;

/**
 * 인덱스가 있는 값을 저장하는 레코드
 */
record IndexedValue(double value, int index) implements Comparable<IndexedValue> {

    @Override
    public int compareTo(IndexedValue other) {
        return Double.compare(this.value, other.value);
    }
}