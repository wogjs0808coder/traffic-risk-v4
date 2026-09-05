package com.trafficrisk.v4.profile;

public class CreateProfileRequest {
    private String userId; // 없으면 신규 생성
    private String vehicleType;
    private String ageGroup;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }
    public String getAgeGroup() { return ageGroup; }
    public void setAgeGroup(String ageGroup) { this.ageGroup = ageGroup; }
}
