package com.NewEmployeeManagement.Service;

import com.NewEmployeeManagement.DTO.EmployeeCountResponse;

import java.time.LocalDate;
import java.util.Map;


public interface DashboardService
{
    EmployeeCountResponse getEmployeeCounts(String role, String email,String filter, LocalDate startDate, LocalDate endDate);

    Map<String, Object> getSalarySummary(String role, String email, Integer month, Integer year);
}
