package svs.weatherapp.producer;

import org.springframework.scheduling.annotation.Scheduled;

public interface WeatherProducerService {
    @Scheduled(fixedRate = 2000)
    void sendWeather();
}