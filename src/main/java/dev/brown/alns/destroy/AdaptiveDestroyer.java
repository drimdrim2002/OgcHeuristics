package dev.brown.alns.destroy;

import dev.brown.alns.parameter.HyperParameter;
import dev.brown.domain.Solution;
import java.util.*;

public class AdaptiveDestroyer {
    // destroy 연산자들을 저장하는 Map
    private final Map<DestroyMethod, DestroyOperator> operators;
    // 각 destroy 연산자들의 가중치를 저장하는 리스트
    private final List<Float> weights;
    // 각 메서드들의 호출 횟수
    private final List<Integer> numCalled;
    // 각 메서드들의 성공 횟수
    private final List<Integer> numSuccess;
    private final Random random;
    private final HyperParameter hparam;

    // 현재 선택된 destroy 메서드
    private DestroyMethod currentMethod;
    // 반복 횟수
    private int iteration = 0;

    /**
     * AdaptiveDestroyer 생성자
     * @param hparam 하이퍼파라미터
     * @param seed 랜덤 시드
     */
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

    /**
     * destroy 연산자들 초기화
     */
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

    /*
       예를 들어 5개의 destroy 메서드가 있고 각각의 가중치가 다음과 같다고 가정:
        weights = [0.3, 0.25, 0.2, 0.15, 0.1]  // 합계 = 1.0

        // 1. 가중치에 따른 선택 구간:
        RANDOM:      0.0  ~ 0.3   (30% 확률)
        SHAW:        0.3  ~ 0.55  (25% 확률)
        WORST:       0.55 ~ 0.75  (20% 확률)
        HISTORICAL:  0.75 ~ 0.9   (15% 확률)
        ROUTE:       0.9  ~ 1.0   (10% 확률)

        // 2. random.nextDouble() * total이 생성한 값에 따른 선택:
        r = 0.42 → SHAW 선택
        r = 0.12 → RANDOM 선택
        r = 0.82 → HISTORICAL 선택
     */

    private DestroyMethod selectMethod() {
        // 모든 가중치들의 합계 계산
        double total = weights.stream().mapToDouble(w -> w).sum();
        // 0부터 총합 사이의 랜덤 값 생성
        double r = random.nextDouble() * total;
        // 롤렛 휠 선택 수행
        double sum = 0;
        for (int i = 0; i < weights.size(); i++) {
            sum += weights.get(i);
            if (r <= sum) {
                return DestroyMethod.values()[i];
            }
        }
        // 에외 처리를 위한 기본값 반환
        return DestroyMethod.RANDOM;  // 기본값
    }

    /**
     * 제거할 주문 수 계산
     */
    private int calculateDestroySize(Solution solution) {
        int totalOrders = solution.orderMap().size();
        int minDestroy = Math.max(4, (int)(totalOrders * hparam.getMinProb()));
        int maxDestroy = Math.min(hparam.getMaxDestroy(),
            (int)(totalOrders * hparam.getMaxProb()));
        return minDestroy + random.nextInt(maxDestroy - minDestroy + 1);
    }

    /**
     * destroy 메서드의 성능에 따라 가중치 업데이트
     */
    public void updateWeights(int score) {
        numCalled.set(currentMethod.ordinal(),
            numCalled.get(currentMethod.ordinal()) + 1);
        numSuccess.set(currentMethod.ordinal(),
            numSuccess.get(currentMethod.ordinal()) + score);

        if (iteration % hparam.getUpdatePeriod() == 0) {
            updateMethodWeights();
        }
    }
    /**
     * 각 메서드의 가중치 업데이트
     */
    private void updateMethodWeights() {
        for (int i = 0; i < weights.size(); i++) {
            float performance = (float) numSuccess.get(i) / numCalled.get(i);
            weights.set(i, weights.get(i) * (1 - hparam.getRho()) +
                performance * hparam.getRho());
        }
        normalizeWeights();
    }

    /**
     * 가중치 정규화 (합이 1이 되도록)
     */
    private void normalizeWeights() {
        float total = weights.stream().reduce(0f, Float::sum);
        for (int i = 0; i < weights.size(); i++) {
            weights.set(i, weights.get(i) / total);
        }
    }


    /**
     * 초기 가중치 설정
     */
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