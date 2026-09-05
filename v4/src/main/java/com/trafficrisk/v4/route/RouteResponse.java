package com.trafficrisk.v4.route;

import java.util.List;

public class RouteResponse {
    private Long routeId;
    private List<double[]> coordinates;

    public RouteResponse(Long routeId, List<double[]> coordinates) {
        this.routeId = routeId;
        this.coordinates = coordinates;
    }

    public Long getRouteId() { return routeId; }
    public List<double[]> getCoordinates() { return coordinates; }
}
