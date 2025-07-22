package com.NewEmployeeManagement.Service;

import com.NewEmployeeManagement.Entity.EmployeeDepartment;

import java.util.List;

public interface DepartmentService {
    EmployeeDepartment createDepartment(EmployeeDepartment employeeDepartment, String role, String email);
    List<EmployeeDepartment> getAllDepartments(String role, String email);
    EmployeeDepartment updateDepartment(Long id, EmployeeDepartment employeeDepartment, String role, String email);
    void deleteDepartment(Long id, String role, String email);
    EmployeeDepartment getDepartmentById(Long id, String role, String email);
}