package dev.brown.alns.parameter;
import java.util.Map;

/**
 * ALNS 알고리즘의 하이퍼파라미터 설정을 관리하는 클래스
 */
public class HyperParameter {
    // Time for polish
    private double timeMargin = 0.5;  // need fix when n>300

    // Adaptive Destroyer
    private float rho = 0.5f;              // updating parameter
    private float smoothingRatio = 0.01f;  // smoothing for insertion operators
    private float minProb = 0.05f;         // destroy prob min
    private float maxProb = 0.15f;         // destroy prob max
    private int maxDestroy = 1000;         // just infinite value
    private float routeRemovalRatio = 0.2f; // route removal ratio
    private int updatePeriod = 100;        // period for updating prob

    // Operator scores
    private int score1 = 20;  // operator score when a better overall solution is found
    private int score2 = 10;  // operator score when a better local solution is found
    private int score3 = 2;   // operator score when a worst-cost solution is accepted

    // Shaw removal parameters
    private float shawNoise = 6.0f;
    private float shawD = 9.0f;
    private float shawT = 3.0f;
    private float shawL = 2.0f;

    // Worst removal parameters
    private float worstNoise = 3.0f;

    // Initial Semi-Parallel Solution weights (should add up to 1)
    private float alpha1 = 1.0f;  // cost weight
    private float alpha2 = 0.0f;  // empty space weight

    private int considerSize = 12;

    private float wtWeight = 0.2f;
    private float twWeight = 1.0f;

    private boolean useWorst = true;
    private boolean useOld = false;

    /**
     * 기본 생성자
     */
    public HyperParameter() {}

    /**
     * Map을 이용한 생성자
     * @param hparam 하이퍼파라미터 맵
     */
    public HyperParameter(Map<String, Float> hparam) {
        // Time margin
        timeMargin = hparam.getOrDefault("time_margin", (float)timeMargin).doubleValue();

        // Adaptive Destroyer parameters
        rho = hparam.getOrDefault("rho", rho);
        smoothingRatio = hparam.getOrDefault("smoothing_ratio", smoothingRatio);
        minProb = hparam.getOrDefault("min_prob", minProb);
        maxProb = hparam.getOrDefault("max_prob", maxProb);
        maxDestroy = hparam.getOrDefault("max_destroy", (float)maxDestroy).intValue();
        routeRemovalRatio = hparam.getOrDefault("route_removal_ratio", routeRemovalRatio);
        updatePeriod = hparam.getOrDefault("update_period", (float)updatePeriod).intValue();

        // Scores
        score1 = hparam.getOrDefault("score1", (float)score1).intValue();
        score2 = hparam.getOrDefault("score2", (float)score2).intValue();
        score3 = hparam.getOrDefault("score3", (float)score3).intValue();

        // Shaw removal parameters
        shawNoise = hparam.getOrDefault("shaw_noise", shawNoise);
        shawD = hparam.getOrDefault("shaw_d", shawD);
        shawT = hparam.getOrDefault("shaw_t", shawT);
        shawL = hparam.getOrDefault("shaw_l", shawL);

        // Worst removal parameter
        worstNoise = hparam.getOrDefault("worst_noise", worstNoise);

        // Solution weights
        alpha1 = hparam.getOrDefault("alpha1", alpha1);
        alpha2 = hparam.getOrDefault("alpha2", alpha2);

        // Other parameters
        considerSize = hparam.getOrDefault("consider_size", (float)considerSize).intValue();
        wtWeight = hparam.getOrDefault("wt_weight", wtWeight);
        twWeight = hparam.getOrDefault("tw_weight", twWeight);

        // Boolean parameters
        useWorst = hparam.getOrDefault("use_worst", useWorst ? 1.0f : 0.0f) > 0.5f;
        useOld = hparam.getOrDefault("use_old", useOld ? 1.0f : 0.0f) > 0.5f;
    }

    // Getter methods
    public double getTimeMargin() { return timeMargin; }
    public float getRho() { return rho; }
    public float getSmoothingRatio() { return smoothingRatio; }
    public float getMinProb() { return minProb; }
    public float getMaxProb() { return maxProb; }
    public int getMaxDestroy() { return maxDestroy; }
    public float getRouteRemovalRatio() { return routeRemovalRatio; }
    public int getUpdatePeriod() { return updatePeriod; }

    public int getScore1() { return score1; }
    public int getScore2() { return score2; }
    public int getScore3() { return score3; }

    public float getShawNoise() { return shawNoise; }
    public float getShawD() { return shawD; }
    public float getShawT() { return shawT; }
    public float getShawL() { return shawL; }

    public float getWorstNoise() { return worstNoise; }

    public float getAlpha1() { return alpha1; }
    public float getAlpha2() { return alpha2; }

    public int getConsiderSize() { return considerSize; }

    public float getWtWeight() { return wtWeight; }
    public float getTwWeight() { return twWeight; }

    public boolean isUseWorst() { return useWorst; }
    public boolean isUseOld() { return useOld; }

    /**
     * Builder 클래스
     */
    public static class Builder {
        private final HyperParameter params = new HyperParameter();

        // Time margin
        public Builder timeMargin(double val) {
            params.timeMargin = val;
            return this;
        }

        // Adaptive Destroyer parameters
        public Builder rho(float val) {
            params.rho = val;
            return this;
        }

        public Builder smoothingRatio(float val) {
            params.smoothingRatio = val;
            return this;
        }

        public Builder minProb(float val) {
            params.minProb = val;
            return this;
        }

        public Builder maxProb(float val) {
            params.maxProb = val;
            return this;
        }

        public Builder maxDestroy(int val) {
            params.maxDestroy = val;
            return this;
        }

        public Builder routeRemovalRatio(float val) {
            params.routeRemovalRatio = val;
            return this;
        }

        public Builder updatePeriod(int val) {
            params.updatePeriod = val;
            return this;
        }

        // Operator scores
        public Builder score1(int val) {
            params.score1 = val;
            return this;
        }

        public Builder score2(int val) {
            params.score2 = val;
            return this;
        }

        public Builder score3(int val) {
            params.score3 = val;
            return this;
        }

        // Shaw removal parameters
        public Builder shawNoise(float val) {
            params.shawNoise = val;
            return this;
        }

        public Builder shawD(float val) {
            params.shawD = val;
            return this;
        }

        public Builder shawT(float val) {
            params.shawT = val;
            return this;
        }

        public Builder shawL(float val) {
            params.shawL = val;
            return this;
        }

        // Worst removal parameter
        public Builder worstNoise(float val) {
            params.worstNoise = val;
            return this;
        }

        // Solution weights
        public Builder alpha1(float val) {
            params.alpha1 = val;
            return this;
        }

        public Builder alpha2(float val) {
            params.alpha2 = val;
            return this;
        }

        // Other parameters
        public Builder considerSize(int val) {
            params.considerSize = val;
            return this;
        }

        public Builder wtWeight(float val) {
            params.wtWeight = val;
            return this;
        }

        public Builder twWeight(float val) {
            params.twWeight = val;
            return this;
        }

        // Boolean parameters
        public Builder useWorst(boolean val) {
            params.useWorst = val;
            return this;
        }

        public Builder useOld(boolean val) {
            params.useOld = val;
            return this;
        }

        public HyperParameter build() {
            validateParameters();
            return params;
        }

        private void validateParameters() {
            // alpha1 + alpha2 should add up to 1.0
            if (Math.abs(params.alpha1 + params.alpha2 - 1.0) > 0.001) {
                throw new IllegalStateException("alpha1 + alpha2 must equal 1.0");
            }

            // Probability checks
            if (params.minProb >= params.maxProb) {
                throw new IllegalStateException("minProb must be less than maxProb");
            }

            // Other validation rules
            if (params.timeMargin <= 0) {
                throw new IllegalStateException("timeMargin must be positive");
            }

            if (params.rho < 0 || params.rho > 1) {
                throw new IllegalStateException("rho must be between 0 and 1");
            }

            if (params.maxDestroy <= 0) {
                throw new IllegalStateException("maxDestroy must be positive");
            }

            if (params.updatePeriod <= 0) {
                throw new IllegalStateException("updatePeriod must be positive");
            }
        }
    }
}