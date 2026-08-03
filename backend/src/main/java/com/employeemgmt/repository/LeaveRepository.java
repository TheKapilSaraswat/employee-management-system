package com.employeemgmt.repository;

import com.employeemgmt.model.Leave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRepository extends JpaRepository<Leave, String> {

    List<Leave> findByEmployeeId(String employeeId);

    List<Leave> findByStatus(String status);

    List<Leave> findByEmployeeIdAndStatus(String employeeId, String status);

    List<Leave> findByType(String type);

    List<Leave> findByEmployeeIdAndTypeAndStatus(String employeeId, String type, String status);

    List<Leave> findByStartDateBetween(LocalDate startDate, LocalDate endDate);

    List<Leave> findByEmployeeIdAndStartDateBetween(String employeeId, LocalDate startDate, LocalDate endDate);

    long countByTypeAndStatus(String type, String status);

    long countByStatus(String status);
}
