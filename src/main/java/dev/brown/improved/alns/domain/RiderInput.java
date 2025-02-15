package dev.brown.improved.alns.domain;

public class RiderInput {

    private final String type;
    private final double speed;
    private final int capa;
    private final int varCost;
    private final int fixedCost;
    private final int serviceTime;
    private final int availableNumber;
    private final int[][] timeMatrix;

    public RiderInput(String type, double speed, int capa, int varCost, int fixedCost, int serviceTime,
        int availableNumber, int[][] distMatrix) {
        this.type = type;
        this.speed = speed;
        this.capa = capa;
        this.varCost = varCost;
        this.fixedCost = fixedCost;
        this.serviceTime = serviceTime;
        this.availableNumber = availableNumber;
        this.timeMatrix = new int[distMatrix.length][distMatrix.length];
        for (int fromIndex = 0; fromIndex < distMatrix.length; fromIndex++) {
            for (int toIndex = 0; toIndex < distMatrix[fromIndex].length; toIndex++) {
                timeMatrix[fromIndex][toIndex]
                    =  (int) Math.round(distMatrix[fromIndex][toIndex] / speed  + serviceTime);
            }
        }
    }

    public int getAvailableNumber() {
        return availableNumber;
    }

    public int getCapa() {
        return capa;
    }

    public int getFixedCost() {
        return fixedCost;
    }

    public int getServiceTime() {
        return serviceTime;
    }

    public double getSpeed() {
        return speed;
    }

    public String getType() {
        return type;
    }

    public int getVarCost() {
        return varCost;
    }


}
