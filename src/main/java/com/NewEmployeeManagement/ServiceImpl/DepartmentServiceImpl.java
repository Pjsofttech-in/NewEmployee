package com.NewEmployeeManagement.ServiceImpl;

import com.NewEmployeeManagement.Entity.Department;
import com.NewEmployeeManagement.Repository.DepartmentRepository;
import com.NewEmployeeManagement.Service.DepartmentService;
import com.NewEmployeeManagement.Service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PermissionService permissionService;

    @Override
    public Department createDepartment(Department department, String role, String email) {
        if (!permissionService.hasPermission(role, email, "POST")) {
            throw new AccessDeniedException("No permission to create department");
        }

        String branchCode = permissionService.fetchBranchCode(role, email);
        department.setRole(role);
        department.setCreatedByEmail(email);
        department.setBranchCode(branchCode);

        return departmentRepository.save(department);
    }

    @Override
    public List<Department> getAllDepartments(String role, String email) {
        if (!permissionService.hasPermission(role, email, "GET")) {
            throw new AccessDeniedException("No permission to view departments");
        }

        String branchCode = permissionService.fetchBranchCode(role, email);
        return departmentRepository.findAllByBranchCode(branchCode);
    }

    @Override
    public Department updateDepartment(int id, Department department, String role, String email) {
        if (!permissionService.hasPermission(role, email, "PUT")) {
            throw new AccessDeniedException("No permission to update department");
        }

        Department existing = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found"));

        existing.setDepartment(department.getDepartment());

        return departmentRepository.save(existing);
    }

    @Override
    public void deleteDepartment(int id, String role, String email) {
        if (!permissionService.hasPermission(role, email, "DELETE")) {
            throw new AccessDeniedException("No permission to delete department");
        }

        Department existing = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found"));

        departmentRepository.delete(existing); // ❌ No soft delete, directly remove
    }

    @Override
    public Department getDepartmentById(int id, String role, String email) {
        if (!permissionService.hasPermission(role, email, "GET")) {
            throw new AccessDeniedException("No permission to view department");
        }

        return departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found"));
    }
}