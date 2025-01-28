package dev.brown.alns.parameter;

/**
 * ALNS 알고리즘의 하이퍼파라미터를 정의하는 레코드
 */
public record HyperParameter(
    // Destroy 관련
    double destroyRatio,           // 파괴할 솔루션의 비율
    int iterationsPerSegment,      // 각 세그먼트당 반복 횟수

    // Simulated Annealing 관련
    double initialTemperature,     // 초기 온도
    double coolingRate,            // 냉각 속도
    double minTemperature,         // 최소 온도

    // Adaptive Weight 관련
    double reactionFactor,         // 적응형 가중치 업데이트 계수

    // Score 관련
    double newBestScore,          // 새로운 최적해 발견 시 점수
    double betterScore,           // 이전보다 개선된 해 발견 시 점수
    double acceptedScore          // 수용된 해에 대한 점수
) {
    /**
     * 기본 하이퍼파라미터 값을 반환하는 팩토리 메서드
     */
    public static HyperParameter getDefault() {
        return new HyperParameter(
            0.4,    // destroyRatio
            100,    // iterationsPerSegment
            100.0,  // initialTemperature
            0.99,   // coolingRate
            0.01,   // minTemperature
            0.1,    // reactionFactor
            33.0,   // newBestScore
            20.0,   // betterScore
            10.0    // acceptedScore
        );
    }

    /**
     * 하이퍼파라미터 유효성 검증을 위한 빌더
     */
    public static class Builder {
        private double destroyRatio = 0.4;
        private int iterationsPerSegment = 100;
        private double initialTemperature = 100.0;
        private double coolingRate = 0.99;
        private double minTemperature = 0.01;
        private double reactionFactor = 0.1;
        private double newBestScore = 33.0;
        private double betterScore = 20.0;
        private double acceptedScore = 10.0;

        public Builder destroyRatio(double value) {
            if (value <= 0 || value >= 1) {
                throw new IllegalArgumentException("Destroy ratio must be between 0 and 1");
            }
            this.destroyRatio = value;
            return this;
        }

        public Builder iterationsPerSegment(int value) {
            if (value <= 0) {
                throw new IllegalArgumentException("Iterations must be positive");
            }
            this.iterationsPerSegment = value;
            return this;
        }

        public Builder initialTemperature(double value) {
            if (value <= 0) {
                throw new IllegalArgumentException("Initial temperature must be positive");
            }
            this.initialTemperature = value;
            return this;
        }

        public Builder coolingRate(double value) {
            if (value <= 0 || value >= 1) {
                throw new IllegalArgumentException("Cooling rate must be between 0 and 1");
            }
            this.coolingRate = value;
            return this;
        }

        public Builder minTemperature(double value) {
            if (value < 0) {
                throw new IllegalArgumentException("Minimum temperature must be non-negative");
            }
            this.minTemperature = value;
            return this;
        }

        public Builder reactionFactor(double value) {
            if (value <= 0 || value >= 1) {
                throw new IllegalArgumentException("Reaction factor must be between 0 and 1");
            }
            this.reactionFactor = value;
            return this;
        }

        public Builder newBestScore(double value) {
            if (value <= 0) {
                throw new IllegalArgumentException("Score must be positive");
            }
            this.newBestScore = value;
            return this;
        }

        public Builder betterScore(double value) {
            if (value <= 0) {
                throw new IllegalArgumentException("Score must be positive");
            }
            this.betterScore = value;
            return this;
        }

        public Builder acceptedScore(double value) {
            if (value <= 0) {
                throw new IllegalArgumentException("Score must be positive");
            }
            this.acceptedScore = value;
            return this;
        }

        public HyperParameter build() {
            // 추가적인 유효성 검사
            if (minTemperature >= initialTemperature) {
                throw new IllegalStateException("Minimum temperature must be less than initial temperature");
            }
            if (betterScore > newBestScore) {
                throw new IllegalStateException("Better score should not exceed new best score");
            }
            if (acceptedScore > betterScore) {
                throw new IllegalStateException("Accepted score should not exceed better score");
            }

            return new HyperParameter(
                destroyRatio,
                iterationsPerSegment,
                initialTemperature,
                coolingRate,
                minTemperature,
                reactionFactor,
                newBestScore,
                betterScore,
                acceptedScore
            );
        }
    }
}