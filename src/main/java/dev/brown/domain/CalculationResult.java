package dev.brown.domain;

import java.util.List;

public class CalculationResult {

    private final boolean isFeasible;
    private int cost;

    public CalculationResult(boolean isFeasible) {
        this.isFeasible = isFeasible;
    }

    public int cost() {
        return cost;
    }

    public boolean isFeasible() {
        return isFeasible;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

}
