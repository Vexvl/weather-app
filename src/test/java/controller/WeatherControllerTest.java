package controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import svs.weatherapp.analytics.WeatherAnalyticsService;
import svs.weatherapp.controller.WeatherController;
import svs.weatherapp.dto.WeatherAnalyticsDto;
import svs.weatherapp.dto.WeatherSummaryDto;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class WeatherControllerTest {

    @Mock
    private WeatherAnalyticsService weatherAnalyticsService;

    @InjectMocks
    private WeatherController weatherController;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(weatherController).build();
    }

    @Test
    void getStats_ShouldReturnList() throws Exception {
        List<WeatherAnalyticsDto> analytics = List.of(
                WeatherAnalyticsDto.builder()
                        .city("MOSCOW")
                        .sunnyDays(10)
                        .rainyDays(5)
                        .cloudyDays(3)
                        .minTemperature(-5.0)
                        .maxTemperature(25.0)
                        .averageTemperature(10.0)
                        .build()
        );

        when(weatherAnalyticsService.getCityStats()).thenReturn(analytics);

        mockMvc.perform(get("/weather/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].city", is("MOSCOW")))
                .andExpect(jsonPath("$[0].sunnyDays", is(10)))
                .andExpect(jsonPath("$[0].rainyDays", is(5)))
                .andExpect(jsonPath("$[0].cloudyDays", is(3)))
                .andExpect(jsonPath("$[0].minTemperature", is(-5.0)))
                .andExpect(jsonPath("$[0].maxTemperature", is(25.0)))
                .andExpect(jsonPath("$[0].averageTemperature", is(10.0)));
    }

    @Test
    void getSummaryForPeriod_ShouldReturnSummary() throws Exception {
        WeatherSummaryDto summary = WeatherSummaryDto.builder()
                .mostRainyCity("MOSCOW")
                .mostSunnyCity("TYUMEN")
                .mostCloudyCity("PITER")
                .hottestDay("2025-07-13 in MAGADAN")
                .coldestDay("2025-07-13 in MOSCOW")
                .cloudiestDay("2025-07-12 in TYUMEN")
                .hottestAverageCity("MAGADAN")
                .coldestAverageCity("MOSCOW")
                .cloudiestAverageCity("PITER")
                .totalMeasurements(35)
                .averageTemperature(19.29)
                .totalRainyDays(11)
                .totalSunnyDays(14)
                .totalCloudyDays(10)
                .build();

        when(weatherAnalyticsService.getSummaryForPeriod(any(), any())).thenReturn(summary);

        mockMvc.perform(get("/weather/summary/period")
                        .param("start", "2025-07-10T08:00:00")
                        .param("end", "2025-07-11T20:30:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mostRainyCity", is("MOSCOW")))
                .andExpect(jsonPath("$.mostSunnyCity", is("TYUMEN")))
                .andExpect(jsonPath("$.mostCloudyCity", is("PITER")))
                .andExpect(jsonPath("$.hottestDay", is("2025-07-13 in MAGADAN")))
                .andExpect(jsonPath("$.coldestDay", is("2025-07-13 in MOSCOW")))
                .andExpect(jsonPath("$.cloudiestDay", is("2025-07-12 in TYUMEN")))
                .andExpect(jsonPath("$.hottestAverageCity", is("MAGADAN")))
                .andExpect(jsonPath("$.coldestAverageCity", is("MOSCOW")))
                .andExpect(jsonPath("$.cloudiestAverageCity", is("PITER")))
                .andExpect(jsonPath("$.totalMeasurements", is(35)))
                .andExpect(jsonPath("$.averageTemperature", is(19.29)))
                .andExpect(jsonPath("$.totalRainyDays", is(11)))
                .andExpect(jsonPath("$.totalSunnyDays", is(14)))
                .andExpect(jsonPath("$.totalCloudyDays", is(10)));
    }
}