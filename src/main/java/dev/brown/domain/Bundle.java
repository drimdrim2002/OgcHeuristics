package dev.brown.domain;


import java.util.ArrayList;
import java.util.List;

public class Bundle {
    private List<Integer> source;  // 픽업 순서
    private List<Integer> dest;    // 배달 순서
    private String riderType;      // 할당된 라이더 타입
    private double cost;          // 번들 비용
    private boolean isFeasible;   // 실행 가능성

    public Bundle(List<Integer> source, List<Integer> dest,
        String riderType, double cost, boolean isFeasible) {
        this.source = new ArrayList<>(source);
        this.dest = new ArrayList<>(dest);
        this.riderType = riderType;
        this.cost = cost;
        this.isFeasible = isFeasible;
    }

    // Getters and setters
    public List<Integer> getSource() { return source; }
    public List<Integer> getDest() { return dest; }
    public String getRiderType() { return riderType; }
    public double getCost() { return cost; }
    public boolean isFeasible() { return isFeasible; }
}