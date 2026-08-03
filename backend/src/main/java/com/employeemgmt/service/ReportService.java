package com.employeemgmt.service;

import com.employeemgmt.model.Attendance;
import com.employeemgmt.model.Department;
import com.employeemgmt.model.Employee;
import com.employeemgmt.model.Leave;
import com.employeemgmt.model.Payroll;
import com.employeemgmt.repository.AttendanceRepository;
import com.employeemgmt.repository.DepartmentRepository;
import com.employeemgmt.repository.EmployeeRepository;
import com.employeemgmt.repository.LeaveRepository;
import com.employeemgmt.repository.PayrollRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;
    private final LeaveRepository leaveRepository;
    private final PayrollRepository payrollRepository;

    public ReportService(DepartmentRepository departmentRepository,
                         EmployeeRepository employeeRepository,
                         AttendanceRepository attendanceRepository,
                         LeaveRepository leaveRepository,
                         PayrollRepository payrollRepository) {
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
        this.attendanceRepository = attendanceRepository;
        this.leaveRepository = leaveRepository;
        this.payrollRepository = payrollRepository;
    }

    public List<Map<String, Object>> getEmployeesByDepartment() {
        List<Department> departments = departmentRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Department dept : departments) {
            List<Employee> deptEmployees = employeeRepository.findByDepartmentId(dept.getId());
            long activeCount = deptEmployees.stream().filter(e -> "active".equals(e.getStatus())).count();
            long inactiveCount = deptEmployees.stream().filter(e -> "inactive".equals(e.getStatus())).count();

            Map<String, Object> item = new HashMap<>();
            item.put("department", dept.getName());
            item.put("employee_count", deptEmployees.size());
            item.put("active_count", activeCount);
            item.put("inactive_count", inactiveCount);
            result.add(item);
        }

        return result;
    }

    public List<Map<String, Object>> getAttendanceSummary(LocalDate startDate, LocalDate endDate) {
        List<Attendance> attendances = attendanceRepository.findByDateBetween(startDate, endDate);
        List<Department> departments = departmentRepository.findAll();

        List<Map<String, Object>> result = new ArrayList<>();
        for (Department dept : departments) {
            List<Employee> deptEmployees = employeeRepository.findByDepartmentId(dept.getId());
            Set<String> deptEmployeeIds = deptEmployees.stream().map(Employee::getId).collect(Collectors.toSet());

            List<Attendance> deptAttendances = attendances.stream()
                    .filter(a -> deptEmployeeIds.contains(a.getEmployeeId()))
                    .collect(Collectors.toList());

            long deptPresent = deptAttendances.stream().filter(a -> "present".equals(a.getStatus())).count();
            long deptAbsent = deptAttendances.stream().filter(a -> "absent".equals(a.getStatus())).count();
            long deptLate = deptAttendances.stream().filter(a -> "late".equals(a.getStatus())).count();
            long deptTotal = deptAttendances.size();

            Map<String, Object> deptData = new HashMap<>();
            deptData.put("department", dept.getName());
            deptData.put("total_attendance", deptTotal);
            deptData.put("present", deptPresent);
            deptData.put("absent", deptAbsent);
            deptData.put("late", deptLate);
            deptData.put("attendance_percentage", deptTotal > 0 ? Math.round((double) deptPresent / deptTotal * 100) : 0);

            result.add(deptData);
        }

        return result;
    }

    public List<Map<String, Object>> getLeaveSummary() {
        List<Map<String, Object>> result = new ArrayList<>();

        addLeaveTypeData(result, "Annual", leaveRepository);
        addLeaveTypeData(result, "Sick", leaveRepository);
        addLeaveTypeData(result, "Casual", leaveRepository);

        return result;
    }

    private void addLeaveTypeData(List<Map<String, Object>> result, String type, LeaveRepository repo) {
        long totalApplied = repo.countByTypeAndStatus(type, "approved")
                + repo.countByTypeAndStatus(type, "rejected")
                + repo.countByTypeAndStatus(type, "pending");
        long approved = repo.countByTypeAndStatus(type, "approved");
        long rejected = repo.countByTypeAndStatus(type, "rejected");
        long pending = repo.countByTypeAndStatus(type, "pending");

        Map<String, Object> item = new HashMap<>();
        item.put("type", type);
        item.put("total_applied", totalApplied);
        item.put("approved", approved);
        item.put("rejected", rejected);
        item.put("pending", pending);
        item.put("total_days", approved * 5);
        result.add(item);
    }

    public List<Map<String, Object>> getPayrollSummary() {
        List<Payroll> allPayrolls = payrollRepository.findAll();

        Map<String, List<Payroll>> groupedByMonth = allPayrolls.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getPayYear() + "-" + String.format("%02d", p.getPayMonth()),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, List<Payroll>> entry : groupedByMonth.entrySet()) {
            String[] parts = entry.getKey().split("-");
            List<Payroll> monthPayrolls = entry.getValue();

            double totalBasic = monthPayrolls.stream().mapToDouble(Payroll::getBasicPay).sum();
            double totalAllowances = monthPayrolls.stream().mapToDouble(Payroll::getAllowances).sum();
            double totalDeductions = monthPayrolls.stream().mapToDouble(Payroll::getDeductions).sum();
            double totalNet = monthPayrolls.stream().mapToDouble(Payroll::getNetPay).sum();

            Map<String, Object> item = new HashMap<>();
            item.put("month", Integer.parseInt(parts[1]));
            item.put("year", Integer.parseInt(parts[0]));
            item.put("employee_count", monthPayrolls.size());
            item.put("total_basic", totalBasic);
            item.put("total_allowances", totalAllowances);
            item.put("total_deductions", totalDeductions);
            item.put("total_net_pay", totalNet);
            item.put("average_net_pay", monthPayrolls.size() > 0 ? totalNet / monthPayrolls.size() : 0);
            result.add(item);
        }

        return result;
    }
}
