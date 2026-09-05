package com.trafficrisk.v4.prediction;

public class PredictRequest {

    private String userId;         // 선택값, UUID 문자열 (없으면 비로그인 예측)
    private String region;
    private String timeOfDay;      // 주야
    private String weather;        // weather
    private String roadCondition;  // road_condition
    private String vehicleType;    // vehicle_type
    private String ageGroup;       // age_group
    private String season;         // season

    private double avgTemp = 0.0;          // 평균기온(°C)
    private double precipitation = 0.0;    // 일강수량_클립(mm)
    private double windSpeed = 0.0;        // 평균 풍속(m/s)
    private double humidity = 0.0;         // 평균 상대습도(%)
    private double heavyRainFlag = 0.0;    // 폭우_여부_플래그

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getTimeOfDay() { return timeOfDay; }
    public void setTimeOfDay(String timeOfDay) { this.timeOfDay = timeOfDay; }

    public String getWeather() { return weather; }
    public void setWeather(String weather) { this.weather = weather; }

    public String getRoadCondition() { return roadCondition; }
    public void setRoadCondition(String roadCondition) { this.roadCondition = roadCondition; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public String getAgeGroup() { return ageGroup; }
    public void setAgeGroup(String ageGroup) { this.ageGroup = ageGroup; }

    public String getSeason() { return season; }
    public void setSeason(String season) { this.season = season; }

    public double getAvgTemp() { return avgTemp; }
    public void setAvgTemp(double avgTemp) { this.avgTemp = avgTemp; }

    public double getPrecipitation() { return precipitation; }
    public void setPrecipitation(double precipitation) { this.precipitation = precipitation; }

    public double getWindSpeed() { return windSpeed; }
    public void setWindSpeed(double windSpeed) { this.windSpeed = windSpeed; }

    public double getHumidity() { return humidity; }
    public void setHumidity(double humidity) { this.humidity = humidity; }

    public double getHeavyRainFlag() { return heavyRainFlag; }
    public void setHeavyRainFlag(double heavyRainFlag) { this.heavyRainFlag = heavyRainFlag; }
}
