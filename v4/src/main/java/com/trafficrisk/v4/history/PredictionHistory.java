package com.trafficrisk.v4.history;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "prediction_history", schema = "production")
public class PredictionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "region", nullable = false, length = 20)
    private String region;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "input_json", nullable = false)
    private String inputJson;

    @Column(name = "predicted_type", nullable = false, length = 50)
    private String predictedType;

    @Column(name = "confidence", nullable = false, precision = 5, scale = 4)
    private BigDecimal confidence;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected PredictionHistory() {}

    public PredictionHistory(UUID userId, String region, String inputJson,
                              String predictedType, BigDecimal confidence) {
        this.userId = userId;
        this.region = region;
        this.inputJson = inputJson;
        this.predictedType = predictedType;
        this.confidence = confidence;
    }

    public Long getHistoryId() { return historyId; }
    public UUID getUserId() { return userId; }
    public String getRegion() { return region; }
    public String getInputJson() { return inputJson; }
    public String getPredictedType() { return predictedType; }
    public BigDecimal getConfidence() { return confidence; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
