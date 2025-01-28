package dev.brown.alns.repair;

import dev.brown.alns.parameter.HyperParameter;
import dev.brown.domain.Solution;
import java.util.*;

public class AdaptiveRepair {
    private final Map<RepairMethod, RepairOperator> operators;
    private final List<Float> weights;
    private final List<Integer> numCalled;
    private final List<Integer> numSuccess;
    private final Random random;
    private final HyperParameter hparam;

    private RepairMethod currentMethod;
    private int iteration = 0;

    public AdaptiveRepair(HyperParameter hparam, long seed) {
        this.hparam = hparam;
        this.random = new Random(seed);
        this.operators = new EnumMap<>(RepairMethod.class);
        this.weights = new ArrayList<>();
        this.numCalled = new ArrayList<>();
        this.numSuccess = new ArrayList<>();

        initializeOperators();
        initializeWeights();
    }

    private void initializeOperators() {
        operators.put(RepairMethod.GREEDY, new GreedyRepair(random));
        operators.put(RepairMethod.REGRET_2, new RegretRepair(2, hparam, random));
        operators.put(RepairMethod.REGRET_3, new RegretRepair(3, hparam, random));
        operators.put(RepairMethod.BEST_POSITION, new BestPositionRepair(hparam, random));
        operators.put(RepairMethod.RANDOM_REPAIR, new RandomRepair(random));
    }

    private void initializeWeights() {
        for (int i = 0; i < RepairMethod.values().length; i++) {
            weights.add(1.0f);
            numCalled.add(0);
            numSuccess.add(0);
        }
        normalizeWeights();
    }

    private void normalizeWeights() {
        float total = weights.stream().reduce(0f, Float::sum);
        for (int i = 0; i < weights.size(); i++) {
            weights.set(i, weights.get(i) / total);
        }
    }

    public boolean repair(Solution solution, List<Integer> removedOrders) {
        iteration++;
        currentMethod = selectMethod();
        return operators.get(currentMethod).repair(solution, removedOrders);
    }

    private RepairMethod selectMethod() {
        float roulette = random.nextFloat();
        float sum = 0;

        for (int i = 0; i < weights.size(); i++) {
            sum += weights.get(i);
            if (roulette <= sum) {
                return RepairMethod.values()[i];
            }
        }

        return RepairMethod.GREEDY;  // 기본값
    }

    public void updateWeights(int score) {
        numCalled.set(currentMethod.ordinal(),
            numCalled.get(currentMethod.ordinal()) + 1);
        numSuccess.set(currentMethod.ordinal(),
            numSuccess.get(currentMethod.ordinal()) + score);

        if (iteration % hparam.getUpdatePeriod() == 0) {
            updateMethodWeights();
        }
    }

    private void updateMethodWeights() {
        for (int i = 0; i < weights.size(); i++) {
            float performance = (float) numSuccess.get(i) / Math.max(1, numCalled.get(i));
            weights.set(i, weights.get(i) * (1 - hparam.getRho()) +
                performance * hparam.getRho());
        }
        normalizeWeights();
    }

    public RepairMethod getCurrentMethod() {
        return currentMethod;
    }
}