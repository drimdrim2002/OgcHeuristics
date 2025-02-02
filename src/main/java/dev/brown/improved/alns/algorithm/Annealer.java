package dev.brown.improved.alns.algorithm;

import java.util.Random;

/**
 * Simulated Annealing 알고리즘을 구현한 클래스
 */
public class Annealer {
    private double temperature;
    private final double modifyRatio;
    private int modifyCount;
    private final Random random;
    private final int iterPerSecond;

    /**
     * Annealer 생성자
     * @param initialTemperature 초기 온도
     * @param modifyRatio 온도 수정 비율
     * @param K 주문 개수
     * @param seed 랜덤 시드
     */
    public Annealer(double initialTemperature, double modifyRatio, int K, long seed) {
        this.temperature = initialTemperature;
        this.modifyRatio = modifyRatio;
        this.modifyCount = 0;
        this.random = new Random(seed);
        this.iterPerSecond = 200000 / K;
    }

    /**
     * 기본 시드(42)를 사용하는 생성자
     */
    public Annealer(double initialTemperature, double modifyRatio, int K) {
        this(initialTemperature, modifyRatio, K, 42L);
    }

    /**
     * 새로운 해를 수락할지 결정
     * @param newCost 새로운 해의 비용
     * @param oldCost 현재 해의 비용
     * @param iteration 현재 반복 횟수
     * @return 새로운 해 수락 여부
     */
    public boolean accept(double newCost, double oldCost, int iteration) {
        // 온도 수정이 필요한지 확인
        if (modifyCount + 1 < iteration / iterPerSecond) {
            modifyCount++;
            modifyTemperature();
        }

        // 새로운 해가 더 좋으면 무조건 수락
        if (newCost < oldCost) {
            return true;
        }

        // Simulated Annealing 확률 계산
        double acceptanceProbability = Math.exp((oldCost - newCost) / temperature);
        double randomValue = random.nextDouble();

        return randomValue < acceptanceProbability;
    }

    /**
     * 온도 수정
     */
    private void modifyTemperature() {
        temperature /= modifyRatio;
    }

    /**
     * 현재 온도 반환
     */
    public double getTemperature() {
        return temperature;
    }

    /**
     * 현재 수정 횟수 반환
     */
    public int getModifyCount() {
        return modifyCount;
    }

    /**
     * 초당 반복 횟수 반환
     */
    public int getIterPerSecond() {
        return iterPerSecond;
    }
}