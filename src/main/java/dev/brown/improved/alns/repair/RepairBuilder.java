package dev.brown.improved.alns.repair;

import dev.brown.improved.alns.parameter.HyperParameter;
import dev.brown.improved.alns.domain.*;
import java.util.*;

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
        RiderOptimizer.optimizeRiderType(
            indices,
            solution,
            ridersAvailable,
            riderInfo,
            ordersPtr,
            distMatPtr,
            matrixLength
        );

        // 복구 전략 적용
        LittleRandomRepair littleRandomRepair = new LittleRandomRepair(
            K,
            ordersPtr,
            riderInfo,
            distMatPtr,
            hparam,
            usePower,
            hparam.getConsiderSize(),
            random.nextLong()
        );

        if (!useOld) {
            littleRandomRepair.repairNew(insertOrder, idsToBuild, solution, ridersAvailable);
        } else {
            littleRandomRepair.repairOld(insertOrder, idsToBuild, solution, ridersAvailable);
        }
    }
}