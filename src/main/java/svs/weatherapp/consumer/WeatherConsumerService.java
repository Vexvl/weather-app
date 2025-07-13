package svs.weatherapp.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import svs.weatherapp.model.Weather;

public interface WeatherConsumerService {

    @KafkaListener(topics = "${app.kafka.topic}", groupId = "${app.kafka.group}")
    void listen(Weather weather);
}