package mapper;

import org.junit.Test;
import svs.weatherapp.dto.WeatherSummaryDto;
import svs.weatherapp.mapper.WeatherSummaryMapper;
import svs.weatherapp.model.City;
import svs.weatherapp.model.Weather;
import svs.weatherapp.model.WeatherCondition;

import java.time.LocalDateTime;

import static org.junit.Assert.*;

public class WeatherSummaryMapperTest {

    @Test
    public void testToWeatherSummaryDto_withAllData() {
        LocalDateTime now = LocalDateTime.of(2025, 7, 13, 10, 0);

        Weather hottestWeather = Weather.builder()
                .city(City.MAGADAN)
                .temperature(30.0)
                .condition(WeatherCondition.SUNNY)
                .date(now)
                .build();

        Weather coldestWeather = Weather.builder()
                .city(City.MOSCOW)
                .temperature(-5.0)
                .condition(WeatherCondition.CLOUDY)
                .date(now.minusDays(1))
                .build();

        Weather cloudiestWeather = Weather.builder()
                .city(City.PITER)
                .temperature(15.0)
                .condition(WeatherCondition.CLOUDY)
                .date(now.minusDays(2))
                .build();

        WeatherSummaryDto dto = WeatherSummaryMapper.toWeatherSummaryDto(
                City.MOSCOW, City.TYUMEN, City.PITER,
                hottestWeather, coldestWeather, cloudiestWeather,
                City.MOSCOW, City.MAGADAN, City.PITER,
                100, 15.5,
                30, 40, 30
        );

        assertEquals("MOSCOW", dto.getMostRainyCity());
        assertEquals("TYUMEN", dto.getMostSunnyCity());
        assertEquals("PITER", dto.getMostCloudyCity());
        assertEquals("2025-07-13 in MAGADAN", dto.getHottestDay());
        assertEquals("2025-07-12 in MOSCOW", dto.getColdestDay());
        assertEquals("2025-07-11 in PITER", dto.getCloudiestDay());
        assertEquals("MOSCOW", dto.getColdestAverageCity());
        assertEquals("MAGADAN", dto.getHottestAverageCity());
        assertEquals("PITER", dto.getCloudiestAverageCity());
        assertEquals(100, dto.getTotalMeasurements());
        assertEquals(15.5, dto.getAverageTemperature(), 0.0001);
        assertEquals(30, dto.getTotalRainyDays());
        assertEquals(40, dto.getTotalSunnyDays());
        assertEquals(30, dto.getTotalCloudyDays());
    }

    @Test
    public void testToWeatherSummaryDto_withNulls() {
        WeatherSummaryDto dto = WeatherSummaryMapper.toWeatherSummaryDto(
                null, null, null,
                null, null, null,
                null, null, null,
                0, 0,
                0, 0, 0
        );

        assertEquals("N/A", dto.getMostRainyCity());
        assertEquals("N/A", dto.getMostSunnyCity());
        assertEquals("N/A", dto.getMostCloudyCity());
        assertEquals("N/A", dto.getHottestDay());
        assertEquals("N/A", dto.getColdestDay());
        assertEquals("N/A", dto.getCloudiestDay());
        assertEquals("N/A", dto.getColdestAverageCity());
        assertEquals("N/A", dto.getHottestAverageCity());
        assertEquals("N/A", dto.getCloudiestAverageCity());
        assertEquals(0, dto.getTotalMeasurements());
        assertEquals(0.0, dto.getAverageTemperature(), 0.0001);
        assertEquals(0, dto.getTotalRainyDays());
        assertEquals(0, dto.getTotalSunnyDays());
        assertEquals(0, dto.getTotalCloudyDays());
    }
}