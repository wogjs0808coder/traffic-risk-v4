package com.trafficrisk.v4.prediction;

import ml.dmlc.xgboost4j.java.Booster;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/models")
public class ModelInfoController {

    private final XgboostModelLoader modelLoader;

    public ModelInfoController(XgboostModelLoader modelLoader) {
        this.modelLoader = modelLoader;
    }

    // 지역 모델이 어떤 요인을 가장 중요하게 보는지(feature importance) 상위 10개
    @GetMapping("/{region}/feature-importance")
    public List<FeatureImportance> featureImportance(@PathVariable String region) {
        XgboostModelLoader.RegionModel model = modelLoader.get(region);
        Booster booster = model.booster();

        try {
            Map<String, Integer> rawScores = booster.getFeatureScore((String) null);
            double total = rawScores.values().stream().mapToInt(Integer::intValue).sum();

            List<FeatureImportance> result = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : rawScores.entrySet()) {
                double ratio = total > 0 ? entry.getValue() / total : 0;
                result.add(new FeatureImportance(entry.getKey(), ratio));
            }

            result.sort(Comparator.comparingDouble(FeatureImportance::importance).reversed());
            return result.subList(0, Math.min(10, result.size()));
        } catch (Exception e) {
            throw new IllegalStateException("feature importance 조회 실패: " + region, e);
        }
    }
}
