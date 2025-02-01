package dev.brown.improved.alns.domain;

import java.util.List;
import java.util.Map;

public class RiderInfo {
    private final int[] walkTimeMatrix;
    private final List<Integer> walkInfo;
    private final int[] bikeTimeMatrix;
    private final List<Integer> bikeInfo;
    private final int[] carTimeMatrix;
    private final List<Integer> carInfo;

    public RiderInfo(int[] walkTimeMatrix, List<Integer> walkInfo,
        int[] bikeTimeMatrix, List<Integer> bikeInfo,
        int[] carTimeMatrix, List<Integer> carInfo) {
        this.walkTimeMatrix = walkTimeMatrix;
        this.walkInfo = walkInfo;
        this.bikeTimeMatrix = bikeTimeMatrix;
        this.bikeInfo = bikeInfo;
        this.carTimeMatrix = carTimeMatrix;
        this.carInfo = carInfo;
    }

    public Map.Entry<int[], List<Integer>> prepare(String type) {
        return switch (type) {
            case "WALK" -> Map.entry(walkTimeMatrix, walkInfo);
            case "BIKE" -> Map.entry(bikeTimeMatrix, bikeInfo);
            case "CAR" -> Map.entry(carTimeMatrix, carInfo);
            default -> throw new IllegalArgumentException("Unknown rider type: " + type);
        };
    }

    public int[] getTimeMatrix(String type) {
        return switch (type) {
            case "WALK" -> walkTimeMatrix;
            case "BIKE" -> bikeTimeMatrix;
            case "CAR" -> carTimeMatrix;
            default -> throw new IllegalArgumentException("Unknown rider type: " + type);
        };
    }

    public List<Integer> getInfo(String type) {
        return switch (type) {
            case "WALK" -> walkInfo;
            case "BIKE" -> bikeInfo;
            case "CAR" -> carInfo;
            default -> throw new IllegalArgumentException("Unknown rider type: " + type);
        };
    }
}