package svs.weatherapp.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import svs.weatherapp.analytics.WeatherAnalyticsService;
import svs.weatherapp.model.Weather;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherConsumerServiceImpl implements WeatherConsumerService {

    private final WeatherAnalyticsService weatherAnalyticsService;

    @Override
    @KafkaListener(topics = "${app.kafka.topic}", groupId = "${app.kafka.group}")
    public void listen(Weather weather) {
        log.info("Weather received: {}", weather);
        weatherAnalyticsService.processWeather(weather);
    }
}