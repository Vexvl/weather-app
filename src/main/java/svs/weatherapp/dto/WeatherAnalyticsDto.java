package svs.weatherapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class WeatherAnalyticsDto {
    private String city;
    private long sunnyDays;
    private long rainyDays;
    private long cloudyDays;
    private double minTemperature;
    private double maxTemperature;
    private double averageTemperature;
}