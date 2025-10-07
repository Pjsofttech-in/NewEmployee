package com.NewEmployeeManagement.ServiceImpl;

import com.NewEmployeeManagement.DTO.EmployeeSalaryFilterDTO;
import com.NewEmployeeManagement.DTO.MonthSalaryResponse;
import com.NewEmployeeManagement.DTO.SalarySummaryResponseDTO;
import com.NewEmployeeManagement.Entity.Employee;
import com.NewEmployeeManagement.Entity.EmployeeCategory;
import com.NewEmployeeManagement.Entity.EmployeeSalary;
import com.NewEmployeeManagement.Pageination.EmployeeSalarySpecification;
import com.NewEmployeeManagement.Repository.*;
import com.NewEmployeeManagement.Service.AttendenceService;
import com.NewEmployeeManagement.Service.PermissionService;
import com.NewEmployeeManagement.Service.SalaryService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;

@Service
public class SalaryServiceImpl implements SalaryService
{
    @Autowired
     EmployeeRepository employeeRepository;

    @Autowired
    EmployeeCategoryRepository employeeCategoryRepository;

    @Autowired
    SalaryRepository employeeSalaryRepository;

    @Autowired
     AttendenceRepository attendanceRepository;

    @Autowired
    LeaveRequestRepository leaveRequestRepository;

    @Autowired
     HolidaysRepository holidaysRepository;

    @Autowired
     AttendenceService attendenceService;

    @Autowired
    PermissionService permissionService;


    @Override
    public EmployeeSalary saveSalary(EmployeeSalary salary, Long empId, String role, String email) {

        if (!permissionService.hasPermission(role, email, "POST")) {
            throw new AccessDeniedException("No permission to create salary");
        }
        String branchCode = permissionService.fetchBranchCode(role, email);


        Employee employee = employeeRepository.findById(empId)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + empId));

        boolean exists = employeeSalaryRepository.existsByEmpIdAndMonthAndYear(
                empId, salary.getMonth(), salary.getYear()
        );
        if (exists) {
            String monthName = java.time.Month.of(salary.getMonth())
                    .getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH);

            throw new RuntimeException(
                    "Salary already exists for Employee ID = " + empId +
                            " for " + monthName + " " + salary.getYear()
            );
        }

        salary.setEmpId(empId);
        salary.setFullName(employee.getFullName());
        salary.setDepartment(employee.getDepartment());
        salary.setEmployeecategory(employee.getCategoryName());
        salary.setCreatedByEmail(email);
        salary.setRole(role);
        salary.setBranchCode(branchCode);
        salary.setBasicSalary(employee.getSalary());

//        int daysInMonth = YearMonth.of(salary.getYear(), salary.getMonth()).lengthOfMonth();
        salary.setDaysOfMonth(salary.getDaysOfMonth());

        double presentDays = Optional.ofNullable(
                attendenceService.getAttendanceCount(empId, salary.getMonth(), salary.getYear())
        ).orElse(0L).doubleValue();

        Double paidLeave = Optional.ofNullable(
                leaveRequestRepository.getPaidLeaveForMonth(empId, salary.getMonth(), salary.getYear())
        ).orElse(0.0);

        Double paidHolidays = Optional.ofNullable(
                holidaysRepository.getPaidHolidaysForMonth(branchCode, salary.getMonth(), salary.getYear())
        ).map(Long::doubleValue).orElse(0.0);

        double totalWorkingDays = presentDays + paidLeave + paidHolidays;
        salary.setWorkingDays(totalWorkingDays);

        EmployeeSalary computed = calculateSalary(salary, salary.getDaysOfMonth(), employee);

        return employeeSalaryRepository.save(computed);
    }

    private EmployeeSalary calculateSalary(EmployeeSalary salary, int daysInMonth, Employee employee) {
        EmployeeCategory cat = employeeCategoryRepository.findCategoryByCategoryName(
                employee.getCategoryName(), salary.getBranchCode()
        );
        if (cat == null) {
            throw new RuntimeException("Employee category not found for: " + employee.getCategoryName());
        }

        BigDecimal monthlyBasic = employee.getSalary() != null ? employee.getSalary() : BigDecimal.ZERO;
        if (monthlyBasic.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Basic salary not set for employee: " + salary.getEmpId());
        }

        BigDecimal perDay = monthlyBasic.divide(BigDecimal.valueOf(daysInMonth), 2, RoundingMode.HALF_UP);
        BigDecimal actualBasic = perDay.multiply(BigDecimal.valueOf(salary.getWorkingDays()))
                .setScale(2, RoundingMode.HALF_UP);
        salary.setActualBasic(actualBasic);

        salary.setHraAllowance(percentOf(actualBasic, cat.getHraPercentage()));
        salary.setMedicalAllowance(percentOf(actualBasic, BigDecimal.valueOf(
                Optional.ofNullable(cat.getMedicalAllowancePercentage()).orElse(0.0)
        )));
        salary.setTaAllowance(nullable(salary.getTaAllowance()));
        salary.setIncentive(nullable(salary.getIncentive()));
        salary.setSpi(nullable(salary.getSpi()));
        salary.setCompanyFund(nullable(salary.getCompanyFund()));

        Long otMinutes = Optional.ofNullable(
                attendanceRepository.sumOvertimeMinutesForMonth(salary.getEmpId(), salary.getMonth(), salary.getYear())
        ).orElse(0L);

        BigDecimal otHours = BigDecimal.valueOf(otMinutes)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        BigDecimal hourlyRate = perDay.divide(BigDecimal.valueOf(8), 2, RoundingMode.HALF_UP);
        BigDecimal overtimePay = hourlyRate.multiply(otHours).setScale(2, RoundingMode.HALF_UP);

        BigDecimal totalAdditions = actualBasic
                .add(salary.getHraAllowance())
                .add(salary.getMedicalAllowance())
                .add(salary.getTaAllowance())
                .add(salary.getIncentive())
                .add(salary.getSpi())
                .add(salary.getCompanyFund())
                .add(overtimePay)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal pf = percentOf(actualBasic, cat.getPfPercentage());
        BigDecimal esic = percentOf(actualBasic, cat.getEsicPercentage());
        BigDecimal penalty = nullable(salary.getPenalty());

        salary.setPf(pf);
        salary.setEsic(esic);
        salary.setPenalty(penalty);

        BigDecimal deductionsBeforeTax = pf.add(esic).add(penalty).setScale(2, RoundingMode.HALF_UP);

        BigDecimal netBeforeTax = totalAdditions.subtract(deductionsBeforeTax).setScale(2, RoundingMode.HALF_UP);
        salary.setNetSalaryBeforeTaxes(netBeforeTax);

        BigDecimal professionalTax = nullable(cat.getProfessionalTaxPercentage());
        BigDecimal incomeTax = percentOf(netBeforeTax, cat.getIncomeTaxPercentage());
        BigDecimal tdsAmount = percentOf(netBeforeTax, cat.getTds());

        salary.setProfessionalTax(professionalTax);
        salary.setIncomeTax(incomeTax);
        salary.setTds(tdsAmount);

        BigDecimal totalDeductions = deductionsBeforeTax
                .add(professionalTax)
                .add(incomeTax)
                .add(tdsAmount)
                .setScale(2, RoundingMode.HALF_UP);
        salary.setDeductions(totalDeductions);

        BigDecimal finalNet = netBeforeTax.subtract(totalDeductions).setScale(2, RoundingMode.HALF_UP);
        salary.setFinalNetSalary(finalNet);

        return salary;
    }

    private static BigDecimal nullable(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private static BigDecimal percentOf(BigDecimal base, BigDecimal pct) {
        if (base == null) base = BigDecimal.ZERO;
        if (pct == null) pct = BigDecimal.ZERO;
        return base.multiply(pct).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }


    @Override
    public EmployeeSalary getSalaryByEmpIdMonthYear(String role, String email, Long empId, int month, int year)
    {
        if (!permissionService.hasPermission(role, email, "Get")) {
            throw new AccessDeniedException("No permission to Get salary");
        }
        EmployeeSalary salary = employeeSalaryRepository.findByEmpIdAndMonthAndYear(empId, month, year);

        return salary;
    }

    @Override
    public Page<EmployeeSalary> getAllSalaryByEmpId(String role, String email, Long empId, Integer month, Integer year, int page, int size) {
        if (!permissionService.hasPermission(role, email, "Get")) {
            throw new AccessDeniedException("No permission to Get salary");
        }

        Pageable pageable = PageRequest.of(page, size);

        Specification<EmployeeSalary> spec = EmployeeSalarySpecification.filterByEmpIdMonthYear(empId, month, year);

        return employeeSalaryRepository.findAll(spec, pageable);
    }

    @Override
    public SalarySummaryResponseDTO getFilteredSalaries(EmployeeSalaryFilterDTO filter, int page, int size, String role, String email) {
        if (!permissionService.hasPermission(role, email, "Get")) {
            throw new AccessDeniedException("No permission to Get salary");
        }

        String branchCode = permissionService.fetchBranchCode(role, email);

        Specification<EmployeeSalary> spec = EmployeeSalarySpecification.filterSalaries(
                filter.getEmpId(),
                filter.getFullName(),
                filter.getDepartment(),
                filter.getStatus(),
                filter.getEmployeecategory(),
                filter.getMonth(),
                filter.getYear(),
                branchCode
        );

        Pageable pageable = PageRequest.of(page, size);
        Page<EmployeeSalary> salaries = employeeSalaryRepository.findAll(spec, pageable);

        long totalCount = employeeSalaryRepository.count(spec);
        BigDecimal totalSum = employeeSalaryRepository.findAll(spec).stream()
                .map(EmployeeSalary::getFinalNetSalary)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new SalarySummaryResponseDTO(salaries, totalCount, totalSum);
    }


    @Override
    @Transactional
    public String updateSalaryStatus(Long salaryId, String status, Long transactionId, String role, String email) {
        if (!permissionService.hasPermission(role, email, "Put")) {
            throw new AccessDeniedException("No permission to update salary status");
        }

        int updated = employeeSalaryRepository.updateSalaryStatus(salaryId, status, transactionId);

        if (updated > 0) {
            return "Salary status updated successfully for ID " + salaryId;
        } else {
            throw new RuntimeException("Salary record not found or already deleted with ID " + salaryId);
        }
    }


    @Override
    public MonthSalaryResponse getSalarySummary(String role, String email, int month, int year)
    {
        if (!permissionService.hasPermission(role, email, "Get")) {
            throw new AccessDeniedException("No permission to View salary Graph");
        }

        List<EmployeeSalary> salaryList = employeeSalaryRepository.findByMonthAndYear(month, year);

        BigDecimal paidAmount = BigDecimal.ZERO;
        BigDecimal pendingAmount = BigDecimal.ZERO;
        long paidCount = 0;
        long pendingCount = 0;

        for (EmployeeSalary salary : salaryList) {
            if ("Paid".equalsIgnoreCase(salary.getStatus())) {
                paidAmount = paidAmount.add(salary.getFinalNetSalary() != null ? salary.getFinalNetSalary() : BigDecimal.ZERO);
                paidCount++;
            } else if ("Pending".equalsIgnoreCase(salary.getStatus())) {
                pendingAmount = pendingAmount.add(salary.getFinalNetSalary() != null ? salary.getFinalNetSalary() : BigDecimal.ZERO);
                pendingCount++;
            }
        }

        return new MonthSalaryResponse(paidAmount, pendingAmount, paidCount, pendingCount);
    }

}



