package com.itsm.analytics.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * DTO for satisfaction statistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SatisfactionStatisticsDTO {
    
    private BigDecimal averageScore;
    private Integer totalResponses;
    private BigDecimal responseRate;
    private Map<Integer, Long> distribution; // score -> count
    private Map<String, Object> period;
    private Integer positiveCount;
    private Integer neutralCount;
    private Integer negativeCount;
}
