package dev.brown.alns.parameter;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class TimeManagerTest {

    @Test
    @DisplayName("120초 미만의 시간 제한에 대한 테스트")
    void testTimeLimitLessThan120() {
        // given
        int K = 100;
        double timeLimit = 60.0;

        // when
        var config = TimeManager.getGeneralTimeListAndAnneal(K, timeLimit);

        // then
        var timeList = config.timeList();
        assertEquals(1, timeList.size());
        assertEquals(30.0, timeList.get(0).alnsTime());
        assertEquals(30.0, timeList.get(0).spTime());
        assertEquals(1.0, config.spareTime());
    }

    @Test
    @DisplayName("120초에서 180초 사이의 시간 제한에 대한 테스트")
    void testTimeLimitBetween120And180() {
        // given
        int K = 100;
        double timeLimit = 150.0;

        // when
        var config = TimeManager.getGeneralTimeListAndAnneal(K, timeLimit);

        // then
        var timeList = config.timeList();
        assertEquals(2, timeList.size());
        assertEquals(90.0, timeList.get(0).alnsTime());  // 150 - 60
        assertEquals(30.0, timeList.get(0).spTime());
        assertEquals(15.0, timeList.get(1).alnsTime());
        assertEquals(15.0, timeList.get(1).spTime());
    }

    @Test
    @DisplayName("180초 초과 시간 제한에 대한 테스트")
    void testTimeLimitOver180() {
        // given
        int K = 100;
        double timeLimit = 240.0;

        // when
        var config = TimeManager.getGeneralTimeListAndAnneal(K, timeLimit);

        // then
        var timeList = config.timeList();
        assertEquals(3, timeList.size());
        assertEquals(135.0, timeList.get(0).alnsTime());  // 240 - 105
        assertEquals(45.0, timeList.get(0).spTime());
        assertEquals(15.0, timeList.get(1).alnsTime());
        assertEquals(15.0, timeList.get(1).spTime());
        assertEquals(15.0, timeList.get(2).alnsTime());
        assertEquals(15.0, timeList.get(2).spTime());
    }

    @ParameterizedTest
    @DisplayName("대규모 문제(K >= 1000)에 대한 테스트")
    @CsvSource({
        "1000, 180, 3.5",
        "2000, 180, 3.5",
        "3000, 240, 3.5"
    })
    void testLargeScaleProblem(int K, double timeLimit, double expectedSpareTime) {
        // when
        var config = TimeManager.getGeneralTimeListAndAnneal(K, timeLimit);

        // then
        assertEquals(expectedSpareTime, config.spareTime());
    }

    @Test
    @DisplayName("매우 큰 문제(K >= 2000)에 대한 시간 조정 테스트")
    void testVeryLargeScaleProblem() {
        // given
        int K = 2000;
        double timeLimit = 240.0;

        // when
        var config = TimeManager.getGeneralTimeListAndAnneal(K, timeLimit);

        // then
        var timeList = config.timeList();
        assertEquals(130.0, timeList.get(0).alnsTime());  // (240 - 105) - 5
        assertEquals(40.0, timeList.get(0).spTime());     // 45 - 5
    }

    @ParameterizedTest
    @DisplayName("어닐링 비율 계산 테스트")
    @MethodSource("provideTimeValuesForAnnealing")
    void testAnnealingRateCalculation(double timeLimit, double expectedRate) {
        // when
        var config = TimeManager.getGeneralTimeListAndAnneal(100, timeLimit);

        // then
        assertEquals(expectedRate, config.annealingRate(), 0.0001);
    }

    private static Stream<Arguments> provideTimeValuesForAnnealing() {
        return Stream.of(
            // 60초 케이스: timelimit/2 = 30초 ALNS 시간
            Arguments.of(60.0, Math.min(1.12, Math.pow(6.0, 1.0/30.0))),  // ≈ 1.0615449167090512

            // 150초 케이스: timelimit-60 = 90초 ALNS 시간
            Arguments.of(150.0, Math.min(1.12, Math.pow(6.0, 1.0/90.0))),

            // 240초 케이스: timelimit-105 = 135초 ALNS 시간
            Arguments.of(240.0, Math.min(1.12, Math.pow(6.0, 1.0/135.0)))
        );
    }

    @Test
    @DisplayName("반환된 TimeList가 불변인지 테스트")
    void testTimeListImmutability() {
        // given
        var config = TimeManager.getGeneralTimeListAndAnneal(100, 240.0);
        var timeList = config.timeList();

        // then
        assertThrows(UnsupportedOperationException.class, () ->
            timeList.add(new TimeManager.TimeSegment(10.0, 10.0))
        );
    }
}