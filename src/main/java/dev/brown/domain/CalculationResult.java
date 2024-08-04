package dev.brown.domain;

public class CalculationResult {

    private final boolean isFeasible;
    private  int cost;
    private  int extraTime;

    public CalculationResult(boolean isFeasible) {
        this.isFeasible = isFeasible;
    }

    public int cost() {
        return cost;
    }

    public int extraTime() {
        return extraTime;
    }

    public boolean isFeasible() {
        return isFeasible;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public void setExtraTime(int extraTime) {
        this.extraTime = extraTime;
    }
}
