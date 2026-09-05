package com.trafficrisk.v4.history;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface PredictionHistoryRepository extends JpaRepository<PredictionHistory, Long> {

    Page<PredictionHistory> findByRegionOrderByCreatedAtDesc(String region, Pageable pageable);

    Page<PredictionHistory> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<PredictionHistory> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    Page<PredictionHistory> findByUserIdAndRegionOrderByCreatedAtDesc(UUID userId, String region, Pageable pageable);

    @Query("SELECT h.region, h.predictedType, COUNT(h) FROM PredictionHistory h GROUP BY h.region, h.predictedType")
    java.util.List<Object[]> countByPredictedType();

    @Query("SELECT h.region, h.predictedType, COUNT(h) FROM PredictionHistory h WHERE h.region = :region GROUP BY h.region, h.predictedType")
    java.util.List<Object[]> countByRegionAndPredictedType(@Param("region") String region);
}
