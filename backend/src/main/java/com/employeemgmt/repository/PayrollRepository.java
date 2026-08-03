package com.employeemgmt.repository;

import com.employeemgmt.model.Payroll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PayrollRepository extends JpaRepository<Payroll, String> {

    List<Payroll> findByEmployeeId(String employeeId);

    List<Payroll> findByPayMonthAndPayYear(int payMonth, int payYear);

    List<Payroll> findByEmployeeIdAndPayMonthAndPayYear(String employeeId, int payMonth, int payYear);

    List<Payroll> findByStatus(String status);

    Optional<Payroll> findByEmployeeIdAndPayMonthAndPayYearAndStatus(String employeeId, int payMonth, int payYear, String status);

    List<Payroll> findByPayYear(int payYear);
}
