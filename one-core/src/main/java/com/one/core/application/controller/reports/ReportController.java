package com.one.core.application.controller.reports;

import com.one.core.application.dto.reports.OperationalReportJsonDTO;
import com.one.core.application.dto.reports.ReportFilterDTO;
import com.one.core.domain.service.reports.ReportService;
import jakarta.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.data.domain.Pageable;
import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN')")
@Validated
public class ReportController {

    private final ReportService reportService;

    @Autowired
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/operational-summary")
    public ResponseEntity<InputStreamResource> getOperationalSummaryReport(
            @RequestParam @Pattern(regexp = "DAILY|WEEKLY|MONTHLY", message = "El tipo de reporte debe ser DAILY, WEEKLY, o MONTHLY") String type,
            @RequestParam("date") String dateString) {

        LocalDate date = LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE);

        ByteArrayInputStream bis = reportService.generateOperationalSummaryReport(type, date);

        HttpHeaders headers = new HttpHeaders();
        String typeFormatted = type.substring(0, 1).toUpperCase() + type.substring(1).toLowerCase();
        String filename = String.format("%s_Report_%s.xlsx", typeFormatted, date.toString());


        headers.add("Content-Disposition", "attachment; filename=" + filename);

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(bis));
    }

    @GetMapping("/operational-summary/json")
    public ResponseEntity<OperationalReportJsonDTO> getOperationalSummaryJson(
            @RequestParam @Pattern(regexp = "DAILY|WEEKLY|MONTHLY", message = "El tipo de reporte debe ser DAILY, WEEKLY, o MONTHLY") String type,
            @RequestParam("date") String dateString,
            ReportFilterDTO filter, // Spring mapea los query params a los campos del DTO
            @PageableDefault(size = 10, sort = "orderDate", direction = Sort.Direction.DESC) Pageable pageable) {

        LocalDate date = LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE);

        OperationalReportJsonDTO reportData = reportService.getOperationalSummaryJson(type, date, filter, pageable);

        return ResponseEntity.ok(reportData);
    }
}