package kafka;

import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import svs.weatherapp.model.City;
import svs.weatherapp.model.Weather;
import svs.weatherapp.model.WeatherCondition;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = svs.weatherapp.WeatherAppApplication.class,
        properties = {"spring.profiles.active=test"}
)
@EmbeddedKafka(partitions = 1, topics = "test-weather-topic")
public class WeatherAppKafkaTest {

    @Autowired
    private KafkaTemplate<String, Weather> kafkaTemplate;

    @Test
    void testProducerSendsWeather(@Autowired EmbeddedKafkaBroker embeddedKafka) {
        var consumerProps = KafkaTestUtils.consumerProps("testGroup", "false", embeddedKafka);
        var consumerFactory = new DefaultKafkaConsumerFactory<>(
                consumerProps,
                new StringDeserializer(),
                new JsonDeserializer<>(Weather.class, false)
        );
        var consumer = consumerFactory.createConsumer();
        embeddedKafka.consumeFromAnEmbeddedTopic(consumer, "test-weather-topic");

        Weather weather = Weather.builder()
                .city(City.MOSCOW)
                .temperature(25.0)
                .condition(WeatherCondition.SUNNY)
                .date(LocalDateTime.now())
                .build();

        kafkaTemplate.send("test-weather-topic", "TEST", weather);

        var record = KafkaTestUtils.getSingleRecord(consumer, "test-weather-topic");

        assertThat(record.value().getCity()).isEqualTo(City.MOSCOW);

        consumer.close();
    }
}