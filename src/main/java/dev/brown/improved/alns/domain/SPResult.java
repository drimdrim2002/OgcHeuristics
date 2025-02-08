package dev.brown.improved.alns.domain;

import java.util.Arrays;
import java.util.List;

/**
 * GurobiSP의 solveSP 메서드 결과를 담는 클래스
 */
public class SPResult {
    private final Solution bestSol;
    private final int[][] solEdge;
    private final int[] solRider;
    private final List<Bundle> initBundle;
    private final double gurobiScore;

    /**
     * SPResult 객체를 생성합니다.
     *
     * @param bestSol 최적 해결책
     * @param solEdge 해결책 엣지 행렬
     * @param solRider 해결책 라이더 배열
     * @param initBundle 초기 번들 리스트
     * @param gurobiScore Gurobi 점수
     */
    public SPResult(Solution bestSol, int[][] solEdge, int[] solRider,
        List<Bundle> initBundle, double gurobiScore) {
        this.bestSol = bestSol;
        this.solEdge = solEdge;
        this.solRider = solRider;
        this.initBundle = initBundle;
        this.gurobiScore = gurobiScore;
    }

    /**
     * 최적 해결책을 반환합니다.
     */
    public Solution getBestSol() {
        return bestSol;
    }

    /**
     * 해결책 엣지 행렬을 반환합니다.
     */
    public int[][] getSolEdge() {
        return solEdge;
    }

    /**
     * 해결책 라이더 배열을 반환합니다.
     */
    public int[] getSolRider() {
        return solRider;
    }

    /**
     * 초기 번들 리스트를 반환합니다.
     */
    public List<Bundle> getInitBundle() {
        return initBundle;
    }

    /**
     * Gurobi 점수를 반환합니다.
     */
    public double getGurobiScore() {
        return gurobiScore;
    }

    @Override
    public String toString() {
        return "SPResult{" +
            "bestSol=" + bestSol +
            ", solEdge=" + Arrays.deepToString(solEdge) +
            ", solRider=" + Arrays.toString(solRider) +
            ", initBundle=" + initBundle +
            ", gurobiScore=" + gurobiScore +
            '}';
    }
}