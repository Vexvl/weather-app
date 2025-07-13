package svs.weatherapp.mapper;

import org.springframework.stereotype.Component;
import svs.weatherapp.dto.WeatherAnalyticsDto;
import svs.weatherapp.model.City;

import java.util.DoubleSummaryStatistics;

@Component
public class WeatherAnalyticsMapper {

    public static WeatherAnalyticsDto toWeatherAnalyticsDto(City city, DoubleSummaryStatistics stats, long sunnyDays,
                                                            long rainyDays, long cloudyDays) {
        return WeatherAnalyticsDto.builder()
                .city(city.name())
                .sunnyDays(sunnyDays)
                .rainyDays(rainyDays)
                .cloudyDays(cloudyDays)
                .minTemperature(stats != null ? stats.getMin() : 0)
                .maxTemperature(stats != null ? stats.getMax() : 0)
                .averageTemperature(stats != null ? stats.getAverage() : 0)
                .build();
    }
}