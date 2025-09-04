package com.NewEmployeeManagement.Service;

import com.NewEmployeeManagement.Entity.EmployeeSalary;

import java.util.List;

public interface SalaryService
{

    EmployeeSalary saveSalary(EmployeeSalary salary, Long empId, String role, String email);
    EmployeeSalary getSalaryByEmpIdMonthYear(String role, String email, Long empId, int month, int year);

    List<EmployeeSalary> getAllSalary(String role, String email);
}
