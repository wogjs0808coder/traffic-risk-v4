package com.trafficrisk.v4.history;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/history")
public class HistoryController {

    private final PredictionHistoryRepository repository;

    public HistoryController(PredictionHistoryRepository repository) {
        this.repository = repository;
    }

    // FR-04: 페이지네이션 이력 목록 (userId, region 선택 필터)
    @GetMapping
    public Page<PredictionHistory> list(
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageRequest pageRequest = PageRequest.of(page, size);
        UUID uid = (userId != null && !userId.isBlank()) ? UUID.fromString(userId) : null;

        if (uid != null && region != null && !region.isBlank()) {
            return repository.findByUserIdAndRegionOrderByCreatedAtDesc(uid, region, pageRequest);
        }
        if (uid != null) {
            return repository.findByUserIdOrderByCreatedAtDesc(uid, pageRequest);
        }
        if (region != null && !region.isBlank()) {
            return repository.findByRegionOrderByCreatedAtDesc(region, pageRequest);
        }
        return repository.findAllByOrderByCreatedAtDesc(pageRequest);
    }

    // FR-05: 지역별/사고유형별 집계
    @GetMapping("/stats")
    public java.util.List<Object[]> stats(@RequestParam(required = false) String region) {
        if (region != null && !region.isBlank()) {
            return repository.countByRegionAndPredictedType(region);
        }
        return repository.countByPredictedType();
    }
}
