package com.itsm.analytics.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * DTO for volume statistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VolumeStatisticsDTO {
    
    private Integer totalTickets;
    private Integer resolvedTickets;
    private Integer openTickets;
    private Integer closedTickets;
    private String groupBy; // daily, weekly, monthly
    private Map<String, Object> period;
    private List<VolumeDataPoint> volumeData;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VolumeDataPoint {
        private LocalDate date;
        private String label;
        private Integer created;
        private Integer resolved;
        private Integer closed;
    }
}
