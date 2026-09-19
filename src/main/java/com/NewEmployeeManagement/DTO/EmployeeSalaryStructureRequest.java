package com.NewEmployeeManagement.DTO;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class EmployeeSalaryStructureRequest {

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @NotNull(message = "Basic salary is required")
    @DecimalMin(
            value = "0.00",
            message = "Basic salary cannot be negative"
    )
    private BigDecimal basicSalary;

    @DecimalMin(
            value = "0.00",
            message = "HRA cannot be negative"
    )
    private BigDecimal hra = BigDecimal.ZERO;

    @DecimalMin(
            value = "0.00",
            message = "Other allowances cannot be negative"
    )
    private BigDecimal otherAllowances = BigDecimal.ZERO;

    @DecimalMin(
            value = "0.00",
            message = "Monthly incentive cannot be negative"
    )
    private BigDecimal monthlyIncentive = BigDecimal.ZERO;

    private boolean pfApplicable = true;

    private BigDecimal employeePfPercentage =
            new BigDecimal("12.00");

    private BigDecimal employerPfPercentage =
            new BigDecimal("12.00");

    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;
}