package dev.brown.alns.destroy;

import dev.brown.alns.parameter.HyperParameter;
import dev.brown.domain.Solution;
import java.util.*;

public class AdaptiveDestroyer {
    private final Map<DestroyMethod, DestroyOperator> operators;
    private final List<Float> weights;
    private final List<Integer> numCalled;
    private final List<Integer> numSuccess;
    private final Random random;
    private final HyperParameter hparam;

    private DestroyMethod currentMethod;
    private int iteration = 0;

    public AdaptiveDestroyer(HyperParameter hparam, long seed) {
        this.hparam = hparam;
        this.random = new Random(seed);
        this.operators = new EnumMap<>(DestroyMethod.class);
        this.weights = new ArrayList<>();
        this.numCalled = new ArrayList<>();
        this.numSuccess = new ArrayList<>();

        initializeOperators();
        initializeWeights();
    }

    private void initializeOperators() {
        operators.put(DestroyMethod.RANDOM, new RandomRemoval(random));
        operators.put(DestroyMethod.SHAW, new ShawRemoval(hparam, random));
        operators.put(DestroyMethod.WORST, new WorstRemoval(hparam, random));
        operators.put(DestroyMethod.HISTORICAL, new HistoricalRemoval(hparam, random));
        operators.put(DestroyMethod.ROUTE, new RouteRemoval(hparam, random));
    }

    public List<Integer> destroy(Solution solution) {
        iteration++;
        currentMethod = selectMethod();
        int numToDestroy = calculateDestroySize(solution);

        return operators.get(currentMethod).destroy(solution, numToDestroy);
    }

    private DestroyMethod selectMethod() {
        double total = weights.stream().mapToDouble(w -> w).sum();
        double r = random.nextDouble() * total;
        double sum = 0;

        for (int i = 0; i < weights.size(); i++) {
            sum += weights.get(i);
            if (r <= sum) {
                return DestroyMethod.values()[i];
            }
        }
        return DestroyMethod.RANDOM;  // 기본값
    }

    private int calculateDestroySize(Solution solution) {
        int totalOrders = solution.orderMap().size();
        int minDestroy = Math.max(4, (int)(totalOrders * hparam.getMinProb()));
        int maxDestroy = Math.min(hparam.getMaxDestroy(),
            (int)(totalOrders * hparam.getMaxProb()));
        return minDestroy + random.nextInt(maxDestroy - minDestroy + 1);
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
            float performance = (float) numSuccess.get(i) / numCalled.get(i);
            weights.set(i, weights.get(i) * (1 - hparam.getRho()) +
                performance * hparam.getRho());
        }
        normalizeWeights();
    }

    private void normalizeWeights() {
        float total = weights.stream().reduce(0f, Float::sum);
        for (int i = 0; i < weights.size(); i++) {
            weights.set(i, weights.get(i) / total);
        }
    }


    private void initializeWeights() {
        // DestroyMethod enum의 각 메서드에 대한 초기 가중치 설정
        for (int i = 0; i < DestroyMethod.values().length; i++) {
            weights.add(1.0f);  // 모든 메서드에 동일한 초기 가중치
            numCalled.add(0);   // 호출 횟수 초기화
            numSuccess.add(0);  // 성공 횟수 초기화
        }

        // 가중치 정규화
        normalizeWeights();
    }
}