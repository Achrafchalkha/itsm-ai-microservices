package com.itsm.analytics.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itsm.analytics.application.service.KPICalculationEngine;
import com.itsm.analytics.application.service.SLAConfigurationService;
import com.itsm.analytics.infrastructure.client.TicketServiceClient;
import com.itsm.analytics.infrastructure.client.UserServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for AdminDashboardController
 */
@WebMvcTest(AdminDashboardController.class)
class AdminDashboardControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private KPICalculationEngine kpiCalculationEngine;
    
    @MockBean
    private SLAConfigurationService slaConfigurationService;
    
    @MockBean
    private TicketServiceClient ticketServiceClient;
    
    @MockBean
    private UserServiceClient userServiceClient;
    
    private KPICalculationEngine.GlobalKPIResult mockGlobalKPIs;
    
    @BeforeEach
    void setUp() {
        mockGlobalKPIs = new KPICalculationEngine.GlobalKPIResult();
        mockGlobalKPIs.setTotalTickets(100);
        mockGlobalKPIs.setTicketsResolved(85);
        mockGlobalKPIs.setSlaComplianceRate(BigDecimal.valueOf(92.5));
        mockGlobalKPIs.setAverageSatisfactionScore(BigDecimal.valueOf(4.2));
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAdminDashboard_Success() throws Exception {
        // Given
        when(kpiCalculationEngine.calculateGlobalKPIs(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(mockGlobalKPIs);
        when(ticketServiceClient.getTicketsBreachedSLA())
                .thenReturn(List.of());
        when(ticketServiceClient.getTicketsApproachingSLA(anyInt()))
                .thenReturn(List.of());
        when(userServiceClient.getAllTeams())
                .thenReturn(List.of());
        
        // When & Then
        mockMvc.perform(get("/api/analytics/admin/dashboard")
                .param("days", "30")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.globalKPIs").exists())
                .andExpect(jsonPath("$.globalKPIs.totalTickets").value(100))
                .andExpect(jsonPath("$.globalKPIs.ticketsResolved").value(85))
                .andExpect(jsonPath("$.slaOverview").exists())
                .andExpect(jsonPath("$.teamPerformance").exists())
                .andExpect(jsonPath("$.criticalTicketsCount").exists())
                .andExpect(jsonPath("$.approachingSLATicketsCount").exists());
    }
    
    @Test
    @WithMockUser(roles = "MANAGER")
    void testGetAdminDashboard_Forbidden() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/analytics/admin/dashboard")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetGlobalKPIs_Success() throws Exception {
        // Given
        when(kpiCalculationEngine.calculateGlobalKPIs(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(mockGlobalKPIs);
        
        // When & Then
        mockMvc.perform(get("/api/analytics/admin/kpis/global")
                .param("startDate", "2024-01-01")
                .param("endDate", "2024-01-31")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalTickets").value(100))
                .andExpect(jsonPath("$.ticketsResolved").value(85))
                .andExpect(jsonPath("$.resolutionRate").exists())
                .andExpect(jsonPath("$.slaComplianceRate").value(92.5))
                .andExpect(jsonPath("$.averageSatisfactionScore").value(4.2));
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllTickets_Success() throws Exception {
        // Given
        when(ticketServiceClient.getTicketsByPeriod(any(), any()))
                .thenReturn(List.of());
        
        // When & Then
        mockMvc.perform(get("/api/analytics/admin/tickets/all")
                .param("page", "0")
                .param("size", "20")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllTickets_WithFilters() throws Exception {
        // Given
        when(ticketServiceClient.getTicketsByPeriod(any(), any()))
                .thenReturn(List.of());
        
        // When & Then
        mockMvc.perform(get("/api/analytics/admin/tickets/all")
                .param("page", "0")
                .param("size", "20")
                .param("status", "OUVERT")
                .param("priority", "HAUTE")
                .param("category", "RESEAU")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetSLABreachedTickets_Success() throws Exception {
        // Given
        when(ticketServiceClient.getTicketsBreachedSLA())
                .thenReturn(List.of());
        
        // When & Then
        mockMvc.perform(get("/api/analytics/admin/tickets/sla-breached")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetSLAApproachingTickets_Success() throws Exception {
        // Given
        when(ticketServiceClient.getTicketsApproachingSLA(4))
                .thenReturn(List.of());
        
        // When & Then
        mockMvc.perform(get("/api/analytics/admin/tickets/sla-approaching")
                .param("hoursBeforeDeadline", "4")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetVolumeStatistics_Success() throws Exception {
        // Given
        when(ticketServiceClient.getTicketsByPeriod(any(), any()))
                .thenReturn(List.of());
        
        // When & Then
        mockMvc.perform(get("/api/analytics/admin/stats/volume")
                .param("startDate", "2024-01-01")
                .param("endDate", "2024-01-31")
                .param("groupBy", "daily")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalTickets").exists())
                .andExpect(jsonPath("$.resolvedTickets").exists())
                .andExpect(jsonPath("$.openTickets").exists())
                .andExpect(jsonPath("$.period").exists())
                .andExpect(jsonPath("$.groupBy").value("daily"));
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetSatisfactionStatistics_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/analytics/admin/stats/satisfaction")
                .param("startDate", "2024-01-01")
                .param("endDate", "2024-01-31")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.averageScore").exists())
                .andExpect(jsonPath("$.totalResponses").exists())
                .andExpect(jsonPath("$.responseRate").exists())
                .andExpect(jsonPath("$.distribution").exists())
                .andExpect(jsonPath("$.period").exists());
    }
    
    @Test
    void testGetAdminDashboard_Unauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/analytics/admin/dashboard"))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @WithMockUser(roles = "UTILISATEUR")
    void testGetAdminDashboard_InsufficientRole() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/analytics/admin/dashboard")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }
}
