package com.trafficrisk.v4.weather;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.JsonNode;

import java.util.Map;

@Service
public class WeatherService {

    private static final Map<String, double[]> REGION_COORDS = Map.of(
            "seoul", new double[]{37.5665, 126.9780},
            "busan", new double[]{35.1796, 129.0756},
            "daegu", new double[]{35.8714, 128.6014},
            "incheon", new double[]{37.4563, 126.7052},
            "daejeon", new double[]{36.3504, 127.3845},
            "yangsan", new double[]{35.3350, 129.1378}
    );

    @Value("${weather.api-key}")
    private String apiKey;

    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/weather")
            .build();

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    public WeatherFeatures fetch(String region) {
        double[] coords = REGION_COORDS.get(region);
        if (coords == null) {
            throw new IllegalArgumentException("지원하지 않는 지역: " + region);
        }

        String body = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("lat", coords[0])
                        .queryParam("lon", coords[1])
                        .queryParam("appid", apiKey)
                        .queryParam("units", "metric")
                        .build())
                .retrieve()
                .body(String.class);

        JsonNode root = jsonMapper.readTree(body);

        double temp = root.path("main").path("temp").asDouble(0.0);
        double humidity = root.path("main").path("humidity").asDouble(0.0);
        double windSpeed = root.path("wind").path("speed").asDouble(0.0);
        double rain1h = root.path("rain").path("1h").asDouble(0.0);
        double snow1h = root.path("snow").path("1h").asDouble(0.0);
        String main = root.path("weather").path(0).path("main").asString("Clear");

        String weatherCategory = mapWeatherCategory(main);
        String roadCondition = mapRoadCondition(main, temp, rain1h, snow1h);
        double heavyRainFlag = rain1h >= 10.0 ? 1.0 : 0.0;

        return new WeatherFeatures(weatherCategory, roadCondition, temp, rain1h, windSpeed, humidity, heavyRainFlag);
    }

    private String mapWeatherCategory(String openWeatherMain) {
        return switch (openWeatherMain) {
            case "Clear" -> "맑음";
            case "Clouds" -> "흐림";
            case "Rain", "Drizzle", "Thunderstorm" -> "비";
            case "Snow" -> "눈";
            case "Mist", "Fog", "Haze", "Smoke", "Dust", "Sand" -> "안개";
            default -> "기타";
        };
    }

    // 실제 학습 데이터 카테고리: 건조/기타/서리·결빙/적설/젖음·습기/침수/해빙
    private String mapRoadCondition(String openWeatherMain, double temp, double rain1h, double snow1h) {
        if (snow1h > 0) {
            return "적설";
        }
        if (rain1h > 0 && temp <= 0) {
            return "서리/결빙";
        }
        if (rain1h > 0) {
            return "젖음/습기";
        }
        return "건조";
    }
}
