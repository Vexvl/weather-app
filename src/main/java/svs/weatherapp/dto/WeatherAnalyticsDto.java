package svs.weatherapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class WeatherAnalyticsDto {
    private final String city;
    private final long sunnyDays;
    private final long rainyDays;
    private final long cloudyDays;
    private final double minTemperature;
    private final double maxTemperature;
    private final double averageTemperature;
}