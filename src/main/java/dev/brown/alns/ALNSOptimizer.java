package dev.brown.alns;

import dev.brown.alns.destroy.AdaptiveDestroyer;
import dev.brown.alns.repair.AdaptiveRepair;
import dev.brown.alns.parameter.HyperParameter;
import dev.brown.domain.Solution;
import java.util.*;
import java.util.logging.Logger;

public class ALNSOptimizer {
    private static final Logger logger = Logger.getLogger(ALNSOptimizer.class.getName());

    private final Random random;
    private final HyperParameter params;
    private final AdaptiveDestroyer destroyer;
    private final AdaptiveRepair repairer;
    private final SimulatedAnnealing sa;

    // 최적화 상태 변수들
    private Solution bestSolution;
    private Solution currentSolution;
    private double bestCost;
    private double currentCost;
    private int iteration;
    private int iterationsWithoutImprovement;

    /**
     * ALNSOptimizer 생성자
     */
    public ALNSOptimizer(HyperParameter params, long seed, Solution initialSolution) {
        this.params = params;
        this.random = new Random(seed);
        this.destroyer = new AdaptiveDestroyer(params, seed);
        this.repairer = new AdaptiveRepair(params, random.nextLong());
        this.iteration = 0;
        this.iterationsWithoutImprovement = 0;

        // 초기 솔루션 설정
        setupInitialSolution(initialSolution);

        // SA 초기화 (생성자에서)
        this.sa = new SimulatedAnnealing(params, random.nextLong(), bestCost);

        logger.info("ALNS Optimizer initialized with seed: " + seed);
    }

    /**
     * 최적화 수행
     */
    public Solution optimize(double timeLimit) {
        // 초기화
        long startTime = System.currentTimeMillis();

        logger.info("Starting optimization with initial cost: " + bestCost);

        // 메인 최적화 루프
        while (!isTerminationConditionMet(startTime, timeLimit)) {
            iteration++;

            // 1. Destroy
            List<Integer> removedOrders = destroyer.destroy(currentSolution);

            // 2. Repair
            Solution candidateSolution = currentSolution.copy();
            boolean repairSuccess = repairer.repair(candidateSolution, removedOrders);

            if (!repairSuccess) {
                iterationsWithoutImprovement++;
                continue;
            }

            // 3. 해 평가
            double candidateCost = candidateSolution.calculateTotalCost();
            boolean isNewBest = candidateCost < bestCost;

            // 4. 해 수용 여부 결정 및 가중치 업데이트
            if (sa.accept(currentCost, candidateCost, isNewBest)) {
                updateCurrentSolution(candidateSolution, candidateCost);

                // 최적해 갱신
                if (isNewBest) {
                    updateBestSolution(candidateSolution, candidateCost);
                    updateOperatorScores(params.getScore1());
                    iterationsWithoutImprovement = 0;
                } else if (candidateCost < currentCost) {
                    updateOperatorScores(params.getScore2());
                } else {
                    updateOperatorScores(params.getScore3());
                }
            } else {
                iterationsWithoutImprovement++;
            }

            // 5. 파라미터 업데이트
            sa.updateTemperature();

            // 6. 로깅
            if (iteration % params.getUpdatePeriod() == 0) {
                logProgress(startTime);
            }
        }

        logger.info(String.format("Optimization completed after %d iterations. Best cost: %.2f",
            iteration, bestCost));

        return bestSolution;
    }

    private void setupInitialSolution(Solution initialSolution) {
        currentSolution = initialSolution.copy();
        bestSolution = initialSolution.copy();
        currentCost = initialSolution.calculateTotalCost();
        bestCost = currentCost;
    }

    private boolean isTerminationConditionMet(long startTime, double timeLimit) {
        double elapsedTime = (System.currentTimeMillis() - startTime) / 1000.0;
        return elapsedTime >= timeLimit ||
            iterationsWithoutImprovement >= params.getMaxDestroy();
    }

    private void updateCurrentSolution(Solution newSolution, double newCost) {
        currentSolution = newSolution;
        currentCost = newCost;
    }

    private void updateBestSolution(Solution newSolution, double newCost) {
        bestSolution = newSolution.copy();
        bestCost = newCost;
        logger.info(String.format("New best solution found with cost: %.2f", bestCost));
    }

    private void updateOperatorScores(int score) {
        destroyer.updateWeights(score);
        repairer.updateWeights(score);
    }

    private void logProgress(long startTime) {
        double elapsedTime = (System.currentTimeMillis() - startTime) / 1000.0;
        logger.info(String.format(
            "Iteration %d: Current=%.2f, Best=%.2f, Temp=%.2f, Time=%.1fs",
            iteration, currentCost, bestCost, sa.getTemperature(), elapsedTime
        ));
    }

    @Override
    public String toString() {
        return String.format(
            "ALNS[iteration=%d, bestCost=%.2f, currentCost=%.2f]",
            iteration, bestCost, currentCost
        );
    }
}