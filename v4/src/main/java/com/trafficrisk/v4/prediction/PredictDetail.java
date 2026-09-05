package com.trafficrisk.v4.prediction;

import java.util.List;

public record PredictDetail(
        String predictedType,
        double confidence,
        List<TypeProbability> topTypes,
        String timeOfDay,
        String weather,
        String roadCondition,
        String season,
        double avgTemp
) {}
