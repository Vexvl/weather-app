package svs.weatherapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class WeatherSummaryDto {
    private String mostRainyCity;
    private String mostSunnyCity;
    private String mostCloudyCity;
    private String hottestDay;
    private String coldestDay;
    private String cloudiestDay;
    private String hottestAverageCity;
    private String coldestAverageCity;
    private String cloudiestAverageCity;
    private long totalRainyDays;
    private long totalSunnyDays;
    private long totalCloudyDays;
    private long totalMeasurements;
    private double averageTemperature;
}