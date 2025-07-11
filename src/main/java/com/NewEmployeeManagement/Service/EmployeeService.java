package com.NewEmployeeManagement.Service;

import com.NewEmployeeManagement.DTO.EmployeeCreateDTO;
import com.NewEmployeeManagement.DTO.EmployeeResponseDTO;
import com.NewEmployeeManagement.Entity.Employee;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface EmployeeService {
    Employee createEmployee(EmployeeCreateDTO dto, String role, String email, int departmentId, Long categoryId,
                            MultipartFile idProof,
                            MultipartFile employeePhoto,
                            MultipartFile resume,
                            MultipartFile addressProof,
                            MultipartFile experienceLetter);

    List<Employee> getAllEmployees(String role, String email);

    EmployeeResponseDTO getEmployeeById(int id, String role, String email);

    Employee updateEmployee(
            int id, EmployeeCreateDTO dto, String role,
            String email, int departmentId,Long categoryId, MultipartFile idProof,
            MultipartFile employeePhoto, MultipartFile resume, MultipartFile addressProof, MultipartFile experienceLetter
    );

    void deleteEmployee(int id, String role, String email);
    void carryForwardLeavesForEligibleEmployees();

    Map<String, Object> getCrudPermissionForEmployeeByEmail(String empEmail);

}