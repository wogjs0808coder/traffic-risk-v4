package com.trafficrisk.v4.route;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;

@Service
public class GeocodingService {

    private static final Map<String, String> REGION_KEYWORDS = Map.of(
            "서울", "seoul",
            "부산", "busan",
            "대구", "daegu",
            "인천", "incheon",
            "대전", "daejeon",
            "양산", "yangsan"
    );

    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://nominatim.openstreetmap.org")
            .defaultHeader("User-Agent", "traffic-risk-v4/1.0 (graduation project)")
            .build();

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    // 주소 -> 좌표 (FR-08)
    public double[] geocode(String address) {
        String body = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search")
                        .queryParam("q", address)
                        .queryParam("format", "json")
                        .queryParam("limit", 1)
                        .queryParam("countrycodes", "kr")
                        .build())
                .retrieve()
                .body(String.class);

        JsonNode root = jsonMapper.readTree(body);
        if (!root.isArray() || root.isEmpty()) {
            throw new IllegalArgumentException("주소를 찾을 수 없음: " + address);
        }
        JsonNode first = root.get(0);
        double lat = Double.parseDouble(first.path("lat").asString());
        double lon = Double.parseDouble(first.path("lon").asString());
        return new double[]{lat, lon};
    }

    // 좌표 -> 지원 지역 코드 (FR-12). 매칭 실패 시 null 반환
    public String reverseToRegion(double lat, double lon) {
        String body = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/reverse")
                        .queryParam("lat", lat)
                        .queryParam("lon", lon)
                        .queryParam("format", "json")
                        .queryParam("zoom", 8)
                        .build())
                .retrieve()
                .body(String.class);

        JsonNode root = jsonMapper.readTree(body);
        String displayName = root.path("display_name").asString("");

        for (Map.Entry<String, String> entry : REGION_KEYWORDS.entrySet()) {
            if (displayName.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }
}
