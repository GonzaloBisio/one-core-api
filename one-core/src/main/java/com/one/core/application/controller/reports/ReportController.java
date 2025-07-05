package com.one.core.application.controller.reports;

import com.one.core.domain.service.reports.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN')")
public class ReportController {

    private final ReportService reportService;

    @Autowired
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/weekly-summary")
    public ResponseEntity<InputStreamResource> getWeeklySummaryReport(
            @RequestParam("date") String dateString) {

        LocalDate date = LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE);

        ByteArrayInputStream bis = reportService.generateWeeklySummaryReport(date);

        HttpHeaders headers = new HttpHeaders();
        String filename = "Weekly_Report_" + date.toString() + ".xlsx";
        headers.add("Content-Disposition", "attachment; filename=" + filename);

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(bis));
    }
}