package com.NewEmployeeManagement.ServiceImpl;

import com.NewEmployeeManagement.DTO.EmployeeCountResponse;
import com.NewEmployeeManagement.Repository.AttendenceRepository;
import com.NewEmployeeManagement.Repository.EmployeeRepository;
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
import java.util.*;

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
        List<Object[]> results = attendenceRepository.getDailyWorkMinutesByMonth(empId, month, year);

        Map<LocalDate, Long> attendanceMap = new LinkedHashMap<>();
        for (Object[] row : results) {
            LocalDate date = (LocalDate) row[0];
            Long totalMinutes = (Long) row[1];
            attendanceMap.put(date, totalMinutes);
        }
        return attendanceMap;
    }

}
