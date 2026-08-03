package com.employeemgmt.service;

import com.employeemgmt.dto.EmployeeRequest;
import com.employeemgmt.model.Department;
import com.employeemgmt.model.Employee;
import com.employeemgmt.repository.DepartmentRepository;
import com.employeemgmt.repository.EmployeeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    public EmployeeService(EmployeeRepository employeeRepository,
                           DepartmentRepository departmentRepository) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
    }

    public Map<String, Object> getAllEmployees(String search, String departmentId,
                                               String status, int page, int size) {
        List<Employee> employees;

        if (search != null && !search.isEmpty()) {
            if (status != null && !status.isEmpty()) {
                employees = employeeRepository.searchEmployees(status, search);
            } else {
                employees = employeeRepository.searchEmployeesAll(search);
            }
        } else if (departmentId != null && !departmentId.isEmpty()) {
            if (status != null && !status.isEmpty()) {
                employees = employeeRepository.findByDepartmentIdAndStatus(departmentId, status);
            } else {
                employees = employeeRepository.findByDepartmentId(departmentId);
            }
        } else if (status != null && !status.isEmpty()) {
            employees = employeeRepository.findByStatus(status);
        } else {
            employees = employeeRepository.findAll();
        }

        int total = employees.size();
        int start = page * size;
        int end = Math.min(start + size, total);

        List<Employee> pageEmployees;
        if (start >= total) {
            pageEmployees = new ArrayList<>();
        } else {
            pageEmployees = employees.subList(start, end);
        }

        List<Map<String, Object>> employeeList = pageEmployees.stream().map(emp -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", emp.getId());
            map.put("employeeCode", emp.getEmployeeCode());
            map.put("firstName", emp.getFirstName());
            map.put("lastName", emp.getLastName());
            map.put("email", emp.getEmail());
            map.put("phone", emp.getPhone());
            map.put("position", emp.getPosition());
            map.put("departmentId", emp.getDepartmentId());
            map.put("salary", emp.getSalary());
            map.put("hireDate", emp.getHireDate());
            map.put("status", emp.getStatus());
            map.put("createdAt", emp.getCreatedAt());
            map.put("updatedAt", emp.getUpdatedAt());

            if (emp.getDepartmentId() != null) {
                departmentRepository.findById(emp.getDepartmentId())
                        .ifPresent(dept -> map.put("departmentName", dept.getName()));
            }

            return map;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("employees", employeeList);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", (int) Math.ceil((double) total / size));

        return result;
    }

    public Map<String, Object> getEmployee(String id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));

        Map<String, Object> map = new HashMap<>();
        map.put("id", employee.getId());
        map.put("employeeCode", employee.getEmployeeCode());
        map.put("firstName", employee.getFirstName());
        map.put("lastName", employee.getLastName());
        map.put("email", employee.getEmail());
        map.put("phone", employee.getPhone());
        map.put("position", employee.getPosition());
        map.put("departmentId", employee.getDepartmentId());
        map.put("salary", employee.getSalary());
        map.put("hireDate", employee.getHireDate());
        map.put("status", employee.getStatus());
        map.put("createdAt", employee.getCreatedAt());
        map.put("updatedAt", employee.getUpdatedAt());

        if (employee.getDepartmentId() != null) {
            departmentRepository.findById(employee.getDepartmentId())
                    .ifPresent(dept -> map.put("departmentName", dept.getName()));
        }

        return map;
    }

    public Employee createEmployee(EmployeeRequest request) {
        Employee employee = new Employee();
        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setEmail(request.getEmail());
        employee.setPhone(request.getPhone());
        employee.setPosition(request.getPosition());
        employee.setDepartmentId(request.getDepartmentId());
        employee.setSalary(request.getSalary());
        employee.setHireDate(request.getHireDate());
        employee.setEmployeeCode(generateEmployeeCode());

        if (request.getDepartmentId() != null && !request.getDepartmentId().isEmpty()) {
            departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Department not found"));
        }

        return employeeRepository.save(employee);
    }

    public Employee updateEmployee(String id, EmployeeRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));

        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setEmail(request.getEmail());
        employee.setPhone(request.getPhone());
        employee.setPosition(request.getPosition());
        employee.setDepartmentId(request.getDepartmentId());
        employee.setSalary(request.getSalary());
        employee.setHireDate(request.getHireDate());

        if (request.getDepartmentId() != null && !request.getDepartmentId().isEmpty()) {
            departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Department not found"));
        }

        return employeeRepository.save(employee);
    }

    public void deactivateEmployee(String id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));

        employee.setStatus("inactive");
        employeeRepository.save(employee);
    }

    private String generateEmployeeCode() {
        Optional<String> maxCode = employeeRepository.findMaxEmployeeCode();
        int nextId = 1;

        if (maxCode.isPresent()) {
            String code = maxCode.get();
            int num = Integer.parseInt(code.substring(4));
            nextId = num + 1;
        }

        return String.format("EMP-%03d", nextId);
    }
}
