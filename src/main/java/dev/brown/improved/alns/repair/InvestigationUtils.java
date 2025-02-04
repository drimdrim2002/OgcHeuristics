package dev.brown.improved.alns.repair;

import dev.brown.improved.alns.domain.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 주문 조사를 위한 유틸리티 클래스
 */
public class InvestigationUtils {
    private static final List<String> RIDER_TYPES = Arrays.asList("WALK", "BIKE", "CAR");

    /**
     * 주문 조사 수행
     */
    public static InvestigationResult investigate(
        int orderId,
        List<Integer> source,
        List<Integer> dest,
        RiderInfo riderInfo,
        int[] ordersPtr,
        int[] distMatPtr,
        int matrixLength) {

        InvestigationResult result = new InvestigationResult();

        // 각 라이더 타입에 대해 조사
        for (String riderType : RIDER_TYPES) {
            // 새로운 소스/데스티네이션 리스트 생성
            List<Integer> newSource = new ArrayList<>(source);
            List<Integer> newDest = new ArrayList<>(dest);

            // 새 주문 추가
            newSource.add(orderId);
            newDest.add(orderId);

            // 라이더 정보 가져오기
            Map.Entry<int[], List<Integer>> riderData = riderInfo.prepare(riderType);
            int[] timeMatrix = riderData.getKey();
            List<Integer> riderParams = riderData.getValue();

            // 용량 제약 확인
            double totalLoad = 0;
            for (int id : newSource) {
                totalLoad += ordersPtr[id * 3 + 2]; // load information
            }

            boolean isFeasible = totalLoad <= riderParams.get(0); // capacity check

            if (isFeasible) {
                // 시간 제약 확인
                boolean timeConstraintMet = checkTimeConstraints(
                    newSource, newDest, timeMatrix, ordersPtr, matrixLength);

                if (timeConstraintMet) {
                    // 비용 계산
                    double cost = calculateCost(
                        newSource, newDest, riderParams, timeMatrix, matrixLength);

                    // 결과 저장
                    result.setFeasibility(riderType, true);
                    result.setCost(riderType, cost);
                    result.setSource(riderType, newSource);
                    result.setDest(riderType, newDest);
                } else {
                    result.setFeasibility(riderType, false);
                }
            } else {
                result.setFeasibility(riderType, false);
            }
        }

        // 최적 순서 결정
        List<String> optimalOrder = determineOptimalOrder(result);
        result.setOptimalOrder(optimalOrder);

        return result;
    }

    /**
     * 시간 제약 확인
     */
    private static boolean checkTimeConstraints(
        List<Integer> source,
        List<Integer> dest,
        int[] timeMatrix,
        int[] ordersPtr,
        int matrixLength) {

        if (source.isEmpty()) return true;

        int currentTime = 0;
        int m = source.size();

        // 출발지 순회
        for (int i = 0; i < m; i++) {
            int id = source.get(i);
            int pickupTime = ordersPtr[id * 3]; // pickup time

            if (i > 0) {
                currentTime += timeMatrix[source.get(i-1) * matrixLength + id];
            }

            if (currentTime > pickupTime) {
                return false;
            }
            currentTime = Math.max(currentTime, pickupTime);
        }

        // 마지막 출발지에서 첫 도착지로
        currentTime += timeMatrix[source.get(m-1) * matrixLength + (dest.get(0) + matrixLength)];

        // 도착지 순회
        for (int i = 0; i < m; i++) {
            int id = dest.get(i);
            int deliveryTime = ordersPtr[id * 3 + 1]; // delivery time

            if (i > 0) {
                currentTime += timeMatrix[(dest.get(i-1) + matrixLength) * matrixLength
                    + (id + matrixLength)];
            }

            if (currentTime > deliveryTime) {
                return false;
            }
            currentTime = Math.max(currentTime, deliveryTime);
        }

        return true;
    }

    /**
     * 비용 계산
     */
    private static double calculateCost(
        List<Integer> source,
        List<Integer> dest,
        List<Integer> riderParams,
        int[] timeMatrix,
        int matrixLength) {

        if (source.isEmpty()) {
            return 0.0;
        }

        double cost = riderParams.get(1); // 기본 비용
        int m = source.size();

        // 출발지 간 이동 비용
        for (int i = 0; i < m - 1; i++) {
            cost += riderParams.get(2) *
                timeMatrix[source.get(i) * matrixLength + source.get(i + 1)] / 100.0;
        }

        // 마지막 출발지에서 첫 도착지까지의 비용
        cost += riderParams.get(2) *
            timeMatrix[source.get(m - 1) * matrixLength + (dest.get(0) + matrixLength)] / 100.0;

        // 도착지 간 이동 비용
        for (int i = 0; i < m - 1; i++) {
            cost += riderParams.get(2) *
                timeMatrix[(dest.get(i) + matrixLength) * matrixLength +
                    (dest.get(i + 1) + matrixLength)] / 100.0;
        }

        return cost;
    }

    public static InvestigationResult investigateOld(
        int orderId,
        List<Integer> source,
        List<Integer> dest,
        RiderInfo riderInfo,
        int[] ordersPtr,
        int[] distMatPtr,
        int matrixLength) {

        InvestigationResult result = new InvestigationResult();

        for (String riderType : RIDER_TYPES) {
            List<Integer> newSource = new ArrayList<>(source);
            List<Integer> newDest = new ArrayList<>(dest);

            if (orderId != -1) {
                newSource.add(orderId);
                newDest.add(orderId);
            }

            Map.Entry<int[], List<Integer>> riderData = riderInfo.prepare(riderType);
            int[] timeMatrix = riderData.getKey();
            List<Integer> riderParams = riderData.getValue();

            // 용량 제약 확인
            double totalLoad = 0;
            for (int id : newSource) {
                totalLoad += ordersPtr[id * 3 + 2];
            }

            boolean isFeasible = totalLoad <= riderParams.get(0);

            if (isFeasible) {
                // 시간 제약 확인 - 이전 버전의 로직 사용
                boolean timeConstraintMet = checkTimeConstraintsOld(
                    newSource, newDest, timeMatrix, ordersPtr, matrixLength);

                if (timeConstraintMet) {
                    // 비용 계산 - 이전 버전의 로직 사용
                    double cost = calculateCostOld(
                        newSource, newDest, riderParams, timeMatrix, matrixLength);

                    result.setFeasibility(riderType, true);
                    result.setCost(riderType, cost);
                    result.setSource(riderType, newSource);
                    result.setDest(riderType, newDest);
                } else {
                    result.setFeasibility(riderType, false);
                }
            } else {
                result.setFeasibility(riderType, false);
            }
        }

        List<String> optimalOrder = determineOptimalOrder(result);
        result.setOptimalOrder(optimalOrder);

        return result;
    }

    /**
     * 이전 버전의 시간 제약 확인
     */
    private static boolean checkTimeConstraintsOld(
        List<Integer> source,
        List<Integer> dest,
        int[] timeMatrix,
        int[] ordersPtr,
        int matrixLength) {

        if (source.isEmpty()) return true;

        int currentTime = 0;
        int m = source.size();

        // 출발지 체크
        for (int i = 0; i < m; i++) {
            if (i > 0) {
                currentTime += timeMatrix[source.get(i-1) * matrixLength + source.get(i)];
            }
            int pickupTime = ordersPtr[source.get(i) * 3];
            if (currentTime > pickupTime) return false;
            currentTime = Math.max(currentTime, pickupTime);
        }

        // 마지막 출발지에서 첫 도착지로
        currentTime += timeMatrix[source.get(m-1) * matrixLength + (dest.get(0) + matrixLength)];

        // 도착지 체크
        for (int i = 0; i < m; i++) {
            if (i > 0) {
                currentTime += timeMatrix[(dest.get(i-1) + matrixLength) * matrixLength
                    + (dest.get(i) + matrixLength)];
            }
            int deliveryTime = ordersPtr[dest.get(i) * 3 + 1];
            if (currentTime > deliveryTime) return false;
            currentTime = Math.max(currentTime, deliveryTime);
        }

        return true;
    }

    /**
     * 이전 버전의 비용 계산
     */
    private static double calculateCostOld(
        List<Integer> source,
        List<Integer> dest,
        List<Integer> riderParams,
        int[] timeMatrix,
        int matrixLength) {

        if (source.isEmpty()) return 0.0;

        double cost = riderParams.get(1);
        int m = source.size();

        // 출발지 간 이동 비용
        for (int i = 1; i < m; i++) {
            cost += riderParams.get(2) *
                timeMatrix[source.get(i-1) * matrixLength + source.get(i)] / 100.0;
        }

        // 마지막 출발지에서 첫 도착지까지
        cost += riderParams.get(2) *
            timeMatrix[source.get(m-1) * matrixLength + (dest.get(0) + matrixLength)] / 100.0;

        // 도착지 간 이동 비용
        for (int i = 1; i < m; i++) {
            cost += riderParams.get(2) *
                timeMatrix[(dest.get(i-1) + matrixLength) * matrixLength
                    + (dest.get(i) + matrixLength)] / 100.0;
        }

        return cost;
    }


    /**
     * 최적 라이더 순서 결정
     */
    private static List<String> determineOptimalOrder(InvestigationResult result) {
        return RIDER_TYPES.stream()
            .filter(type -> result.getFeasibility(type))
            .sorted(Comparator.comparingDouble(type -> result.getCost(type)))
            .collect(Collectors.toList());
    }
}