package dev.brown.improved.alns.util;

import dev.brown.improved.alns.domain.Bundle;
import dev.brown.improved.alns.domain.Constants;
import dev.brown.improved.alns.domain.PathResult;
import dev.brown.improved.alns.domain.Result;
import dev.brown.improved.alns.domain.RiderInfo;
import dev.brown.improved.alns.domain.Solution;
import java.util.*;

public final class AlnsUtils {

    private static final double INF = Constants.INF;
    private static final List<String> RIDER = Constants.RIDER;

    private AlnsUtils() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    private static int flattenIndex(int i, int j, int l) {
        return i * l + j;
    }

    public static double getBundleCost(List<Integer> source,
        List<Integer> dest,
        List<Integer> rider,
        int[] distMatrix,
        int l) {
        int K = l / 2;
        int n = source.size();
        if (n == 0)
            return 0;

        int dist = 0;
        for (int i = 1; i < n; i++) {
            dist += distMatrix[flattenIndex(source.get(i - 1), source.get(i), l)];
        }
        dist += distMatrix[flattenIndex(source.get(n - 1), dest.get(0) + K, l)];
        for (int i = 1; i < n; i++) {
            dist += distMatrix[flattenIndex(dest.get(i - 1) + K, dest.get(i) + K, l)];
        }

        return rider.get(1) + rider.get(2) * (double) dist / 100;
    }

    public static List<Map.Entry<String, Double>> getOptimalRiderType(
        List<Integer> source,
        List<Integer> dest,
        RiderInfo riderInfo,
        int[] orders,
        int[] distMatrix,
        int l) {

        int totalVolume = 0;
        for (int id : source) {
            totalVolume += orders[flattenIndex(id, 2, 3)];
        }

        var walkInfo = riderInfo.prepare("WALK");
        var bikeInfo = riderInfo.prepare("BIKE");
        var carInfo = riderInfo.prepare("CAR");

        var resWalk = checkPath(source, dest, walkInfo.getKey(), walkInfo.getValue(), orders, distMatrix, l);
        var resBike = checkPath(source, dest, bikeInfo.getKey(), bikeInfo.getValue(), orders, distMatrix, l);
        var resCar = checkPath(source, dest, carInfo.getKey(), carInfo.getValue(), orders, distMatrix, l);

        List<Map.Entry<Double, String>> feasibleTypes = new ArrayList<>();
        if (resWalk.getKey() && totalVolume <= walkInfo.getValue().get(0)) {
            feasibleTypes.add(Map.entry(resWalk.getValue(), "WALK"));
        }
        if (resBike.getKey() && totalVolume <= bikeInfo.getValue().get(0)) {
            feasibleTypes.add(Map.entry(resBike.getValue(), "BIKE"));
        }
        if (resCar.getKey() && totalVolume <= carInfo.getValue().get(0)) {
            feasibleTypes.add(Map.entry(resCar.getValue(), "CAR"));
        }

        // Double 값을 기준으로 정렬
        feasibleTypes.sort(Comparator.comparing(Map.Entry::getKey));

        // 결과 형식 변환
        return feasibleTypes.stream()
            .map(entry -> Map.entry(entry.getValue(), entry.getKey()))
            .toList();
    }

    private static Map.Entry<Boolean, Double> checkPath(
        List<Integer> source,
        List<Integer> dest,
        int[] timeMatrix,
        List<Integer> rider,
        int[] orders,
        int[] distMatrix,
        int l) {

        int K = l / 2;
        int n = source.size();

        int dist = 0;
        int currentTime = orders[flattenIndex(source.get(0), 0, 3)];

        for (int i = 1; i < n; i++) {
            dist += distMatrix[flattenIndex(source.get(i - 1), source.get(i), l)];
            currentTime = Math.max(currentTime + timeMatrix[flattenIndex(source.get(i - 1), source.get(i), l)],
                orders[flattenIndex(source.get(i), 0, 3)]);
        }

        int before = source.get(n - 1);
        for (int i = 0; i < n; i++) {
            int after = dest.get(i) + K;
            dist += distMatrix[flattenIndex(before, after, l)];
            currentTime += timeMatrix[flattenIndex(before, after, l)];
            before = after;

            if (currentTime > orders[flattenIndex(dest.get(i), 1, 3)]) {
                return Map.entry(false, INF);
            }
        }

        return Map.entry(true, rider.get(1) + rider.get(2) * (double) dist / 100);
    }

    public static List<List<Integer>> insertAtAllPositions(int x, List<Integer> v) {
        int n = v.size();
        List<List<Integer>> result = new ArrayList<>();

        for (int i = 0; i <= n; i++) {
            List<Integer> temp = new ArrayList<>();
            for (int j = 0; j < i; j++) {
                temp.add(v.get(j));
            }
            temp.add(x);
            for (int j = i; j < n; j++) {
                temp.add(v.get(j));
            }
            result.add(temp);
        }
        return result;
    }

    public static PathResult findBestPath(
        int x,
        List<Integer> currSource,
        List<Integer> currDest,
        int[] timeMatrix,
        List<Integer> rider,
        int[] orders,
        int[] distMatrix,
        int l) {

        // 1. 용량 제약 검사
        int totalVolume = orders[flattenIndex(x, 2, 3)];
        for (int id : currSource) {
            totalVolume += orders[flattenIndex(id, 2, 3)];
        }
        if (totalVolume > rider.get(0)) {
            return PathResult.infeasible();
        }

        int K = l / 2;
        int n = currSource.size();
        List<Integer> s = new ArrayList<>(n + 1);
        List<Integer> d = new ArrayList<>(n + 1);

        // 초기 배열 설정
        s.add(x);
        d.add(x);
        s.addAll(currSource);
        d.addAll(currDest);

        // 단일 주문 케이스 처리
        if (n == 0) {
            if (orders[flattenIndex(x, 0, 3)] + timeMatrix[flattenIndex(x, x + K, l)] >
                orders[flattenIndex(x, 1, 3)]) {
                return PathResult.infeasible();
            }

            int dist = distMatrix[flattenIndex(x, x + K, l)];
            double cost = rider.get(1) + rider.get(2) * (double) dist / 100;
            return new PathResult(true, cost, s, d);
        }

        // source_time 계산
        List<Integer> sourceTime = calculateSourceTime(s, n, orders, timeMatrix, l);

        // dest_time 계산
        List<Integer> destTime = calculateDestTime(d, n, K, orders, timeMatrix, l);

        // 최적 삽입 위치 찾기
        InsertionResult bestInsertion = findBestInsertion(s, d, n, K, sourceTime, destTime,
            timeMatrix, distMatrix, l);

        if (bestInsertion.increment() == Integer.MAX_VALUE) {
            return PathResult.infeasible();
        }

        // 경로 업데이트
        updatePath(s, d, bestInsertion.sourceIdx(), bestInsertion.destIdx(), n);

        // 총 거리 및 비용 계산
        int totalDist = calculateTotalDistance(s, d, n, K, distMatrix, l);
        double cost = rider.get(1) + rider.get(2) * (double) totalDist / 100;

        return new PathResult(true, cost, s, d);
    }

    public static Result investigate(
        int idToInsert,
        List<Integer> source,
        List<Integer> dest,
        RiderInfo riderInfo,
        int[] orders,
        int[] distMatrix,
        int l) {

        var walkInfo = riderInfo.prepare("WALK");
        var bikeInfo = riderInfo.prepare("BIKE");
        var carInfo = riderInfo.prepare("CAR");

        PathResult walkResult = findBestPath(idToInsert, source, dest,
            walkInfo.getKey(), walkInfo.getValue(), orders, distMatrix, l);
        PathResult bikeResult = findBestPath(idToInsert, source, dest,
            bikeInfo.getKey(), bikeInfo.getValue(), orders, distMatrix, l);
        PathResult carResult = findBestPath(idToInsert, source, dest,
            carInfo.getKey(), carInfo.getValue(), orders, distMatrix, l);

        boolean feasible = walkResult.feasible() || bikeResult.feasible() || carResult.feasible();

        double walkCost = walkResult.cost();
        double bikeCost = bikeResult.cost();
        double carCost = carResult.cost();

        List<String> optOrder = determineOptimalOrder(walkCost, bikeCost, carCost);

        return new Result(walkResult, bikeResult, carResult, optOrder, feasible);
    }

    private static List<String> determineOptimalOrder(double walkCost, double bikeCost, double carCost) {
        if (walkCost <= bikeCost && walkCost < carCost) {
            return bikeCost <= carCost ?
                List.of("WALK", "BIKE", "CAR") :
                List.of("WALK", "CAR", "BIKE");
        } else if (bikeCost <= walkCost && bikeCost < carCost) {
            return walkCost <= carCost ?
                List.of("BIKE", "WALK", "CAR") :
                List.of("BIKE", "CAR", "WALK");
        } else {
            return walkCost <= bikeCost ?
                List.of("CAR", "WALK", "BIKE") :
                List.of("CAR", "BIKE", "WALK");
        }
    }

    // 헬퍼 클래스
    private record InsertionResult(
        int increment,
        int sourceIdx,
        int destIdx
    ) {
    }

    // 헬퍼 메서드들
    private static List<Integer> calculateSourceTime(List<Integer> s, int n,
        int[] orders, int[] timeMatrix, int l) {
        List<Integer> sourceTime = new ArrayList<>(n + 1);
        for (int i = 0; i <= n; i++) {
            int time = orders[flattenIndex(s.get(0), 0, 3)];
            for (int j = 1; j <= n; j++) {
                time = Math.max(time + timeMatrix[flattenIndex(s.get(j - 1), s.get(j), l)],
                    orders[flattenIndex(s.get(j), 0, 3)]);
            }
            sourceTime.add(time);
            if (i != n) {
                Collections.swap(s, i, i + 1);
            }
        }
        return sourceTime;
    }

    private static List<Integer> calculateDestTime(List<Integer> d, int n, int K,
        int[] orders, int[] timeMatrix, int l) {
        List<Integer> destTime = new ArrayList<>(n + 1);
        for (int i = 0; i <= n; i++) {
            int time = orders[flattenIndex(d.get(n), 1, 3)];
            for (int j = n - 1; j >= 0; j--) {
                time = Math.min(time - timeMatrix[flattenIndex(d.get(j) + K, d.get(j + 1) + K, l)],
                    orders[flattenIndex(d.get(j), 1, 3)]);
            }
            destTime.add(time);
            if (i != n) {
                Collections.swap(d, i, i + 1);
            }
        }
        return destTime;
    }

    private static InsertionResult findBestInsertion(
        List<Integer> s, List<Integer> d, int n, int K,
        List<Integer> sourceTime, List<Integer> destTime,
        int[] timeMatrix, int[] distMatrix, int l) {

        int bestIncrement = Integer.MAX_VALUE;
        int bestSourceIdx = -1;
        int bestDestIdx = -1;

        for (int i = 0; i <= n; i++) {
            // source에서의 거리 증가량 계산
            int sourceIncrement = calculateSourceIncrement(s, i, n, distMatrix, l);

            for (int j = 0; j <= n; j++) {
                // 시간 제약 검사
                int bef = (i == n ? s.get(n) : s.get(n - 1));
                int aft = (j == 0 ? d.get(0) + K : d.get(0) + K);

                if (sourceTime.get(i) + timeMatrix[flattenIndex(bef, aft, l)] > destTime.get(j)) {
                    continue;
                }

                // dest에서의 거리 증가량 계산
                int destIncrement = calculateDestIncrement(d, j, n, K, distMatrix, l);

                // source와 dest 사이의 거리 증가량 계산
                int btwIncrement = distMatrix[flattenIndex(bef, aft, l)] -
                    distMatrix[flattenIndex(s.get(n - 1), d.get(0) + K, l)];

                int totalIncrement = sourceIncrement + destIncrement + btwIncrement;
                if (totalIncrement < bestIncrement) {
                    bestIncrement = totalIncrement;
                    bestSourceIdx = i;
                    bestDestIdx = j;
                }
            }
        }

        return new InsertionResult(bestIncrement, bestSourceIdx, bestDestIdx);
    }

    private static int calculateSourceIncrement(List<Integer> s, int i, int n, int[] distMatrix, int l) {
        int increment = 0;
        if (i != 0) {
            increment += distMatrix[flattenIndex(s.get(i - 1), s.get(i), l)];
        }
        if (i != n) {
            increment += distMatrix[flattenIndex(s.get(i), s.get(i + 1), l)];
        }
        if (i != 0 && i != n) {
            increment -= distMatrix[flattenIndex(s.get(i - 1), s.get(i + 1), l)];
        }
        return increment;
    }

    private static int calculateDestIncrement(List<Integer> d, int j, int n, int K,
        int[] distMatrix, int l) {
        int increment = 0;
        if (j != 0) {
            increment += distMatrix[flattenIndex(d.get(j - 1) + K, d.get(j) + K, l)];
        }
        if (j != n) {
            increment += distMatrix[flattenIndex(d.get(j) + K, d.get(j + 1) + K, l)];
        }
        if (j != 0 && j != n) {
            increment -= distMatrix[flattenIndex(d.get(j - 1) + K, d.get(j + 1) + K, l)];
        }
        return increment;
    }

    private static void updatePath(List<Integer> s, List<Integer> d,
        int sourceIdx, int destIdx, int n) {
        for (int i = n - 1; i >= sourceIdx; i--) {
            Collections.swap(s, i, i + 1);
        }
        for (int i = n - 1; i >= destIdx; i--) {
            Collections.swap(d, i, i + 1);
        }
    }

    private static int calculateTotalDistance(List<Integer> s, List<Integer> d,
        int n, int K, int[] distMatrix, int l) {
        int dist = 0;
        for (int i = 1; i <= n; i++) {
            dist += distMatrix[flattenIndex(s.get(i - 1), s.get(i), l)];
        }
        dist += distMatrix[flattenIndex(s.get(n), d.get(0) + K, l)];
        for (int i = 1; i <= n; i++) {
            dist += distMatrix[flattenIndex(d.get(i - 1) + K, d.get(i) + K, l)];
        }
        return dist;
    }

    public static Solution inputToSolution(
        int[] solEdgePtr,
        int[] solRiderPtr,
        RiderInfo riderInfo,
        int[] distMatrix,
        Map<String, Integer> ridersAvailable,
        int K) {

        int l = 2 * K;
        Solution solution = new Solution();

        for (int i = 0; i < K; i++) {
            boolean ok = true;
            for (int j = 0; j < K; j++) {
                if (solEdgePtr[flattenIndex(j, i, l)] != 0) {
                    ok = false;
                    break;
                }
            }
            if (!ok) {
                continue;
            }

            String riderType = RIDER.get(solRiderPtr[i]);
            List<Integer> rider = riderInfo.getInfo(riderType);

            List<Integer> merged = new ArrayList<>();
            merged.add(i);

            while (true) {
                int x = merged.get(merged.size() - 1);
                int y = -1;
                for (int j = 0; j < l; j++) {
                    if (solEdgePtr[flattenIndex(x, j, l)] != 0) {
                        y = j;
                        break;
                    }
                }
                if (y == -1) {
                    break;
                }
                merged.add(y);
            }

            int n = merged.size() / 2;
            List<Integer> source = new ArrayList<>(n);
            List<Integer> dest = new ArrayList<>(n);

            for (int j = 0; j < n; j++) {
                source.add(merged.get(j));
                dest.add(merged.get(j + n) - K);
            }

            double cost = getBundleCost(source, dest, rider, distMatrix, l);
            solution.append(new Bundle(riderType, cost, source, dest));
            ridersAvailable.merge(riderType, -1, Integer::sum);
        }

        return solution;
    }
}