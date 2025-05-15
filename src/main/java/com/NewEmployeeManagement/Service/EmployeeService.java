package com.NewEmployeeManagement.Service;

import com.NewEmployeeManagement.Entity.Employee;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface EmployeeService {
    Employee createEmployee(Employee employee, String role, String email, int departmentId, Long categoryId);
    List<Employee> getAllEmployees(String role, String email);
    Employee getEmployeeById(int id, String role, String email);
    Employee updateEmployee(int id, Employee employee, String role, String email);
    void deleteEmployee(int id, String role, String email);
    Employee uploadDocuments(int id, MultipartFile idProof, MultipartFile photo,
                             MultipartFile resume, MultipartFile addressProof,
                             MultipartFile experienceLetter, String role, String email);
}