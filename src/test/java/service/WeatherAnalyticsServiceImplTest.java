package service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import svs.weatherapp.analytics.WeatherAnalyticsServiceImpl;
import svs.weatherapp.dto.WeatherAnalyticsDto;
import svs.weatherapp.dto.WeatherSummaryDto;
import svs.weatherapp.model.City;
import svs.weatherapp.model.Weather;
import svs.weatherapp.model.WeatherCondition;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class WeatherAnalyticsServiceImplTest {

    private WeatherAnalyticsServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new WeatherAnalyticsServiceImpl();
    }

    @Test
    void processWeather_shouldAccumulateStatsAndCounts() {
        LocalDateTime now = LocalDateTime.now();

        Weather sunnyWeather = Weather.builder()
                .city(City.MOSCOW)
                .temperature(20.5)
                .condition(WeatherCondition.SUNNY)
                .date(now)
                .build();

        Weather rainyWeather = Weather.builder()
                .city(City.MOSCOW)
                .temperature(15.0)
                .condition(WeatherCondition.RAINY)
                .date(now)
                .build();

        Weather cloudyWeather = Weather.builder()
                .city(City.PITER)
                .temperature(10.0)
                .condition(WeatherCondition.CLOUDY)
                .date(now)
                .build();

        service.processWeather(sunnyWeather);
        service.processWeather(rainyWeather);
        service.processWeather(cloudyWeather);

        List<WeatherAnalyticsDto> stats = service.getCityStats();

        WeatherAnalyticsDto moscowStats = stats.stream()
                .filter(dto -> "MOSCOW".equals(dto.getCity()))
                .findFirst()
                .orElseThrow();

        assertEquals(1, moscowStats.getSunnyDays());
        assertEquals(1, moscowStats.getRainyDays());
        assertEquals(0, moscowStats.getCloudyDays());
        assertEquals(20.5, moscowStats.getMaxTemperature());
        assertEquals(15.0, moscowStats.getMinTemperature());
        assertEquals((20.5 + 15.0) / 2, moscowStats.getAverageTemperature(), 0.0001);

        WeatherAnalyticsDto piterStats = stats.stream()
                .filter(dto -> "PITER".equals(dto.getCity()))
                .findFirst()
                .orElseThrow();

        assertEquals(0, piterStats.getSunnyDays());
        assertEquals(0, piterStats.getRainyDays());
        assertEquals(1, piterStats.getCloudyDays());
        assertEquals(10.0, piterStats.getMaxTemperature());
        assertEquals(10.0, piterStats.getMinTemperature());
        assertEquals(10.0, piterStats.getAverageTemperature(), 0.0001);
    }

    @Test
    void getCityStats_withNoData_returnsZeroCountsAndDefaults() {
        List<WeatherAnalyticsDto> stats = service.getCityStats();
        assertEquals(City.values().length, stats.size());
        stats.forEach(dto -> {
            assertNotNull(dto.getCity());
            assertEquals(0, dto.getSunnyDays());
            assertEquals(0, dto.getRainyDays());
            assertEquals(0, dto.getCloudyDays());
            assertEquals(0.0, dto.getMinTemperature());
            assertEquals(0.0, dto.getMaxTemperature());
            assertEquals(0.0, dto.getAverageTemperature());
        });
    }

    @Test
    void getSummaryForPeriod_withEmptyPeriod_returnsDefaults() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();

        WeatherSummaryDto summary = service.getSummaryForPeriod(start, end);

        assertEquals("N/A", summary.getMostRainyCity());
        assertEquals("N/A", summary.getMostSunnyCity());
        assertEquals("N/A", summary.getMostCloudyCity());
        assertEquals("N/A", summary.getHottestDay());
        assertEquals("N/A", summary.getColdestDay());
        assertEquals("N/A", summary.getCloudiestDay());
        assertEquals("N/A", summary.getColdestAverageCity());
        assertEquals("N/A", summary.getHottestAverageCity());
        assertEquals("N/A", summary.getCloudiestAverageCity());
        assertEquals(0, summary.getTotalMeasurements());
        assertEquals(0.0, summary.getAverageTemperature());
        assertEquals(0, summary.getTotalRainyDays());
        assertEquals(0, summary.getTotalSunnyDays());
        assertEquals(0, summary.getTotalCloudyDays());
    }

    @Test
    void getSummaryForPeriod_withData_returnsCorrectSummary() {
        LocalDateTime baseDate = LocalDateTime.of(2025, 7, 12, 0, 0);

        Weather w1 = Weather.builder()
                .city(City.MOSCOW)
                .temperature(10)
                .condition(WeatherCondition.SUNNY)
                .date(baseDate.minusHours(3))
                .build();

        Weather w2 = Weather.builder()
                .city(City.PITER)
                .temperature(5)
                .condition(WeatherCondition.RAINY)
                .date(baseDate.minusDays(1).withHour(12))
                .build();

        Weather w3 = Weather.builder()
                .city(City.MOSCOW)
                .temperature(0)
                .condition(WeatherCondition.CLOUDY)
                .date(baseDate.minusHours(1))
                .build();

        Weather w4 = Weather.builder()
                .city(City.PITER)
                .temperature(15)
                .condition(WeatherCondition.SUNNY)
                .date(baseDate)
                .build();

        service.processWeather(w1);
        service.processWeather(w2);
        service.processWeather(w3);
        service.processWeather(w4);

        WeatherSummaryDto summary = service.getSummaryForPeriod(baseDate.minusDays(2), baseDate.plusDays(1));

        assertEquals("MOSCOW", summary.getMostSunnyCity());
        assertEquals("PITER", summary.getMostRainyCity());
        assertEquals("MOSCOW", summary.getMostCloudyCity());

        assertTrue(summary.getHottestDay().contains("PITER"));
        assertTrue(summary.getCloudiestDay().contains("MOSCOW"));
        assertTrue(summary.getColdestDay().contains("MOSCOW"));

        assertEquals("MOSCOW", summary.getColdestAverageCity());
        assertEquals("PITER", summary.getHottestAverageCity());
        assertEquals("MOSCOW", summary.getCloudiestAverageCity());

        assertEquals(4, summary.getTotalMeasurements());

        double expectedAvg = (10 + 5 + 15) / 4.0;
        assertEquals(expectedAvg, summary.getAverageTemperature(), 0.001);
        assertEquals(2, summary.getTotalSunnyDays());
        assertEquals(1, summary.getTotalRainyDays());
        assertEquals(1, summary.getTotalCloudyDays());
    }

    @Test
    void getSummaryForPeriod_withPartialPeriod_filtersCorrectly() {
        LocalDateTime now = LocalDateTime.now();

        Weather w1 = Weather.builder()
                .city(City.MOSCOW)
                .temperature(10)
                .condition(WeatherCondition.SUNNY)
                .date(now.minusDays(10))
                .build();

        Weather w2 = Weather.builder()
                .city(City.PITER)
                .temperature(5)
                .condition(WeatherCondition.RAINY)
                .date(now.minusDays(1))
                .build();

        service.processWeather(w1);
        service.processWeather(w2);

        WeatherSummaryDto summary = service.getSummaryForPeriod(now.minusDays(2), now);

        assertEquals("PITER", summary.getMostRainyCity());
        assertEquals(1, summary.getTotalMeasurements());
    }

    @Test
    void filterWeathersByPeriod_returnsCorrectSubset() {
        LocalDateTime now = LocalDateTime.now();
        Weather w1 = Weather.builder().city(City.MOSCOW).temperature(10).condition(WeatherCondition.SUNNY).
                date(now.minusDays(3)).build();
        Weather w2 = Weather.builder().city(City.PITER).temperature(15).condition(WeatherCondition.CLOUDY).
                date(now.minusDays(1)).build();

        service.processWeather(w1);
        service.processWeather(w2);

        List<Weather> filtered = service.filterWeathersByPeriod(now.minusDays(2), now);

        assertEquals(1, filtered.size());
        assertEquals(City.PITER, filtered.getFirst().getCity());
    }

    @Test
    void countByCondition_countsCorrectly() {
        List<Weather> list = List.of(
                Weather.builder().city(City.MOSCOW).condition(WeatherCondition.SUNNY).build(),
                Weather.builder().city(City.PITER).condition(WeatherCondition.SUNNY).build(),
                Weather.builder().city(City.MOSCOW).condition(WeatherCondition.RAINY).build()
        );

        Map<City, Long> counts = service.countByCondition(list, WeatherCondition.SUNNY);

        assertEquals(2, counts.size());
        assertEquals(1L, counts.get(City.PITER));
        assertEquals(1L, counts.get(City.MOSCOW));
    }

    @Test
    void summarizeTemperatureByCity_calculatesStatsCorrectly() {
        List<Weather> list = List.of(
                Weather.builder().city(City.MOSCOW).temperature(10).build(),
                Weather.builder().city(City.MOSCOW).temperature(20).build(),
                Weather.builder().city(City.PITER).temperature(15).build()
        );

        Map<City, DoubleSummaryStatistics> stats = service.summarizeTemperatureByCity(list);

        assertEquals(2, stats.get(City.MOSCOW).getCount());
        assertEquals(10, stats.get(City.MOSCOW).getMin(), 0.001);
        assertEquals(20, stats.get(City.MOSCOW).getMax(), 0.001);
        assertEquals(15, stats.get(City.MOSCOW).getAverage(), 0.001);
        assertEquals(1, stats.get(City.PITER).getCount());
    }

    @Test
    void getCityWithMaxCount_returnsCorrectCity() {
        Map<City, Long> counts = Map.of(
                City.MOSCOW, 5L,
                City.PITER, 10L
        );
        City result = service.getCityWithMaxCount(counts);
        assertEquals(City.PITER, result);
        assertNull(service.getCityWithMaxCount(Collections.emptyMap()));
    }

    @Test
    void getExtremeWeather_returnsCorrectExtreme() {
        List<Weather> list = List.of(
                Weather.builder().temperature(10).build(),
                Weather.builder().temperature(20).build(),
                Weather.builder().temperature(15).build()
        );

        Weather max = service.getExtremeWeather(list, true);
        assertEquals(20, max.getTemperature(), 0.001);

        Weather min = service.getExtremeWeather(list, false);
        assertEquals(10, min.getTemperature(), 0.001);
        assertNull(service.getExtremeWeather(Collections.emptyList(), true));
    }

    @Test
    void getCloudiestWeather_returnsCorrectWeather() {
        LocalDateTime now = LocalDateTime.now();
        Weather w1 = Weather.builder().condition(WeatherCondition.CLOUDY).date(now.minusDays(1)).build();
        Weather w2 = Weather.builder().condition(WeatherCondition.CLOUDY).date(now).build();
        Weather w3 = Weather.builder().condition(WeatherCondition.SUNNY).date(now).build();

        List<Weather> list = List.of(w1, w2, w3);

        Weather cloudiest = service.getCloudiestWeather(list);

        assertEquals(WeatherCondition.CLOUDY, cloudiest.getCondition());
    }

    @Test
    void calculateAverageTemperature_returnsAverage() {
        List<Weather> list = List.of(
                Weather.builder().temperature(10).build(),
                Weather.builder().temperature(20).build()
        );
        double avg = service.calculateAverageTemperature(list);
        assertEquals(15.0, avg, 0.001);
        assertEquals(0.0, service.calculateAverageTemperature(Collections.emptyList()), 0.001);
    }

    @Test
    void getCityWithExtremeAvgTemp_returnsCorrectCity() {
        Map<City, DoubleSummaryStatistics> stats = Map.of(
                City.MOSCOW, new DoubleSummaryStatistics() {{
                    accept(10);
                    accept(20);
                }},
                City.PITER, new DoubleSummaryStatistics() {{
                    accept(15);
                    accept(25);
                }}
        );

        City hottest = service.getCityWithExtremeAvgTemp(stats, true);
        City coldest = service.getCityWithExtremeAvgTemp(stats, false);

        assertEquals(City.PITER, hottest);
        assertEquals(City.MOSCOW, coldest);
        assertNull(service.getCityWithExtremeAvgTemp(Collections.emptyMap(), true));
    }

    @Test
    void getCloudiestAverageCity_returnsCorrectCity() {
        LocalDateTime now = LocalDateTime.now();

        List<Weather> weathers = List.of(
                Weather.builder().city(City.MOSCOW).condition(WeatherCondition.CLOUDY).temperature(10).date(now).build(),
                Weather.builder().city(City.PITER).condition(WeatherCondition.CLOUDY).temperature(5).date(now).build(),
                Weather.builder().city(City.MOSCOW).condition(WeatherCondition.SUNNY).temperature(30).date(now).build()
        );

        City cloudiest = service.getCloudiestAverageCity(weathers);

        assertEquals(City.PITER, cloudiest);
        assertNull(service.getCloudiestAverageCity(Collections.emptyList()));
    }

    @Test
    void sumValues_returnsCorrectSum() {
        Map<City, Long> counts = Map.of(
                City.MOSCOW, 5L,
                City.PITER, 10L
        );

        long sum = service.sumValues(counts);
        assertEquals(15L, sum);
        assertEquals(0L, service.sumValues(Collections.emptyMap()));
    }
}