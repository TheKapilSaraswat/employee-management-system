package com.employeemgmt.controller;

import com.employeemgmt.dto.*;
import com.employeemgmt.model.*;
import com.employeemgmt.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService employeeService;
    @MockBean
    private DepartmentService departmentService;
    @MockBean
    private AuthService authService;
    @MockBean
    private AttendanceService attendanceService;
    @MockBean
    private LeaveService leaveService;
    @MockBean
    private PayrollService payrollService;
    @MockBean
    private ReportService reportService;

    // ==================== HealthController ====================

    @Test
    @DisplayName("GET /api/health returns status ok")
    void health_ok() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ==================== AuthController ====================

    @Test
    @DisplayName("POST /api/auth/register success")
    void register_success() throws Exception {
        AuthResponse resp = new AuthResponse("token-123", "user-1", "Admin", "admin@test.com", "ADMIN");
        when(authService.register(any(RegisterRequest.class))).thenReturn(resp);

        RegisterRequest req = new RegisterRequest();
        req.setName("Admin");
        req.setEmail("admin@test.com");
        req.setPassword("password");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-123"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    @DisplayName("POST /api/auth/login success")
    void login_success() throws Exception {
        AuthResponse resp = new AuthResponse("token-456", "user-2", "Employee", "emp@test.com", "EMPLOYEE");
        when(authService.login(any(LoginRequest.class))).thenReturn(resp);

        LoginRequest req = new LoginRequest();
        req.setEmail("emp@test.com");
        req.setPassword("password");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-456"));
    }

    @Test
    @DisplayName("GET /api/auth/me with auth returns user")
    @WithMockUser
    void me_withAuth() throws Exception {
        User user = new User();
        user.setId("user-1");
        user.setName("Admin");
        user.setEmail("admin@test.com");
        user.setRole("ADMIN");
        when(authService.getCurrentUser(anyString())).thenReturn(user);

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Admin"))
                .andExpect(jsonPath("$.email").value("admin@test.com"));
    }

    // ==================== EmployeeController ====================

    @Test
    @DisplayName("GET /api/employees returns paginated results")
    @WithMockUser
    void employees_getAll() throws Exception {
        Map<String, Object> result = new HashMap<>();
        result.put("employees", List.of());
        result.put("total", 0);
        result.put("page", 0);
        result.put("size", 10);
        result.put("totalPages", 0);
        when(employeeService.getAllEmployees(null, null, null, 0, 10)).thenReturn(result);

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0));
    }

    @Test
    @DisplayName("GET /api/employees with query params")
    @WithMockUser
    void employees_withParams() throws Exception {
        Map<String, Object> result = new HashMap<>();
        result.put("employees", List.of());
        result.put("total", 0);
        result.put("page", 0);
        result.put("size", 5);
        result.put("totalPages", 0);
        when(employeeService.getAllEmployees("John", "dept-1", "active", 0, 5)).thenReturn(result);

        mockMvc.perform(get("/api/employees")
                        .param("search", "John")
                        .param("departmentId", "dept-1")
                        .param("status", "active")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(5));
    }

    @Test
    @DisplayName("POST /api/employees creates employee")
    @WithMockUser
    void employees_create() throws Exception {
        Employee emp = new Employee();
        emp.setId("emp-1");
        emp.setFirstName("John");
        emp.setLastName("Doe");
        emp.setEmail("john@test.com");
        emp.setEmployeeCode("EMP-001");
        emp.setStatus("active");
        when(employeeService.createEmployee(any(EmployeeRequest.class))).thenReturn(emp);

        EmployeeRequest req = new EmployeeRequest();
        req.setFirstName("John");
        req.setLastName("Doe");
        req.setEmail("john@test.com");

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.first_name").value("John"))
                .andExpect(jsonPath("$.employee_code").value("EMP-001"));
    }

    @Test
    @DisplayName("POST /api/employees validation fails with blank name")
    @WithMockUser
    void employees_createValidation() throws Exception {
        EmployeeRequest req = new EmployeeRequest();
        req.setFirstName("");
        req.setEmail("test@test.com");

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/employees/{id} returns employee")
    @WithMockUser
    void employees_getById() throws Exception {
        Map<String, Object> result = new HashMap<>();
        result.put("id", "emp-1");
        result.put("firstName", "John");
        when(employeeService.getEmployee("emp-1")).thenReturn(result);

        mockMvc.perform(get("/api/employees/emp-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    @DisplayName("PUT /api/employees/{id} updates employee")
    @WithMockUser
    void employees_update() throws Exception {
        Employee emp = new Employee();
        emp.setId("emp-1");
        emp.setFirstName("Updated");
        when(employeeService.updateEmployee(eq("emp-1"), any(EmployeeRequest.class))).thenReturn(emp);

        EmployeeRequest req = new EmployeeRequest();
        req.setFirstName("Updated");
        req.setLastName("Doe");
        req.setEmail("test@test.com");

        mockMvc.perform(put("/api/employees/emp-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.first_name").value("Updated"));
    }

    @Test
    @DisplayName("DELETE /api/employees/{id} deactivates employee")
    @WithMockUser
    void employees_delete() throws Exception {
        mockMvc.perform(delete("/api/employees/emp-1"))
                .andExpect(status().isNoContent());
        verify(employeeService).deactivateEmployee("emp-1");
    }

    // ==================== DepartmentController ====================

    @Test
    @DisplayName("GET /api/departments returns list")
    @WithMockUser
    void departments_getAll() throws Exception {
        Map<String, Object> dept = new HashMap<>();
        dept.put("id", "dept-1");
        dept.put("name", "Engineering");
        dept.put("employeeCount", 5L);
        when(departmentService.getAllDepartments()).thenReturn(List.of(dept));

        mockMvc.perform(get("/api/departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Engineering"));
    }

    @Test
    @DisplayName("POST /api/departments requires ADMIN role")
    @WithMockUser(roles = "EMPLOYEE")
    void departments_create_requiresAdmin() throws Exception {
        DepartmentRequest req = new DepartmentRequest();
        req.setName("New Dept");

        mockMvc.perform(post("/api/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/departments creates department as ADMIN")
    @WithMockUser(roles = "ADMIN")
    void departments_create_asAdmin() throws Exception {
        Department dept = new Department();
        dept.setId("dept-2");
        dept.setName("New Dept");
        when(departmentService.createDepartment(any(DepartmentRequest.class))).thenReturn(dept);

        DepartmentRequest req = new DepartmentRequest();
        req.setName("New Dept");

        mockMvc.perform(post("/api/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Dept"));
    }

    @Test
    @DisplayName("PUT /api/departments/{id} requires ADMIN role")
    @WithMockUser(roles = "EMPLOYEE")
    void departments_update_requiresAdmin() throws Exception {
        DepartmentRequest req = new DepartmentRequest();
        req.setName("Updated");

        mockMvc.perform(put("/api/departments/dept-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/departments/{id} requires ADMIN role")
    @WithMockUser(roles = "EMPLOYEE")
    void departments_delete_requiresAdmin() throws Exception {
        mockMvc.perform(delete("/api/departments/dept-1"))
                .andExpect(status().isForbidden());
    }

    // ==================== AttendanceController ====================

    @Test
    @DisplayName("GET /api/attendance returns records")
    @WithMockUser
    void attendance_getAll() throws Exception {
        when(attendanceService.getAllAttendances(null, null, null)).thenReturn(List.of());
        mockMvc.perform(get("/api/attendance"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/attendance records attendance")
    @WithMockUser
    void attendance_record() throws Exception {
        Attendance att = new Attendance();
        att.setId("att-1");
        att.setEmployeeId("emp-1");
        att.setStatus("present");
        when(attendanceService.recordAttendance(any(AttendanceRequest.class))).thenReturn(att);

        AttendanceRequest req = new AttendanceRequest();
        req.setEmployeeId("emp-1");
        req.setAction("in");

        mockMvc.perform(post("/api/attendance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("present"));
    }

    @Test
    @DisplayName("GET /api/attendance/today returns today records")
    @WithMockUser
    void attendance_getToday() throws Exception {
        when(attendanceService.getTodayAttendance()).thenReturn(List.of());
        mockMvc.perform(get("/api/attendance/today"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/attendance/{id} updates record")
    @WithMockUser
    void attendance_update() throws Exception {
        Attendance att = new Attendance();
        att.setId("att-1");
        when(attendanceService.updateAttendance(eq("att-1"), any(AttendanceRequest.class))).thenReturn(att);

        AttendanceRequest req = new AttendanceRequest();
        req.setEmployeeId("emp-1");
        req.setNotes("Updated");

        mockMvc.perform(put("/api/attendance/att-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    // ==================== LeaveController ====================

    @Test
    @DisplayName("GET /api/leaves returns list")
    @WithMockUser
    void leaves_getAll() throws Exception {
        when(leaveService.getAllLeaves(null, null, null, null)).thenReturn(List.of());
        mockMvc.perform(get("/api/leaves"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/leaves applies leave")
    @WithMockUser
    void leaves_apply() throws Exception {
        Leave leave = new Leave();
        leave.setId("leave-1");
        leave.setStatus("pending");
        when(leaveService.applyLeave(any(LeaveRequest.class))).thenReturn(leave);

        LeaveRequest req = new LeaveRequest();
        req.setEmployeeId("emp-1");
        req.setType("Annual");
        req.setStartDate(LocalDate.now());
        req.setEndDate(LocalDate.now().plusDays(3));

        mockMvc.perform(post("/api/leaves")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("pending"));
    }

    @Test
    @DisplayName("PUT /api/leaves/{id}/approve requires ADMIN")
    @WithMockUser(roles = "EMPLOYEE")
    void leaves_approve_requiresAdmin() throws Exception {
        mockMvc.perform(put("/api/leaves/leave-1/approve"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /api/leaves/{id}/approve as ADMIN")
    @WithMockUser(roles = "ADMIN")
    void leaves_approve_asAdmin() throws Exception {
        Leave leave = new Leave();
        leave.setId("leave-1");
        leave.setStatus("approved");
        when(leaveService.approveLeave(eq("leave-1"), anyString())).thenReturn(leave);

        mockMvc.perform(put("/api/leaves/leave-1/approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("approved"));
    }

    @Test
    @DisplayName("PUT /api/leaves/{id}/reject requires ADMIN")
    @WithMockUser(roles = "EMPLOYEE")
    void leaves_reject_requiresAdmin() throws Exception {
        mockMvc.perform(put("/api/leaves/leave-1/reject"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/leaves/balance/{employeeId} returns balance")
    @WithMockUser
    void leaves_balance() throws Exception {
        Map<String, Object> balance = Map.of(
                "annual", Map.of("total", 20, "used", 5, "remaining", 15),
                "sick", Map.of("total", 12, "used", 0, "remaining", 12),
                "casual", Map.of("total", 10, "used", 2, "remaining", 8)
        );
        when(leaveService.getLeaveBalance("emp-1")).thenReturn(balance);

        mockMvc.perform(get("/api/leaves/balance/emp-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.annual.total").value(20));
    }

    // ==================== PayrollController ====================

    @Test
    @DisplayName("GET /api/payroll returns records")
    @WithMockUser
    void payroll_getAll() throws Exception {
        when(payrollService.getAllPayrolls(null, null, null)).thenReturn(List.of());
        mockMvc.perform(get("/api/payroll"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/payroll/generate requires ADMIN")
    @WithMockUser(roles = "EMPLOYEE")
    void payroll_generate_requiresAdmin() throws Exception {
        PayrollRequest req = new PayrollRequest();
        req.setPayMonth(1);
        req.setPayYear(2024);

        mockMvc.perform(post("/api/payroll/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/payroll/generate as ADMIN")
    @WithMockUser(roles = "ADMIN")
    void payroll_generate_asAdmin() throws Exception {
        when(payrollService.generatePayroll(any(PayrollRequest.class))).thenReturn(List.of());

        PayrollRequest req = new PayrollRequest();
        req.setPayMonth(1);
        req.setPayYear(2024);

        mockMvc.perform(post("/api/payroll/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/payroll/{id}/process requires ADMIN")
    @WithMockUser(roles = "EMPLOYEE")
    void payroll_process_requiresAdmin() throws Exception {
        mockMvc.perform(put("/api/payroll/pay-1/process"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /api/payroll/{id}/process as ADMIN")
    @WithMockUser(roles = "ADMIN")
    void payroll_process_asAdmin() throws Exception {
        Payroll p = new Payroll();
        p.setId("pay-1");
        p.setStatus("paid");
        when(payrollService.processPayroll("pay-1")).thenReturn(p);

        mockMvc.perform(put("/api/payroll/pay-1/process"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("paid"));
    }

    @Test
    @DisplayName("GET /api/payroll/summary requires ADMIN")
    @WithMockUser(roles = "EMPLOYEE")
    void payroll_summary_requiresAdmin() throws Exception {
        mockMvc.perform(get("/api/payroll/summary"))
                .andExpect(status().isForbidden());
    }

    // ==================== ReportController ====================

    @Test
    @DisplayName("All report endpoints require ADMIN role")
    @WithMockUser(roles = "EMPLOYEE")
    void reports_allRequireAdmin() throws Exception {
        mockMvc.perform(get("/api/reports/employees-by-department"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/reports/attendance-summary")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-01-31"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/reports/leave-summary"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/reports/payroll-summary"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/reports/employees-by-department as ADMIN")
    @WithMockUser(roles = "ADMIN")
    void reports_employeesByDept() throws Exception {
        when(reportService.getEmployeesByDepartment()).thenReturn(List.of());
        mockMvc.perform(get("/api/reports/employees-by-department"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/reports/attendance-summary as ADMIN")
    @WithMockUser(roles = "ADMIN")
    void reports_attendanceSummary() throws Exception {
        when(reportService.getAttendanceSummary(any(), any())).thenReturn(List.of());
        mockMvc.perform(get("/api/reports/attendance-summary")
                        .param("start_date", "2024-01-01")
                        .param("end_date", "2024-01-31"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/reports/leave-summary as ADMIN")
    @WithMockUser(roles = "ADMIN")
    void reports_leaveSummary() throws Exception {
        when(reportService.getLeaveSummary()).thenReturn(List.of());
        mockMvc.perform(get("/api/reports/leave-summary"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/reports/payroll-summary as ADMIN")
    @WithMockUser(roles = "ADMIN")
    void reports_payrollSummary() throws Exception {
        when(reportService.getPayrollSummary()).thenReturn(List.of());
        mockMvc.perform(get("/api/reports/payroll-summary"))
                .andExpect(status().isOk());
    }
}
