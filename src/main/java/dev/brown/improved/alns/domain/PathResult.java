package dev.brown.improved.alns.domain;

import java.util.List;

/**
 * 경로 계산 결과를 저장하는 클래스
 * C++의 tuple<bool, float, vint, vint>를 Java record로 변환
 */
public record PathResult(
    boolean feasible,     // 경로 실행 가능 여부 (isFeasible -> feasible로 변경)
    double cost,          // 경로 비용
    List<Integer> source, // 픽업 순서
    List<Integer> dest    // 배달 순서
) {
    /**
     * 불변성을 보장하기 위한 생성자
     */
    public PathResult {
        source = List.copyOf(source); // 불변 리스트로 변환
        dest = List.copyOf(dest);     // 불변 리스트로 변환
    }

    /**
     * 경로 실행 가능 여부를 반환하는 메서드
     * @return 실행 가능 여부
     */
    public boolean isFeasible() {
        return feasible;
    }

    /**
     * 빈 경로 결과를 생성하는 팩토리 메서드
     */
    public static PathResult empty() {
        return new PathResult(
            false,
            Double.MAX_VALUE,
            List.of(),
            List.of()
        );
    }

    /**
     * 실행 가능한 경로 결과를 생성하는 팩토리 메서드
     */
    public static PathResult feasible(
        double cost,
        List<Integer> source,
        List<Integer> dest
    ) {
        return new PathResult(true, cost, source, dest);
    }

    /**
     * 실행 불가능한 경로 결과를 생성하는 팩토리 메서드
     */
    public static PathResult infeasible() {
        return new PathResult(
            false,
            Double.MAX_VALUE,
            List.of(),
            List.of()
        );
    }
}