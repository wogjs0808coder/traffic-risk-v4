package com.trafficrisk.v4.route;

public class RouteRiskRequest {
    private String vehicleType;
    private String ageGroup;

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }
    public String getAgeGroup() { return ageGroup; }
    public void setAgeGroup(String ageGroup) { this.ageGroup = ageGroup; }
}
