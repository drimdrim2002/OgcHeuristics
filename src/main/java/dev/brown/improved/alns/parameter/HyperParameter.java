package dev.brown.improved.alns.parameter;

import java.util.Map;

/**
 * ALNS 알고리즘의 하이퍼파라미터를 관리하는 클래스
 */
public class HyperParameter {
    // Time for polish
    private double timeMargin = 0.5;    // need fix when n>300

    // Adaptive Destroyer
    private double rho = 0.5;           // updating parameter
    private double smoothingRatio = 0.01; // smoothing for insertion operators
    private double minProb = 0.05;      // destroy prob min
    private double maxProb = 0.15;      // destroy prob max
    private int maxDestroy = 1000;      // just infinite value
    private double routeRemovalRatio = 0.2; // route removal ratio
    private int updatePeriod = 100;     // period for updating prob

    // Scoring parameters
    private int score1 = 20;            // operator score when a better overall solution is found
    private int score2 = 10;            // operator score when a better local solution is found
    private int score3 = 2;             // operator score when a worst-cost solution is accepted

    // Shaw removal
    private double shawNoise = 6.0;
    private double shawD = 9.0;
    private double shawT = 3.0;
    private double shawL = 2.0;

    // Worst removal
    private double worstNoise = 3.0;

    // Initial Semi-Parallel Solution
    private double alpha1 = 1.0;        // cost
    private double alpha2 = 0.0;        // empty space

    private int considerSize = 12;

    private double wtWeight = 0.2;
    private double twWeight = 1.0;

    private boolean useWorst = true;
    private boolean useOld = false;

    // 기본 생성자
    public HyperParameter() {}

    /**
     * Map으로부터 HyperParameter 객체를 생성하는 생성자
     */
    public HyperParameter(Map<String, Double> hparam) {
        hparam.computeIfPresent("time_margin", (k, v) -> { timeMargin = v; return v; });
        hparam.computeIfPresent("rho", (k, v) -> { rho = v; return v; });
        hparam.computeIfPresent("smoothing_ratio", (k, v) -> { smoothingRatio = v; return v; });
        hparam.computeIfPresent("min_prob", (k, v) -> { minProb = v; return v; });
        hparam.computeIfPresent("max_prob", (k, v) -> { maxProb = v; return v; });
        hparam.computeIfPresent("max_destroy", (k, v) -> { maxDestroy = v.intValue(); return v; });
        hparam.computeIfPresent("route_removal_ratio", (k, v) -> { routeRemovalRatio = v; return v; });
        hparam.computeIfPresent("update_period", (k, v) -> { updatePeriod = v.intValue(); return v; });

        hparam.computeIfPresent("score1", (k, v) -> { score1 = v.intValue(); return v; });
        hparam.computeIfPresent("score2", (k, v) -> { score2 = v.intValue(); return v; });
        hparam.computeIfPresent("score3", (k, v) -> { score3 = v.intValue(); return v; });

        hparam.computeIfPresent("shaw_noise", (k, v) -> { shawNoise = v; return v; });
        hparam.computeIfPresent("shaw_d", (k, v) -> { shawD = v; return v; });
        hparam.computeIfPresent("shaw_t", (k, v) -> { shawT = v; return v; });
        hparam.computeIfPresent("shaw_l", (k, v) -> { shawL = v; return v; });

        hparam.computeIfPresent("worst_noise", (k, v) -> { worstNoise = v; return v; });

        hparam.computeIfPresent("alpha1", (k, v) -> { alpha1 = v; return v; });
        hparam.computeIfPresent("alpha2", (k, v) -> { alpha2 = v; return v; });

        hparam.computeIfPresent("consider_size", (k, v) -> { considerSize = v.intValue(); return v; });

        hparam.computeIfPresent("wt_weight", (k, v) -> { wtWeight = v; return v; });
        hparam.computeIfPresent("tw_weight", (k, v) -> { twWeight = v; return v; });

        hparam.computeIfPresent("use_worst", (k, v) -> { useWorst = v != 0; return v; });
        hparam.computeIfPresent("use_old", (k, v) -> { useOld = v != 0; return v; });
    }

    // Getter 메서드들
    public double getTimeMargin() { return timeMargin; }
    public double getRho() { return rho; }
    public double getSmoothingRatio() { return smoothingRatio; }
    public double getMinProb() { return minProb; }
    public double getMaxProb() { return maxProb; }
    public int getMaxDestroy() { return maxDestroy; }
    public double getRouteRemovalRatio() { return routeRemovalRatio; }
    public int getUpdatePeriod() { return updatePeriod; }
    public int getScore1() { return score1; }
    public int getScore2() { return score2; }
    public int getScore3() { return score3; }
    public double getShawNoise() { return shawNoise; }
    public double getShawD() { return shawD; }
    public double getShawT() { return shawT; }
    public double getShawL() { return shawL; }
    public double getWorstNoise() { return worstNoise; }
    public double getAlpha1() { return alpha1; }
    public double getAlpha2() { return alpha2; }
    public int getConsiderSize() { return considerSize; }
    public double getWtWeight() { return wtWeight; }
    public double getTwWeight() { return twWeight; }
    public boolean isUseWorst() { return useWorst; }
    public boolean isUseOld() { return useOld; }

    // Builder 패턴 구현
    public static class Builder {
        private final HyperParameter params = new HyperParameter();

        public Builder timeMargin(double value) {
            params.timeMargin = value;
            return this;
        }

        public Builder rho(double value) {
            params.rho = value;
            return this;
        }

        public Builder smoothingRatio(double value) {
            params.smoothingRatio = value;
            return this;
        }

        public Builder minProb(double value) {
            params.minProb = value;
            return this;
        }

        public Builder maxProb(double value) {
            params.maxProb = value;
            return this;
        }

        public Builder maxDestroy(int value) {
            params.maxDestroy = value;
            return this;
        }

        public Builder routeRemovalRatio(double value) {
            params.routeRemovalRatio = value;
            return this;
        }

        public Builder updatePeriod(int value) {
            params.updatePeriod = value;
            return this;
        }

        public Builder score1(int value) {
            params.score1 = value;
            return this;
        }

        public Builder score2(int value) {
            params.score2 = value;
            return this;
        }

        public Builder score3(int value) {
            params.score3 = value;
            return this;
        }

        public Builder shawNoise(double value) {
            params.shawNoise = value;
            return this;
        }

        public Builder shawD(double value) {
            params.shawD = value;
            return this;
        }

        public Builder shawT(double value) {
            params.shawT = value;
            return this;
        }

        public Builder shawL(double value) {
            params.shawL = value;
            return this;
        }

        public Builder worstNoise(double value) {
            params.worstNoise = value;
            return this;
        }

        public Builder alpha1(double value) {
            params.alpha1 = value;
            return this;
        }

        public Builder alpha2(double value) {
            params.alpha2 = value;
            return this;
        }

        public Builder considerSize(int value) {
            params.considerSize = value;
            return this;
        }

        public Builder wtWeight(double value) {
            params.wtWeight = value;
            return this;
        }

        public Builder twWeight(double value) {
            params.twWeight = value;
            return this;
        }

        public Builder useWorst(boolean value) {
            params.useWorst = value;
            return this;
        }

        public Builder useOld(boolean value) {
            params.useOld = value;
            return this;
        }

        public HyperParameter build() {
            return params;
        }
    }
}