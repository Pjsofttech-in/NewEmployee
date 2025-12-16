package com.NewEmployeeManagement.ServiceImpl;

import com.NewEmployeeManagement.DTO.*;
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
import java.util.stream.Collectors;

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
    StaffService staffService;

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

        System.out.println("🔹 Starting saveSalary() for empId=" + empId + ", role=" + role + ", email=" + email);

        if (!permissionService.hasPermission(role, email, "POST")) {
            System.out.println("❌ Permission denied for email=" + email + " with role=" + role);
            throw new AccessDeniedException("No permission to create salary");
        }

        String branchCode = permissionService.fetchBranchCode(role, email);
        System.out.println("✅ Branch code fetched: " + branchCode);

        Employee employee = employeeRepository.findById(empId)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + empId));
        System.out.println("✅ Employee found: " + employee.getFullName());

        boolean exists = employeeSalaryRepository.existsByEmpIdAndMonthAndYear(
                empId, salary.getMonth(), salary.getYear()
        );
        System.out.println("🔍 Checking if salary exists for empId=" + empId +
                ", month=" + salary.getMonth() + ", year=" + salary.getYear() + " -> " + exists);

        if (exists) {
            String monthName = java.time.Month.of(salary.getMonth())
                    .getDisplayName(java.time.format.TextStyle.FULL, Locale.ENGLISH);
            System.out.println("❌ Salary already exists for Employee ID=" + empId +
                    " for " + monthName + " " + salary.getYear());
            throw new RuntimeException("Salary already exists for Employee ID = " + empId +
                    " for " + monthName + " " + salary.getYear());
        }

        // Setting employee and system info
        salary.setEmpId(empId);
        salary.setFullName(employee.getFullName());
        salary.setDepartment(employee.getDepartment());
        salary.setEmployeecategory(employee.getCategoryName());
        salary.setCreatedByEmail(email);
        salary.setRole(role);
        salary.setBranchCode(branchCode);
        salary.setBasicSalary(employee.getSalary());

        System.out.println("💾 Employee details set in salary: " + salary);

        System.out.println("📅 Days of Month: " + salary.getDaysOfMonth());

        double presentDays = Optional.ofNullable(
                attendenceService.getAttendanceCount(empId, salary.getMonth(), salary.getYear())
        ).orElse(0L).doubleValue();
        System.out.println("🕒 Present Days: " + presentDays);

        Double paidLeave = Optional.ofNullable(
                leaveRequestRepository.getPaidLeaveForMonth(empId, salary.getMonth(), salary.getYear())
        ).orElse(0.0);
        System.out.println("🏖️ Paid Leave Days: " + paidLeave);

        Double paidHolidays = Optional.ofNullable(
                holidaysRepository.getPaidHolidaysForMonth(branchCode, salary.getMonth(), salary.getYear())
        ).map(Long::doubleValue).orElse(0.0);
        System.out.println("📆 Paid Holidays: " + paidHolidays);

        double totalWorkingDays = presentDays + paidLeave + paidHolidays;
        salary.setWorkingDays(totalWorkingDays);
        System.out.println("✅ Total Working Days calculated: " + totalWorkingDays);

        EmployeeSalary computed = calculateSalary(salary, salary.getDaysOfMonth(), employee);
        System.out.println("💰 Salary computation completed for empId=" + empId);

        EmployeeSalary saved = employeeSalaryRepository.save(computed);
        System.out.println("✅ Salary saved successfully with ID=" + saved.getId());

        return saved;
    }

    private EmployeeSalary calculateSalary(EmployeeSalary salary, int daysInMonth, Employee employee) {
        System.out.println("🔹 Starting calculateSalary() for empId=" + salary.getEmpId());

        EmployeeCategory cat = employeeCategoryRepository.findCategoryByCategoryName(
                employee.getCategoryName(), salary.getBranchCode()
        );

        if (cat == null) {
            System.out.println("❌ Category not found for " + employee.getCategoryName());
            throw new RuntimeException("Employee category not found for: " + employee.getCategoryName());
        }
        System.out.println("✅ Employee category found: " + cat.getCategoryName());

        BigDecimal monthlyBasic = employee.getSalary() != null ? employee.getSalary() : BigDecimal.ZERO;
        System.out.println("💵 Monthly Basic Salary: " + monthlyBasic);

        if (monthlyBasic.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Basic salary not set for employee: " + salary.getEmpId());
        }

        BigDecimal perDay = monthlyBasic.divide(BigDecimal.valueOf(daysInMonth), 2, RoundingMode.HALF_UP);
        System.out.println("📊 Per Day Rate: " + perDay);

        BigDecimal actualBasic = perDay.multiply(BigDecimal.valueOf(salary.getWorkingDays()))
                .setScale(2, RoundingMode.HALF_UP);
        salary.setActualBasic(actualBasic);
        System.out.println("✅ Actual Basic Calculated: " + actualBasic);

        salary.setHraAllowance(percentOf(actualBasic, cat.getHraPercentage()));
        salary.setMedicalAllowance(percentOf(actualBasic, BigDecimal.valueOf(
                Optional.ofNullable(cat.getMedicalAllowancePercentage()).orElse(0.0)
        )));
        salary.setTaAllowance(nullable(salary.getTaAllowance()));
        salary.setIncentive(nullable(salary.getIncentive()));
        salary.setSpi(nullable(salary.getSpi()));
        salary.setCompanyFund(nullable(salary.getCompanyFund()));

        System.out.println("🏦 Allowances set: HRA=" + salary.getHraAllowance() +
                ", Medical=" + salary.getMedicalAllowance() +
                ", TA=" + salary.getTaAllowance() +
                ", Incentive=" + salary.getIncentive() +
                ", SPI=" + salary.getSpi() +
                ", Fund=" + salary.getCompanyFund());

        Long otMinutes = Optional.ofNullable(
                attendanceRepository.sumOvertimeMinutesForMonth(salary.getEmpId(), salary.getMonth(), salary.getYear())
        ).orElse(0L);
        System.out.println("⏱️ Overtime Minutes: " + otMinutes);

        BigDecimal otHours = BigDecimal.valueOf(otMinutes)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        BigDecimal hourlyRate = perDay.divide(BigDecimal.valueOf(8), 2, RoundingMode.HALF_UP);
        BigDecimal overtimePay = hourlyRate.multiply(otHours).setScale(2, RoundingMode.HALF_UP);
        System.out.println("🕒 Overtime Pay Calculated: " + overtimePay);

        BigDecimal totalAdditions = actualBasic
                .add(salary.getHraAllowance())
                .add(salary.getMedicalAllowance())
                .add(salary.getTaAllowance())
                .add(salary.getIncentive())
                .add(salary.getSpi())
                .add(salary.getCompanyFund())
                .add(overtimePay)
                .setScale(2, RoundingMode.HALF_UP);
        System.out.println("💹 Total Additions: " + totalAdditions);

        BigDecimal pf = percentOf(actualBasic, cat.getPfPercentage());
        BigDecimal esic = percentOf(actualBasic, cat.getEsicPercentage());
        BigDecimal penalty = nullable(salary.getPenalty());
        System.out.println("📉 Deductions before Tax (PF=" + pf + ", ESIC=" + esic + ", Penalty=" + penalty + ")");

        salary.setPf(pf);
        salary.setEsic(esic);
        salary.setPenalty(penalty);

        BigDecimal deductionsBeforeTax = pf.add(esic).add(penalty).setScale(2, RoundingMode.HALF_UP);
        BigDecimal netBeforeTax = totalAdditions.subtract(deductionsBeforeTax).setScale(2, RoundingMode.HALF_UP);
        salary.setNetSalaryBeforeTaxes(netBeforeTax);
        System.out.println("💰 Net Salary before Taxes: " + netBeforeTax);

        BigDecimal professionalTax = nullable(cat.getProfessionalTaxPercentage());
        BigDecimal incomeTax = percentOf(netBeforeTax, cat.getIncomeTaxPercentage());
        BigDecimal tdsAmount = percentOf(netBeforeTax, cat.getTds());
        System.out.println("💸 Tax Deductions -> Professional=" + professionalTax +
                ", IncomeTax=" + incomeTax + ", TDS=" + tdsAmount);

        salary.setProfessionalTax(professionalTax);
        salary.setIncomeTax(incomeTax);
        salary.setTds(tdsAmount);

        BigDecimal totalDeductions = deductionsBeforeTax
                .add(professionalTax)
                .add(incomeTax)
                .add(tdsAmount)
                .setScale(2, RoundingMode.HALF_UP);
        salary.setDeductions(totalDeductions);
        System.out.println("📊 Total Deductions: " + totalDeductions);

        BigDecimal finalNet = netBeforeTax.subtract(totalDeductions).setScale(2, RoundingMode.HALF_UP);
        salary.setFinalNetSalary(finalNet);
        System.out.println("✅ Final Net Salary: " + finalNet);

        System.out.println("🏁 Salary computation finished successfully for empId=" + salary.getEmpId());
        return salary;
    }

    private BigDecimal nullable(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private BigDecimal percentOf(BigDecimal base, BigDecimal percent) {
        if (base == null || percent == null) return BigDecimal.ZERO;
        return base.multiply(percent).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal percentOf(BigDecimal base, Double percent) {
        return percentOf(base, BigDecimal.valueOf(Optional.ofNullable(percent).orElse(0.0)));
    }
//    private static BigDecimal nullable(BigDecimal v) {
//        return v == null ? BigDecimal.ZERO : v;
//    }
//
//    private static BigDecimal percentOf(BigDecimal base, BigDecimal pct) {
//        if (base == null) base = BigDecimal.ZERO;
//        if (pct == null) pct = BigDecimal.ZERO;
//        return base.multiply(pct).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
//    }


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
    public SalarySummaryResponseDTO getFilteredSalaries(
            EmployeeSalaryFilterDTO filter, int page, int size, String role, String email) {

        if (!permissionService.hasPermission(role, email, "GET")) {
            throw new AccessDeniedException("No permission to Get salary");
        }

        List<String> effectiveBranchCodes = new ArrayList<>();

        if ("SUPERADMIN".equalsIgnoreCase(role)) {

            effectiveBranchCodes = staffService.getBranchCodesByInstituteEmail(email);

            if (effectiveBranchCodes.isEmpty()) {
                throw new RuntimeException("No branch codes mapped for SuperAdmin email: " + email);
            }

            if (filter.getBranchCode() != null && !filter.getBranchCode().isBlank()) {

                String requestedBranch = filter.getBranchCode().trim();

                if (!effectiveBranchCodes.contains(requestedBranch)) {
                    throw new RuntimeException("Invalid branchCode for this SuperAdmin: " + requestedBranch);
                }

                effectiveBranchCodes = List.of(requestedBranch);
            }

        } else {
            String branchCode = permissionService.fetchBranchCode(role, email);

            if (branchCode == null || branchCode.isBlank()) {
                throw new RuntimeException("Unable to resolve branch code for role: " + role);
            }

            effectiveBranchCodes = List.of(branchCode);
        }

        final List<String> finalBranchCodes = new ArrayList<>(effectiveBranchCodes);

        Specification<EmployeeSalary> spec = (root, query, cb) ->
                root.get("branchCode").in(finalBranchCodes);

        spec = spec.and(EmployeeSalarySpecification.filterSalaries(
                filter.getEmpId(),
                filter.getFullName(),
                filter.getDepartment(),
                filter.getStatus(),
                filter.getEmployeecategory(),
                filter.getMonth(),
                filter.getYear(),
                null
        ));
        Pageable pageable = PageRequest.of(page, size);
        Page<EmployeeSalary> salaryPage =
                employeeSalaryRepository.findAll(spec, pageable);

        long totalCount = employeeSalaryRepository.count(spec);

        BigDecimal totalSum = salaryPage.getContent().stream()
                .map(EmployeeSalary::getFinalNetSalary)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Set<Long> empIds = salaryPage.getContent().stream()
                .map(EmployeeSalary::getEmpId)
                .collect(Collectors.toSet());

        Map<Long, Employee> employeeMap =
                employeeRepository.findByEmpIdIn(empIds)
                        .stream()
                        .collect(Collectors.toMap(Employee::getId, e -> e));

        Page<FlatSalaryDTO> flatPage = salaryPage.map(salary -> {

            Employee emp = employeeMap.get(salary.getEmpId());

            FlatSalaryDTO dto = new FlatSalaryDTO();

            // ===== Salary mapping =====
            dto.setId(salary.getId());
            dto.setEmpId(salary.getEmpId());
            dto.setFullName(salary.getFullName());
            dto.setDepartment(salary.getDepartment());
            dto.setEmployeecategory(salary.getEmployeecategory());
            dto.setBasicSalary(salary.getBasicSalary());
            dto.setActualBasic(salary.getActualBasic());
            dto.setHraAllowance(salary.getHraAllowance());
            dto.setTaAllowance(salary.getTaAllowance());
            dto.setIncentive(salary.getIncentive());
            dto.setSpi(salary.getSpi());
            dto.setMedicalAllowance(salary.getMedicalAllowance());
            dto.setPf(salary.getPf());
            dto.setEsic(salary.getEsic());
            dto.setProfessionalTax(salary.getProfessionalTax());
            dto.setIncomeTax(salary.getIncomeTax());
            dto.setCompanyFund(salary.getCompanyFund());
            dto.setDeductions(salary.getDeductions());
            dto.setTds(salary.getTds());
            dto.setNetSalaryBeforeTaxes(salary.getNetSalaryBeforeTaxes());
            dto.setFinalNetSalary(salary.getFinalNetSalary());
            dto.setMonth(salary.getMonth());
            dto.setYear(salary.getYear());
            dto.setWorkingDays(salary.getWorkingDays());
            dto.setDaysOfMonth(salary.getDaysOfMonth());
            dto.setTransactionId(salary.getTransactionId());
            dto.setPenalty(salary.getPenalty());
            dto.setCreatedByEmail(salary.getCreatedByEmail());
            dto.setRole(salary.getRole());
            dto.setBranchCode(salary.getBranchCode());
            dto.setStatus(salary.getStatus());
            dto.setPaymentDate(salary.getPaymentDate());
            dto.setDeleted(salary.isDeleted());

            // ===== Employee mapping =====
            if (emp != null) {
                dto.setDob(emp.getDob());
                dto.setAdharNo(emp.getAdharNo());
                dto.setPanNo(emp.getPanNo());
                dto.setMobileNo(emp.getMobileNo());
                dto.setEmpEmail(emp.getEmpEmail());
                dto.setBankName(emp.getBankName());
                dto.setAccountNumber(emp.getAccountNumber());
                dto.setCpfNo(emp.getCpfNo());
                dto.setDesignation(emp.getDesignation());
            }

            return dto;
        });

        return new SalarySummaryResponseDTO(flatPage, totalCount, totalSum);


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



