package dev.brown.alns.parameter;

import java.util.ArrayList;
import java.util.List;

public final class TimeManager {

    public record TimeSegment(double alnsTime, double spTime) {}

    public record TimeConfiguration(
        List<TimeSegment> timeList,
        double annealingRate,
        double spareTime
    ) {}

    private TimeManager() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    /**
     * 문제 크기와 제한 시간에 따라 시간 설정을 계산합니다.
     * @param K 문제 크기 (주문 수)
     * @param timeLimit 전체 제한 시간
     * @return TimeConfiguration 객체
     */
    public static TimeConfiguration getGeneralTimeListAndAnneal(int K, double timeLimit) {
        var timeList = new ArrayList<TimeSegment>();
        double annealingRate;
        double spareTime = 1.0;

        // 기본 시간 분배
        if (timeLimit <= 120) {
            timeList.add(new TimeSegment(timeLimit/2, timeLimit/2));
        } else if (timeLimit <= 180) {
            timeList.add(new TimeSegment(timeLimit - 60, 30));
            timeList.add(new TimeSegment(15, 15));
        } else {
            timeList.add(new TimeSegment(timeLimit - 105, 45));
            timeList.add(new TimeSegment(15, 15));
            timeList.add(new TimeSegment(15, 15));
        }

        // 어닐링 비율 계산
        annealingRate = Math.min(1.12, Math.pow(6, 1.0/timeList.getFirst().alnsTime()));

        // 대규모 문제에 대한 예외 처리
        if (K >= 1000 && timeLimit >= 180) {
            spareTime = 3.5;
        }

        if (K >= 2000) {
            var firstSegment = timeList.getFirst();
            timeList.set(0, new TimeSegment(
                firstSegment.alnsTime() - 5,
                firstSegment.spTime() - 5
            ));
        }

        return new TimeConfiguration(List.copyOf(timeList), annealingRate, spareTime);
    }
}