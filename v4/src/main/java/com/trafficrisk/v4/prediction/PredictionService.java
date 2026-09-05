package com.trafficrisk.v4.prediction;

import com.trafficrisk.v4.history.PredictionHistory;
import com.trafficrisk.v4.history.PredictionHistoryRepository;
import com.trafficrisk.v4.weather.WeatherFeatures;
import com.trafficrisk.v4.weather.WeatherService;
import ml.dmlc.xgboost4j.java.DMatrix;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PredictionService {

    private final XgboostModelLoader modelLoader;
    private final PredictionHistoryRepository historyRepository;
    private final WeatherService weatherService;
    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    public PredictionService(XgboostModelLoader modelLoader,
                              PredictionHistoryRepository historyRepository,
                              WeatherService weatherService) {
        this.modelLoader = modelLoader;
        this.historyRepository = historyRepository;
        this.weatherService = weatherService;
    }

    private record Core(float[][] proba, List<String> classes) {}

    public PredictResponse predict(PredictRequest request) {
        Core core = computeCore(request);
        int predIdx = argmax(core.proba()[0]);
        String predictedType = core.classes().get(predIdx);
        double confidence = core.proba()[0][predIdx];

        saveHistory(request, predictedType, confidence);
        return new PredictResponse(predictedType, confidence);
    }

    // 경로 위험 예측(FR-13)에서 사용 - 상위 3개 예측 유형까지 포함한 상세 결과
    public PredictDetail predictDetailed(PredictRequest request) {
        Core core = computeCore(request);
        float[] row = core.proba()[0];
        List<String> classes = core.classes();

        List<TypeProbability> ranked = new ArrayList<>();
        for (int i = 0; i < row.length; i++) {
            ranked.add(new TypeProbability(classes.get(i), row[i]));
        }
        ranked.sort(Comparator.comparingDouble(TypeProbability::probability).reversed());
        List<TypeProbability> top3 = ranked.subList(0, Math.min(3, ranked.size()));

        TypeProbability best = top3.get(0);
        saveHistory(request, best.type(), best.probability());

        return new PredictDetail(
                best.type(), best.probability(), top3,
                request.getTimeOfDay(), request.getWeather(), request.getRoadCondition(),
                request.getSeason(), request.getAvgTemp()
        );
    }

    private Core computeCore(PredictRequest request) {
        XgboostModelLoader.RegionModel model = modelLoader.get(request.getRegion());
        List<String> trainColumns = model.trainColumns();
        List<String> classes = model.classes();

        applyDefaults(request);

        Map<String, String> categorical = new LinkedHashMap<>();
        categorical.put("주야", request.getTimeOfDay());
        categorical.put("weather", request.getWeather());
        categorical.put("road_condition", request.getRoadCondition());
        categorical.put("vehicle_type", request.getVehicleType());
        categorical.put("age_group", request.getAgeGroup());
        categorical.put("season", request.getSeason());

        Map<String, Double> numeric = new LinkedHashMap<>();
        numeric.put("평균기온(°C)", request.getAvgTemp());
        numeric.put("일강수량_클립(mm)", request.getPrecipitation());
        numeric.put("평균 풍속(m/s)", request.getWindSpeed());
        numeric.put("평균 상대습도(%)", request.getHumidity());
        numeric.put("폭우_여부_플래그", request.getHeavyRainFlag());

        float[] row = new float[trainColumns.size()];

        for (Map.Entry<String, Double> entry : numeric.entrySet()) {
            int idx = trainColumns.indexOf(entry.getKey());
            if (idx >= 0) row[idx] = entry.getValue().floatValue();
        }
        for (Map.Entry<String, String> entry : categorical.entrySet()) {
            String col = entry.getKey() + "_" + entry.getValue();
            int idx = trainColumns.indexOf(col);
            if (idx >= 0) row[idx] = 1f;
        }

        try {
            DMatrix matrix = new DMatrix(row, 1, trainColumns.size(), Float.NaN);
            float[][] proba = model.booster().predict(matrix);
            return new Core(proba, classes);
        } catch (Exception e) {
            throw new IllegalStateException("예측 실패: " + request.getRegion(), e);
        }
    }

    private int argmax(float[] arr) {
        int idx = 0;
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] > arr[idx]) idx = i;
        }
        return idx;
    }

    private void applyDefaults(PredictRequest request) {
        if (request.getSeason() == null || request.getSeason().isBlank()) {
            request.setSeason(currentSeason());
        }
        if (request.getTimeOfDay() == null || request.getTimeOfDay().isBlank()) {
            int hour = LocalDateTime.now().getHour();
            request.setTimeOfDay(hour >= 6 && hour < 18 ? "주간" : "야간");
        }
        boolean needsWeather = request.getWeather() == null || request.getWeather().isBlank()
                || request.getRoadCondition() == null || request.getRoadCondition().isBlank();
        if (needsWeather) {
            WeatherFeatures weather = weatherService.fetch(request.getRegion());
            request.setWeather(weather.weatherCategory());
            request.setRoadCondition(weather.roadCondition());
            request.setAvgTemp(weather.avgTemp());
            request.setPrecipitation(weather.precipitation());
            request.setWindSpeed(weather.windSpeed());
            request.setHumidity(weather.humidity());
            request.setHeavyRainFlag(weather.heavyRainFlag());
        }
    }

    private String currentSeason() {
        int month = LocalDateTime.now().getMonthValue();
        if (month >= 3 && month <= 5) return "봄";
        if (month >= 6 && month <= 8) return "여름";
        if (month >= 9 && month <= 11) return "가을";
        return "겨울";
    }

    private void saveHistory(PredictRequest request, String predictedType, double confidence) {
        Map<String, Object> inputAll = new LinkedHashMap<>();
        inputAll.put("주야", request.getTimeOfDay());
        inputAll.put("weather", request.getWeather());
        inputAll.put("road_condition", request.getRoadCondition());
        inputAll.put("vehicle_type", request.getVehicleType());
        inputAll.put("age_group", request.getAgeGroup());
        inputAll.put("season", request.getSeason());
        inputAll.put("평균기온(°C)", request.getAvgTemp());
        inputAll.put("일강수량_클립(mm)", request.getPrecipitation());
        inputAll.put("평균 풍속(m/s)", request.getWindSpeed());
        inputAll.put("평균 상대습도(%)", request.getHumidity());
        inputAll.put("폭우_여부_플래그", request.getHeavyRainFlag());

        String inputJson = jsonMapper.writeValueAsString(inputAll);

        UUID userId = null;
        if (request.getUserId() != null && !request.getUserId().isBlank()) {
            userId = UUID.fromString(request.getUserId());
        }

        BigDecimal confidenceValue = BigDecimal.valueOf(confidence).setScale(4, RoundingMode.HALF_UP);

        PredictionHistory history = new PredictionHistory(
                userId, request.getRegion(), inputJson, predictedType, confidenceValue
        );
        historyRepository.save(history);
    }
}
