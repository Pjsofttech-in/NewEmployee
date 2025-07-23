package com.NewEmployeeManagement.Service;

import com.NewEmployeeManagement.DTO.EmployeeCountResponse;

import java.time.LocalDate;


public interface DashboardService
{
    EmployeeCountResponse getEmployeeCounts(String role, String email,String filter, LocalDate startDate, LocalDate endDate);
}
