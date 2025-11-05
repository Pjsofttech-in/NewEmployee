package com.NewEmployeeManagement.Service;

import com.NewEmployeeManagement.DTO.EmployeeCountResponse;
import jakarta.annotation.Nullable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;


public interface DashboardService
{
    EmployeeCountResponse getEmployeeCounts(String role, String email, String filter, LocalDate startDate, LocalDate endDate, @Nullable String branchCodeFilter);
    Map<String, BigDecimal> getSalaryComparison(String role,String email,int month, int year);
    Map<String, Object> getSalarySummary(String role, String email, Integer month, Integer year, @Nullable String branchCodeFilter);
    Map<String, BigDecimal> getYearlyComparison(String role, String email,int year1, int year2);
    Map<String, Map<String, Object>> getMonthlySalaryTotals(String role, String email, int year);
    Map<LocalDate, Long> getMonthlyAttendance(String role, String email, Long empId, int month, int year);
    Map<String, Double> getYearlySalary(String role, String email, Long empId, int year);

    Map<String, Double> getYearlyLeaves(String role, String email,Long empId, int year);

    Map<String, Long> getAttendanceReport(String role, String email, String filter, LocalDate startDate, LocalDate endDate);
}
