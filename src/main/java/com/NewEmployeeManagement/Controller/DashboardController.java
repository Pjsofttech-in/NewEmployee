package com.NewEmployeeManagement.Controller;

import com.NewEmployeeManagement.DTO.EmployeeCountResponse;
import com.NewEmployeeManagement.DTO.MonthSalaryResponse;
import com.NewEmployeeManagement.Service.DashboardService;
import com.NewEmployeeManagement.Service.SalaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@RestController
//@CrossOrigin(origins = "http://localhost:3000")
@CrossOrigin(origins = "https://pjsofttech.in")
public class DashboardController
{
    @Autowired
    DashboardService dashboardService;

    @Autowired
    SalaryService salaryService;

    @GetMapping("/getEmployeeCountForCardsAndGraph")
    public ResponseEntity<EmployeeCountResponse> getEmployeeCounts(
           @RequestParam String role,
           @RequestParam String email,
            @RequestParam String filter,
           @RequestParam(required = false) String branchCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        EmployeeCountResponse response = dashboardService.getEmployeeCounts(role, email,filter, startDate, endDate,branchCode);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/geSalarySummeryForCards")
    public Map<String, Object> getSalarySummary(
            @RequestParam String role,
            @RequestParam String email,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String branchCode) {
        return dashboardService.getSalarySummary(role,email,month, year,branchCode);
    }

    @GetMapping("/SalaryComparisonBetweenTwoMonth")
    public ResponseEntity<Map<String, BigDecimal>> getSalaryComparison(
            @RequestParam String role,
            @RequestParam String email,
            @RequestParam int month,
            @RequestParam int year) {
        return ResponseEntity.ok(dashboardService.getSalaryComparison(role,email,month, year));
    }

    @GetMapping("/SalaryComparisonByYears")
    public ResponseEntity<Map<String, BigDecimal>> getYearlyComparison(
            @RequestParam String role,
            @RequestParam String email,
            @RequestParam int year1,
            @RequestParam int year2) {
        return ResponseEntity.ok(dashboardService.getYearlyComparison(role,email,year1, year2));
    }

    @GetMapping("/getSalaryRevenewByMonthofYear")
    public Map<String, Map<String, Object>> getMonthlySalaryTotals(
            @RequestParam String role,
            @RequestParam String email,
            @RequestParam int year) {
        try {
            return dashboardService.getMonthlySalaryTotals(role, email, year);
        } catch (AccessDeniedException e) {
            throw new RuntimeException("Access denied: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Error fetching salary report: " + e.getMessage());
        }
    }


    @GetMapping("/getMonthlyAttendaceByEmpId")
    public ResponseEntity<Map<LocalDate, Long>> getMonthlyAttendance(
            @RequestParam String role,
            @RequestParam String email,
            @RequestParam Long empId,
            @RequestParam int month,
            @RequestParam int year) {

        Map<LocalDate, Long> response = dashboardService.getMonthlyAttendance(role,email,empId, month, year);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/getSalariesOfMonthByYear")
    public ResponseEntity<Map<String, Double>> getYearlySalary(
            @RequestParam String role,
            @RequestParam String email,
            @RequestParam Long empId,
            @RequestParam int year) {

        Map<String, Double> response = dashboardService.getYearlySalary(role,email,empId, year);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/getLeaveReportByYearByEmpId")
    public ResponseEntity<Map<String, Double>> getYearlyLeaves(
            @RequestParam String role,
            @RequestParam String email,
            @RequestParam Long empId,
            @RequestParam int year) {

        Map<String, Double> response = dashboardService.getYearlyLeaves(role,email,empId, year);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/getCardsDataForAdmin")
    public ResponseEntity<Map<String, Long>> getAttendanceReport(
            @RequestParam String role,
            @RequestParam String email,
            @RequestParam String filter,
            @RequestParam(required = false) String branchCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        Map<String, Long> report = dashboardService.getAttendanceReport(role, email, filter, startDate, endDate,branchCode);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/getPaidPendingSalaryByMonth")
    public MonthSalaryResponse getSalarySummary( @RequestParam String role,
                                                 @RequestParam String email,
                                                 @RequestParam int month, @RequestParam int year) {
        return salaryService.getSalarySummary(role,email,month, year);
    }
}
