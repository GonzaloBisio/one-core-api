package com.one.core.application.controller.reports;

import com.one.core.domain.service.reports.ReportService;
import jakarta.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN')")
@Validated // Necesario para que la validación en el parámetro funcione
public class ReportController {

    private final ReportService reportService;

    @Autowired
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }


    @GetMapping("/operational-summary")
    public ResponseEntity<InputStreamResource> getOperationalSummaryReport(
            @RequestParam
            @Pattern(regexp = "DAILY|WEEKLY|MONTHLY", message = "Report type must be DAILY, WEEKLY, or MONTHLY")
            String type,
            @RequestParam("date") String dateString) {

        LocalDate date = LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE);

        ByteArrayInputStream bis = reportService.generateOperationalSummaryReport(type, date);

        HttpHeaders headers = new HttpHeaders();
        String filename = type + "_Report_" + date.toString() + ".xlsx";
        headers.add("Content-Disposition", "attachment; filename=" + filename);

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(bis));
    }
}