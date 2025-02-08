package dev.brown.improved.alns.domain;

public class ExtractResult {
    private final int[][] timeMatrix;
    private final int[] info;

    public ExtractResult(int[][] timeMatrix, int[] info) {
        this.timeMatrix = timeMatrix;
        this.info = info;
    }

    public int[][] getTimeMatrix() {
        return timeMatrix;
    }

    public int[] getInfo() {
        return info;
    }
}
