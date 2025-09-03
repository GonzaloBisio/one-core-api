package com.one.core.application.controller.reports;

import com.one.core.application.controller.reports.ReportController;
import com.one.core.application.dto.reports.OperationalReportJsonDTO;
import com.one.core.application.dto.reports.ReportFilterDTO;
import com.one.core.domain.service.reports.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReportControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ReportService reportService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(new ReportController(reportService))
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void rejectsInvalidPaymentMethod() throws Exception {
        mockMvc.perform(get("/api/reports/operational-summary/json")
                        .param("type", "DAILY")
                        .param("date", "2024-01-01")
                        .param("paymentMethod", "CHEQUE"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void acceptsValidPaymentMethod() throws Exception {
        when(reportService.getOperationalSummaryJson(anyString(), any(LocalDate.class), any(ReportFilterDTO.class), any(Pageable.class)))
                .thenReturn(new OperationalReportJsonDTO());

        mockMvc.perform(get("/api/reports/operational-summary/json")
                        .param("type", "DAILY")
                        .param("date", "2024-01-01")
                        .param("paymentMethod", "DEBIT"))
                .andExpect(status().isOk());

        verify(reportService).getOperationalSummaryJson(eq("DAILY"), eq(LocalDate.of(2024, 1, 1)), any(ReportFilterDTO.class), any(Pageable.class));
    }
}
