package com.trafficrisk.v4.route;

import com.trafficrisk.v4.prediction.TypeProbability;

import java.util.List;

public class SegmentRisk {
    private String region;
    private String riskLevel;
    private String predictedType;
    private double confidence;
    private List<TypeProbability> topTypes;
    private String timeOfDay;
    private String weather;
    private String roadCondition;
    private String season;
    private double avgTemp;

    public SegmentRisk(String region, String riskLevel, String predictedType, double confidence,
                        List<TypeProbability> topTypes, String timeOfDay, String weather,
                        String roadCondition, String season, double avgTemp) {
        this.region = region;
        this.riskLevel = riskLevel;
        this.predictedType = predictedType;
        this.confidence = confidence;
        this.topTypes = topTypes;
        this.timeOfDay = timeOfDay;
        this.weather = weather;
        this.roadCondition = roadCondition;
        this.season = season;
        this.avgTemp = avgTemp;
    }

    public String getRegion() { return region; }
    public String getRiskLevel() { return riskLevel; }
    public String getPredictedType() { return predictedType; }
    public double getConfidence() { return confidence; }
    public List<TypeProbability> getTopTypes() { return topTypes; }
    public String getTimeOfDay() { return timeOfDay; }
    public String getWeather() { return weather; }
    public String getRoadCondition() { return roadCondition; }
    public String getSeason() { return season; }
    public double getAvgTemp() { return avgTemp; }
}
