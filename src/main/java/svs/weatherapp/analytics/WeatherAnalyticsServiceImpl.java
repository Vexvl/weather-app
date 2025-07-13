package svs.weatherapp.analytics;

import org.springframework.stereotype.Service;
import svs.weatherapp.dto.WeatherAnalyticsDto;
import svs.weatherapp.dto.WeatherSummaryDto;
import svs.weatherapp.mapper.WeatherAnalyticsMapper;
import svs.weatherapp.mapper.WeatherSummaryMapper;
import svs.weatherapp.model.City;
import svs.weatherapp.model.Weather;
import svs.weatherapp.model.WeatherCondition;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WeatherAnalyticsServiceImpl implements WeatherAnalyticsService {
    private final Map<City, Integer> sunnyDays = new HashMap<>();
    private final Map<City, Integer> rainyDays = new HashMap<>();

    private final Map<City, Integer> cloudyDays = new HashMap<>();

    private final Map<City, DoubleSummaryStatistics> temperatureStats = new HashMap<>();

    private final List<Weather> allWeathers = new ArrayList<>();

    @Override
    public void processWeather(Weather weather) {
        allWeathers.add(weather);

        temperatureStats.computeIfAbsent(weather.getCity(), c -> new DoubleSummaryStatistics())
                .accept(weather.getTemperature());

        switch (weather.getCondition()) {
            case SUNNY:
                sunnyDays.merge(weather.getCity(), 1, Integer::sum);
                break;
            case RAINY:
                rainyDays.merge(weather.getCity(), 1, Integer::sum);
                break;
            case CLOUDY:
                cloudyDays.merge(weather.getCity(), 1, Integer::sum);
                break;
            default:
                break;
        }
    }

    @Override
    public List<WeatherAnalyticsDto> getCityStats() {
        return Arrays.stream(City.values())
                .map(city -> WeatherAnalyticsMapper.toWeatherAnalyticsDto(
                        city,
                        temperatureStats.get(city),
                        sunnyDays.getOrDefault(city, 0),
                        rainyDays.getOrDefault(city, 0),
                        cloudyDays.getOrDefault(city, 0)
                ))
                .collect(Collectors.toList());
    }

    @Override
    public WeatherSummaryDto getSummaryForPeriod(LocalDateTime start, LocalDateTime end) {
        List<Weather> periodWeathers = filterWeathersByPeriod(start, end);

        Map<City, Long> rainyCount = countByCondition(periodWeathers, WeatherCondition.RAINY);
        Map<City, Long> sunnyCount = countByCondition(periodWeathers, WeatherCondition.SUNNY);
        Map<City, Long> cloudyCount = countByCondition(periodWeathers, WeatherCondition.CLOUDY);
        Map<City, DoubleSummaryStatistics> tempStats = summarizeTemperatureByCity(periodWeathers);

        City mostRainyCity = getCityWithMaxCount(rainyCount);
        City mostSunnyCity = getCityWithMaxCount(sunnyCount);
        City mostCloudyCity = getCityWithMaxCount(cloudyCount);

        Weather hottestWeather = getExtremeWeather(periodWeathers, true);
        Weather coldestWeather = getExtremeWeather(periodWeathers, false);
        Weather cloudiestWeather = getCloudiestWeather(periodWeathers);

        City coldestAvgCity = getCityWithExtremeAvgTemp(tempStats, false);
        City hottestAvgCity = getCityWithExtremeAvgTemp(tempStats, true);
        City cloudiestAvgCity = getCloudiestAverageCity(periodWeathers);

        long totalMeasurements = periodWeathers.size();
        double averageTemperature = calculateAverageTemperature(periodWeathers);

        long totalRainyDays = sumValues(rainyCount);
        long totalSunnyDays = sumValues(sunnyCount);
        long totalCloudyDays = sumValues(cloudyCount);

        return WeatherSummaryMapper.toWeatherSummaryDto(
                mostRainyCity, mostSunnyCity, mostCloudyCity,
                hottestWeather, coldestWeather, cloudiestWeather,
                coldestAvgCity, hottestAvgCity, cloudiestAvgCity,
                totalMeasurements, averageTemperature,
                totalRainyDays, totalSunnyDays, totalCloudyDays
        );
    }

    public List<Weather> filterWeathersByPeriod(LocalDateTime start, LocalDateTime end) {
        return allWeathers.stream()
                .filter(w -> !w.getDate().isBefore(start) && !w.getDate().isAfter(end))
                .toList();
    }

    public Map<City, Long> countByCondition(List<Weather> weathers, WeatherCondition condition) {
        return weathers.stream()
                .filter(w -> w.getCondition() == condition)
                .collect(Collectors.groupingBy(Weather::getCity, Collectors.counting()));
    }

    public Map<City, DoubleSummaryStatistics> summarizeTemperatureByCity(List<Weather> weathers) {
        return weathers.stream()
                .collect(Collectors.groupingBy(Weather::getCity,
                        Collectors.summarizingDouble(Weather::getTemperature)));
    }

    public City getCityWithMaxCount(Map<City, Long> counts) {
        return counts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    public Weather getExtremeWeather(List<Weather> weathers, boolean max) {
        Comparator<Weather> comparator = Comparator.comparingDouble(Weather::getTemperature);
        if (max) {
            return weathers.stream().max(comparator).orElse(null);
        } else {
            return weathers.stream().min(comparator).orElse(null);
        }
    }

    public City getCityWithExtremeAvgTemp(Map<City, DoubleSummaryStatistics> tempStats, boolean max) {
        Comparator<Map.Entry<City, DoubleSummaryStatistics>> comparator =
                Comparator.comparingDouble(e -> e.getValue().getAverage());
        if (max) {
            return tempStats.entrySet().stream().max(comparator).map(Map.Entry::getKey).orElse(null);
        } else {
            return tempStats.entrySet().stream().min(comparator).map(Map.Entry::getKey).orElse(null);
        }
    }

    public Weather getCloudiestWeather(List<Weather> weathers) {
        Map<LocalDate, Long> cloudyCountByDate = weathers.stream()
                .filter(w -> w.getCondition() == WeatherCondition.CLOUDY)
                .collect(Collectors.groupingBy(w -> w.getDate().toLocalDate(), Collectors.counting()));

        LocalDate cloudiestDate = cloudyCountByDate.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        if (cloudiestDate == null) return null;

        return weathers.stream()
                .filter(w -> w.getCondition() == WeatherCondition.CLOUDY)
                .filter(w -> w.getDate().toLocalDate().equals(cloudiestDate))
                .findFirst()
                .orElse(null);
    }

    public City getCloudiestAverageCity(List<Weather> weathers) {
        Map<City, DoubleSummaryStatistics> cloudyTempStatsByCity = weathers.stream()
                .filter(w -> w.getCondition() == WeatherCondition.CLOUDY)
                .collect(Collectors.groupingBy(Weather::getCity, Collectors.summarizingDouble(Weather::getTemperature)));

        return cloudyTempStatsByCity.entrySet().stream()
                .min(Comparator.comparingDouble(e -> e.getValue().getAverage()))
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    public double calculateAverageTemperature(List<Weather> weathers) {
        return weathers.stream()
                .mapToDouble(Weather::getTemperature)
                .average()
                .orElse(0.0);
    }

    public long sumValues(Map<City, Long> counts) {
        return counts.values().stream().mapToLong(Long::longValue).sum();
    }
}