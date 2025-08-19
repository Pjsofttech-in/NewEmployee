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
    private BigDecimal hraPercentage;   //percentage value
    private Double medicalAllowancePercentage;  // percentage value
    private BigDecimal pfPercentage;        //percentage value
    private BigDecimal esicPercentage;  //percentage value
    private Double professionalTaxPercentage;  //ruppess
    private Double incomeTaxPercentage;     //percentage value
    private Double totalPaidLeave;
    private Double totalUnpaidLeave;
    private boolean isDeleted = false;
    private Double pt;
    private Double insentive;           //ruppess
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