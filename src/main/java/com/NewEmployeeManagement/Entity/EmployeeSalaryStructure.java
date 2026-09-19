package com.NewEmployeeManagement.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "employee_salary_structure",
        indexes = {
                @Index(
                        name = "idx_salary_employee",
                        columnList = "employee_id"
                ),
                @Index(
                        name = "idx_salary_status",
                        columnList = "status"
                )
        }
)
public class EmployeeSalaryStructure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "employee_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_salary_employee"
            )
    )
    @JsonIgnore
    private Employee employee;


    // =========================
    // EARNINGS
    // =========================

    @Column(
            name = "basic_salary",
            precision = 12,
            scale = 2,
            nullable = false
    )
    private BigDecimal basicSalary = BigDecimal.ZERO;

    @Column(
            name = "hra",
            precision = 12,
            scale = 2,
            nullable = false
    )
    private BigDecimal hra = BigDecimal.ZERO;

    @Column(
            name = "other_allowances",
            precision = 12,
            scale = 2,
            nullable = false
    )
    private BigDecimal otherAllowances = BigDecimal.ZERO;

    @Column(
            name = "monthly_incentive",
            precision = 12,
            scale = 2,
            nullable = false
    )
    private BigDecimal monthlyIncentive = BigDecimal.ZERO;


    // =========================
    // PF
    // =========================

    @Column(name = "pf_applicable", nullable = false)
    private boolean pfApplicable = true;

    @Column(
            name = "employee_pf_percentage",
            precision = 5,
            scale = 2
    )
    private BigDecimal employeePfPercentage =
            new BigDecimal("12.00");

    @Column(
            name = "employer_pf_percentage",
            precision = 5,
            scale = 2
    )
    private BigDecimal employerPfPercentage =
            new BigDecimal("12.00");

    @Column(
            name = "employee_pf",
            precision = 12,
            scale = 2,
            nullable = false
    )
    private BigDecimal employeePf = BigDecimal.ZERO;

    @Column(
            name = "employer_pf",
            precision = 12,
            scale = 2,
            nullable = false
    )
    private BigDecimal employerPf = BigDecimal.ZERO;


    // =========================
    // CALCULATED VALUES
    // =========================

    @Column(
            name = "gross_salary",
            precision = 12,
            scale = 2,
            nullable = false
    )
    private BigDecimal grossSalary = BigDecimal.ZERO;

    @Column(
            name = "net_salary",
            precision = 12,
            scale = 2,
            nullable = false
    )
    private BigDecimal netSalary = BigDecimal.ZERO;

    @Column(
            name = "monthly_ctc",
            precision = 12,
            scale = 2,
            nullable = false
    )
    private BigDecimal monthlyCtc = BigDecimal.ZERO;


    // =========================
    // EFFECTIVE PERIOD
    // =========================

    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;


    // =========================
    // STATUS
    // =========================

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            length = 20,
            nullable = false
    )
    private SalaryStatus status = SalaryStatus.ACTIVE;


    // =========================
    // AUDIT
    // =========================

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;


    @PrePersist
    protected void onCreate() {

        LocalDateTime now = LocalDateTime.now();

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {

        updatedAt = LocalDateTime.now();
    }

    public enum SalaryStatus {
        ACTIVE,
        INACTIVE
    }
}