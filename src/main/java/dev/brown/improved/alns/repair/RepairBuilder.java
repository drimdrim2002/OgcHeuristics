package dev.brown.improved.alns.repair;

import dev.brown.improved.alns.domain.Bundle;
import dev.brown.improved.alns.domain.RiderInfo;
import dev.brown.improved.alns.domain.Solution;
import dev.brown.improved.alns.parameter.HyperParameter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 복구 전략을 구현하는 빌더 클래스
 */
public class RepairBuilder {
    private final HyperParameter hparam;
    private final int K;
    private final int matrixLength;
    private final Random random;
    private final boolean usePower;
    private final int[] ordersPtr;
    private final RiderInfo riderInfo;
    private final int[] distMatPtr;
    private final boolean useOld;

    /**
     * 빌더 생성자
     */
    public RepairBuilder(
        int K,
        int[] ordersPtr,
        RiderInfo riderInfo,
        int[] distMatPtr,
        boolean usePower,
        HyperParameter hparam,
        long seed) {
        this.K = K;
        this.matrixLength = K * 2;
        this.ordersPtr = ordersPtr;
        this.riderInfo = riderInfo;
        this.distMatPtr = distMatPtr;
        this.usePower = usePower;
        this.hparam = hparam;
        this.useOld = hparam.isUseOld();
        this.random = new Random(seed);
    }

    /**
     * 복구 프로세스 실행
     */
    public void build(
        List<Integer> idsToBuild,
        Solution solution,
        Map<String, Integer> ridersAvailable) {

        // 인덱스 셔플
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < solution.size(); i++) {
            indices.add(i);
        }
        Collections.shuffle(indices, random);

        // 삽입 순서 생성
        List<Integer> insertOrder = new ArrayList<>();
        for (int currSize = idsToBuild.size(); currSize >= 1; currSize--) {
            int randomIndex = random.nextInt(currSize);
            insertOrder.add(randomIndex);
        }

        // 라이더 타입 최적화
        optimizeRiderType(
            indices,
            solution,
            ridersAvailable
        );

        // repair 수행
        if (!useOld) {
            new LittleRandomRepair(K, ordersPtr, riderInfo, distMatPtr, hparam,
                usePower, hparam.getConsiderSize(), random.nextLong())
                .repair(idsToBuild, solution, ridersAvailable);
        } else {
            new LittleRandomRepairOld(K, ordersPtr, riderInfo, distMatPtr, hparam,
                usePower, hparam.getConsiderSize(), random.nextLong())
                .repair(idsToBuild, solution, ridersAvailable);
        }
    }

    public int getK() {
        return K;
    }

    private void optimizeRiderType(
        List<Integer> indices,
        Solution solution,
        Map<String, Integer> ridersAvailable
    ) {
        OptimizeRider optimizer = new OptimizeRider(
            ordersPtr,
            riderInfo,
            distMatPtr,
            matrixLength
        );

        for (int idx : indices) {
            String currentType = solution.getRiderType(idx);
            List<Integer> source = solution.getSource(idx);
            List<Integer> dest = solution.getDest(idx);

            InvestigationResult result = optimizer.investigate(source, dest);

            for (String newType : result.getOptimalOrder()) {
                if (ridersAvailable.getOrDefault(newType, 0) > 0 &&
                    result.getFeasibility(newType)) {
                    if (!newType.equals(currentType)) {
                        // 라이더 타입 변경
                        ridersAvailable.merge(currentType, 1, Integer::sum);
                        ridersAvailable.merge(newType, -1, Integer::sum);

                        solution.updateBundle(idx, new Bundle(
                            newType,
                            result.getCost(newType),
                            result.getSource(newType),
                            result.getDest(newType)
                        ));
                    }
                    break;
                }
            }
        }
    }
}