package com.NewEmployeeManagement.Service;

import com.NewEmployeeManagement.DTO.EmployeeSalaryFilterDTO;
import com.NewEmployeeManagement.DTO.SalarySummaryResponseDTO;
import com.NewEmployeeManagement.Entity.EmployeeSalary;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface SalaryService
{

    EmployeeSalary saveSalary(EmployeeSalary salary, Long empId, String role, String email);
    EmployeeSalary getSalaryByEmpIdMonthYear(String role, String email, Long empId, int month, int year);
    Page<EmployeeSalary> getAllSalaryByEmpId(String role, String email, Long empId, Integer month, Integer year, int page, int size);
    SalarySummaryResponseDTO getFilteredSalaries(EmployeeSalaryFilterDTO filter, int page, int size, String role, String email);
    String updateSalaryStatus(Long salaryId, String status,Long transactionId, String role, String email);

}

