package com.employeemgmt.repository;

import com.employeemgmt.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, String> {

    List<Employee> findByDepartmentId(String departmentId);

    List<Employee> findByStatus(String status);

    List<Employee> findByStatusAndDepartmentId(String status, String departmentId);

    @Query("SELECT e FROM Employee e WHERE e.status = ?1 AND " +
           "(LOWER(e.firstName) LIKE LOWER(CONCAT('%', ?2, '%')) OR " +
           "LOWER(e.lastName) LIKE LOWER(CONCAT('%', ?2, '%')) OR " +
           "LOWER(e.email) LIKE LOWER(CONCAT('%', ?2, '%')) OR " +
           "LOWER(e.employeeCode) LIKE LOWER(CONCAT('%', ?2, '%')))")
    List<Employee> searchEmployees(String status, String search);

    @Query("SELECT e FROM Employee e WHERE " +
           "(LOWER(e.firstName) LIKE LOWER(CONCAT('%', ?1, '%')) OR " +
           "LOWER(e.lastName) LIKE LOWER(CONCAT('%', ?1, '%')) OR " +
           "LOWER(e.email) LIKE LOWER(CONCAT('%', ?1, '%')) OR " +
           "LOWER(e.employeeCode) LIKE LOWER(CONCAT('%', ?1, '%')))")
    List<Employee> searchEmployeesAll(String search);

    @Query("SELECT MAX(e.employeeCode) FROM Employee e")
    Optional<String> findMaxEmployeeCode();

    List<Employee> findByDepartmentIdAndStatus(String departmentId, String status);

    long countByDepartmentId(String departmentId);

    long countByStatus(String status);
}
