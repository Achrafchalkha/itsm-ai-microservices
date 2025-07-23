package com.itsm.analytics.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itsm.analytics.AnalyticsServiceApplication;
import com.itsm.analytics.application.service.SLAConfigurationService;
import com.itsm.analytics.application.service.SatisfactionService;
import com.itsm.analytics.domain.model.SLAConfiguration;
import com.itsm.analytics.domain.model.SatisfactionScore;
import com.itsm.analytics.presentation.dto.CreateSLAConfigurationRequest;
import com.itsm.analytics.presentation.dto.CreateSatisfactionScoreRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Analytics Service
 * Tests the complete flow from REST API to database
 */
@SpringBootTest(classes = AnalyticsServiceApplication.class)
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class AnalyticsServiceIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private SLAConfigurationService slaConfigurationService;
    
    @Autowired
    private SatisfactionService satisfactionService;
    
    @Test
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/analytics/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("analytics-service"));
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void testSLAConfigurationFlow() throws Exception {
        // Create SLA configuration
        CreateSLAConfigurationRequest request = CreateSLAConfigurationRequest.builder()
                .categorie("RESEAU")
                .priorite("HAUTE")
                .delaiPremiereReponseHeures(2)
                .delaiResolutionHeures(8)
                .escaladeManagerHeures(4)
                .escaladeAdminHeures(6)
                .build();
        
        String requestJson = objectMapper.writeValueAsString(request);
        
        mockMvc.perform(post("/api/analytics/sla-configurations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.categorie").value("RESEAU"))
                .andExpect(jsonPath("$.priorite").value("HAUTE"))
                .andExpect(jsonPath("$.delaiPremiereReponseHeures").value(2))
                .andExpect(jsonPath("$.delaiResolutionHeures").value(8))
                .andExpect(jsonPath("$.actif").value(true));
        
        // Verify configuration was created
        var config = slaConfigurationService.getConfiguration("RESEAU", "HAUTE");
        assertTrue(config.isPresent());
        assertEquals("RESEAU", config.get().getCategorie());
        assertEquals("HAUTE", config.get().getPriorite());
        
        // Get all configurations
        mockMvc.perform(get("/api/analytics/sla-configurations")
                .with(csrf()))
                .andExpected(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }
    
    @Test
    @WithMockUser(roles = "UTILISATEUR")
    void testSatisfactionScoreFlow() throws Exception {
        UUID ticketId = UUID.randomUUID();
        UUID technicienId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        
        // Create satisfaction score
        CreateSatisfactionScoreRequest request = CreateSatisfactionScoreRequest.builder()
                .ticketId(ticketId)
                .technicienId(technicienId)
                .teamId(teamId)
                .score(4)
                .commentaire("Excellent service")
                .build();
        
        String requestJson = objectMapper.writeValueAsString(request);
        
        mockMvc.perform(post("/api/analytics/satisfaction")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ticketId").value(ticketId.toString()))
                .andExpect(jsonPath("$.score").value(4))
                .andExpect(jsonPath("$.commentaire").value("Excellent service"));
        
        // Verify satisfaction score was created
        var scores = satisfactionService.getSatisfactionScoresByTicket(ticketId);
        assertEquals(1, scores.size());
        assertEquals(4, scores.get(0).getScore());
        assertEquals("Excellent service", scores.get(0).getCommentaire());
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void testAdminDashboardAccess() throws Exception {
        mockMvc.perform(get("/api/analytics/admin/dashboard")
                .param("days", "30")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.globalKPIs").exists())
                .andExpect(jsonPath("$.slaOverview").exists())
                .andExpect(jsonPath("$.teamPerformance").exists());
    }
    
    @Test
    @WithMockUser(roles = "MANAGER")
    void testManagerDashboardAccess() throws Exception {
        mockMvc.perform(get("/api/analytics/manager/dashboard")
                .param("days", "30")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.teamKPIs").exists())
                .andExpect(jsonPath("$.technicianPerformance").exists())
                .andExpect(jsonPath("$.workloadOverview").exists());
    }
    
    @Test
    @WithMockUser(roles = "UTILISATEUR")
    void testAdminDashboardAccessDenied() throws Exception {
        mockMvc.perform(get("/api/analytics/admin/dashboard")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void testGlobalKPIsEndpoint() throws Exception {
        mockMvc.perform(get("/api/analytics/admin/kpis/global")
                .param("startDate", "2024-01-01")
                .param("endDate", "2024-01-31")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalTickets").exists())
                .andExpect(jsonPath("$.ticketsResolved").exists())
                .andExpect(jsonPath("$.resolutionRate").exists())
                .andExpect(jsonPath("$.slaComplianceRate").exists());
    }
    
    @Test
    @WithMockUser(roles = "MANAGER")
    void testManagerKPIsEndpoint() throws Exception {
        mockMvc.perform(get("/api/analytics/manager/kpis")
                .param("startDate", "2024-01-01")
                .param("endDate", "2024-01-31")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalTickets").exists())
                .andExpect(jsonPath("$.resolvedTickets").exists())
                .andExpect(jsonPath("$.averageResolutionTime").exists())
                .andExpect(jsonPath("$.slaComplianceRate").exists());
    }
    
    @Test
    void testUnauthorizedAccess() throws Exception {
        mockMvc.perform(get("/api/analytics/admin/dashboard"))
                .andExpect(status().isUnauthorized());
        
        mockMvc.perform(get("/api/analytics/manager/dashboard"))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void testVolumeStatistics() throws Exception {
        mockMvc.perform(get("/api/analytics/admin/stats/volume")
                .param("startDate", "2024-01-01")
                .param("endDate", "2024-01-31")
                .param("groupBy", "daily")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalTickets").exists())
                .andExpect(jsonPath("$.resolvedTickets").exists())
                .andExpect(jsonPath("$.groupBy").value("daily"));
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void testSatisfactionStatistics() throws Exception {
        mockMvc.perform(get("/api/analytics/admin/stats/satisfaction")
                .param("startDate", "2024-01-01")
                .param("endDate", "2024-01-31")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.averageScore").exists())
                .andExpect(jsonPath("$.totalResponses").exists())
                .andExpect(jsonPath("$.distribution").exists());
    }
}
