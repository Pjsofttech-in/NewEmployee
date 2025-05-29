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
    private Double medicalAllowancePercentage;
    private BigDecimal pfPercentage;
    private BigDecimal esicPercentage;
    private Double professionalTaxPercentage;
    private Double incomeTaxPercentage;
    private Double totalPaidLeave;
    private Double totalUnpaidLeave;
    private boolean isDeleted = false;
    private Double pt;
    private Double insentive;
    private Double tds;
    private Double ta;
    private Double companyFund;
    @Email
    private String createdByEmail;
    private String role;
    private String branchCode;

    @OneToMany(mappedBy = "employeeCategory", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Employee> employees = new ArrayList<>();

}