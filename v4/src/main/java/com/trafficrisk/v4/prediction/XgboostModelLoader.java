package com.trafficrisk.v4.prediction;

import jakarta.annotation.PostConstruct;
import ml.dmlc.xgboost4j.java.Booster;
import ml.dmlc.xgboost4j.java.XGBoost;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class XgboostModelLoader {

    private static final List<String> REGIONS =
            List.of("seoul", "busan", "daegu", "incheon", "daejeon", "yangsan");

    @Value("${ml.artifacts-dir}")
    private String artifactsDir;

    private final Map<String, RegionModel> cache = new ConcurrentHashMap<>();
    private final JsonMapper objectMapper = JsonMapper.builder().build();

    @PostConstruct
    public void loadAll() {
        for (String region : REGIONS) {
            try {
                cache.put(region, loadRegion(region));
                System.out.println("모델 로드 완료: " + region);
            } catch (Exception e) {
                throw new IllegalStateException("모델 로드 실패: " + region, e);
            }
        }
    }

    private RegionModel loadRegion(String region) throws IOException {
        Path dir = Path.of(artifactsDir, region);

        Booster booster;
        try {
            booster = XGBoost.loadModel(dir.resolve("model.json").toString());
        } catch (Exception e) {
            throw new IOException("Booster 로드 실패: " + dir, e);
        }

        List<String> classes = objectMapper.readValue(
                dir.resolve("classes.json").toFile(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
        );
        List<String> trainColumns = objectMapper.readValue(
                dir.resolve("train_columns.json").toFile(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
        );

        return new RegionModel(booster, classes, trainColumns);
    }

    public RegionModel get(String region) {
        RegionModel model = cache.get(region);
        if (model == null) {
            throw new IllegalArgumentException("지원하지 않는 지역: " + region);
        }
        return model;
    }

    public record RegionModel(Booster booster, List<String> classes, List<String> trainColumns) {}
}
