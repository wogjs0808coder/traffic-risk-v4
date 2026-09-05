package com.trafficrisk.v4.route;

import com.trafficrisk.v4.prediction.PredictDetail;
import com.trafficrisk.v4.prediction.PredictRequest;
import com.trafficrisk.v4.prediction.PredictionService;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class RouteService {

    private static final int MAX_SAMPLES = 15;

    private final GeocodingService geocodingService;
    private final RoutingService routingService;
    private final PredictionService predictionService;
    private final RouteQueryRepository routeQueryRepository;
    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    public RouteService(GeocodingService geocodingService, RoutingService routingService,
                         PredictionService predictionService, RouteQueryRepository routeQueryRepository) {
        this.geocodingService = geocodingService;
        this.routingService = routingService;
        this.predictionService = predictionService;
        this.routeQueryRepository = routeQueryRepository;
    }

    public RouteResponse createRoute(CreateRouteRequest request) {
        double[] origin = geocodingService.geocode(request.getOriginAddress());
        double[] destination = geocodingService.geocode(request.getDestinationAddress());

        RoutingService.RouteResult result = routingService.findRoute(
                origin[0], origin[1], destination[0], destination[1]
        );

        UUID userId = null;
        if (request.getUserId() != null && !request.getUserId().isBlank()) {
            userId = UUID.fromString(request.getUserId());
        }

        RouteQuery routeQuery = new RouteQuery(
                userId, request.getOriginAddress(), request.getDestinationAddress(),
                result.rawGeoJson(), "{}"
        );
        RouteQuery saved = routeQueryRepository.save(routeQuery);

        return new RouteResponse(saved.getRouteId(), result.coordinates());
    }

    public List<SegmentRisk> assessRisk(Long routeId, RouteRiskRequest request) {
        RouteQuery route = routeQueryRepository.findById(routeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 경로: " + routeId));

        List<double[]> coordinates = extractCoordinates(route.getRouteGeoJson());
        List<String> regionSequence = sampleRegions(coordinates);
        List<String> uniqueRegions = dedupePreserveOrder(regionSequence);

        List<SegmentRisk> segments = new ArrayList<>();
        for (String region : uniqueRegions) {
            PredictRequest predictRequest = new PredictRequest();
            predictRequest.setRegion(region);
            predictRequest.setVehicleType(request.getVehicleType());
            predictRequest.setAgeGroup(request.getAgeGroup());

            PredictDetail detail = predictionService.predictDetailed(predictRequest);
            String riskLevel = toRiskLevel(detail.confidence());

            segments.add(new SegmentRisk(
                    region, riskLevel, detail.predictedType(), detail.confidence(),
                    detail.topTypes(), detail.timeOfDay(), detail.weather(),
                    detail.roadCondition(), detail.season(), detail.avgTemp()
            ));
        }

        route.setSegmentRiskJson(jsonMapper.writeValueAsString(segments));
        routeQueryRepository.save(route);

        return segments;
    }

    private List<double[]> extractCoordinates(String geoJson) {
        List<double[]> result = new ArrayList<>();
        var root = jsonMapper.readTree(geoJson);
        for (var feature : root.path("features")) {
            var geometry = feature.path("geometry");
            if (!"LineString".equals(geometry.path("type").asString(""))) continue;
            for (var coord : geometry.path("coordinates")) {
                double lon = coord.get(0).asDouble();
                double lat = coord.get(1).asDouble();
                result.add(new double[]{lat, lon});
            }
        }
        return result;
    }

    private List<String> sampleRegions(List<double[]> coordinates) {
        List<String> regions = new ArrayList<>();
        if (coordinates.isEmpty()) return regions;

        int sampleCount = Math.min(MAX_SAMPLES, coordinates.size());
        int step = Math.max(1, coordinates.size() / sampleCount);

        for (int i = 0; i < coordinates.size(); i += step) {
            double[] point = coordinates.get(i);
            String region = geocodingService.reverseToRegion(point[0], point[1]);
            if (region != null) regions.add(region);
            sleep();
        }
        // 마지막 지점(목적지)이 스텝 계산에서 빠질 수 있어 별도 확인
        double[] last = coordinates.get(coordinates.size() - 1);
        String lastRegion = geocodingService.reverseToRegion(last[0], last[1]);
        if (lastRegion != null) regions.add(lastRegion);

        return regions;
    }

    private List<String> dedupePreserveOrder(List<String> regions) {
        Set<String> seenOrdered = new LinkedHashSet<>(regions);
        return new ArrayList<>(seenOrdered);
    }

    private String toRiskLevel(double confidence) {
        if (confidence >= 0.30) return "높음";
        if (confidence >= 0.20) return "보통";
        return "낮음";
    }

    private void sleep() {
        try {
            Thread.sleep(1100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
