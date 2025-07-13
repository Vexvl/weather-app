package mapper;

import org.junit.Test;
import svs.weatherapp.dto.WeatherAnalyticsDto;
import svs.weatherapp.mapper.WeatherAnalyticsMapper;
import svs.weatherapp.model.City;

import java.util.DoubleSummaryStatistics;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WeatherAnalyticsMapperTest {

    @Test
    public void testToDto_withStats() {
        DoubleSummaryStatistics stats = new DoubleSummaryStatistics();
        stats.accept(10.0);
        stats.accept(20.0);
        stats.accept(15.0);

        WeatherAnalyticsDto dto = WeatherAnalyticsMapper.toWeatherAnalyticsDto(City.MOSCOW, stats, 5, 3, 2);

        assertEquals("MOSCOW", dto.getCity());
        assertEquals(5, dto.getSunnyDays());
        assertEquals(3, dto.getRainyDays());
        assertEquals(2, dto.getCloudyDays());
        assertEquals(10.0, dto.getMinTemperature(), 0.0001);
        assertEquals(20.0, dto.getMaxTemperature(), 0.0001);
        assertEquals(15.0, dto.getAverageTemperature(), 0.0001);
    }

    @Test
    public void testToDto_nullStats() {
        WeatherAnalyticsDto dto = WeatherAnalyticsMapper.toWeatherAnalyticsDto(City.PITER, null, 0, 0, 0);

        assertEquals("PITER", dto.getCity());
        assertEquals(0, dto.getSunnyDays());
        assertEquals(0, dto.getRainyDays());
        assertEquals(0, dto.getCloudyDays());
        assertEquals(0.0, dto.getMinTemperature(), 0.0001);
        assertEquals(0.0, dto.getMaxTemperature(), 0.0001);
        assertEquals(0.0, dto.getAverageTemperature(), 0.0001);
    }
}