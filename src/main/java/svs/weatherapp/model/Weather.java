package svs.weatherapp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class Weather {
    private City city;
    private double temperature;
    private WeatherCondition condition;
    private LocalDateTime date;
}