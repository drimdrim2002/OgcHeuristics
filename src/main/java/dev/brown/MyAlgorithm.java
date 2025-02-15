package dev.brown;

import dev.brown.domain.Order;
import dev.brown.domain.Rider;
import dev.brown.improved.alns.Runner;
import dev.brown.improved.alns.domain.Bundle;
import dev.brown.improved.alns.domain.GurobiInput;
import dev.brown.improved.alns.domain.SPResult;
import dev.brown.improved.alns.domain.Solution;
import dev.brown.improved.alns.domain.SolutionFormat;
import dev.brown.improved.alns.parameter.TimeAndAnnealData;
import dev.brown.solver.SpSolver;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyAlgorithm {

    private static final int VERBOSE = 2;  // 0: no logging, 1: compact logging, 2: all logging
    private static final int SEED = 1;

    /**
     * 주어진 K와 timeLimit 값에 따라 문제 번호를 반환합니다.
     *
     * @param K         주문 개수
     * @param timeLimit 시간 제한 (초)
     * @return 문제 번호 (0-10)
     */
    private static int getProbNum(int K, int timeLimit) {
        if (K == 2000 && timeLimit == 300) {
            return 1;
        }
        if (K == 2000 && timeLimit == 480) {
            return 2;
        }
        if (K == 1000 && timeLimit == 60) {
            return 3;
        }
        if (K == 1000 && timeLimit == 180) {
            return 4;
        }
        if (K == 1000 && timeLimit == 300) {
            return 5;
        }
        if (K == 750 && timeLimit == 30) {
            return 6;
        }
        if (K == 750 && timeLimit == 60) {
            return 7;
        }
        if (K == 500 && timeLimit == 15) {
            return 8;
        }
        if (K == 500 && timeLimit == 30) {
            return 9;
        }
        if (K == 300 && timeLimit == 15) {
            return 10;
        }
        return 0;
    }

    /**
     * 문제 번호에 따른 시간 리스트와 annealing 파라미터를 반환합니다.
     *
     * @param probNum   문제 번호
     * @param K         주문 개수
     * @param timeLimit 시간 제한 (초)
     * @return TimeAndAnnealData 객체 (timeList, annealerModifyRatio, spareTime)
     */
    private static TimeAndAnnealData getTimeListAndAnneal(int probNum, int K, int timeLimit) {
        if (probNum == 0) {
            return getGeneralTimeListAndAnneal(K, timeLimit);
        }

        Map<Integer, TimeAndAnnealData> dataMap = new HashMap<>();

        // 각 문제 번호별 데이터 정의
        dataMap.put(1, new TimeAndAnnealData(
            new double[][]{{195, 40}, {10, 15}, {15, 15}},
            1.003,
            3.5
        ));

        dataMap.put(2, new TimeAndAnnealData(
            new double[][]{{240, 60}, {25, 30}, {25, 30}, {30, 30}},
            1.03,
            3.5
        ));

        dataMap.put(3, new TimeAndAnnealData(
            new double[][]{{25, 15}, {10, 10}},
            1.12,
            1.0
        ));

        dataMap.put(4, new TimeAndAnnealData(
            new double[][]{{15, 15}, {15, 15}, {15, 15}, {15, 15}, {15, 15}, {15, 15}},
            1.04,
            3.0
        ));

        dataMap.put(5, new TimeAndAnnealData(
            new double[][]{{180, 60}, {15, 15}, {15, 15}},
            1.03,
            3.0
        ));

        dataMap.put(6, new TimeAndAnnealData(
            new double[][]{{6.5, 8.5}, {6, 9}},
            1.07,
            0.5
        ));

        dataMap.put(7, new TimeAndAnnealData(
            new double[][]{{20, 20}, {8, 12}},
            1.1,
            0.5
        ));

        dataMap.put(8, new TimeAndAnnealData(
            new double[][]{{6.2, 8.8}},
            1.1,
            0.0
        ));

        dataMap.put(9, new TimeAndAnnealData(
            new double[][]{{7.5, 7.5}, {6.5, 8.5}},
            1.12,
            0.0
        ));

        dataMap.put(10, new TimeAndAnnealData(
            new double[][]{{9.5, 5.5}},
            1.1,
            0.0
        ));

        return dataMap.get(probNum);
    }

    /**
     * 일반적인 경우의 시간 리스트와 annealing 파라미터를 계산합니다. 이 메서드는 별도로 구현되어야 합니다.
     */
    private static TimeAndAnnealData getGeneralTimeListAndAnneal(int K, int timeLimit) {
        double[][] timeList;

        // 시간 리스트 설정
        if (timeLimit < 120) {
            // 기본 케이스: 시간을 반으로 나눔
            timeList = new double[][]{{timeLimit / 2.0, timeLimit / 2.0}};
        } else if (timeLimit <= 180) {
            // 120초 ~ 180초 케이스
            timeList = new double[][]{
                {timeLimit - 60.0, 30.0},
                {15.0, 15.0}
            };
        } else {
            // 180초 초과 케이스
            timeList = new double[][]{
                {timeLimit - 105.0, 45.0},
                {15.0, 15.0},
                {15.0, 15.0}
            };
        }

        // Annealing 파라미터 계산: min(1.12, (6 ** 1/timelist[0][0]))
        double anneal = Math.min(1.12, Math.pow(6.0, 1.0 / timeList[0][0]));

        // Spare time 설정
        double spareTime = 1.0;
        if (K >= 1000 && timeLimit >= 180) {
            spareTime = 3.5;
        }

        // 대규모 문제(K >= 2000)에 대한 시간 조정
        if (K >= 2000) {
            timeList[0][0] -= 5.0;
            timeList[0][1] -= 5.0;
        }

        return new TimeAndAnnealData(timeList, anneal, spareTime);
    }

    /**
     * 문제 번호에 따라 power 사용 여부를 반환합니다.
     *
     * @param probNum 문제 번호
     * @return power 사용 여부
     */
    private static boolean getUsePower(int probNum) {
        return probNum != 3 && probNum != 4;
    }

    /**
     * 문제 번호에 따라 가중치 값들을 반환합니다.
     *
     * @param probNum 문제 번호
     * @return [wt_weight, tw_weight] 가중치 배열
     */
    private static double[] getWtTw(int probNum) {
        if (probNum == 3 || probNum == 10) {
            return new double[]{0.75, 0.75};
        } else {
            return new double[]{0.2, 1.0};
        }
    }

    /**
     * 문제 번호에 따라 worst 사용 여부를 반환합니다.
     *
     * @param probNum 문제 번호
     * @return worst 사용 여부
     */
    private static boolean getUseWorst(int probNum) {
        return probNum != 5;
    }

    /**
     * 문제 번호에 따라 초기 휴리스틱 시간을 반환합니다.
     *
     * @param probNum 문제 번호
     * @return 초기 휴리스틱 시간
     */
    private static int getFirstHeurTime(int probNum) {
        return probNum == 5 ? 3 : 0;
    }

    /**
     * 문제 번호에 따라 이전 버전 사용 여부를 반환합니다.
     *
     * @param probNum 문제 번호
     * @return 이전 버전 사용 여부
     */
    private static boolean getUseOldVersion(int probNum) {
        return probNum == 9;
    }

    /**
     * 알고리즘 파라미터들을 출력합니다.
     */
    private static void printParameters(double alnsTime, double annealerModifyRatio, int seed,
        int[][] solEdge, int[] solRider, List<Bundle> initBundle,
        int[][] orders, int[][] walkT, int[] walkInfo,
        int[][] bikeT, int[] bikeInfo, int[][] carT, int[] carInfo,
        Map<String, Integer> ridersAvailable, int[][] distMat,
        Map<String, Object> hParam, boolean usePower, int verbose) {

        System.out.println("==================== Print Parameters start =========================");
        System.out.println("ALNS_TIME: " + alnsTime);
        System.out.println("annealer_modify_ratio: " + annealerModifyRatio);
        System.out.println("seed: " + seed);
        System.out.println("sol_edge: " + Arrays.deepToString(solEdge));
        System.out.println("sol_rider: " + Arrays.toString(solRider));
        System.out.println("init_bundle: " + initBundle);
        System.out.println("orders: " + Arrays.deepToString(orders));
        System.out.println("walk_T: " + Arrays.deepToString(walkT));
        System.out.println("walk_info: " + Arrays.toString(walkInfo));
        System.out.println("bike_T: " + Arrays.deepToString(bikeT));
        System.out.println("bike_info: " + Arrays.toString(bikeInfo));
        System.out.println("car_T: " + Arrays.deepToString(carT));
        System.out.println("car_info: " + Arrays.toString(carInfo));
        System.out.println("riders_available: " + ridersAvailable);
        System.out.println("dist_mat: " + Arrays.deepToString(distMat));
        System.out.println("hparam: " + hParam);
        System.out.println("use_power: " + usePower);
        System.out.println("verbose: " + verbose);
        System.out.println("==================== Print Parameters end =========================");
    }


    /**
     * 메인 알고리즘을 실행합니다.
     *
     * @param K         주문 개수
     * @param allOrders 모든 주문 목록
     * @param allRiders 모든 라이더 목록
     * @param distMat   거리 행렬
     * @param timeLimit 시간 제한 (초)
     * @param hParam    하이퍼파라미터
     * @return 최적화된 해결책
     */
    public Solution algorithm(int K, List<Order> allOrders, List<Rider> allRiders,
        int[][] distMat, int timeLimit, Map<String, Double> hParam) {

        long startTime = System.currentTimeMillis();

        // 하이퍼파라미터 초기화
        if (hParam == null) {
            hParam = new HashMap<>();
        }

        // 문제 번호와 파라미터 설정
        int probNum = getProbNum(K, timeLimit);
        TimeAndAnnealData timeData = getTimeListAndAnneal(probNum, K, timeLimit);

        // 서버 CPU timelimit 조정
        if (K >= 2000) {
            timeLimit -= 10;
        }

        // 파라미터 설정
        boolean usePower = getUsePower(probNum);
        double[] wtTw = getWtTw(probNum);
        hParam.put("wt_weight", wtTw[0]);
        hParam.put("tw_weight", wtTw[1]);

        boolean useWorst = getUseWorst(probNum);
        hParam.put("use_worst", useWorst ? 1.0 : 0.0);

        int firstHeurTime = getFirstHeurTime(probNum);

        boolean useOld = getUseOldVersion(probNum);
        hParam.put("use_old", useOld ? 1.0 : 0.0);

        // 데이터 준비
        int[][] orders = new int[allOrders.size()][3];
        for (int i = 0; i < allOrders.size(); i++) {
            Order order = allOrders.get(i);
            orders[i][0] = order.getReadyTime();
            orders[i][1] = order.getDeadline();
            orders[i][2] = order.getVolume();
        }

        Map<String, int[][]> riderTimes = new HashMap<>();
        Map<String, Integer> ridersAvailable = new HashMap<>();
        Map<String, List<Integer>> riderInfos = new HashMap<>();

        for (Rider rider : allRiders) {
            String type = rider.getType();

            // rider time update
            if (!riderTimes.containsKey(type)) {
                int[][] timeMatrix = calculateTimeMatrix(distMat, rider.getSpeed(), rider.getServiceTime());
                riderTimes.put(type, timeMatrix);
            }

            Integer prevQty = ridersAvailable.getOrDefault(type, 0);
            ridersAvailable.put(type, prevQty + 1);

            riderInfos.put(type, Arrays.asList(
                rider.getCapacity(),
                rider.getFixedCost(),
                rider.getVarCost()
            ));
        }

        // 초기 솔루션 배열 생성
        int[][] solEdge = new int[2 * K][2 * K];
        int[] solRider = new int[K];
        List<Bundle> initBundle = new ArrayList<>();

        // 로깅
        if (VERBOSE >= 1) {
            System.out.println("############################# Start #############################");
            System.out.println("timeList=" + Arrays.deepToString(timeData.getTimeList()) +
                ", modify_ratio: " + timeData.getAnnealerModifyRatio());
        }

        Solution bestSol = null;
        double[][] timeList = timeData.getTimeList();

        // 메인 반복문
        for (int i = 0; i < timeList.length; i++) {
            if (VERBOSE >= 1) {
                System.out.println("################## iter " + (i + 1) + " #######################");
            }

            // 시간 정보
            double alnsTime = timeList[i][0];
            double spTime = timeList[i][1];

            // 마지막 반복에서 남은 시간 조정
            if (i == timeList.length - 1) {
                double leftTime = timeLimit - (System.currentTimeMillis() - startTime) / 1000.0;
                spTime = timeList[i][1];
                alnsTime = leftTime - spTime - timeData.getSpareTime();
            }

            // ALNS 실행
            if (VERBOSE >= 1) {
                System.out.println("entering ALNS, spent time: " +
                    (System.currentTimeMillis() - startTime) / 1000.0);
            }

            Map.Entry<SolutionFormat, GurobiInput> result = Runner.run(
                alnsTime,
                (float) timeData.getAnnealerModifyRatio(),
                SEED,
                solEdge,
                solRider,
                initBundle,
                orders,
                riderTimes.get("WALK"),
                riderInfos.get("WALK"),
                riderTimes.get("BIKE"),
                riderInfos.get("BIKE"),
                riderTimes.get("CAR"),
                riderInfos.get("CAR"),
                ridersAvailable,
                distMat,
                hParam,
                usePower,
                VERBOSE == 2
            );

            // SP 실행
            if (VERBOSE >= 1) {
                System.out.println("entering SP spent time " +
                    (System.currentTimeMillis() - startTime) / 1000.0);
            }

            int heurTime = (i == 0) ? firstHeurTime : -1;
            SPResult spResult = SpSolver.solveSP(
                result.getValue().edgeMatrix(),
                result.getValue().riderTypes(),
                ridersAvailable,
                result.getValue().bundles(),
                spTime,
                VERBOSE,
                distMat,
                riderTimes,
                riderInfos,
                heurTime
            );

            // 결과 업데이트
            bestSol = spResult.getBestSol();
            solEdge = spResult.getSolEdge();
            solRider = spResult.getSolRider();
            initBundle = spResult.getInitBundle();
        }

        return bestSol;
    }

    private int[][] calculateTimeMatrix(int[][] distMat, double speed, int serviceTime) {
        int n = distMat.length;
        int[][] result = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                result[i][j] = (int) Math.round(distMat[i][j] / speed + serviceTime);
            }
        }
        return result;
    }


}


