package com.trafficrisk.v4.prediction;

public class PredictResponse {

    private String predictedType;
    private double confidence;

    public PredictResponse(String predictedType, double confidence) {
        this.predictedType = predictedType;
        this.confidence = confidence;
    }

    public String getPredictedType() { return predictedType; }
    public double getConfidence() { return confidence; }
}
