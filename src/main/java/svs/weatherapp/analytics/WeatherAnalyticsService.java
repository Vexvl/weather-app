package svs.weatherapp.analytics;

import svs.weatherapp.dto.WeatherAnalyticsDto;
import svs.weatherapp.dto.WeatherSummaryDto;
import svs.weatherapp.model.Weather;

import java.time.LocalDateTime;
import java.util.List;

public interface WeatherAnalyticsService {
    void processWeather(Weather weather);

    List<WeatherAnalyticsDto> getCityStats();

    WeatherSummaryDto getSummaryForPeriod(LocalDateTime start, LocalDateTime end);
}