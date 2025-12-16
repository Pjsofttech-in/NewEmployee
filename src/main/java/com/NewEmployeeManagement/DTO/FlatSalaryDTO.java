package com.NewEmployeeManagement.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FlatSalaryDTO {

    // ===== Salary Fields =====
    private Long id;
    private Long empId;
    private String fullName;
    private String department;
    private String employeecategory;
    private BigDecimal basicSalary;
    private BigDecimal actualBasic;
    private BigDecimal hraAllowance;
    private BigDecimal taAllowance;
    private BigDecimal incentive;
    private BigDecimal spi;
    private BigDecimal medicalAllowance;
    private BigDecimal pf;
    private BigDecimal esic;
    private BigDecimal professionalTax;
    private BigDecimal incomeTax;
    private BigDecimal companyFund;
    private BigDecimal deductions;
    private BigDecimal tds;
    private BigDecimal netSalaryBeforeTaxes;
    private BigDecimal finalNetSalary;
    private Integer month;
    private Integer year;
    private Double workingDays;
    private Integer daysOfMonth;
    private Long transactionId;
    private BigDecimal penalty;
    private String createdByEmail;
    private String role;
    private String branchCode;
    private String status;
    private LocalDate paymentDate;
    private boolean deleted;

    private LocalDate dob;
    private Long adharNo;
    private String panNo;
    private String mobileNo;
    private String empEmail;
    private String bankName;
    private Long accountNumber;
    private String cpfNo;
    private String designation;
}
