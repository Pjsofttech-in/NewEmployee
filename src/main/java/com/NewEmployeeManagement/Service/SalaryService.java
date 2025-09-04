package com.NewEmployeeManagement.Service;

import com.NewEmployeeManagement.DTO.EmployeeSalaryFilterDTO;
import com.NewEmployeeManagement.Entity.EmployeeSalary;
import org.springframework.data.domain.Page;

import java.util.List;

public interface SalaryService
{

    EmployeeSalary saveSalary(EmployeeSalary salary, Long empId, String role, String email);
    EmployeeSalary getSalaryByEmpIdMonthYear(String role, String email, Long empId, int month, int year);
    Page<EmployeeSalary> getAllSalaryByEmpId(String role, String email, Long empId, Integer month, Integer year, int page, int size);
    Page<EmployeeSalary> getFilteredSalaries(EmployeeSalaryFilterDTO filter, int page, int size, String role, String email);
    String updateSalaryStatus(Long salaryId, String status, String role, String email);
}

