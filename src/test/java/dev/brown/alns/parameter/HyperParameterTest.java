package dev.brown.alns.parameter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class HyperParameterTest {
    @Test
    @DisplayName("기본 하이퍼파라미터 값 검증")
    void testDefaultValues() {
        HyperParameter params = HyperParameter.getDefault();

        assertEquals(0.4, params.destroyRatio());
        assertEquals(100, params.iterationsPerSegment());
        assertEquals(100.0, params.initialTemperature());
        assertEquals(0.99, params.coolingRate());
        assertEquals(0.01, params.minTemperature());
        assertEquals(0.1, params.reactionFactor());
        assertEquals(33.0, params.newBestScore());
        assertEquals(20.0, params.betterScore());
        assertEquals(10.0, params.acceptedScore());
    }

    @Nested
    @DisplayName("빌더 유효성 검증 테스트")
    class BuilderValidationTests {

        @Test
        @DisplayName("유효한 파라미터로 빌더 생성 성공")
        void testValidBuilder() {
            HyperParameter params = new HyperParameter.Builder()
                .destroyRatio(0.3)
                .initialTemperature(200.0)
                .coolingRate(0.98)
                .build();

            assertEquals(0.3, params.destroyRatio());
            assertEquals(200.0, params.initialTemperature());
            assertEquals(0.98, params.coolingRate());
        }

        @ParameterizedTest
        @ValueSource(doubles = {-0.1, 0.0, 1.0, 1.1})
        @DisplayName("잘못된 destroyRatio 값 검증")
        void testInvalidDestroyRatio(double invalidRatio) {
            var builder = new HyperParameter.Builder();
            assertThrows(IllegalArgumentException.class,
                () -> builder.destroyRatio(invalidRatio));
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, 0})
        @DisplayName("잘못된 iterationsPerSegment 값 검증")
        void testInvalidIterations(int invalidIterations) {
            var builder = new HyperParameter.Builder();
            assertThrows(IllegalArgumentException.class,
                () -> builder.iterationsPerSegment(invalidIterations));
        }

        @Test
        @DisplayName("잘못된 온도 관계 검증")
        void testInvalidTemperatureRelation() {
            var builder = new HyperParameter.Builder()
                .initialTemperature(10.0)
                .minTemperature(20.0);

            assertThrows(IllegalStateException.class, builder::build);
        }

        @Test
        @DisplayName("잘못된 점수 관계 검증")
        void testInvalidScoreRelation() {
            var builder = new HyperParameter.Builder()
                .newBestScore(10.0)
                .betterScore(20.0);

            assertThrows(IllegalStateException.class, builder::build);
        }
    }

    @Nested
    @DisplayName("파라미터 범위 테스트")
    class ParameterRangeTests {

        @Test
        @DisplayName("온도 파라미터 범위 검증")
        void testTemperatureRanges() {
            assertThrows(IllegalArgumentException.class,
                () -> new HyperParameter.Builder().initialTemperature(-1.0));

            assertThrows(IllegalArgumentException.class,
                () -> new HyperParameter.Builder().minTemperature(-0.1));

            assertThrows(IllegalArgumentException.class,
                () -> new HyperParameter.Builder().coolingRate(1.1));
        }

        @Test
        @DisplayName("점수 파라미터 범위 검증")
        void testScoreRanges() {
            assertThrows(IllegalArgumentException.class,
                () -> new HyperParameter.Builder().newBestScore(-1.0));

            assertThrows(IllegalArgumentException.class,
                () -> new HyperParameter.Builder().betterScore(0.0));

            assertThrows(IllegalArgumentException.class,
                () -> new HyperParameter.Builder().acceptedScore(-0.1));
        }
    }

    @Nested
    @DisplayName("불변성 테스트")
    class ImmutabilityTests {

        @Test
        @DisplayName("동일한 값으로 생성된 객체 비교")
        void testEquality() {
            HyperParameter params1 = new HyperParameter.Builder()
                .destroyRatio(0.3)
                .initialTemperature(200.0)
                .build();

            HyperParameter params2 = new HyperParameter.Builder()
                .destroyRatio(0.3)
                .initialTemperature(200.0)
                .build();

            assertEquals(params1, params2);
            assertEquals(params1.hashCode(), params2.hashCode());
        }
    }

    @Test
    @DisplayName("모든 파라미터를 설정한 빌더 테스트")
    void testCompleteBuilder() {
        HyperParameter params = new HyperParameter.Builder()
            .destroyRatio(0.3)
            .iterationsPerSegment(150)
            .initialTemperature(200.0)
            .coolingRate(0.98)
            .minTemperature(0.005)
            .reactionFactor(0.15)
            .newBestScore(40.0)
            .betterScore(25.0)
            .acceptedScore(15.0)
            .build();

        assertEquals(0.3, params.destroyRatio());
        assertEquals(150, params.iterationsPerSegment());
        assertEquals(200.0, params.initialTemperature());
        assertEquals(0.98, params.coolingRate());
        assertEquals(0.005, params.minTemperature());
        assertEquals(0.15, params.reactionFactor());
        assertEquals(40.0, params.newBestScore());
        assertEquals(25.0, params.betterScore());
        assertEquals(15.0, params.acceptedScore());
    }
}
