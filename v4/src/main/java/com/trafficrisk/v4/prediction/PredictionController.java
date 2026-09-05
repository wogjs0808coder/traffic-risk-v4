package com.trafficrisk.v4.prediction;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PredictionController {

    private final PredictionService predictionService;

    public PredictionController(PredictionService predictionService) {
        this.predictionService = predictionService;
    }

    @PostMapping("/predict")
    public PredictResponse predict(@RequestBody PredictRequest request) {
        return predictionService.predict(request);
    }

    // 상위 3개 사고유형 + 예측에 사용된 조건까지 포함한 상세 결과
    @PostMapping("/predict/detail")
    public PredictDetail predictDetail(@RequestBody PredictRequest request) {
        return predictionService.predictDetailed(request);
    }
}
