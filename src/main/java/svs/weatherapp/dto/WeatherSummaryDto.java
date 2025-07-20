package svs.weatherapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class WeatherSummaryDto {
    private final String mostRainyCity;
    private final String mostSunnyCity;
    private final String mostCloudyCity;
    private final String hottestDay;
    private final String coldestDay;
    private final String cloudiestDay;
    private final String hottestAverageCity;
    private final String coldestAverageCity;
    private final String cloudiestAverageCity;
    private final long totalRainyDays;
    private final long totalSunnyDays;
    private final long totalCloudyDays;
    private final long totalMeasurements;
    private final double averageTemperature;
}