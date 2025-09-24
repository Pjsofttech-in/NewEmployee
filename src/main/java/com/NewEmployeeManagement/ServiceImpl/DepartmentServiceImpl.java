package com.NewEmployeeManagement.ServiceImpl;

import com.NewEmployeeManagement.Entity.EmployeeDepartment;
import com.NewEmployeeManagement.Repository.DepartmentRepository;
import com.NewEmployeeManagement.Service.DepartmentService;
import com.NewEmployeeManagement.Service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PermissionService permissionService;

    @Override
    public EmployeeDepartment createDepartment(EmployeeDepartment employeeDepartment, String role, String email) {
        if (!permissionService.hasPermission(role, email, "POST")) {
            throw new AccessDeniedException("No permission to create department");
        }

        String branchCode = permissionService.fetchBranchCode(role, email);
        employeeDepartment.setRole(role);
        employeeDepartment.setCreatedByEmail(email);
        employeeDepartment.setBranchCode(branchCode);

        boolean exists = departmentRepository.existsByDepartmentNameAndBranchCode(
                employeeDepartment.getDepartment(), branchCode);

        if (exists) {
            throw new IllegalArgumentException("Department with name '" + employeeDepartment.getDepartment() + "' already exists in this branch");
        }


        return departmentRepository.save(employeeDepartment);
    }

    @Override
    public List<EmployeeDepartment> getAllDepartments(String role, String email) {
        if (!permissionService.hasPermission(role, email, "GET")) {
            throw new AccessDeniedException("No permission to view departments");
        }

        String branchCode = permissionService.fetchBranchCode(role, email);
        return departmentRepository.findAllByBranchCode(branchCode);
    }

    @Override
    public EmployeeDepartment updateDepartment(Long id, EmployeeDepartment employeeDepartment, String role, String email) {
        if (!permissionService.hasPermission(role, email, "PUT")) {
            throw new AccessDeniedException("No permission to update department");
        }

        EmployeeDepartment existing = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found"));

        existing.setDepartment(employeeDepartment.getDepartment());

        return departmentRepository.save(existing);
    }

    @Override
    public void deleteDepartment(Long id, String role, String email) {
        if (!permissionService.hasPermission(role, email, "DELETE")) {
            throw new AccessDeniedException("No permission to delete department");
        }

        EmployeeDepartment existing = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found"));

        departmentRepository.delete(existing); // ❌ No soft delete, directly remove
    }

    @Override
    public EmployeeDepartment getDepartmentById(Long id, String role, String email) {
        if (!permissionService.hasPermission(role, email, "GET")) {
            throw new AccessDeniedException("No permission to view department");
        }

        return departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found"));
    }
}