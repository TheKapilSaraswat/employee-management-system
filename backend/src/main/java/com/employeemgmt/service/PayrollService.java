package com.employeemgmt.service;

import com.employeemgmt.dto.PayrollRequest;
import com.employeemgmt.model.Employee;
import com.employeemgmt.model.Payroll;
import com.employeemgmt.repository.EmployeeRepository;
import com.employeemgmt.repository.PayrollRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class PayrollService {

    private final PayrollRepository payrollRepository;
    private final EmployeeRepository employeeRepository;

    public PayrollService(PayrollRepository payrollRepository,
                          EmployeeRepository employeeRepository) {
        this.payrollRepository = payrollRepository;
        this.employeeRepository = employeeRepository;
    }

    public List<Payroll> getAllPayrolls(String employeeId, Integer payMonth, Integer payYear) {
        if (employeeId != null && !employeeId.isEmpty() && payMonth != null && payYear != null) {
            return payrollRepository.findByEmployeeIdAndPayMonthAndPayYear(employeeId, payMonth, payYear);
        } else if (payMonth != null && payYear != null) {
            return payrollRepository.findByPayMonthAndPayYear(payMonth, payYear);
        } else if (employeeId != null && !employeeId.isEmpty()) {
            return payrollRepository.findByEmployeeId(employeeId);
        }
        return payrollRepository.findAll();
    }

    @Transactional
    public List<Payroll> generatePayroll(PayrollRequest request) {
        List<Employee> activeEmployees = employeeRepository.findByStatus("active");
        List<Payroll> generatedPayrolls = new ArrayList<>();

        for (Employee emp : activeEmployees) {
            Optional<Payroll> existing = payrollRepository
                    .findByEmployeeIdAndPayMonthAndPayYearAndStatus(emp.getId(), request.getPayMonth(), request.getPayYear(), "pending");

            if (existing.isPresent()) {
                continue;
            }

            Payroll payroll = new Payroll();
            payroll.setEmployeeId(emp.getId());
            payroll.setPayMonth(request.getPayMonth());
            payroll.setPayYear(request.getPayYear());
            payroll.setBasicPay(emp.getSalary() != null ? emp.getSalary() : 0.0);
            payroll.setAllowances(0.0);
            payroll.setDeductions(0.0);
            payroll.setNetPay(payroll.getBasicPay() + payroll.getAllowances() - payroll.getDeductions());

            generatedPayrolls.add(payrollRepository.save(payroll));
        }

        return generatedPayrolls;
    }

    @Transactional
    public Payroll processPayroll(String id) {
        Payroll payroll = payrollRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payroll record not found"));

        payroll.setStatus("paid");
        payroll.setPaidAt(LocalDateTime.now());

        return payrollRepository.save(payroll);
    }

    public Map<String, Object> getPayrollSummary() {
        List<Payroll> allPayrolls = payrollRepository.findAll();

        double totalGross = allPayrolls.stream().mapToDouble(Payroll::getBasicPay).sum();
        double totalAllowances = allPayrolls.stream().mapToDouble(Payroll::getAllowances).sum();
        double totalDeductions = allPayrolls.stream().mapToDouble(Payroll::getDeductions).sum();
        double totalNet = allPayrolls.stream().mapToDouble(Payroll::getNetPay).sum();
        long pendingCount = allPayrolls.stream().filter(p -> "pending".equals(p.getStatus())).count();
        long paidCount = allPayrolls.stream().filter(p -> "paid".equals(p.getStatus())).count();

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalGross", totalGross);
        summary.put("totalAllowances", totalAllowances);
        summary.put("totalDeductions", totalDeductions);
        summary.put("totalNet", totalNet);
        summary.put("pendingCount", pendingCount);
        summary.put("paidCount", paidCount);
        summary.put("totalRecords", allPayrolls.size());

        return summary;
    }
}
