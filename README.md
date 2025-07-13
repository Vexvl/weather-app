# WeatherApp - Описание проекта и функциональности

## Общее описание
WeatherApp - это Spring Boot приложение для получения, обработки и анализа погодных данных по нескольким городам.  
Данные о погоде публикуются через Kafka-продюсер, потребляются Kafka-консьюмером и обрабатываются в сервисе аналитики.  
Приложение предоставляет REST API для получения статистики и сводных данных за период.

---

# Основные модули и компоненты

## 1. Модель данных

- **City** - enum, список городов: MOSCOW, MAGADAN, TYUMEN, PITER
- **WeatherCondition** - enum погодных условий: SUNNY, CLOUDY, RAINY
- **Weather** - класс, описывающий измерение погоды с полями:
    - city (City)
    - temperature (double)
    - condition (WeatherCondition)
    - date (LocalDateTime)

---

## 2. Продюсер (WeatherProducerService)

- Интерфейс `WeatherProducerService` с методом `sendWeather()`
- Реализация `WeatherProducerServiceImpl`:
    - Каждые 2 секунды случайным образом генерирует объект Weather (город, температура, условие, дата)
    - Отправляет данные в Kafka-топик, имя которого берётся из конфигурации (`app.kafka.topic`)
    - Использует `KafkaTemplate<String, Weather>` для отправки сообщений

---

## 3. Консьюмер (WeatherConsumerService)

- Интерфейс `WeatherConsumerService` с методом `listen(Weather weather)` с аннотацией `@KafkaListener`
- Реализация `WeatherConsumerServiceImpl`:
    - Слушает Kafka-топик (`app.kafka.topic`) в группе (`app.kafka.group`)
    - При получении Weather вызывает метод `processWeather` сервиса аналитики
    - Логирует полученные данные

---

## 4. Сервис аналитики (WeatherAnalyticsService)

- Интерфейс с методами:
    - `processWeather(Weather weather)` - обработка и накопление данных
    - `getCityStats()` - возвращает статистику по городам в виде списка DTO
    - `getSummaryForPeriod(LocalDateTime start, LocalDateTime end)` - возвращает сводный отчёт за указанный период

- Реализация `WeatherAnalyticsServiceImpl`:
    - Хранит в памяти списки погодных данных и агрегаты по городам (кол-во дней по условиям, статистики температуры)
    - `processWeather` обновляет внутренние структуры
    - `getCityStats` возвращает для каждого города DTO с количеством солнечных, дождливых, облачных дней, минимальной, максимальной и средней температурой
    - `getSummaryForPeriod` формирует сводный отчёт с:
        - Городами с максимальным количеством каждого типа погоды
        - Самым жарким, холодным и облачным днём (с датой и городом)
        - Городами с экстремальной средней температурой (самая холодная, горячая, облачная)
        - Общим количеством измерений и средней температурой за период

---

## 5. DTO (Data Transfer Objects)

- `WeatherAnalyticsDto` - статистика по городу:
    - city (String)
    - sunnyDays, rainyDays, cloudyDays (long)
    - minTemperature, maxTemperature, averageTemperature (double)

- `WeatherSummaryDto` - сводный отчёт:
    - mostRainyCity, mostSunnyCity, mostCloudyCity (String)
    - hottestDay, coldestDay, cloudiestDay (String, с датой и городом)
    - hottestAverageCity, coldestAverageCity, cloudiestAverageCity (String)
    - totalRainyDays, totalSunnyDays, totalCloudyDays (long)
    - totalMeasurements (long)
    - averageTemperature (double)

---

## 6. Мапперы

- `WeatherAnalyticsMapper` - преобразует агрегаты в `WeatherAnalyticsDto`
- `WeatherSummaryMapper` - преобразует агрегаты и отдельные измерения в `WeatherSummaryDto`

---

## 7. Контроллер (WeatherController)

- REST API на базе Spring MVC:
    - `GET /weather/stats` - возвращает список статистики по городам
    - `GET /weather/summary/period?start=yyyy-MM-dd'T'HH:mm:ss&end=yyyy-MM-dd'T'HH:mm:ss` - возвращает сводный отчёт за период
- Логирует запросы и форматирует даты для парсинга

---

## 8. Тесты

- Юнит-тесты для сервиса аналитики `WeatherAnalyticsServiceImplTest` - проверяют накопление данных, корректность сводок, фильтрацию по датам
- Тесты для мапперов - проверка правильного преобразования данных в DTO
- Интеграционный тест `WeatherAppKafkaTest` - проверяет работу продюсера и консьюмера с embedded Kafka (в тестовом окружении)
- Контроллер тестируется с использованием MockMvc и Mockito, проверяется корректность возвращаемых JSON

---

## 9. Как работает поток данных

1. `WeatherProducerServiceImpl` каждые 2 секунды генерирует случайные данные о погоде и публикует их в Kafka-топик.
2. `WeatherConsumerServiceImpl` слушает этот топик, принимает сообщения и передаёт данные в `WeatherAnalyticsService`.
3. `WeatherAnalyticsServiceImpl` накапливает статистику и предоставляет API для её запроса.
4. Клиенты могут обращаться к REST API для получения статистики и сводных данных.

---

## 10. Расширение и кастомизация

- Легко добавить новые города, расширив enum `City`.
- Можно добавить новые погодные условия в `WeatherCondition`.
- Методы сервиса аналитики можно доработать для поддержки новых метрик.
- Топики Kafka и настройки групп можно менять через конфигурацию.

---

## 11. Логирование и отладка

- Используется `Slf4j` для логирования действий продюсера, консьюмера и контроллера.
- В логах видны отправленные и полученные сообщения, запросы к API.

---

## 12. Безопасность

- В текущей версии безопасность не настроена.
- Для продакшен-среды рекомендуется добавить аутентификацию и авторизацию (Spring Security).
- Также можно настроить шифрование и проверку подписи сообщений Kafka.

---

## 13. Ограничения и будущие улучшения

- Вся аналитика хранится в памяти - при перезапуске данные теряются.
- Для масштабируемости и сохранности можно использовать БД.
- Добавить мониторинг и метрики (например, Prometheus).
- Возможность фильтровать и сортировать результаты по дополнительным параметрам.

---

## 14. Конфигурация Kafka (application.properties)

```properties
spring.kafka.bootstrap-servers=localhost:9092
app.kafka.topic=weather-topic
app.kafka.group=weather-group
app.kafka.trusted-packages=svs.weatherapp.model