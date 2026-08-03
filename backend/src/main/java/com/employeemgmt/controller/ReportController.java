package com.employeemgmt.controller;

import com.employeemgmt.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasRole('ADMIN')")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/employees-by-department")
    public ResponseEntity<List<Map<String, Object>>> getEmployeesByDepartment() {
        return ResponseEntity.ok(reportService.getEmployeesByDepartment());
    }

    @GetMapping("/attendance-summary")
    public ResponseEntity<List<Map<String, Object>>> getAttendanceSummary(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        return ResponseEntity.ok(reportService.getAttendanceSummary(startDate, endDate));
    }

    @GetMapping("/leave-summary")
    public ResponseEntity<List<Map<String, Object>>> getLeaveSummary() {
        return ResponseEntity.ok(reportService.getLeaveSummary());
    }

    @GetMapping("/payroll-summary")
    public ResponseEntity<List<Map<String, Object>>> getPayrollSummary() {
        return ResponseEntity.ok(reportService.getPayrollSummary());
    }
}
