package com.employeemgmt.repository;

import com.employeemgmt.model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, String> {

    List<Attendance> findByEmployeeId(String employeeId);

    List<Attendance> findByDateBetween(LocalDate startDate, LocalDate endDate);

    List<Attendance> findByEmployeeIdAndDateBetween(String employeeId, LocalDate startDate, LocalDate endDate);

    Optional<Attendance> findByEmployeeIdAndDate(String employeeId, LocalDate date);

    List<Attendance> findByDate(LocalDate date);

    long countByDate(LocalDate date);

    List<Attendance> findByEmployeeIdAndDateAndCheckOutIsNull(String employeeId, LocalDate date);
}
