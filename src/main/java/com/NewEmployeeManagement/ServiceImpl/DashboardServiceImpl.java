package com.NewEmployeeManagement.ServiceImpl;

import com.NewEmployeeManagement.DTO.EmployeeCountResponse;
import com.NewEmployeeManagement.Entity.Employee;
import com.NewEmployeeManagement.Entity.EmployeeLeaveRequest;
import com.NewEmployeeManagement.Repository.AttendenceRepository;
import com.NewEmployeeManagement.Repository.EmployeeRepository;
import com.NewEmployeeManagement.Repository.LeaveRequestRepository;
import com.NewEmployeeManagement.Repository.SalaryRepository;
import com.NewEmployeeManagement.Service.DashboardService;
import com.NewEmployeeManagement.Service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static java.time.temporal.ChronoUnit.MONTHS;

@Service
public class DashboardServiceImpl implements DashboardService
{

    @Autowired
    EmployeeRepository employeeRepository;

    @Autowired
    PermissionService permissionService;

    @Autowired
    SalaryRepository salaryRepository;

    @Autowired
    AttendenceRepository attendenceRepository;


    @Autowired
    LeaveRequestRepository leaveRequestRepository;

    @Override
    public EmployeeCountResponse getEmployeeCounts(String role, String email,String filter, LocalDate startDate, LocalDate endDate)
    {
        if (!permissionService.hasPermission(role, email, "GET")) {
            throw new AccessDeniedException("No permission to view Get Count");
        }
        String branchCode = permissionService.fetchBranchCode(role, email);
        LocalDate now = LocalDate.now();
        switch (filter.toLowerCase()) {
            case "today" -> {
                startDate = now;
                endDate = now;
            }
            case "7days" -> {
                startDate = now.minusDays(6);
                endDate = now;
            }
            case "30days" -> {
                startDate = now.minusDays(29);
                endDate = now;
            }
            case "365days" -> {
                startDate = now.minusDays(364);
                endDate = now;
            }
            case "all" -> {
                startDate = LocalDate.of(2000, 1, 1);
                endDate = now;
            }
            case "custom" -> {
                if (startDate == null || endDate == null)
                    throw new IllegalArgumentException("Custom filter requires startDate and endDate.");
            }
            default -> throw new IllegalArgumentException("Invalid filter type: " + filter);
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        Long total = employeeRepository.countTotalEmployeesBetweenDatesAndBranchCode(startDateTime, endDateTime, branchCode);

        List<Object[]> statusList = employeeRepository.countByStatusBetweenDatesAndBranchCode(startDateTime, endDateTime,branchCode);
        List<Object[]> deptList = employeeRepository.countByDepartmentJoinedBetweenDatesAndBranchCode(startDateTime, endDateTime,branchCode);
        List<Object[]> categoryList = employeeRepository.countByCategoryJoinedBetweenDatesAndBranchCode(startDateTime, endDateTime,branchCode);

        Map<String, Long> statusMap = new HashMap<>();
        statusMap.put("Joined", 0L);
        statusMap.put("Terminated", 0L);

        for (Object[] obj : statusList) {
            String status = (String) obj[0];
            Long count = (Long) obj[1];
            statusMap.put(status, count);
        }

        statusMap.put("total", total);

        Map<String, Long> deptMap = new HashMap<>();
        Map<String, Long> categoryMap = new HashMap<>();

        statusList.forEach(obj -> statusMap.put((String) obj[0], (Long) obj[1]));
        deptList.forEach(obj -> deptMap.put((String) obj[0], (Long) obj[1]));
        categoryList.forEach(obj -> categoryMap.put((String) obj[0], (Long) obj[1]));

        EmployeeCountResponse response = new EmployeeCountResponse();
        response.setStatusCounts(statusMap);
        response.setDepartmentCounts(deptMap);
        response.setCategoryCounts(categoryMap);

        return response;
    }

    @Override
    public Map<String, Object> getSalarySummary(String role, String email, Integer month, Integer year)
    {
        if (!permissionService.hasPermission(role, email, "Get")) {
            throw new AccessDeniedException("No permission to Get salary Report");
        }

        String branchCode = permissionService.fetchBranchCode(role, email);
        Map<String, Object> result = new HashMap<>();

        long totalCount = salaryRepository.countAllSalaries(month, year,branchCode);
        long paidCount = salaryRepository.countPaidSalaries(month, year,branchCode);
        long pendingCount = salaryRepository.countPendingSalaries(month, year,branchCode);

        BigDecimal totalSum = salaryRepository.sumAllFinalNetSalary(month, year,branchCode);
        BigDecimal paidSum = salaryRepository.sumPaidFinalNetSalary(month, year,branchCode);
        BigDecimal pendingSum = salaryRepository.sumPendingFinalNetSalary(month, year,branchCode);

        result.put("totalCount", totalCount);
        result.put("paidCount", paidCount);
        result.put("pendingCount", pendingCount);

        result.put("totalSum", totalSum);
        result.put("paidSum", paidSum);
        result.put("pendingSum", pendingSum);

        return result;
    }

    @Override
    public Map<String, BigDecimal> getSalaryComparison(String role, String email,int month, int year)
    {
        if (!permissionService.hasPermission(role, email, "Get")) {
            throw new AccessDeniedException("No permission to Get salary Report");
        }

        String branchCode = permissionService.fetchBranchCode(role, email);
        int previousMonth = (month == 1) ? 12 : month - 1;
        int previousYear = (month == 1) ? year - 1 : year;

        BigDecimal prevMonthSalary = salaryRepository
                .getTotalNetSalaryByMonthAndYear(previousMonth, previousYear,branchCode);
        BigDecimal currentMonthSalary = salaryRepository
                .getTotalNetSalaryByMonthAndYear(month, year,branchCode);

        String prevMonthName = Month.of(previousMonth).getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        String currentMonthName = Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH);

        Map<String, BigDecimal> result = new HashMap<>();
        result.put(prevMonthName, prevMonthSalary);
        result.put(currentMonthName, currentMonthSalary);
        result.put("Difference", currentMonthSalary.subtract(prevMonthSalary));

        return result;
    }


    @Override
    public Map<String, BigDecimal> getYearlyComparison(String role, String email,int year1, int year2)
    {
        if (!permissionService.hasPermission(role, email, "Get")) {
            throw new AccessDeniedException("No permission to Get salary Report");
        }

        String branchCode = permissionService.fetchBranchCode(role, email);
        BigDecimal totalYear1 = salaryRepository.getTotalNetSalaryByYear(year1,branchCode);
        BigDecimal totalYear2 = salaryRepository.getTotalNetSalaryByYear(year2,branchCode);

        Map<String, BigDecimal> result = new LinkedHashMap<>();
        result.put(String.valueOf(year1), totalYear1);
        result.put(String.valueOf(year2), totalYear2);
        result.put("Difference", totalYear1.subtract(totalYear2));

        return result;
    }

    @Override
    public Map<String, BigDecimal> getMonthlySalaryTotals(String role, String email,int year)
    {
        if (!permissionService.hasPermission(role, email, "Get")) {
            throw new AccessDeniedException("No permission to Get salary Report");
        }

        String branchCode = permissionService.fetchBranchCode(role, email);
        List<Object[]> monthlyData = salaryRepository.getMonthlySalaryTotalsByYear(year,branchCode);

        Map<String, BigDecimal> result = new LinkedHashMap<>();

        for (int i = 1; i <= 12; i++) {
            String monthName = Month.of(i).getDisplayName(TextStyle.FULL, Locale.ENGLISH);
            result.put(monthName, BigDecimal.ZERO);
        }

        for (Object[] row : monthlyData) {
            int month = (int) row[0];
            BigDecimal total = (BigDecimal) row[1];
            String monthName = Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH);
            result.put(monthName, total);
        }

        return result;
    }


    @Override
    public Map<LocalDate, Long> getMonthlyAttendance(String role, String email,Long empId, int month, int year) {

        if (!permissionService.hasPermission(role, email, "Get")) {
            throw new AccessDeniedException("No permission to Get Attendace Report");
        }

        List<Object[]> results = attendenceRepository.getDailyWorkMinutesByMonth(empId, month, year);

        Map<LocalDate, Long> attendanceMap = new LinkedHashMap<>();
        for (Object[] row : results) {
            LocalDate date = (LocalDate) row[0];
            Long totalMinutes = (Long) row[1];
            attendanceMap.put(date, totalMinutes);
        }
        return attendanceMap;
    }

    @Override
    public Map<String, Double> getYearlySalary(String role, String email, Long empId, int year) {

        if (!permissionService.hasPermission(role, email, "Get")) {
            throw new AccessDeniedException("No permission to Get salary Report");
        }

        Map<String, Double> salaryMap = new LinkedHashMap<>();
        for (Month month : Month.values()) {
            salaryMap.put(
                    month.getDisplayName(TextStyle.FULL, Locale.ENGLISH), // "January"
                    0.0
            );
        }

        // Fetch salaries grouped by month from DB
        List<Object[]> results = salaryRepository.getSalaryByYear(empId, year);

        // Update salaryMap with actual totals
        for (Object[] row : results) {
            int monthNumber = (int) row[0]; // month number (1–12)
            Double total = ((Number) row[1]).doubleValue();
            Month month = Month.of(monthNumber);
            salaryMap.put(
                    month.getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                    total
            );
        }

        return salaryMap;
    }



    @Override
    public Map<String, Double> getYearlyLeaves(String role, String email,Long empId, int year)
    {
        if (!permissionService.hasPermission(role, email, "Get")) {
            throw new AccessDeniedException("No permission to Get salary Leave");
        }
        Map<String, Double> leaveMap = new LinkedHashMap<>();
        for (Month month : Month.values()) {
            leaveMap.put(month.getDisplayName(TextStyle.FULL, Locale.ENGLISH), 0.0);
        }

        List<Object[]> results = leaveRequestRepository.getLeaveByYear(empId, year);

        for (Object[] row : results) {
            int monthNumber = (int) row[0]; // 1–12
            Double totalLeaves = ((Number) row[1]).doubleValue();

            Month month = Month.of(monthNumber);
            leaveMap.put(month.getDisplayName(TextStyle.FULL, Locale.ENGLISH), totalLeaves);
        }

        return leaveMap;
    }

    @Override
    public Map<String, Long> getAttendanceReport(String role, String email, String filter,
                                                 LocalDate startDate, LocalDate endDate) {

        if (!permissionService.hasPermission(role, email, "GET")) {
            throw new AccessDeniedException("No permission to view Get Count");
        }
        String branchCode = permissionService.fetchBranchCode(role, email);

        // --- apply filter ---
        LocalDate today = LocalDate.now();
        LocalDate fromDate;
        LocalDate toDate = today;
        switch (filter.toLowerCase()) {
            case "today" -> fromDate = today;
            case "7days" -> fromDate = today.minusDays(6);
            case "30days" -> fromDate = today.minusDays(29);
            case "365days" -> fromDate = today.minusDays(364);
            case "custom" -> {
                fromDate = startDate;
                toDate = endDate;
            }
            default -> throw new IllegalArgumentException("Invalid filter.");
        }

        // --- 1) employees active in this period ---
        List<Employee> employees = employeeRepository.findEmployeesActiveBetween(branchCode, fromDate, toDate);
        long totalEmployees = employees.size();

        // --- 2) calculate expected attendances ---
        long expectedAttendances = 0L;
        for (Employee e : employees) {
            LocalDate empJoin = e.getJoiningDate();
            LocalDate empRejoin = e.getRejoiningData();
            LocalDate effectiveStart = (empRejoin != null && (empJoin == null || empRejoin.isAfter(empJoin)))
                    ? empRejoin : empJoin;

            if (effectiveStart == null) continue;
            LocalDate effectiveEnd = (e.getTerminatDate() != null) ? e.getTerminatDate() : toDate;

            LocalDate overlapStart = effectiveStart.isAfter(fromDate) ? effectiveStart : fromDate;
            LocalDate overlapEnd = effectiveEnd.isBefore(toDate) ? effectiveEnd : toDate;

            if (!overlapStart.isAfter(overlapEnd)) {
                long days = ChronoUnit.DAYS.between(overlapStart, overlapEnd) + 1;
                expectedAttendances += days;
            }
        }

        // --- 3) present counts ---
        long onTimeCount = attendenceRepository.countOnTimeRecords(branchCode, fromDate, toDate);
        long lateCount = attendenceRepository.countLateRecords(branchCode, fromDate, toDate);
        long presentCount = onTimeCount + lateCount;

        // --- 4) leave days ---
        long leaveCount = 0L;
        List<EmployeeLeaveRequest> leaves = leaveRequestRepository.findApprovedLeavesOverlapping(branchCode, fromDate, toDate);
        for (EmployeeLeaveRequest lr : leaves) {
            LocalDate leaveStart = lr.getFromDate();
            LocalDate leaveEnd = lr.getToDate();
            LocalDate overlapStart = (leaveStart.isAfter(fromDate)) ? leaveStart : fromDate;
            LocalDate overlapEnd = (leaveEnd.isBefore(toDate)) ? leaveEnd : toDate;
            if (!overlapStart.isAfter(overlapEnd)) {
                leaveCount += ChronoUnit.DAYS.between(overlapStart, overlapEnd) + 1;
            }
        }

        long absentCount = expectedAttendances - presentCount - leaveCount;
        if (absentCount < 0) absentCount = 0;

        // --- 6) averages ---
        Map<String, Long> report = new HashMap<>();
        if (!filter.equalsIgnoreCase("today")) {
            // average PER EMPLOYEE
            if (totalEmployees > 0) {
                report.put("avgTotalEmployees", totalEmployees);
                report.put("avgExpectedAttendances", expectedAttendances / totalEmployees);
                report.put("avgPresentCount", presentCount / totalEmployees);
                report.put("avgLateCount", lateCount / totalEmployees);
                report.put("avgAbsentCount", absentCount / totalEmployees);
                report.put("avgLeaveCount", leaveCount / totalEmployees);
            } else {
                report.put("avgTotalEmployees", 0L);
                report.put("avgExpectedAttendances", 0L);
                report.put("avgPresentCount", 0L);
                report.put("avgLateCount", 0L);
                report.put("avgAbsentCount", 0L);
                report.put("avgLeaveCount", 0L);
            }
        } else {
            // today's raw counts
            report.put("totalEmployees", totalEmployees);
            report.put("expectedAttendances", expectedAttendances);
            report.put("presentCount", presentCount);
            report.put("lateCount", lateCount);
            report.put("absentCount", absentCount);
            report.put("leaveCount", leaveCount);
        }

        return report;
    }


}
