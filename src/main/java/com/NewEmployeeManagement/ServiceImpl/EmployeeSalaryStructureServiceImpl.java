package com.NewEmployeeManagement.ServiceImpl;

import com.NewEmployeeManagement.DTO.EmployeeSalaryStructureRequest;
import com.NewEmployeeManagement.Entity.Employee;
import com.NewEmployeeManagement.Entity.EmployeeSalaryStructure;
import com.NewEmployeeManagement.Entity.EmployeeSalaryStructure.SalaryStatus;
import com.NewEmployeeManagement.Repository.EmployeeRepository;
import com.NewEmployeeManagement.Repository.EmployeeSalaryStructureRepository;
import com.NewEmployeeManagement.Service.EmployeeSalaryStructureService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeSalaryStructureServiceImpl
        implements EmployeeSalaryStructureService {

    private final EmployeeSalaryStructureRepository salaryRepository;

    private final EmployeeRepository employeeRepository;


    @Override
    @Transactional
    public EmployeeSalaryStructure createSalaryStructure(
            EmployeeSalaryStructureRequest request) {

        Employee employee =
                employeeRepository.findById(request.getEmployeeId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Employee not found with ID: "
                                                + request.getEmployeeId()
                                )
                        );


        /*
         * If a new salary structure is created,
         * deactivate the previous active structure.
         */
        salaryRepository
                .findByEmployeeIdAndStatus(
                        employee.getId(),
                        SalaryStatus.ACTIVE
                )
                .ifPresent(existing -> {

                    existing.setStatus(
                            SalaryStatus.INACTIVE
                    );

                    if (request.getEffectiveFrom() != null) {
                        existing.setEffectiveTo(
                                request.getEffectiveFrom().minusDays(1)
                        );
                    }

                    salaryRepository.save(existing);
                });


        EmployeeSalaryStructure salary =
                new EmployeeSalaryStructure();

        salary.setEmployee(employee);

        salary.setBasicSalary(
                defaultZero(request.getBasicSalary())
        );

        salary.setHra(
                defaultZero(request.getHra())
        );

        salary.setOtherAllowances(
                defaultZero(request.getOtherAllowances())
        );

        salary.setMonthlyIncentive(
                defaultZero(request.getMonthlyIncentive())
        );

        salary.setPfApplicable(
                request.isPfApplicable()
        );

        salary.setEmployeePfPercentage(
                defaultPercentage(
                        request.getEmployeePfPercentage()
                )
        );

        salary.setEmployerPfPercentage(
                defaultPercentage(
                        request.getEmployerPfPercentage()
                )
        );

        salary.setEffectiveFrom(
                request.getEffectiveFrom()
        );

        salary.setEffectiveTo(
                request.getEffectiveTo()
        );

        salary.setStatus(SalaryStatus.ACTIVE);


        calculateSalary(salary);


        return salaryRepository.save(salary);
    }


    @Override
    @Transactional
    public EmployeeSalaryStructure updateSalaryStructure(
            Long id,
            EmployeeSalaryStructureRequest request) {

        EmployeeSalaryStructure salary =
                salaryRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Salary structure not found with ID: "
                                                + id
                                )
                        );


        Employee employee =
                employeeRepository.findById(request.getEmployeeId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Employee not found with ID: "
                                                + request.getEmployeeId()
                                )
                        );


        salary.setEmployee(employee);

        salary.setBasicSalary(
                defaultZero(request.getBasicSalary())
        );

        salary.setHra(
                defaultZero(request.getHra())
        );

        salary.setOtherAllowances(
                defaultZero(request.getOtherAllowances())
        );

        salary.setMonthlyIncentive(
                defaultZero(request.getMonthlyIncentive())
        );

        salary.setPfApplicable(
                request.isPfApplicable()
        );

        salary.setEmployeePfPercentage(
                defaultPercentage(
                        request.getEmployeePfPercentage()
                )
        );

        salary.setEmployerPfPercentage(
                defaultPercentage(
                        request.getEmployerPfPercentage()
                )
        );

        salary.setEffectiveFrom(
                request.getEffectiveFrom()
        );

        salary.setEffectiveTo(
                request.getEffectiveTo()
        );


        calculateSalary(salary);


        return salaryRepository.save(salary);
    }


    @Override
    @Transactional(readOnly = true)
    public EmployeeSalaryStructure getSalaryStructureById(
            Long id) {

        return salaryRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Salary structure not found with ID: "
                                        + id
                        )
                );
    }


    @Override
    @Transactional(readOnly = true)
    public EmployeeSalaryStructure getCurrentSalaryStructure(
            Long employeeId) {

        return salaryRepository
                .findByEmployeeIdAndStatus(
                        employeeId,
                        SalaryStatus.ACTIVE
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Active salary structure not found for employee: "
                                        + employeeId
                        )
                );
    }


    @Override
    @Transactional(readOnly = true)
    public List<EmployeeSalaryStructure> getSalaryHistory(
            Long employeeId) {

        return salaryRepository
                .findByEmployeeIdOrderByEffectiveFromDesc(
                        employeeId
                );
    }


    @Override
    @Transactional
    public void deleteSalaryStructure(Long id) {

        EmployeeSalaryStructure salary =
                salaryRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Salary structure not found with ID: "
                                                + id
                                )
                        );

        salaryRepository.delete(salary);
    }


    // ==========================================
    // SALARY CALCULATION
    // ==========================================

    private void calculateSalary(
            EmployeeSalaryStructure salary) {

        BigDecimal basic =
                defaultZero(salary.getBasicSalary());

        BigDecimal hra =
                defaultZero(salary.getHra());

        BigDecimal allowances =
                defaultZero(
                        salary.getOtherAllowances()
                );

        BigDecimal incentive =
                defaultZero(
                        salary.getMonthlyIncentive()
                );


        // ------------------------------------------
        // Gross Salary
        // ------------------------------------------

        BigDecimal grossSalary =
                basic
                        .add(hra)
                        .add(allowances)
                        .add(incentive)
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        salary.setGrossSalary(grossSalary);


        // ------------------------------------------
        // PF
        // ------------------------------------------

        BigDecimal employeePf =
                BigDecimal.ZERO;

        BigDecimal employerPf =
                BigDecimal.ZERO;


        if (salary.isPfApplicable()) {

            BigDecimal employeePercentage =
                    defaultPercentage(
                            salary.getEmployeePfPercentage()
                    );

            BigDecimal employerPercentage =
                    defaultPercentage(
                            salary.getEmployerPfPercentage()
                    );


            employeePf =
                    basic
                            .multiply(employeePercentage)
                            .divide(
                                    new BigDecimal("100"),
                                    2,
                                    RoundingMode.HALF_UP
                            );


            employerPf =
                    basic
                            .multiply(employerPercentage)
                            .divide(
                                    new BigDecimal("100"),
                                    2,
                                    RoundingMode.HALF_UP
                            );
        }


        salary.setEmployeePf(employeePf);
        salary.setEmployerPf(employerPf);


        // ------------------------------------------
        // Net Salary
        // ------------------------------------------

        BigDecimal netSalary =
                grossSalary
                        .subtract(employeePf)
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        salary.setNetSalary(netSalary);


        // ------------------------------------------
        // Monthly CTC
        // ------------------------------------------

        BigDecimal monthlyCtc =
                grossSalary
                        .add(employerPf)
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        salary.setMonthlyCtc(monthlyCtc);
    }


    private BigDecimal defaultZero(
            BigDecimal value) {

        return value != null
                ? value
                : BigDecimal.ZERO;
    }


    private BigDecimal defaultPercentage(
            BigDecimal value) {

        return value != null
                ? value
                : new BigDecimal("12.00");
    }
}