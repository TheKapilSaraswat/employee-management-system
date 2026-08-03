package com.employeemgmt.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class AttendanceRequest {

    @NotBlank
    private String employeeId;

    private LocalDate date;

    private LocalTime checkIn;

    private LocalTime checkOut;

    private String notes;

    private String action;
}
