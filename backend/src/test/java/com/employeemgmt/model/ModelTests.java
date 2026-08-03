package com.employeemgmt.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ModelTests {

    @Nested
    @DisplayName("Employee Tests")
    class EmployeeTests {

        @Test
        @DisplayName("prePersist generates UUID when id is null")
        void prePersist_generatesUuid() {
            Employee employee = new Employee();
            employee.setEmployeeCode("EMP001");
            employee.setFirstName("John");
            employee.setLastName("Doe");
            employee.setEmail("john@example.com");

            employee.prePersist();

            assertNotNull(employee.getId());
            assertDoesNotThrow(() -> UUID.fromString(employee.getId()));
        }

        @Test
        @DisplayName("prePersist does not overwrite existing id")
        void prePersist_preservesExistingId() {
            Employee employee = new Employee();
            String existingId = UUID.randomUUID().toString();
            employee.setId(existingId);
            employee.setEmployeeCode("EMP001");
            employee.setFirstName("John");
            employee.setLastName("Doe");
            employee.setEmail("john@example.com");

            employee.prePersist();

            assertEquals(existingId, employee.getId());
        }

        @Test
        @DisplayName("prePersist sets status to active when null")
        void prePersist_setsDefaultStatus() {
            Employee employee = new Employee();
            employee.setEmployeeCode("EMP001");
            employee.setFirstName("John");
            employee.setLastName("Doe");
            employee.setEmail("john@example.com");

            assertNull(employee.getStatus());

            employee.prePersist();

            assertEquals("active", employee.getStatus());
        }

        @Test
        @DisplayName("prePersist does not overwrite existing status")
        void prePersist_preservesExistingStatus() {
            Employee employee = new Employee();
            employee.setEmployeeCode("EMP001");
            employee.setFirstName("John");
            employee.setLastName("Doe");
            employee.setEmail("john@example.com");
            employee.setStatus("inactive");

            employee.prePersist();

            assertEquals("inactive", employee.getStatus());
        }

        @Test
        @DisplayName("prePersist sets createdAt and updatedAt when null")
        void prePersist_setsTimestamps() {
            Employee employee = new Employee();
            employee.setEmployeeCode("EMP001");
            employee.setFirstName("John");
            employee.setLastName("Doe");
            employee.setEmail("john@example.com");

            LocalDateTime before = LocalDateTime.now().minusSeconds(1);

            employee.prePersist();

            LocalDateTime after = LocalDateTime.now().plusSeconds(1);

            assertNotNull(employee.getCreatedAt());
            assertNotNull(employee.getUpdatedAt());
            assertFalse(employee.getCreatedAt().isBefore(before));
            assertFalse(employee.getCreatedAt().isAfter(after));
            assertFalse(employee.getUpdatedAt().isBefore(before));
            assertFalse(employee.getUpdatedAt().isAfter(after));
        }

        @Test
        @DisplayName("prePersist does not overwrite existing timestamps")
        void prePersist_preservesExistingTimestamps() {
            Employee employee = new Employee();
            employee.setEmployeeCode("EMP001");
            employee.setFirstName("John");
            employee.setLastName("Doe");
            employee.setEmail("john@example.com");

            LocalDateTime fixedCreatedAt = LocalDateTime.of(2020, 1, 1, 12, 0);
            LocalDateTime fixedUpdatedAt = LocalDateTime.of(2021, 6, 15, 8, 30);
            employee.setCreatedAt(fixedCreatedAt);
            employee.setUpdatedAt(fixedUpdatedAt);

            employee.prePersist();

            assertEquals(fixedCreatedAt, employee.getCreatedAt());
            assertEquals(fixedUpdatedAt, employee.getUpdatedAt());
        }

        @Test
        @DisplayName("preUpdate refreshes updatedAt")
        void preUpdate_refreshesUpdatedAt() {
            Employee employee = new Employee();
            employee.setEmployeeCode("EMP001");
            employee.setFirstName("John");
            employee.setLastName("Doe");
            employee.setEmail("john@example.com");
            employee.prePersist();

            LocalDateTime originalUpdatedAt = employee.getUpdatedAt();

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            employee.preUpdate();

            assertNotNull(employee.getUpdatedAt());
            assertFalse(employee.getUpdatedAt().isBefore(originalUpdatedAt));
        }

        @Test
        @DisplayName("prePersist full lifecycle produces valid entity state")
        void prePersist_fullLifecycle() {
            Employee employee = new Employee();
            employee.setEmployeeCode("EMP001");
            employee.setFirstName("John");
            employee.setLastName("Doe");
            employee.setEmail("john@example.com");
            employee.setPhone("1234567890");
            employee.setPosition("Engineer");
            employee.setDepartmentId("dept-1");
            employee.setSalary(75000.0);
            employee.setHireDate(LocalDate.of(2025, 3, 15));

            employee.prePersist();

            assertNotNull(employee.getId());
            assertEquals("active", employee.getStatus());
            assertNotNull(employee.getCreatedAt());
            assertNotNull(employee.getUpdatedAt());
            assertEquals("EMP001", employee.getEmployeeCode());
            assertEquals("John", employee.getFirstName());
            assertEquals("Doe", employee.getLastName());
            assertEquals("john@example.com", employee.getEmail());
            assertEquals("1234567890", employee.getPhone());
            assertEquals("Engineer", employee.getPosition());
            assertEquals("dept-1", employee.getDepartmentId());
            assertEquals(75000.0, employee.getSalary());
            assertEquals(LocalDate.of(2025, 3, 15), employee.getHireDate());
        }
    }

    @Nested
    @DisplayName("Department Tests")
    class DepartmentTests {

        @Test
        @DisplayName("prePersist generates UUID when id is null")
        void prePersist_generatesUuid() {
            Department department = new Department();
            department.setName("Engineering");

            department.prePersist();

            assertNotNull(department.getId());
            assertDoesNotThrow(() -> UUID.fromString(department.getId()));
        }

        @Test
        @DisplayName("prePersist does not overwrite existing id")
        void prePersist_preservesExistingId() {
            Department department = new Department();
            String existingId = UUID.randomUUID().toString();
            department.setId(existingId);
            department.setName("Engineering");

            department.prePersist();

            assertEquals(existingId, department.getId());
        }

        @Test
        @DisplayName("prePersist sets createdAt and updatedAt when null")
        void prePersist_setsTimestamps() {
            Department department = new Department();
            department.setName("Engineering");

            LocalDateTime before = LocalDateTime.now().minusSeconds(1);

            department.prePersist();

            LocalDateTime after = LocalDateTime.now().plusSeconds(1);

            assertNotNull(department.getCreatedAt());
            assertNotNull(department.getUpdatedAt());
            assertFalse(department.getCreatedAt().isBefore(before));
            assertFalse(department.getCreatedAt().isAfter(after));
            assertFalse(department.getUpdatedAt().isBefore(before));
            assertFalse(department.getUpdatedAt().isAfter(after));
        }

        @Test
        @DisplayName("prePersist does not overwrite existing timestamps")
        void prePersist_preservesExistingTimestamps() {
            Department department = new Department();
            department.setName("Engineering");

            LocalDateTime fixedCreatedAt = LocalDateTime.of(2020, 1, 1, 12, 0);
            LocalDateTime fixedUpdatedAt = LocalDateTime.of(2021, 6, 15, 8, 30);
            department.setCreatedAt(fixedCreatedAt);
            department.setUpdatedAt(fixedUpdatedAt);

            department.prePersist();

            assertEquals(fixedCreatedAt, department.getCreatedAt());
            assertEquals(fixedUpdatedAt, department.getUpdatedAt());
        }

        @Test
        @DisplayName("preUpdate refreshes updatedAt")
        void preUpdate_refreshesUpdatedAt() {
            Department department = new Department();
            department.setName("Engineering");
            department.prePersist();

            LocalDateTime originalUpdatedAt = department.getUpdatedAt();

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            department.preUpdate();

            assertNotNull(department.getUpdatedAt());
            assertFalse(department.getUpdatedAt().isBefore(originalUpdatedAt));
        }

        @Test
        @DisplayName("preUpdate does not modify createdAt")
        void preUpdate_doesNotTouchCreatedAt() {
            Department department = new Department();
            department.setName("Engineering");
            department.prePersist();

            LocalDateTime originalCreatedAt = department.getCreatedAt();

            department.preUpdate();

            assertEquals(originalCreatedAt, department.getCreatedAt());
        }
    }

    @Nested
    @DisplayName("User Tests")
    class UserTests {

        @Test
        @DisplayName("prePersist generates UUID when id is null")
        void prePersist_generatesUuid() {
            User user = new User();
            user.setEmail("user@example.com");
            user.setPassword("secret");
            user.setName("Alice");
            user.setRole("admin");

            user.prePersist();

            assertNotNull(user.getId());
            assertDoesNotThrow(() -> UUID.fromString(user.getId()));
        }

        @Test
        @DisplayName("prePersist does not overwrite existing id")
        void prePersist_preservesExistingId() {
            User user = new User();
            String existingId = UUID.randomUUID().toString();
            user.setId(existingId);
            user.setEmail("user@example.com");
            user.setPassword("secret");
            user.setName("Alice");
            user.setRole("admin");

            user.prePersist();

            assertEquals(existingId, user.getId());
        }

        @Test
        @DisplayName("prePersist sets createdAt when null")
        void prePersist_setsCreatedAt() {
            User user = new User();
            user.setEmail("user@example.com");
            user.setPassword("secret");
            user.setName("Alice");
            user.setRole("admin");

            LocalDateTime before = LocalDateTime.now().minusSeconds(1);

            user.prePersist();

            LocalDateTime after = LocalDateTime.now().plusSeconds(1);

            assertNotNull(user.getCreatedAt());
            assertFalse(user.getCreatedAt().isBefore(before));
            assertFalse(user.getCreatedAt().isAfter(after));
        }

        @Test
        @DisplayName("prePersist does not overwrite existing createdAt")
        void prePersist_preservesExistingCreatedAt() {
            User user = new User();
            user.setEmail("user@example.com");
            user.setPassword("secret");
            user.setName("Alice");
            user.setRole("admin");

            LocalDateTime fixedCreatedAt = LocalDateTime.of(2020, 1, 1, 12, 0);
            user.setCreatedAt(fixedCreatedAt);

            user.prePersist();

            assertEquals(fixedCreatedAt, user.getCreatedAt());
        }

        @Test
        @DisplayName("prePersist full lifecycle produces valid entity state")
        void prePersist_fullLifecycle() {
            User user = new User();
            user.setEmail("admin@example.com");
            user.setPassword("hashedPassword123");
            user.setName("Admin User");
            user.setRole("admin");

            user.prePersist();

            assertNotNull(user.getId());
            assertNotNull(user.getCreatedAt());
            assertEquals("admin@example.com", user.getEmail());
            assertEquals("hashedPassword123", user.getPassword());
            assertEquals("Admin User", user.getName());
            assertEquals("admin", user.getRole());
        }
    }

    @Nested
    @DisplayName("Attendance Tests")
    class AttendanceTests {

        @Test
        @DisplayName("prePersist generates UUID when id is null")
        void prePersist_generatesUuid() {
            Attendance attendance = new Attendance();
            attendance.setEmployeeId("emp-1");
            attendance.setDate(LocalDate.of(2025, 7, 20));

            attendance.prePersist();

            assertNotNull(attendance.getId());
            assertDoesNotThrow(() -> UUID.fromString(attendance.getId()));
        }

        @Test
        @DisplayName("prePersist does not overwrite existing id")
        void prePersist_preservesExistingId() {
            Attendance attendance = new Attendance();
            String existingId = UUID.randomUUID().toString();
            attendance.setId(existingId);
            attendance.setEmployeeId("emp-1");
            attendance.setDate(LocalDate.of(2025, 7, 20));

            attendance.prePersist();

            assertEquals(existingId, attendance.getId());
        }

        @Test
        @DisplayName("prePersist sets status to present when null")
        void prePersist_setsDefaultStatus() {
            Attendance attendance = new Attendance();
            attendance.setEmployeeId("emp-1");
            attendance.setDate(LocalDate.of(2025, 7, 20));

            assertNull(attendance.getStatus());

            attendance.prePersist();

            assertEquals("present", attendance.getStatus());
        }

        @Test
        @DisplayName("prePersist does not overwrite existing status")
        void prePersist_preservesExistingStatus() {
            Attendance attendance = new Attendance();
            attendance.setEmployeeId("emp-1");
            attendance.setDate(LocalDate.of(2025, 7, 20));
            attendance.setStatus("absent");

            attendance.prePersist();

            assertEquals("absent", attendance.getStatus());
        }

        @Test
        @DisplayName("prePersist sets createdAt when null")
        void prePersist_setsCreatedAt() {
            Attendance attendance = new Attendance();
            attendance.setEmployeeId("emp-1");
            attendance.setDate(LocalDate.of(2025, 7, 20));

            LocalDateTime before = LocalDateTime.now().minusSeconds(1);

            attendance.prePersist();

            LocalDateTime after = LocalDateTime.now().plusSeconds(1);

            assertNotNull(attendance.getCreatedAt());
            assertFalse(attendance.getCreatedAt().isBefore(before));
            assertFalse(attendance.getCreatedAt().isAfter(after));
        }

        @Test
        @DisplayName("prePersist does not overwrite existing createdAt")
        void prePersist_preservesExistingCreatedAt() {
            Attendance attendance = new Attendance();
            attendance.setEmployeeId("emp-1");
            attendance.setDate(LocalDate.of(2025, 7, 20));

            LocalDateTime fixedCreatedAt = LocalDateTime.of(2020, 1, 1, 12, 0);
            attendance.setCreatedAt(fixedCreatedAt);

            attendance.prePersist();

            assertEquals(fixedCreatedAt, attendance.getCreatedAt());
        }

        @Test
        @DisplayName("prePersist full lifecycle produces valid entity state")
        void prePersist_fullLifecycle() {
            Attendance attendance = new Attendance();
            attendance.setEmployeeId("emp-1");
            attendance.setDate(LocalDate.of(2025, 7, 20));
            attendance.setCheckIn(LocalTime.of(9, 0));
            attendance.setCheckOut(LocalTime.of(17, 30));
            attendance.setStatus("late");
            attendance.setNotes("Traffic delay");

            attendance.prePersist();

            assertNotNull(attendance.getId());
            assertEquals("late", attendance.getStatus());
            assertNotNull(attendance.getCreatedAt());
            assertEquals("emp-1", attendance.getEmployeeId());
            assertEquals(LocalDate.of(2025, 7, 20), attendance.getDate());
            assertEquals(LocalTime.of(9, 0), attendance.getCheckIn());
            assertEquals(LocalTime.of(17, 30), attendance.getCheckOut());
            assertEquals("Traffic delay", attendance.getNotes());
        }
    }

    @Nested
    @DisplayName("Leave Tests")
    class LeaveTests {

        @Test
        @DisplayName("prePersist generates UUID when id is null")
        void prePersist_generatesUuid() {
            Leave leave = new Leave();
            leave.setEmployeeId("emp-1");
            leave.setType("vacation");
            leave.setStartDate(LocalDate.of(2025, 8, 1));
            leave.setEndDate(LocalDate.of(2025, 8, 5));

            leave.prePersist();

            assertNotNull(leave.getId());
            assertDoesNotThrow(() -> UUID.fromString(leave.getId()));
        }

        @Test
        @DisplayName("prePersist does not overwrite existing id")
        void prePersist_preservesExistingId() {
            Leave leave = new Leave();
            String existingId = UUID.randomUUID().toString();
            leave.setId(existingId);
            leave.setEmployeeId("emp-1");
            leave.setType("vacation");
            leave.setStartDate(LocalDate.of(2025, 8, 1));
            leave.setEndDate(LocalDate.of(2025, 8, 5));

            leave.prePersist();

            assertEquals(existingId, leave.getId());
        }

        @Test
        @DisplayName("prePersist sets status to pending when null")
        void prePersist_setsDefaultStatus() {
            Leave leave = new Leave();
            leave.setEmployeeId("emp-1");
            leave.setType("sick");
            leave.setStartDate(LocalDate.of(2025, 8, 1));
            leave.setEndDate(LocalDate.of(2025, 8, 3));

            assertNull(leave.getStatus());

            leave.prePersist();

            assertEquals("pending", leave.getStatus());
        }

        @Test
        @DisplayName("prePersist does not overwrite existing status")
        void prePersist_preservesExistingStatus() {
            Leave leave = new Leave();
            leave.setEmployeeId("emp-1");
            leave.setType("vacation");
            leave.setStartDate(LocalDate.of(2025, 8, 1));
            leave.setEndDate(LocalDate.of(2025, 8, 5));
            leave.setStatus("approved");

            leave.prePersist();

            assertEquals("approved", leave.getStatus());
        }

        @Test
        @DisplayName("prePersist sets createdAt when null")
        void prePersist_setsCreatedAt() {
            Leave leave = new Leave();
            leave.setEmployeeId("emp-1");
            leave.setType("vacation");
            leave.setStartDate(LocalDate.of(2025, 8, 1));
            leave.setEndDate(LocalDate.of(2025, 8, 5));

            LocalDateTime before = LocalDateTime.now().minusSeconds(1);

            leave.prePersist();

            LocalDateTime after = LocalDateTime.now().plusSeconds(1);

            assertNotNull(leave.getCreatedAt());
            assertFalse(leave.getCreatedAt().isBefore(before));
            assertFalse(leave.getCreatedAt().isAfter(after));
        }

        @Test
        @DisplayName("prePersist does not overwrite existing createdAt")
        void prePersist_preservesExistingCreatedAt() {
            Leave leave = new Leave();
            leave.setEmployeeId("emp-1");
            leave.setType("vacation");
            leave.setStartDate(LocalDate.of(2025, 8, 1));
            leave.setEndDate(LocalDate.of(2025, 8, 5));

            LocalDateTime fixedCreatedAt = LocalDateTime.of(2020, 1, 1, 12, 0);
            leave.setCreatedAt(fixedCreatedAt);

            leave.prePersist();

            assertEquals(fixedCreatedAt, leave.getCreatedAt());
        }

        @Test
        @DisplayName("prePersist full lifecycle produces valid entity state")
        void prePersist_fullLifecycle() {
            Leave leave = new Leave();
            leave.setEmployeeId("emp-1");
            leave.setType("vacation");
            leave.setStartDate(LocalDate.of(2025, 8, 1));
            leave.setEndDate(LocalDate.of(2025, 8, 5));
            leave.setReason("Summer holiday");
            leave.setApprovedBy("manager-1");

            leave.prePersist();

            assertNotNull(leave.getId());
            assertEquals("pending", leave.getStatus());
            assertNotNull(leave.getCreatedAt());
            assertEquals("emp-1", leave.getEmployeeId());
            assertEquals("vacation", leave.getType());
            assertEquals(LocalDate.of(2025, 8, 1), leave.getStartDate());
            assertEquals(LocalDate.of(2025, 8, 5), leave.getEndDate());
            assertEquals("Summer holiday", leave.getReason());
            assertEquals("manager-1", leave.getApprovedBy());
        }
    }

    @Nested
    @DisplayName("Payroll Tests")
    class PayrollTests {

        @Test
        @DisplayName("prePersist generates UUID when id is null")
        void prePersist_generatesUuid() {
            Payroll payroll = new Payroll();
            payroll.setEmployeeId("emp-1");
            payroll.setPayMonth(7);
            payroll.setPayYear(2025);
            payroll.setBasicPay(5000.0);
            payroll.setNetPay(5000.0);

            payroll.prePersist();

            assertNotNull(payroll.getId());
            assertDoesNotThrow(() -> UUID.fromString(payroll.getId()));
        }

        @Test
        @DisplayName("prePersist does not overwrite existing id")
        void prePersist_preservesExistingId() {
            Payroll payroll = new Payroll();
            String existingId = UUID.randomUUID().toString();
            payroll.setId(existingId);
            payroll.setEmployeeId("emp-1");
            payroll.setPayMonth(7);
            payroll.setPayYear(2025);
            payroll.setBasicPay(5000.0);
            payroll.setNetPay(5000.0);

            payroll.prePersist();

            assertEquals(existingId, payroll.getId());
        }

        @Test
        @DisplayName("prePersist sets allowances to 0.0 when null")
        void prePersist_setsDefaultAllowances() {
            Payroll payroll = new Payroll();
            payroll.setEmployeeId("emp-1");
            payroll.setPayMonth(7);
            payroll.setPayYear(2025);
            payroll.setBasicPay(5000.0);
            payroll.setNetPay(5000.0);

            assertNull(payroll.getAllowances());

            payroll.prePersist();

            assertEquals(0.0, payroll.getAllowances());
        }

        @Test
        @DisplayName("prePersist does not overwrite existing allowances")
        void prePersist_preservesExistingAllowances() {
            Payroll payroll = new Payroll();
            payroll.setEmployeeId("emp-1");
            payroll.setPayMonth(7);
            payroll.setPayYear(2025);
            payroll.setBasicPay(5000.0);
            payroll.setAllowances(1500.0);
            payroll.setNetPay(6500.0);

            payroll.prePersist();

            assertEquals(1500.0, payroll.getAllowances());
        }

        @Test
        @DisplayName("prePersist sets deductions to 0.0 when null")
        void prePersist_setsDefaultDeductions() {
            Payroll payroll = new Payroll();
            payroll.setEmployeeId("emp-1");
            payroll.setPayMonth(7);
            payroll.setPayYear(2025);
            payroll.setBasicPay(5000.0);
            payroll.setNetPay(5000.0);

            assertNull(payroll.getDeductions());

            payroll.prePersist();

            assertEquals(0.0, payroll.getDeductions());
        }

        @Test
        @DisplayName("prePersist does not overwrite existing deductions")
        void prePersist_preservesExistingDeductions() {
            Payroll payroll = new Payroll();
            payroll.setEmployeeId("emp-1");
            payroll.setPayMonth(7);
            payroll.setPayYear(2025);
            payroll.setBasicPay(5000.0);
            payroll.setDeductions(500.0);
            payroll.setNetPay(4500.0);

            payroll.prePersist();

            assertEquals(500.0, payroll.getDeductions());
        }

        @Test
        @DisplayName("prePersist sets status to pending when null")
        void prePersist_setsDefaultStatus() {
            Payroll payroll = new Payroll();
            payroll.setEmployeeId("emp-1");
            payroll.setPayMonth(7);
            payroll.setPayYear(2025);
            payroll.setBasicPay(5000.0);
            payroll.setNetPay(5000.0);

            assertNull(payroll.getStatus());

            payroll.prePersist();

            assertEquals("pending", payroll.getStatus());
        }

        @Test
        @DisplayName("prePersist does not overwrite existing status")
        void prePersist_preservesExistingStatus() {
            Payroll payroll = new Payroll();
            payroll.setEmployeeId("emp-1");
            payroll.setPayMonth(7);
            payroll.setPayYear(2025);
            payroll.setBasicPay(5000.0);
            payroll.setNetPay(5000.0);
            payroll.setStatus("paid");

            payroll.prePersist();

            assertEquals("paid", payroll.getStatus());
        }

        @Test
        @DisplayName("prePersist sets createdAt when null")
        void prePersist_setsCreatedAt() {
            Payroll payroll = new Payroll();
            payroll.setEmployeeId("emp-1");
            payroll.setPayMonth(7);
            payroll.setPayYear(2025);
            payroll.setBasicPay(5000.0);
            payroll.setNetPay(5000.0);

            LocalDateTime before = LocalDateTime.now().minusSeconds(1);

            payroll.prePersist();

            LocalDateTime after = LocalDateTime.now().plusSeconds(1);

            assertNotNull(payroll.getCreatedAt());
            assertFalse(payroll.getCreatedAt().isBefore(before));
            assertFalse(payroll.getCreatedAt().isAfter(after));
        }

        @Test
        @DisplayName("prePersist does not overwrite existing createdAt")
        void prePersist_preservesExistingCreatedAt() {
            Payroll payroll = new Payroll();
            payroll.setEmployeeId("emp-1");
            payroll.setPayMonth(7);
            payroll.setPayYear(2025);
            payroll.setBasicPay(5000.0);
            payroll.setNetPay(5000.0);

            LocalDateTime fixedCreatedAt = LocalDateTime.of(2020, 1, 1, 12, 0);
            payroll.setCreatedAt(fixedCreatedAt);

            payroll.prePersist();

            assertEquals(fixedCreatedAt, payroll.getCreatedAt());
        }

        @Test
        @DisplayName("prePersist full lifecycle produces valid entity state")
        void prePersist_fullLifecycle() {
            Payroll payroll = new Payroll();
            payroll.setEmployeeId("emp-1");
            payroll.setPayMonth(7);
            payroll.setPayYear(2025);
            payroll.setBasicPay(6000.0);
            payroll.setAllowances(2000.0);
            payroll.setDeductions(800.0);
            payroll.setNetPay(7200.0);

            payroll.prePersist();

            assertNotNull(payroll.getId());
            assertEquals(2000.0, payroll.getAllowances());
            assertEquals(800.0, payroll.getDeductions());
            assertEquals("pending", payroll.getStatus());
            assertNotNull(payroll.getCreatedAt());
            assertEquals("emp-1", payroll.getEmployeeId());
            assertEquals(7, payroll.getPayMonth());
            assertEquals(2025, payroll.getPayYear());
            assertEquals(6000.0, payroll.getBasicPay());
            assertEquals(7200.0, payroll.getNetPay());
        }
    }
}
