package com.employeemgmt.controller;

import com.employeemgmt.dto.PayrollRequest;
import com.employeemgmt.model.Payroll;
import com.employeemgmt.service.PayrollService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payroll")
public class PayrollController {

    private final PayrollService payrollService;

    public PayrollController(PayrollService payrollService) {
        this.payrollService = payrollService;
    }

    @GetMapping
    public ResponseEntity<List<Payroll>> getAllPayrolls(
            @RequestParam(required = false) String employeeId,
            @RequestParam(required = false) Integer payMonth,
            @RequestParam(required = false) Integer payYear) {
        return ResponseEntity.ok(payrollService.getAllPayrolls(employeeId, payMonth, payYear));
    }

    @PostMapping("/generate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Payroll>> generatePayroll(@Valid @RequestBody PayrollRequest request) {
        return ResponseEntity.ok(payrollService.generatePayroll(request));
    }

    @PutMapping("/{id}/process")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Payroll> processPayroll(@PathVariable String id) {
        return ResponseEntity.ok(payrollService.processPayroll(id));
    }

    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getPayrollSummary() {
        return ResponseEntity.ok(payrollService.getPayrollSummary());
    }
}
