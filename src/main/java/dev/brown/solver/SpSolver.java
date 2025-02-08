package dev.brown.solver;


import dev.brown.improved.alns.domain.Bundle;
import dev.brown.improved.alns.domain.SPResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.google.ortools.Loader;
import com.google.ortools.sat.CpModel;
import com.google.ortools.sat.CpSolver;
import com.google.ortools.sat.CpSolverStatus;
import com.google.ortools.sat.IntVar;
import com.google.ortools.sat.LinearExpr;
import com.google.ortools.sat.LinearExprBuilder;

import dev.brown.improved.alns.domain.Solution;

public class SpSolver {
    static {
        Loader.loadNativeLibraries();
    }

    /**
     * SP 문제를 해결합니다.
     */
    public static SPResult solveSP(
        int[][] edgeMatrix,
        int[] riderTypes,
        Map<String, Integer> ridersAvailable,
        List<Bundle> bundles,
        double timeLimit,
        int verbose,
        int[][] distMat,
        Map<String, int[][]> riderTimes,
        Map<String, List<Integer>> riderInfos,
        int heurTime) {

        int K = riderTypes.length;
        int numBundles = bundles.size();

        // CP-SAT 모델 생성
        CpModel model = new CpModel();

        // 결정 변수 생성: 각 번들의 선택 여부
        IntVar[] bundleVars = new IntVar[numBundles];
        for (int i = 0; i < numBundles; i++) {
            bundleVars[i] = model.newBoolVar("bundle_" + i);
        }

        // 제약 조건 1: 각 주문은 정확히 하나의 번들에 할당되어야 함
        for (int i = 0; i < K; i++) {
            List<IntVar> bundlesContainingOrder = new ArrayList<>();
            for (int j = 0; j < numBundles; j++) {
                Bundle bundle = bundles.get(j);
                // source나 dest에 주문이 포함되어 있는지 확인
                if (bundle.source().contains(i) || bundle.dest().contains(i)) {
                    bundlesContainingOrder.add(bundleVars[j]);
                }
            }
            model.addEquality(LinearExpr.sum(bundlesContainingOrder.toArray(new IntVar[0])), 1);
        }
        // 제약 조건 2: 각 라이더 타입별 사용 가능한 수 제한
        for (Map.Entry<String, Integer> entry : ridersAvailable.entrySet()) {
            String riderType = entry.getKey();
            int available = entry.getValue();

            List<IntVar> riderTypeBundles = new ArrayList<>();
            for (int j = 0; j < numBundles; j++) {
                if (bundles.get(j).riderType().equals(riderType)) {
                    riderTypeBundles.add(bundleVars[j]);
                }
            }

            if (!riderTypeBundles.isEmpty()) {
                model.addLessOrEqual(
                    LinearExpr.sum(riderTypeBundles.toArray(new IntVar[0])),
                    available
                );
            }
        }

        // 목적 함수: 총 비용 최소화
        LinearExprBuilder objective = LinearExpr.newBuilder();
        for (int i = 0; i < numBundles; i++) {
            objective.addTerm(bundleVars[i], (long)(bundles.get(i).cost() * 1000)); // 정수로 변환
        }
        model.minimize(objective);

        // 솔버 설정
        CpSolver solver = new CpSolver();
        solver.getParameters().setMaxTimeInSeconds(timeLimit);

        if (verbose < 2) {
            solver.getParameters().setLogToStdout(false);
        }

        // 최적화 실행
        CpSolverStatus status = solver.solve(model);

        // 결과 처리
        Solution bestSol = new Solution();
        int[][] newSolEdge = new int[2 * K][2 * K];
        int[] newSolRider = new int[K];
        List<Bundle> newInitBundle = new ArrayList<>();
        double gurobiScore = Double.MAX_VALUE;

        if (status == CpSolverStatus.OPTIMAL || status == CpSolverStatus.FEASIBLE) {
            gurobiScore = solver.objectiveValue() / 1000.0; // 원래 스케일로 변환

            // 선택된 번들 처리
            for (int i = 0; i < numBundles; i++) {
                if (solver.value(bundleVars[i]) > 0.5) {
                    Bundle selectedBundle = bundles.get(i);
                    updateSolution(selectedBundle, newSolEdge, newSolRider, newInitBundle);
                }
            }
        }

        return new SPResult(bestSol, newSolEdge, newSolRider, newInitBundle, gurobiScore);
    }

    /**
     * 선택된 번들로 해결책을 업데이트합니다.
     */
    private static void updateSolution(Bundle bundle, int[][] solEdge,
        int[] solRider, List<Bundle> initBundle) {
        // 엣지 매트릭스 업데이트
        List<Integer> route = new ArrayList<>();
        route.addAll(bundle.source());
        route.addAll(bundle.dest());
        for (int i = 0; i < route.size() - 1; i++) {
            solEdge[route.get(i)][route.get(i + 1)] = 1;
        }

        // 라이더 할당 업데이트
        for (int orderId : bundle.source()) {
            solRider[orderId] = getRiderTypeId(bundle.riderType());
        }

        // 초기 번들 리스트 업데이트
        initBundle.add(new Bundle(bundle));
    }

    /**
     * 라이더 타입 문자열을 ID로 변환합니다.
     */
    private static int getRiderTypeId(String riderType) {
        return switch (riderType) {
            case "WALK" -> 0;
            case "BIKE" -> 1;
            case "CAR" -> 2;
            default -> throw new IllegalArgumentException("Unknown rider type: " + riderType);
        };
    }
}
