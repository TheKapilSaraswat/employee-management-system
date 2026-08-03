package com.employeemgmt.service;

import com.employeemgmt.dto.AttendanceRequest;
import com.employeemgmt.model.Attendance;
import com.employeemgmt.repository.AttendanceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;

    public AttendanceService(AttendanceRepository attendanceRepository) {
        this.attendanceRepository = attendanceRepository;
    }

    public List<Attendance> getAllAttendances(String employeeId, LocalDate startDate, LocalDate endDate, LocalDate date) {
        if (date != null) {
            if (employeeId != null && !employeeId.isEmpty()) {
                return attendanceRepository.findByEmployeeIdAndDate(employeeId, date)
                        .map(List::of).orElseGet(List::of);
            }
            return attendanceRepository.findByDate(date);
        }
        if (employeeId != null && !employeeId.isEmpty() && startDate != null && endDate != null) {
            return attendanceRepository.findByEmployeeIdAndDateBetween(employeeId, startDate, endDate);
        } else if (employeeId != null && !employeeId.isEmpty()) {
            return attendanceRepository.findByEmployeeId(employeeId);
        } else if (startDate != null && endDate != null) {
            return attendanceRepository.findByDateBetween(startDate, endDate);
        }
        return attendanceRepository.findAll();
    }

    public Attendance recordAttendance(AttendanceRequest request) {
        LocalDate today = LocalDate.now();

        if ("in".equalsIgnoreCase(request.getAction())) {
            Attendance existing = attendanceRepository
                    .findByEmployeeIdAndDateAndCheckOutIsNull(request.getEmployeeId(), today)
                    .stream().findFirst().orElse(null);

            if (existing != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Already checked in today");
            }

            Attendance attendance = new Attendance();
            attendance.setEmployeeId(request.getEmployeeId());
            attendance.setDate(today);
            attendance.setCheckIn(LocalTime.now());
            attendance.setStatus("present");
            attendance.setNotes(request.getNotes());

            return attendanceRepository.save(attendance);
        } else if ("out".equalsIgnoreCase(request.getAction())) {
            Attendance attendance = attendanceRepository
                    .findByEmployeeIdAndDateAndCheckOutIsNull(request.getEmployeeId(), today)
                    .stream().findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No check-in record found for today"));

            attendance.setCheckOut(LocalTime.now());
            return attendanceRepository.save(attendance);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Action must be 'in' or 'out'");
        }
    }

    public List<Attendance> getTodayAttendance() {
        return attendanceRepository.findByDate(LocalDate.now());
    }

    public Attendance updateAttendance(String id, AttendanceRequest request) {
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Attendance record not found"));

        if (request.getCheckIn() != null) {
            attendance.setCheckIn(request.getCheckIn());
        }
        if (request.getCheckOut() != null) {
            attendance.setCheckOut(request.getCheckOut());
        }
        if (request.getNotes() != null) {
            attendance.setNotes(request.getNotes());
        }

        return attendanceRepository.save(attendance);
    }
}
