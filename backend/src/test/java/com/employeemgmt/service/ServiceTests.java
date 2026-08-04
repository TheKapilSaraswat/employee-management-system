package com.employeemgmt.service;

import com.employeemgmt.dto.*;
import com.employeemgmt.model.*;
import com.employeemgmt.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceTests {

    @Nested
    @DisplayName("EmployeeService Tests")
    class EmployeeServiceTests {

        @Mock
        private EmployeeRepository employeeRepository;
        @Mock
        private DepartmentRepository departmentRepository;
        @InjectMocks
        private EmployeeService employeeService;

        private Employee sampleEmployee() {
            Employee e = new Employee();
            e.setId("emp-1");
            e.setEmployeeCode("EMP-001");
            e.setFirstName("John");
            e.setLastName("Doe");
            e.setEmail("john@test.com");
            e.setStatus("active");
            e.setDepartmentId("dept-1");
            return e;
        }

        @Test
        @DisplayName("getAllEmployees returns paginated results")
        void getAllEmployees_returnsPaginated() {
            List<Employee> employees = List.of(sampleEmployee(), sampleEmployee());
            when(employeeRepository.findAll()).thenReturn(employees);

            Map<String, Object> result = employeeService.getAllEmployees(null, null, null, 0, 10);

            assertEquals(2, result.get("total"));
            assertEquals(0, result.get("page"));
            assertEquals(10, result.get("size"));
            assertEquals(1, result.get("totalPages"));
        }

        @Test
        @DisplayName("getAllEmployees with search filters correctly")
        void getAllEmployees_withSearch() {
            Employee emp = sampleEmployee();
            when(employeeRepository.searchEmployeesAll("John")).thenReturn(List.of(emp));

            Map<String, Object> result = employeeService.getAllEmployees("John", null, null, 0, 10);
            assertEquals(1, result.get("total"));
        }

        @Test
        @DisplayName("getAllEmployees with department filter")
        void getAllEmployees_withDepartment() {
            when(employeeRepository.findByDepartmentId("dept-1")).thenReturn(List.of(sampleEmployee()));

            Map<String, Object> result = employeeService.getAllEmployees(null, "dept-1", null, 0, 10);
            assertEquals(1, result.get("total"));
        }

        @Test
        @DisplayName("getAllEmployees with status filter")
        void getAllEmployees_withStatus() {
            when(employeeRepository.findByStatus("active")).thenReturn(List.of(sampleEmployee()));

            Map<String, Object> result = employeeService.getAllEmployees(null, null, "active", 0, 10);
            assertEquals(1, result.get("total"));
        }

        @Test
        @DisplayName("getAllEmployees with search and status")
        void getAllEmployees_withSearchAndStatus() {
            when(employeeRepository.searchEmployees("active", "John")).thenReturn(List.of(sampleEmployee()));

            Map<String, Object> result = employeeService.getAllEmployees("John", null, "active", 0, 10);
            assertEquals(1, result.get("total"));
        }

        @Test
        @DisplayName("getAllEmployees pagination beyond total returns empty")
        void getAllEmployees_paginationBeyondTotal() {
            when(employeeRepository.findAll()).thenReturn(List.of(sampleEmployee()));

            Map<String, Object> result = employeeService.getAllEmployees(null, null, null, 5, 10);
            assertEquals(1, result.get("total"));
            List<?> employees = (List<?>) result.get("employees");
            assertTrue(employees.isEmpty());
        }

        @Test
        @DisplayName("getEmployee found returns employee data")
        void getEmployee_found() {
            when(employeeRepository.findById("emp-1")).thenReturn(Optional.of(sampleEmployee()));
            when(departmentRepository.findById("dept-1")).thenReturn(Optional.empty());

            Map<String, Object> result = employeeService.getEmployee("emp-1");
            assertEquals("John", result.get("firstName"));
            assertEquals("EMP-001", result.get("employeeCode"));
        }

        @Test
        @DisplayName("getEmployee not found throws 404")
        void getEmployee_notFound() {
            when(employeeRepository.findById("missing")).thenReturn(Optional.empty());
            assertThrows(ResponseStatusException.class, () -> employeeService.getEmployee("missing"));
        }

        @Test
        @DisplayName("createEmployee success")
        void createEmployee_success() {
            when(employeeRepository.findMaxEmployeeCode()).thenReturn(Optional.of("EMP-005"));
            when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> {
                Employee e = inv.getArgument(0);
                e.setId("new-id");
                return e;
            });

            EmployeeRequest req = new EmployeeRequest();
            req.setFirstName("Jane");
            req.setLastName("Smith");
            req.setEmail("jane@test.com");

            Employee result = employeeService.createEmployee(req);
            assertEquals("Jane", result.getFirstName());
            assertEquals("EMP-006", result.getEmployeeCode());
        }

        @Test
        @DisplayName("createEmployee first employee gets EMP-001")
        void createEmployee_firstEmployee() {
            when(employeeRepository.findMaxEmployeeCode()).thenReturn(Optional.empty());
            when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> {
                Employee e = inv.getArgument(0);
                e.setId("new-id");
                return e;
            });

            EmployeeRequest req = new EmployeeRequest();
            req.setFirstName("First");
            req.setLastName("User");
            req.setEmail("first@test.com");

            Employee result = employeeService.createEmployee(req);
            assertEquals("EMP-001", result.getEmployeeCode());
        }

        @Test
        @DisplayName("createEmployee with invalid department throws 400")
        void createEmployee_invalidDepartment() {
            when(employeeRepository.findMaxEmployeeCode()).thenReturn(Optional.empty());
            when(departmentRepository.findById("bad-dept")).thenReturn(Optional.empty());

            EmployeeRequest req = new EmployeeRequest();
            req.setFirstName("Test");
            req.setLastName("User");
            req.setEmail("test@test.com");
            req.setDepartmentId("bad-dept");

            assertThrows(ResponseStatusException.class, () -> employeeService.createEmployee(req));
        }

        @Test
        @DisplayName("updateEmployee success")
        void updateEmployee_success() {
            Employee existing = sampleEmployee();
            when(employeeRepository.findById("emp-1")).thenReturn(Optional.of(existing));
            when(employeeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            EmployeeRequest req = new EmployeeRequest();
            req.setFirstName("Updated");
            req.setLastName("Name");
            req.setEmail("updated@test.com");

            Employee result = employeeService.updateEmployee("emp-1", req);
            assertEquals("Updated", result.getFirstName());
        }

        @Test
        @DisplayName("updateEmployee not found throws 404")
        void updateEmployee_notFound() {
            when(employeeRepository.findById("missing")).thenReturn(Optional.empty());
            EmployeeRequest req = new EmployeeRequest();
            assertThrows(ResponseStatusException.class, () -> employeeService.updateEmployee("missing", req));
        }

        @Test
        @DisplayName("deactivateEmployee sets status inactive")
        void deactivateEmployee_success() {
            Employee emp = sampleEmployee();
            when(employeeRepository.findById("emp-1")).thenReturn(Optional.of(emp));
            when(employeeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            employeeService.deactivateEmployee("emp-1");
            assertEquals("inactive", emp.getStatus());
        }

        @Test
        @DisplayName("deactivateEmployee not found throws 404")
        void deactivateEmployee_notFound() {
            when(employeeRepository.findById("missing")).thenReturn(Optional.empty());
            assertThrows(ResponseStatusException.class, () -> employeeService.deactivateEmployee("missing"));
        }
    }

    @Nested
    @DisplayName("DepartmentService Tests")
    class DepartmentServiceTests {

        @Mock
        private DepartmentRepository departmentRepository;
        @Mock
        private EmployeeRepository employeeRepository;
        @InjectMocks
        private DepartmentService departmentService;

        private Department sampleDept() {
            Department d = new Department();
            d.setId("dept-1");
            d.setName("Engineering");
            d.setDescription("Eng team");
            return d;
        }

        @Test
        @DisplayName("getAllDepartments returns list with employee counts")
        void getAllDepartments() {
            when(departmentRepository.findAll()).thenReturn(List.of(sampleDept()));
            when(employeeRepository.countByDepartmentId("dept-1")).thenReturn(5L);

            List<Map<String, Object>> result = departmentService.getAllDepartments();
            assertEquals(1, result.size());
            assertEquals("Engineering", result.get(0).get("name"));
            assertEquals(5L, result.get(0).get("employeeCount"));
        }

        @Test
        @DisplayName("createDepartment success")
        void createDepartment_success() {
            when(departmentRepository.existsByName("Engineering")).thenReturn(false);
            when(departmentRepository.save(any())).thenAnswer(inv -> {
                Department d = inv.getArgument(0);
                d.setId("new-dept");
                return d;
            });

            DepartmentRequest req = new DepartmentRequest();
            req.setName("Engineering");
            req.setDescription("Eng");

            Department result = departmentService.createDepartment(req);
            assertEquals("Engineering", result.getName());
        }

        @Test
        @DisplayName("createDepartment duplicate name throws 400")
        void createDepartment_duplicateName() {
            when(departmentRepository.existsByName("Engineering")).thenReturn(true);

            DepartmentRequest req = new DepartmentRequest();
            req.setName("Engineering");

            assertThrows(ResponseStatusException.class, () -> departmentService.createDepartment(req));
        }

        @Test
        @DisplayName("updateDepartment success")
        void updateDepartment_success() {
            Department dept = sampleDept();
            when(departmentRepository.findById("dept-1")).thenReturn(Optional.of(dept));
            when(departmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            DepartmentRequest req = new DepartmentRequest();
            req.setName("Engineering");
            req.setDescription("Updated");

            Department result = departmentService.updateDepartment("dept-1", req);
            assertEquals("Updated", result.getDescription());
        }

        @Test
        @DisplayName("updateDepartment not found throws 404")
        void updateDepartment_notFound() {
            when(departmentRepository.findById("missing")).thenReturn(Optional.empty());
            DepartmentRequest req = new DepartmentRequest();
            req.setName("Test");
            assertThrows(ResponseStatusException.class, () -> departmentService.updateDepartment("missing", req));
        }

        @Test
        @DisplayName("deleteDepartment success")
        void deleteDepartment_success() {
            Department dept = sampleDept();
            when(departmentRepository.findById("dept-1")).thenReturn(Optional.of(dept));
            when(employeeRepository.countByDepartmentId("dept-1")).thenReturn(0L);

            departmentService.deleteDepartment("dept-1");
            verify(departmentRepository).delete(dept);
        }

        @Test
        @DisplayName("deleteDepartment with employees throws 400")
        void deleteDepartment_withEmployees() {
            Department dept = sampleDept();
            when(departmentRepository.findById("dept-1")).thenReturn(Optional.of(dept));
            when(employeeRepository.countByDepartmentId("dept-1")).thenReturn(3L);

            assertThrows(ResponseStatusException.class, () -> departmentService.deleteDepartment("dept-1"));
        }

        @Test
        @DisplayName("deleteDepartment not found throws 404")
        void deleteDepartment_notFound() {
            when(departmentRepository.findById("missing")).thenReturn(Optional.empty());
            assertThrows(ResponseStatusException.class, () -> departmentService.deleteDepartment("missing"));
        }
    }

    @Nested
    @DisplayName("AuthService Tests")
    class AuthServiceTests {

        @Mock
        private UserRepository userRepository;
        @Mock
        private PasswordEncoder passwordEncoder;
        @InjectMocks
        private AuthService authService;

        @BeforeEach
        void setUp() {
            ReflectionTestUtils.setField(authService, "jwtSecret", "ems-jwt-secret-key-2024-must-be-at-least-32-characters-long");
            ReflectionTestUtils.setField(authService, "jwtExpiration", 604800000L);
        }

        private User sampleUser() {
            User u = new User();
            u.setId("user-1");
            u.setEmail("admin@test.com");
            u.setPassword("encoded-pass");
            u.setName("Admin User");
            u.setRole("ADMIN");
            return u;
        }

        @Test
        @DisplayName("register first user gets ADMIN role")
        void register_firstUser_admin() {
            when(userRepository.existsByEmail("admin@test.com")).thenReturn(false);
            when(userRepository.count()).thenReturn(0L);
            when(passwordEncoder.encode("password")).thenReturn("encoded-pass");
            when(userRepository.save(any())).thenAnswer(inv -> {
                User u = inv.getArgument(0);
                u.setId("new-user");
                return u;
            });

            RegisterRequest req = new RegisterRequest();
            req.setName("Admin User");
            req.setEmail("admin@test.com");
            req.setPassword("password");

            AuthResponse result = authService.register(req);
            assertEquals("ADMIN", result.getRole());
            assertNotNull(result.getToken());
        }

        @Test
        @DisplayName("register subsequent user gets EMPLOYEE role")
        void register_subsequentUser_employee() {
            when(userRepository.existsByEmail("emp@test.com")).thenReturn(false);
            when(userRepository.count()).thenReturn(5L);
            when(passwordEncoder.encode("password")).thenReturn("encoded-pass");
            when(userRepository.save(any())).thenAnswer(inv -> {
                User u = inv.getArgument(0);
                u.setId("new-user");
                return u;
            });

            RegisterRequest req = new RegisterRequest();
            req.setName("Employee");
            req.setEmail("emp@test.com");
            req.setPassword("password");

            AuthResponse result = authService.register(req);
            assertEquals("EMPLOYEE", result.getRole());
        }

        @Test
        @DisplayName("register duplicate email throws 400")
        void register_duplicateEmail() {
            when(userRepository.existsByEmail("dup@test.com")).thenReturn(true);

            RegisterRequest req = new RegisterRequest();
            req.setName("Dup");
            req.setEmail("dup@test.com");
            req.setPassword("password");

            assertThrows(ResponseStatusException.class, () -> authService.register(req));
        }

        @Test
        @DisplayName("login success returns token")
        void login_success() {
            User user = sampleUser();
            when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("password", "encoded-pass")).thenReturn(true);

            LoginRequest req = new LoginRequest();
            req.setEmail("admin@test.com");
            req.setPassword("password");

            AuthResponse result = authService.login(req);
            assertNotNull(result.getToken());
            assertEquals("admin@test.com", result.getEmail());
        }

        @Test
        @DisplayName("login invalid email throws 401")
        void login_invalidEmail() {
            when(userRepository.findByEmail("bad@test.com")).thenReturn(Optional.empty());

            LoginRequest req = new LoginRequest();
            req.setEmail("bad@test.com");
            req.setPassword("password");

            assertThrows(ResponseStatusException.class, () -> authService.login(req));
        }

        @Test
        @DisplayName("login wrong password throws 401")
        void login_wrongPassword() {
            User user = sampleUser();
            when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("wrong", "encoded-pass")).thenReturn(false);

            LoginRequest req = new LoginRequest();
            req.setEmail("admin@test.com");
            req.setPassword("wrong");

            assertThrows(ResponseStatusException.class, () -> authService.login(req));
        }

        @Test
        @DisplayName("getCurrentUser found")
        void getCurrentUser_found() {
            when(userRepository.findById("user-1")).thenReturn(Optional.of(sampleUser()));
            User result = authService.getCurrentUser("user-1");
            assertEquals("Admin User", result.getName());
        }

        @Test
        @DisplayName("getCurrentUser not found throws 404")
        void getCurrentUser_notFound() {
            when(userRepository.findById("missing")).thenReturn(Optional.empty());
            assertThrows(ResponseStatusException.class, () -> authService.getCurrentUser("missing"));
        }
    }

    @Nested
    @DisplayName("AttendanceService Tests")
    class AttendanceServiceTests {

        @Mock
        private AttendanceRepository attendanceRepository;
        @InjectMocks
        private AttendanceService attendanceService;

        @Test
        @DisplayName("getAllAttendances with no filters returns all")
        void getAllAttendances_noFilters() {
            when(attendanceRepository.findAll()).thenReturn(List.of(new Attendance()));
            assertEquals(1, attendanceService.getAllAttendances(null, null, null, null).size());
        }

        @Test
        @DisplayName("getAllAttendances with employeeId")
        void getAllAttendances_byEmployee() {
            when(attendanceRepository.findByEmployeeId("emp-1")).thenReturn(List.of(new Attendance()));
            assertEquals(1, attendanceService.getAllAttendances("emp-1", null, null, null).size());
        }

        @Test
        @DisplayName("getAllAttendances with date range")
        void getAllAttendances_byDateRange() {
            when(attendanceRepository.findByDateBetween(any(), any())).thenReturn(List.of(new Attendance()));
            assertEquals(1, attendanceService.getAllAttendances(null, LocalDate.now(), LocalDate.now(), null).size());
        }

        @Test
        @DisplayName("getAllAttendances with employeeId and date range")
        void getAllAttendances_byEmployeeAndDateRange() {
            when(attendanceRepository.findByEmployeeIdAndDateBetween(any(), any(), any())).thenReturn(List.of(new Attendance()));
            assertEquals(1, attendanceService.getAllAttendances("emp-1", LocalDate.now(), LocalDate.now(), null).size());
        }

        @Test
        @DisplayName("recordAttendance check-in success")
        void recordAttendance_checkIn() {
            when(attendanceRepository.findByEmployeeIdAndDateAndCheckOutIsNull(any(), any())).thenReturn(List.of());
            when(attendanceRepository.save(any())).thenAnswer(inv -> {
                Attendance a = inv.getArgument(0);
                a.setId("att-1");
                return a;
            });

            AttendanceRequest req = new AttendanceRequest();
            req.setEmployeeId("emp-1");
            req.setAction("in");

            Attendance result = attendanceService.recordAttendance(req);
            assertNotNull(result.getId());
            assertEquals("present", result.getStatus());
        }

        @Test
        @DisplayName("recordAttendance already checked in throws 400")
        void recordAttendance_alreadyCheckedIn() {
            Attendance existing = new Attendance();
            existing.setId("existing");
            when(attendanceRepository.findByEmployeeIdAndDateAndCheckOutIsNull(any(), any()))
                    .thenReturn(List.of(existing));

            AttendanceRequest req = new AttendanceRequest();
            req.setEmployeeId("emp-1");
            req.setAction("in");

            assertThrows(ResponseStatusException.class, () -> attendanceService.recordAttendance(req));
        }

        @Test
        @DisplayName("recordAttendance check-out success")
        void recordAttendance_checkOut() {
            Attendance existing = new Attendance();
            existing.setId("att-1");
            when(attendanceRepository.findByEmployeeIdAndDateAndCheckOutIsNull(any(), any()))
                    .thenReturn(List.of(existing));
            when(attendanceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            AttendanceRequest req = new AttendanceRequest();
            req.setEmployeeId("emp-1");
            req.setAction("out");

            Attendance result = attendanceService.recordAttendance(req);
            assertNotNull(result.getCheckOut());
        }

        @Test
        @DisplayName("recordAttendance check-out no check-in throws 400")
        void recordAttendance_checkOutNoCheckIn() {
            when(attendanceRepository.findByEmployeeIdAndDateAndCheckOutIsNull(any(), any()))
                    .thenReturn(List.of());

            AttendanceRequest req = new AttendanceRequest();
            req.setEmployeeId("emp-1");
            req.setAction("out");

            assertThrows(ResponseStatusException.class, () -> attendanceService.recordAttendance(req));
        }

        @Test
        @DisplayName("recordAttendance invalid action throws 400")
        void recordAttendance_invalidAction() {
            AttendanceRequest req = new AttendanceRequest();
            req.setEmployeeId("emp-1");
            req.setAction("invalid");

            assertThrows(ResponseStatusException.class, () -> attendanceService.recordAttendance(req));
        }

        @Test
        @DisplayName("getTodayAttendance returns today records")
        void getTodayAttendance() {
            when(attendanceRepository.findByDate(LocalDate.now())).thenReturn(List.of(new Attendance()));
            assertEquals(1, attendanceService.getTodayAttendance().size());
        }

        @Test
        @DisplayName("updateAttendance success")
        void updateAttendance_success() {
            Attendance att = new Attendance();
            att.setId("att-1");
            when(attendanceRepository.findById("att-1")).thenReturn(Optional.of(att));
            when(attendanceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            AttendanceRequest req = new AttendanceRequest();
            req.setCheckOut(LocalTime.of(17, 0));
            req.setNotes("Updated");

            Attendance result = attendanceService.updateAttendance("att-1", req);
            assertEquals(LocalTime.of(17, 0), result.getCheckOut());
            assertEquals("Updated", result.getNotes());
        }

        @Test
        @DisplayName("updateAttendance not found throws 404")
        void updateAttendance_notFound() {
            when(attendanceRepository.findById("missing")).thenReturn(Optional.empty());
            assertThrows(ResponseStatusException.class,
                    () -> attendanceService.updateAttendance("missing", new AttendanceRequest()));
        }
    }

    @Nested
    @DisplayName("LeaveService Tests")
    class LeaveServiceTests {

        @Mock
        private LeaveRepository leaveRepository;
        @InjectMocks
        private LeaveService leaveService;

        @Test
        @DisplayName("getAllLeaves no filters returns all")
        void getAllLeaves_noFilters() {
            when(leaveRepository.findAll()).thenReturn(List.of(new Leave()));
            assertEquals(1, leaveService.getAllLeaves(null, null, null, null).size());
        }

        @Test
        @DisplayName("getAllLeaves with employeeId and status")
        void getAllLeaves_byEmployeeAndStatus() {
            when(leaveRepository.findByEmployeeIdAndStatus("emp-1", "pending")).thenReturn(List.of(new Leave()));
            assertEquals(1, leaveService.getAllLeaves("emp-1", "pending", null, null).size());
        }

        @Test
        @DisplayName("getAllLeaves with status only")
        void getAllLeaves_byStatus() {
            when(leaveRepository.findByStatus("approved")).thenReturn(List.of(new Leave()));
            assertEquals(1, leaveService.getAllLeaves(null, "approved", null, null).size());
        }

        @Test
        @DisplayName("getAllLeaves with date range")
        void getAllLeaves_byDateRange() {
            when(leaveRepository.findByStartDateBetween(any(), any())).thenReturn(List.of(new Leave()));
            assertEquals(1, leaveService.getAllLeaves(null, null, LocalDate.now(), LocalDate.now()).size());
        }

        @Test
        @DisplayName("applyLeave success")
        void applyLeave_success() {
            when(leaveRepository.save(any())).thenAnswer(inv -> {
                Leave l = inv.getArgument(0);
                l.setId("leave-1");
                l.setStatus("pending");
                return l;
            });

            LeaveRequest req = new LeaveRequest();
            req.setEmployeeId("emp-1");
            req.setType("Annual");
            req.setStartDate(LocalDate.now());
            req.setEndDate(LocalDate.now().plusDays(3));

            Leave result = leaveService.applyLeave(req);
            assertEquals("pending", result.getStatus());
            assertEquals("Annual", result.getType());
        }

        @Test
        @DisplayName("approveLeave success")
        void approveLeave_success() {
            Leave leave = new Leave();
            leave.setId("leave-1");
            leave.setStatus("pending");
            when(leaveRepository.findById("leave-1")).thenReturn(Optional.of(leave));
            when(leaveRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Leave result = leaveService.approveLeave("leave-1", "admin-user");
            assertEquals("approved", result.getStatus());
            assertEquals("admin-user", result.getApprovedBy());
        }

        @Test
        @DisplayName("approveLeave not found throws 404")
        void approveLeave_notFound() {
            when(leaveRepository.findById("missing")).thenReturn(Optional.empty());
            assertThrows(ResponseStatusException.class, () -> leaveService.approveLeave("missing", "admin"));
        }

        @Test
        @DisplayName("rejectLeave success")
        void rejectLeave_success() {
            Leave leave = new Leave();
            leave.setId("leave-1");
            when(leaveRepository.findById("leave-1")).thenReturn(Optional.of(leave));
            when(leaveRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Leave result = leaveService.rejectLeave("leave-1", "admin-user");
            assertEquals("rejected", result.getStatus());
        }

        @Test
        @DisplayName("rejectLeave not found throws 404")
        void rejectLeave_notFound() {
            when(leaveRepository.findById("missing")).thenReturn(Optional.empty());
            assertThrows(ResponseStatusException.class, () -> leaveService.rejectLeave("missing", "admin"));
        }

        @Test
        @DisplayName("getLeaveBalance calculates correctly")
        void getLeaveBalance() {
            Leave annualLeave = new Leave();
            annualLeave.setStartDate(LocalDate.of(2024, 1, 1));
            annualLeave.setEndDate(LocalDate.of(2024, 1, 5));

            when(leaveRepository.findByEmployeeIdAndTypeAndStatus("emp-1", "Annual", "approved"))
                    .thenReturn(List.of(annualLeave));
            when(leaveRepository.findByEmployeeIdAndTypeAndStatus("emp-1", "Sick", "approved"))
                    .thenReturn(List.of());
            when(leaveRepository.findByEmployeeIdAndTypeAndStatus("emp-1", "Casual", "approved"))
                    .thenReturn(List.of());

            Map<String, Object> balance = leaveService.getLeaveBalance("emp-1");
            assertNotNull(balance.get("annual"));
            assertNotNull(balance.get("sick"));
            assertNotNull(balance.get("casual"));

            @SuppressWarnings("unchecked")
            Map<String, Object> annual = (Map<String, Object>) balance.get("annual");
            assertEquals(20, annual.get("total"));
            assertEquals(5, annual.get("used"));
            assertEquals(15, annual.get("remaining"));
        }
    }

    @Nested
    @DisplayName("PayrollService Tests")
    class PayrollServiceTests {

        @Mock
        private PayrollRepository payrollRepository;
        @Mock
        private EmployeeRepository employeeRepository;
        @InjectMocks
        private PayrollService payrollService;

        @Test
        @DisplayName("getAllPayrolls no filters")
        void getAllPayrolls_noFilters() {
            when(payrollRepository.findAll()).thenReturn(List.of(new Payroll()));
            assertEquals(1, payrollService.getAllPayrolls(null, null, null).size());
        }

        @Test
        @DisplayName("getAllPayrolls with employeeId")
        void getAllPayrolls_byEmployee() {
            when(payrollRepository.findByEmployeeId("emp-1")).thenReturn(List.of(new Payroll()));
            assertEquals(1, payrollService.getAllPayrolls("emp-1", null, null).size());
        }

        @Test
        @DisplayName("getAllPayrolls with month and year")
        void getAllPayrolls_byMonthYear() {
            when(payrollRepository.findByPayMonthAndPayYear(1, 2024)).thenReturn(List.of(new Payroll()));
            assertEquals(1, payrollService.getAllPayrolls(null, 1, 2024).size());
        }

        @Test
        @DisplayName("getAllPayrolls with all filters")
        void getAllPayrolls_allFilters() {
            when(payrollRepository.findByEmployeeIdAndPayMonthAndPayYear("emp-1", 1, 2024))
                    .thenReturn(List.of(new Payroll()));
            assertEquals(1, payrollService.getAllPayrolls("emp-1", 1, 2024).size());
        }

        @Test
        @DisplayName("generatePayroll creates for active employees")
        void generatePayroll_success() {
            Employee emp = new Employee();
            emp.setId("emp-1");
            emp.setSalary(5000.0);
            when(employeeRepository.findByStatus("active")).thenReturn(List.of(emp));
            when(payrollRepository.findByEmployeeIdAndPayMonthAndPayYearAndStatus("emp-1", 1, 2024, "pending"))
                    .thenReturn(Optional.empty());
            when(payrollRepository.save(any())).thenAnswer(inv -> {
                Payroll p = inv.getArgument(0);
                p.setId("pay-1");
                return p;
            });

            PayrollRequest req = new PayrollRequest();
            req.setPayMonth(1);
            req.setPayYear(2024);

            List<Payroll> result = payrollService.generatePayroll(req);
            assertEquals(1, result.size());
            assertEquals(5000.0, result.get(0).getNetPay());
        }

        @Test
        @DisplayName("generatePayroll skips existing pending")
        void generatePayroll_skipsExisting() {
            Employee emp = new Employee();
            emp.setId("emp-1");
            emp.setSalary(5000.0);
            when(employeeRepository.findByStatus("active")).thenReturn(List.of(emp));
            when(payrollRepository.findByEmployeeIdAndPayMonthAndPayYearAndStatus("emp-1", 1, 2024, "pending"))
                    .thenReturn(Optional.of(new Payroll()));

            PayrollRequest req = new PayrollRequest();
            req.setPayMonth(1);
            req.setPayYear(2024);

            List<Payroll> result = payrollService.generatePayroll(req);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("generatePayroll handles null salary")
        void generatePayroll_nullSalary() {
            Employee emp = new Employee();
            emp.setId("emp-1");
            emp.setSalary(null);
            when(employeeRepository.findByStatus("active")).thenReturn(List.of(emp));
            when(payrollRepository.findByEmployeeIdAndPayMonthAndPayYearAndStatus(any(), anyInt(), anyInt(), anyString()))
                    .thenReturn(Optional.empty());
            when(payrollRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            PayrollRequest req = new PayrollRequest();
            req.setPayMonth(1);
            req.setPayYear(2024);

            List<Payroll> result = payrollService.generatePayroll(req);
            assertEquals(0.0, result.get(0).getBasicPay());
        }

        @Test
        @DisplayName("processPayroll success")
        void processPayroll_success() {
            Payroll payroll = new Payroll();
            payroll.setId("pay-1");
            payroll.setStatus("pending");
            when(payrollRepository.findById("pay-1")).thenReturn(Optional.of(payroll));
            when(payrollRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Payroll result = payrollService.processPayroll("pay-1");
            assertEquals("paid", result.getStatus());
            assertNotNull(result.getPaidAt());
        }

        @Test
        @DisplayName("processPayroll not found throws 404")
        void processPayroll_notFound() {
            when(payrollRepository.findById("missing")).thenReturn(Optional.empty());
            assertThrows(ResponseStatusException.class, () -> payrollService.processPayroll("missing"));
        }

        @Test
        @DisplayName("getPayrollSummary calculates totals")
        void getPayrollSummary() {
            Payroll p1 = new Payroll();
            p1.setBasicPay(5000.0);
            p1.setAllowances(500.0);
            p1.setDeductions(200.0);
            p1.setNetPay(5300.0);
            p1.setStatus("paid");

            Payroll p2 = new Payroll();
            p2.setBasicPay(4000.0);
            p2.setAllowances(0.0);
            p2.setDeductions(100.0);
            p2.setNetPay(3900.0);
            p2.setStatus("pending");

            when(payrollRepository.findAll()).thenReturn(List.of(p1, p2));

            Map<String, Object> summary = payrollService.getPayrollSummary();
            assertEquals(9000.0, summary.get("totalGross"));
            assertEquals(500.0, summary.get("totalAllowances"));
            assertEquals(300.0, summary.get("totalDeductions"));
            assertEquals(9200.0, summary.get("totalNet"));
            assertEquals(1L, summary.get("paidCount"));
            assertEquals(1L, summary.get("pendingCount"));
            assertEquals(2, summary.get("totalRecords"));
        }
    }

    @Nested
    @DisplayName("ReportService Tests")
    class ReportServiceTests {

        @Mock
        private DepartmentRepository departmentRepository;
        @Mock
        private EmployeeRepository employeeRepository;
        @Mock
        private AttendanceRepository attendanceRepository;
        @Mock
        private LeaveRepository leaveRepository;
        @Mock
        private PayrollRepository payrollRepository;
        @InjectMocks
        private ReportService reportService;

        @Test
        @DisplayName("getEmployeesByDepartment returns counts")
        void getEmployeesByDepartment() {
            Department dept = new Department();
            dept.setId("dept-1");
            dept.setName("Engineering");

            Employee active1 = new Employee();
            active1.setId("emp-1");
            active1.setStatus("active");
            Employee active2 = new Employee();
            active2.setId("emp-2");
            active2.setStatus("active");
            Employee inactive = new Employee();
            inactive.setId("emp-3");
            inactive.setStatus("inactive");

            when(departmentRepository.findAll()).thenReturn(List.of(dept));
            when(employeeRepository.findByDepartmentId("dept-1")).thenReturn(List.of(active1, active2, inactive));

            List<Map<String, Object>> result = reportService.getEmployeesByDepartment();
            assertEquals(1, result.size());
            assertEquals("Engineering", result.get(0).get("department"));
            assertEquals(3, result.get(0).get("employee_count"));
            assertEquals(2L, result.get(0).get("active_count"));
            assertEquals(1L, result.get(0).get("inactive_count"));
        }

        @Test
        @DisplayName("getAttendanceSummary calculates stats")
        void getAttendanceSummary() {
            Attendance present = new Attendance();
            present.setEmployeeId("emp-1");
            present.setStatus("present");
            Attendance absent = new Attendance();
            absent.setEmployeeId("emp-2");
            absent.setStatus("absent");

            Department dept = new Department();
            dept.setId("dept-1");
            dept.setName("Engineering");

            Employee emp1 = new Employee();
            emp1.setId("emp-1");
            Employee emp2 = new Employee();
            emp2.setId("emp-2");

            when(attendanceRepository.findByDateBetween(any(), any())).thenReturn(List.of(present, present, absent));
            when(departmentRepository.findAll()).thenReturn(List.of(dept));
            when(employeeRepository.findByDepartmentId("dept-1")).thenReturn(List.of(emp1, emp2));

            List<Map<String, Object>> result = reportService.getAttendanceSummary(LocalDate.now(), LocalDate.now());
            assertEquals(1, result.size());
            assertEquals("Engineering", result.get(0).get("department"));
            assertEquals(3L, result.get(0).get("total_attendance"));
            assertEquals(2L, result.get(0).get("present"));
            assertEquals(1L, result.get(0).get("absent"));
            assertEquals(67L, result.get(0).get("attendance_percentage"));
        }

        @Test
        @DisplayName("getAttendanceSummary empty data")
        void getAttendanceSummary_empty() {
            when(attendanceRepository.findByDateBetween(any(), any())).thenReturn(List.of());
            when(departmentRepository.findAll()).thenReturn(List.of());

            List<Map<String, Object>> result = reportService.getAttendanceSummary(LocalDate.now(), LocalDate.now());
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("getLeaveSummary counts by type and status")
        void getLeaveSummary() {
            when(leaveRepository.countByTypeAndStatus("Annual", "approved")).thenReturn(5L);
            when(leaveRepository.countByTypeAndStatus("Annual", "rejected")).thenReturn(1L);
            when(leaveRepository.countByTypeAndStatus("Annual", "pending")).thenReturn(2L);
            when(leaveRepository.countByTypeAndStatus("Sick", "approved")).thenReturn(3L);
            when(leaveRepository.countByTypeAndStatus("Sick", "rejected")).thenReturn(0L);
            when(leaveRepository.countByTypeAndStatus("Sick", "pending")).thenReturn(1L);
            when(leaveRepository.countByTypeAndStatus("Casual", "approved")).thenReturn(2L);
            when(leaveRepository.countByTypeAndStatus("Casual", "rejected")).thenReturn(0L);
            when(leaveRepository.countByTypeAndStatus("Casual", "pending")).thenReturn(0L);

            List<Map<String, Object>> result = reportService.getLeaveSummary();
            assertEquals(3, result.size());

            Map<String, Object> annual = result.get(0);
            assertEquals("Annual", annual.get("type"));
            assertEquals(8L, annual.get("total_applied"));
            assertEquals(5L, annual.get("approved"));
            assertEquals(1L, annual.get("rejected"));
            assertEquals(2L, annual.get("pending"));
            assertEquals(25L, annual.get("total_days"));
        }

        @Test
        @DisplayName("getPayrollSummary calculates monthly totals")
        void getPayrollSummary() {
            Payroll p1 = new Payroll();
            p1.setPayMonth(1);
            p1.setPayYear(2024);
            p1.setBasicPay(5000.0);
            p1.setAllowances(500.0);
            p1.setNetPay(5300.0);
            p1.setDeductions(200.0);

            Payroll p2 = new Payroll();
            p2.setPayMonth(1);
            p2.setPayYear(2024);
            p2.setBasicPay(4000.0);
            p2.setAllowances(300.0);
            p2.setNetPay(4200.0);
            p2.setDeductions(100.0);

            when(payrollRepository.findAll()).thenReturn(List.of(p1, p2));

            List<Map<String, Object>> result = reportService.getPayrollSummary();
            assertEquals(1, result.size());
            Map<String, Object> jan = result.get(0);
            assertEquals(1, jan.get("month"));
            assertEquals(2024, jan.get("year"));
            assertEquals(2, jan.get("employee_count"));
            assertEquals(9000.0, jan.get("total_basic"));
            assertEquals(800.0, jan.get("total_allowances"));
            assertEquals(300.0, jan.get("total_deductions"));
            assertEquals(9500.0, jan.get("total_net_pay"));
        }
    }
}
