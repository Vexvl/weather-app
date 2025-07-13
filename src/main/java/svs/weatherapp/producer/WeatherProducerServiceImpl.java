package svs.weatherapp.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import svs.weatherapp.model.City;
import svs.weatherapp.model.Weather;
import svs.weatherapp.model.WeatherCondition;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@Slf4j
@RequiredArgsConstructor
@EnableScheduling
public class WeatherProducerServiceImpl implements WeatherProducerService {

    @Value("${app.kafka.topic}")
    private String weatherTopic;

    private final KafkaTemplate<String, Weather> kafkaTemplate;

    private final Random random = new Random();

    @Override
    @Scheduled(fixedRate = 2000)
    public void sendWeather() {
        City city = City.values()[random.nextInt(City.values().length)];
        WeatherCondition condition = WeatherCondition.values()[random.nextInt(WeatherCondition.values().length)];
        double temperature = Math.round(random.nextDouble() * 35 * 10) / 10.0;

        Weather weather = Weather.builder()
                .city(city)
                .temperature(temperature)
                .condition(condition)
                .date(LocalDateTime.now())
                .build();

        kafkaTemplate.send(weatherTopic, city.name(), weather);
        log.info("Weather sent: {}", weather);
    }
}