package svs.weatherapp.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import svs.weatherapp.analytics.WeatherAnalyticsService;
import svs.weatherapp.dto.WeatherAnalyticsDto;
import svs.weatherapp.dto.WeatherSummaryDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/weather")
@Slf4j
public class WeatherController {

    private final WeatherAnalyticsService weatherAnalyticsService;

    @GetMapping("/stats")
    @ResponseStatus(HttpStatus.OK)
    public List<WeatherAnalyticsDto> getStats() {
        log.info("Got getStats");
        return weatherAnalyticsService.getCityStats();
    }

    @GetMapping("/summary/period")
    @ResponseStatus(HttpStatus.OK)
    public WeatherSummaryDto getSummaryForPeriod(@RequestParam String start, @RequestParam String end) {
        log.info("Got getSummaryForPeriod: start={}, end={}", start, end);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        LocalDateTime startDate = LocalDateTime.parse(start, formatter);
        LocalDateTime endDate = LocalDateTime.parse(end, formatter);
        return weatherAnalyticsService.getSummaryForPeriod(startDate, endDate);
    }
}