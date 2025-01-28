package dev.brown.alns;

import dev.brown.alns.parameter.HyperParameter;
import java.util.Random;

/**
 * Simulated Annealing을 구현한 클래스
 * ALNS 알고리즘에서 해 수용 여부를 결정하는데 사용
 */
public class SimulatedAnnealing {
    private double temperature;
    private final Random random;
    private final HyperParameter params;
    private int iterationsWithoutImprovement;
    private final double initialTemperature;

    // SA 관련 상수들
    private static final double COOLING_RATE = 0.99975;
    private static final double MIN_TEMPERATURE = 0.01;
    private static final int REHEAT_INTERVAL = 1000;
    private static final double REHEAT_FACTOR = 0.5;

    /**
     * SimulatedAnnealing 생성자
     * @param params 하이퍼파라미터
     * @param seed 랜덤 시드
     * @param initialCost 초기 해의 비용
     */
    public SimulatedAnnealing(HyperParameter params, long seed, double initialCost) {
        this.params = params;
        this.random = new Random(seed);
        this.iterationsWithoutImprovement = 0;

        // 초기 온도 설정 (초기 해의 비용을 기반으로)
        this.initialTemperature = calculateInitialTemperature(initialCost);
        this.temperature = this.initialTemperature;
    }

    /**
     * 초기 온도 계산
     * @param initialCost 초기 해의 비용
     * @return 초기 온도
     */
    private double calculateInitialTemperature(double initialCost) {
        // 초기에 약 50%의 확률로 나쁜 해를 수용하도록 설정
        return -initialCost * params.getTimeMargin() / Math.log(0.5);
    }

    /**
     * 새로운 해의 수용 여부 결정
     * @param currentCost 현재 해의 비용
     * @param newCost 새로운 해의 비용
     * @param isNewBest 새로운 전역 최적해 여부
     * @return 수용 여부
     */
    public boolean accept(double currentCost, double newCost, boolean isNewBest) {
        // 더 좋은 해는 항상 수용
        if (newCost <= currentCost) {
            if (isNewBest) {
                iterationsWithoutImprovement = 0;
            }
            return true;
        }

        // 더 나쁜 해는 확률적으로 수용
        double costDifference = newCost - currentCost;
        double acceptanceProbability = Math.exp(-costDifference / temperature);
        boolean accepted = random.nextDouble() < acceptanceProbability;

        if (!accepted) {
            iterationsWithoutImprovement++;
        }

        return accepted;
    }

    /**
     * 온도 업데이트
     */
    public void updateTemperature() {
        // 기본 냉각
        temperature *= COOLING_RATE;

        // 최소 온도 보장
        if (temperature < MIN_TEMPERATURE) {
            temperature = MIN_TEMPERATURE;
        }

        // 필요시 재가열
        if (shouldReheat()) {
            reheat();
        }
    }

    /**
     * 재가열 필요 여부 확인
     */
    private boolean shouldReheat() {
        return iterationsWithoutImprovement >= REHEAT_INTERVAL;
    }

    /**
     * 온도 재가열
     */
    private void reheat() {
        temperature = initialTemperature * REHEAT_FACTOR;
        iterationsWithoutImprovement = 0;
    }

    /**
     * 현재 온도 반환
     */
    public double getTemperature() {
        return temperature;
    }

    /**
     * 개선 없는 반복 횟수 반환
     */
    public int getIterationsWithoutImprovement() {
        return iterationsWithoutImprovement;
    }

    /**
     * 수용 확률 계산 (디버깅/모니터링용)
     */
    public double calculateAcceptanceProbability(double currentCost, double newCost) {
        if (newCost <= currentCost) {
            return 1.0;
        }
        double costDifference = newCost - currentCost;
        return Math.exp(-costDifference / temperature);
    }

    /**
     * 현재 상태 문자열로 반환
     */
    @Override
    public String toString() {
        return String.format(
            "SA[temp=%.2f, noImprovement=%d]",
            temperature,
            iterationsWithoutImprovement
        );
    }
}