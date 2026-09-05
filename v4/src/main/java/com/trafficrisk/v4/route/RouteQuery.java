package com.trafficrisk.v4.route;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "route_queries", schema = "production")
public class RouteQuery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "route_id")
    private Long routeId;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "origin_address", nullable = false)
    private String originAddress;

    @Column(name = "destination_address", nullable = false)
    private String destinationAddress;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "route_geojson", nullable = false)
    private String routeGeoJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "segment_risk_json", nullable = false)
    private String segmentRiskJson;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected RouteQuery() {}

    public RouteQuery(UUID userId, String originAddress, String destinationAddress,
                       String routeGeoJson, String segmentRiskJson) {
        this.userId = userId;
        this.originAddress = originAddress;
        this.destinationAddress = destinationAddress;
        this.routeGeoJson = routeGeoJson;
        this.segmentRiskJson = segmentRiskJson;
    }

    public Long getRouteId() { return routeId; }
    public UUID getUserId() { return userId; }
    public String getOriginAddress() { return originAddress; }
    public String getDestinationAddress() { return destinationAddress; }
    public String getRouteGeoJson() { return routeGeoJson; }
    public String getSegmentRiskJson() { return segmentRiskJson; }
    public void setSegmentRiskJson(String segmentRiskJson) { this.segmentRiskJson = segmentRiskJson; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
