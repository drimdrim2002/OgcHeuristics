package dev.brown.improved.alns.parameter;

/**
 * 시간 관련 설정을 담는 클래스
 */
public class TimeConfig {
    public final double[][] timeList;           // ALNS와 SP의 시간 할당 리스트
    public final double annealerModifyRatio;    // 어닐링 수정 비율
    public final double spareTime;              // 여유 시간

    /**
     * 생성자
     * @param timeList ALNS와 SP의 시간 할당 리스트 [[ALNS시간, SP시간], ...]
     * @param annealerModifyRatio 어닐링 수정 비율
     * @param spareTime 여유 시간
     */
    public TimeConfig(double[][] timeList, double annealerModifyRatio, double spareTime) {
        this.timeList = deepCopyTimeList(timeList);
        this.annealerModifyRatio = annealerModifyRatio;
        this.spareTime = spareTime;
    }

    /**
     * timeList의 깊은 복사를 수행
     */
    private static double[][] deepCopyTimeList(double[][] original) {
        double[][] copy = new double[original.length][];
        for (int i = 0; i < original.length; i++) {
            copy[i] = original[i].clone();
        }
        return copy;
    }

    /**
     * 일반적인 시간 설정을 생성
     */
    public static TimeConfig createGeneralConfig(int K, double timeLimit) {
        double[][] timeList;
        double annealerModifyRatio;
        double spareTime;

        if (timeLimit <= 30) {
            // 짧은 시간 제한
            timeList = new double[][]{
                {timeLimit * 0.7, timeLimit * 0.2}
            };
            annealerModifyRatio = 1.1;
            spareTime = timeLimit * 0.1;
        } else if (timeLimit <= 180) {
            // 중간 시간 제한
            timeList = new double[][]{
                {timeLimit * 0.4, timeLimit * 0.2},
                {timeLimit * 0.2, timeLimit * 0.1}
            };
            annealerModifyRatio = 1.05;
            spareTime = timeLimit * 0.1;
        } else {
            // 긴 시간 제한
            timeList = new double[][]{
                {timeLimit * 0.3, timeLimit * 0.15},
                {timeLimit * 0.2, timeLimit * 0.15},
                {timeLimit * 0.1, timeLimit * 0.1}
            };
            annealerModifyRatio = 1.03;
            spareTime = timeLimit * 0.1;
        }

        return new TimeConfig(timeList, annealerModifyRatio, spareTime);
    }

    /**
     * 총 ALNS 시간 계산
     */
    public double getTotalAlnsTime() {
        double total = 0;
        for (double[] times : timeList) {
            total += times[0];
        }
        return total;
    }

    /**
     * 총 SP 시간 계산
     */
    public double getTotalSpTime() {
        double total = 0;
        for (double[] times : timeList) {
            total += times[1];
        }
        return total;
    }

    /**
     * 반복 횟수 반환
     */
    public int getIterationCount() {
        return timeList.length;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("TimeConfig{timeList=[");
        for (double[] times : timeList) {
            sb.append(String.format("[%.1f, %.1f], ", times[0], times[1]));
        }
        sb.setLength(sb.length() - 2);  // 마지막 ", " 제거
        sb.append(String.format("], ratio=%.3f, spare=%.1f}",
            annealerModifyRatio, spareTime));
        return sb.toString();
    }
}