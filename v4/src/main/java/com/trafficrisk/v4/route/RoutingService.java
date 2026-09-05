package com.trafficrisk.v4.route;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.List;

@Service
public class RoutingService {

    @Value("${routing.tmap.app-key:}")
    private String tmapAppKey;

    private final RestClient tmapClient = RestClient.builder()
            .baseUrl("https://apis.openapi.sk.com/tmap/routes")
            .build();

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    public record RouteResult(String rawGeoJson, List<double[]> coordinates) {}

    // FR-11: 출발-목적지 경로 좌표 조회 (티맵 우선)
    public RouteResult findRoute(double startLat, double startLon, double endLat, double endLon) {
        if (tmapAppKey == null || tmapAppKey.isBlank()) {
            throw new IllegalStateException("티맵 appKey 미설정 (routing.tmap.app-key)");
        }

        String requestBody = jsonMapper.writeValueAsString(java.util.Map.of(
                "startX", startLon,
                "startY", startLat,
                "endX", endLon,
                "endY", endLat,
                "reqCoordType", "WGS84GEO",
                "resCoordType", "WGS84GEO",
                "startName", "출발",
                "endName", "도착",
                "searchOption", "0"
        ));

        String response = tmapClient.post()
                .uri(uriBuilder -> uriBuilder.queryParam("version", 1).build())
                .header("appKey", tmapAppKey)
                .header("Content-Type", "application/json")
                .body(requestBody)
                .retrieve()
                .body(String.class);

        List<double[]> coordinates = extractCoordinates(response);
        return new RouteResult(response, coordinates);
    }

    private List<double[]> extractCoordinates(String geoJson) {
        List<double[]> result = new ArrayList<>();
        JsonNode root = jsonMapper.readTree(geoJson);
        JsonNode features = root.path("features");

        for (JsonNode feature : features) {
            JsonNode geometry = feature.path("geometry");
            if (!"LineString".equals(geometry.path("type").asString(""))) {
                continue;
            }
            for (JsonNode coord : geometry.path("coordinates")) {
                double lon = coord.get(0).asDouble();
                double lat = coord.get(1).asDouble();
                result.add(new double[]{lat, lon});
            }
        }
        return result;
    }
}
