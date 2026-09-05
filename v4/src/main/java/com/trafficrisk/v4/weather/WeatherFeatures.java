package com.trafficrisk.v4.weather;

public record WeatherFeatures(
        String weatherCategory,     // weather 카테고리 (맑음/흐림/비/눈)
        String roadCondition,       // road_condition 카테고리 (건조/젖음)
        double avgTemp,
        double precipitation,
        double windSpeed,
        double humidity,
        double heavyRainFlag
) {}
