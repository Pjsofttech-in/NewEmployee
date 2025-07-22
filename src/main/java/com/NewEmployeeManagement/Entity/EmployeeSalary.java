package com.NewEmployeeManagement.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeSalary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int empID;
    private String fullName;
    private String department;
    private String employeecategory;
    private BigDecimal basicSalary;
    private BigDecimal hraAllowance;
    private BigDecimal taAllowance;
    private BigDecimal incentive;
    private BigDecimal spi;     //DA on frontend
    private BigDecimal medicalAllowance;
    private BigDecimal pf;
    private BigDecimal esic;
    private BigDecimal professionalTax;
    private BigDecimal incomeTax;
    private BigDecimal deductions;
    private BigDecimal netSalaryBeforeTaxes;
    private BigDecimal finalNetSalary;
    private int month;
    private int year;
    private Double workingDays;
    private int daysOfMonth;
    private Long transactionId;
    private BigDecimal penalty;

    @Email
    private String createdByEmail;
    private String role;
    private String branchCode;


    private boolean isDeleted = false;
    @Column(nullable = false, columnDefinition = "VARCHAR(20) DEFAULT 'Pending'")
    private String status = "Pending";

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate paymentDate;
}
