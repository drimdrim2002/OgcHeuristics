package dev.brown.improved.alns;

import dev.brown.improved.alns.algorithm.Annealer;
import dev.brown.improved.alns.domain.*;
import dev.brown.improved.alns.destroy.AdaptiveDestroyer;
import dev.brown.improved.alns.parameter.HyperParameter;
import dev.brown.improved.alns.repair.RepairBuilder;
import dev.brown.improved.alns.storage.BundleStorage;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ALNS의 주요 반복 로직을 구현하는 클래스
 */
public class MainIteration {

    /**
     * 순차적 파괴-삽입 과정 실행
     */
    public static int sequentialDestroyInsert(
        double totalTimeLimit,
        Instant startTime,
        Solution bestSolution,
        Map<String, Integer> ridersAvailable,
        Annealer annealer,
        AdaptiveDestroyer destroyer,
        RepairBuilder builder,
        BundleStorage bundleStorage,
        HyperParameter hparam,
        boolean verbose) {

        Solution currentSolution = new Solution(bestSolution);
        int iteration = 0;
        int K = builder.getK();

        while (true) {
            int spentTime = getTimeElapsed(startTime);
            if (spentTime > totalTimeLimit - hparam.getTimeMargin()) {
                break;
            }
            iteration++;

            Solution newSolution = new Solution(currentSolution);
            Map<String, Integer> newRidersAvailable = new HashMap<>(ridersAvailable);

            // 솔루션 파괴
            List<Integer> destroyedIds = destroyer.destroy(newSolution, newRidersAvailable);
            List<Integer> copiedIds = new ArrayList<>(destroyedIds);

            // 솔루션 재구성
            builder.build(destroyedIds, newSolution, newRidersAvailable);
            bundleStorage.append(newSolution);

            // 업데이트 점수 계산
            int updateScore = calculateUpdateScore(
                newSolution,
                currentSolution,
                bestSolution,
                annealer,
                spentTime,
                hparam
            );

            // 디버그 출력
            if (verbose && newSolution.getCost() < bestSolution.getCost()) {
                printDebugInfo(copiedIds, newSolution, bestSolution.getCost(),
                    iteration, destroyer.getCurrentMethod());
            }

            // 솔루션 업데이트
            if (updateScore != 0) {
                currentSolution = newSolution;
                ridersAvailable.clear();
                ridersAvailable.putAll(newRidersAvailable);
            }

            destroyer.update(updateScore);
        }

        return iteration;
    }

    /**
     * 업데이트 점수 계산
     */
    private static int calculateUpdateScore(
        Solution newSolution,
        Solution currentSolution,
        Solution bestSolution,
        Annealer annealer,
        int spentTime,
        HyperParameter hparam) {

        if (newSolution.getCost() < bestSolution.getCost()) {
            bestSolution.copyFrom(newSolution);
            return hparam.getScore1();
        } else if (newSolution.getCost() < currentSolution.getCost()) {
            return hparam.getScore2();
        } else if (annealer.accept(newSolution.getCost(), currentSolution.getCost(), spentTime)) {
            return hparam.getScore3();
        }
        return 0;
    }

    /**
     * 디버그 정보 출력
     */
    private static void printDebugInfo(
        List<Integer> destroyedIds,
        Solution newSolution,
        double bestCost,
        int iteration,
        String destroyMethod) {

        System.out.println("Destroyed orders: " +
            destroyedIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(" "))
        );

        System.out.println("Inserted orders into solution: ");
        for (Bundle bundle : newSolution.getBundles()) {
            System.out.print("[ ");
            System.out.print(bundle.source().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(" ")));
            System.out.print(" ] -> [ ");
            System.out.print(bundle.dest().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(" ")));
            System.out.println(" ]");
        }

        System.out.printf("current best : %.2f on iter %d with method : %s%n",
            bestCost / newSolution.size(), iteration, destroyMethod);
    }

    /**
     * 경과 시간 계산
     */
    private static int getTimeElapsed(Instant startTime) {
        return  (int) ((Instant.now().toEpochMilli() - startTime.toEpochMilli()) / 1000);
    }
}