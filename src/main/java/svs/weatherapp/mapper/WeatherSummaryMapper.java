package svs.weatherapp.mapper;

import org.springframework.stereotype.Component;
import svs.weatherapp.dto.WeatherSummaryDto;
import svs.weatherapp.model.City;
import svs.weatherapp.model.Weather;

@Component
public class WeatherSummaryMapper {

    public static WeatherSummaryDto toWeatherSummaryDto(
            City mostRainyCity,
            City mostSunnyCity,
            City mostCloudyCity,
            Weather hottestWeather,
            Weather coldestWeather,
            Weather cloudiestWeather,
            City coldestAvgCity,
            City hottestAvgCity,
            City cloudiestAvgCity,
            long totalMeasurements,
            double averageTemperature,
            long totalRainyDays,
            long totalSunnyDays,
            long totalCloudyDays
    ) {
        return WeatherSummaryDto.builder()
                .mostRainyCity(mostRainyCity != null ? mostRainyCity.name() : "N/A")
                .mostSunnyCity(mostSunnyCity != null ? mostSunnyCity.name() : "N/A")
                .mostCloudyCity(mostCloudyCity != null ? mostCloudyCity.name() : "N/A")
                .hottestDay(hottestWeather != null
                        ? hottestWeather.getDate().toLocalDate() + " in " + hottestWeather.getCity().name()
                        : "N/A")
                .coldestDay(coldestWeather != null
                        ? coldestWeather.getDate().toLocalDate() + " in " + coldestWeather.getCity().name()
                        : "N/A")
                .cloudiestDay(cloudiestWeather != null
                        ? cloudiestWeather.getDate().toLocalDate() + " in " + cloudiestWeather.getCity().name()
                        : "N/A")
                .coldestAverageCity(coldestAvgCity != null ? coldestAvgCity.name() : "N/A")
                .hottestAverageCity(hottestAvgCity != null ? hottestAvgCity.name() : "N/A")
                .cloudiestAverageCity(cloudiestAvgCity != null ? cloudiestAvgCity.name() : "N/A")
                .totalMeasurements(totalMeasurements)
                .averageTemperature(averageTemperature)
                .totalRainyDays(totalRainyDays)
                .totalSunnyDays(totalSunnyDays)
                .totalCloudyDays(totalCloudyDays)
                .build();
    }
}