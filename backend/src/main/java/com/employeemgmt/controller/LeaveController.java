package com.employeemgmt.controller;

import com.employeemgmt.dto.LeaveRequest;
import com.employeemgmt.model.Leave;
import com.employeemgmt.service.LeaveService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/leaves")
public class LeaveController {

    private final LeaveService leaveService;

    public LeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    @GetMapping
    public ResponseEntity<List<Leave>> getAllLeaves(
            @RequestParam(required = false) String employeeId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        return ResponseEntity.ok(leaveService.getAllLeaves(employeeId, status, startDate, endDate));
    }

    @PostMapping
    public ResponseEntity<Leave> applyLeave(@Valid @RequestBody LeaveRequest request) {
        return ResponseEntity.ok(leaveService.applyLeave(request));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Leave> approveLeave(@PathVariable String id, Authentication authentication) {
        return ResponseEntity.ok(leaveService.approveLeave(id, authentication.getName()));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Leave> rejectLeave(@PathVariable String id, Authentication authentication) {
        return ResponseEntity.ok(leaveService.rejectLeave(id, authentication.getName()));
    }

    @GetMapping("/balance/{employeeId}")
    public ResponseEntity<Map<String, Object>> getLeaveBalance(@PathVariable String employeeId) {
        return ResponseEntity.ok(leaveService.getLeaveBalance(employeeId));
    }
}
