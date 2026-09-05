package com.trafficrisk.v4.route;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/routes")
public class RouteController {

    private final RouteService routeService;
    private final RouteQueryRepository routeQueryRepository;

    public RouteController(RouteService routeService, RouteQueryRepository routeQueryRepository) {
        this.routeService = routeService;
        this.routeQueryRepository = routeQueryRepository;
    }

    @PostMapping
    public RouteResponse create(@RequestBody CreateRouteRequest request) {
        return routeService.createRoute(request);
    }

    @PostMapping("/{routeId}/risk")
    public List<SegmentRisk> risk(@PathVariable Long routeId, @RequestBody RouteRiskRequest request) {
        return routeService.assessRisk(routeId, request);
    }

    // 내 경로 기록 조회
    @GetMapping
    public Page<RouteQuery> myRoutes(
            @RequestParam String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return routeQueryRepository.findByUserIdOrderByCreatedAtDesc(UUID.fromString(userId), PageRequest.of(page, size));
    }
}
