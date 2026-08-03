package com.employeemgmt.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payrolls")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payroll {

    @Id
    private String id;

    @Column(nullable = false)
    private String employeeId;

    @Column(name = "pay_month", nullable = false)
    private int payMonth;

    @Column(name = "pay_year", nullable = false)
    private int payYear;

    @Column(nullable = false)
    private Double basicPay;

    private Double allowances;

    private Double deductions;

    @Column(nullable = false)
    private Double netPay;

    @Column(nullable = false)
    private String status;

    private LocalDateTime paidAt;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        if (allowances == null) {
            allowances = 0.0;
        }
        if (deductions == null) {
            deductions = 0.0;
        }
        if (status == null) {
            status = "pending";
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
