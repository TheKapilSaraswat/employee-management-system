package com.employeemgmt.service;

import com.employeemgmt.dto.LeaveRequest;
import com.employeemgmt.model.Leave;
import com.employeemgmt.repository.LeaveRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LeaveService {

    private final LeaveRepository leaveRepository;

    public LeaveService(LeaveRepository leaveRepository) {
        this.leaveRepository = leaveRepository;
    }

    public List<Leave> getAllLeaves(String employeeId, String status,
                                    LocalDate startDate, LocalDate endDate) {
        if (employeeId != null && !employeeId.isEmpty() && status != null && !status.isEmpty()) {
            return leaveRepository.findByEmployeeIdAndStatus(employeeId, status);
        } else if (employeeId != null && !employeeId.isEmpty()) {
            return leaveRepository.findByEmployeeId(employeeId);
        } else if (status != null && !status.isEmpty()) {
            return leaveRepository.findByStatus(status);
        } else if (startDate != null && endDate != null) {
            return leaveRepository.findByStartDateBetween(startDate, endDate);
        }
        return leaveRepository.findAll();
    }

    public Leave applyLeave(LeaveRequest request) {
        Leave leave = new Leave();
        leave.setEmployeeId(request.getEmployeeId());
        leave.setType(request.getType());
        leave.setStartDate(request.getStartDate());
        leave.setEndDate(request.getEndDate());
        leave.setReason(request.getReason());

        return leaveRepository.save(leave);
    }

    public Leave approveLeave(String id, String userId) {
        Leave leave = leaveRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Leave not found"));

        leave.setStatus("approved");
        leave.setApprovedBy(userId);

        return leaveRepository.save(leave);
    }

    public Leave rejectLeave(String id, String userId) {
        Leave leave = leaveRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Leave not found"));

        leave.setStatus("rejected");
        leave.setApprovedBy(userId);

        return leaveRepository.save(leave);
    }

    public Map<String, Object> getLeaveBalance(String employeeId) {
        List<Leave> annualLeaves = leaveRepository.findByEmployeeIdAndTypeAndStatus(employeeId, "Annual", "approved");
        List<Leave> sickLeaves = leaveRepository.findByEmployeeIdAndTypeAndStatus(employeeId, "Sick", "approved");
        List<Leave> casualLeaves = leaveRepository.findByEmployeeIdAndTypeAndStatus(employeeId, "Casual", "approved");

        int annualUsed = annualLeaves.stream()
                .mapToInt(l -> (int) (l.getEndDate().toEpochDay() - l.getStartDate().toEpochDay()) + 1)
                .sum();
        int sickUsed = sickLeaves.stream()
                .mapToInt(l -> (int) (l.getEndDate().toEpochDay() - l.getStartDate().toEpochDay()) + 1)
                .sum();
        int casualUsed = casualLeaves.stream()
                .mapToInt(l -> (int) (l.getEndDate().toEpochDay() - l.getStartDate().toEpochDay()) + 1)
                .sum();

        Map<String, Object> balance = new HashMap<>();
        balance.put("annual", Map.of("total", 20, "used", annualUsed, "remaining", 20 - annualUsed));
        balance.put("sick", Map.of("total", 12, "used", sickUsed, "remaining", 12 - sickUsed));
        balance.put("casual", Map.of("total", 10, "used", casualUsed, "remaining", 10 - casualUsed));

        return balance;
    }
}
