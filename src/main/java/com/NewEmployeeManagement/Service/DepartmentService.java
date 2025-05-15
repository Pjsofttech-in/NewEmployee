package com.NewEmployeeManagement.Service;

import com.NewEmployeeManagement.Entity.Department;

import java.util.List;

public interface DepartmentService {
    Department createDepartment(Department department, String role, String email);
    List<Department> getAllDepartments(String role, String email);
    Department updateDepartment(int id, Department department, String role, String email);
    void deleteDepartment(int id, String role, String email);
    Department getDepartmentById(int id, String role, String email);
}