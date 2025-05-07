package com.NewEmployeeManagement.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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
    private BigDecimal hraPercentage;
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

    @OneToMany(mappedBy = "employeeCategory", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Employee> employees = new ArrayList<>();

}