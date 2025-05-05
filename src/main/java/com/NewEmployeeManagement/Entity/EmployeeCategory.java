package com.NewEmployeeManagement.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import lombok.*;

import java.math.BigDecimal;

@Entity
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class EmployeeCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String categoryName;

    //private BigDecimal bonusPercentage;
//    private String department;
    private BigDecimal hraPercentage;
    //    private BigDecimal taPercentage;
//    private BigDecimal incentivePercentage;
//    private BigDecimal spiPercentage;
    private BigDecimal medicalAllowancePercentage;
    private BigDecimal pfPercentage;
    private BigDecimal esicPercentage;
    private BigDecimal professionalTaxPercentage;
    private BigDecimal incomeTaxPercentage;
    private Double totalPaidLeave;
    private Double totalUnpaidLeave;
    private boolean isDeleted = false;

    @Email
    private String createdByEmail;
    private String role;
    private String branchCode;
}