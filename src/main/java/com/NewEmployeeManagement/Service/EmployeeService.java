package com.NewEmployeeManagement.Service;

import com.NewEmployeeManagement.DTO.EmployeeCreateDTO;
import com.NewEmployeeManagement.DTO.EmployeeFilterDTO;
import com.NewEmployeeManagement.DTO.EmployeeResponseDTO;
import com.NewEmployeeManagement.Entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface EmployeeService {
    Employee createEmployee(EmployeeCreateDTO dto, String role, String email, Long departmentId, Long categoryId,
                            MultipartFile idProof,
                            MultipartFile employeePhoto,
                            MultipartFile resume,
                            MultipartFile addressProof,
                            MultipartFile experienceLetter);

    Page<Employee> getFilteredEmployees(String role, String email, EmployeeFilterDTO filter, String timeFrame, LocalDate startDate, LocalDate endDate, Pageable pageable);
    EmployeeResponseDTO getEmployeeById(Long id, String role, String email);

    Employee updateEmployee(
            Long id, EmployeeCreateDTO dto, String role,
            String email, Long departmentId,Long categoryId, MultipartFile idProof,
            MultipartFile employeePhoto, MultipartFile resume, MultipartFile addressProof, MultipartFile experienceLetter
    );

    void deleteEmployee(Long id, String role, String email);
    void carryForwardLeavesForEligibleEmployees();

    Map<String, Object> getCrudPermissionForEmployeeByEmail(String empEmail);

    Employee updateStatus(Long id, String status);
}