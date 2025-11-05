package com.NewEmployeeManagement.ServiceImpl;

import com.NewEmployeeManagement.Entity.EmployeeCategory;
import com.NewEmployeeManagement.Entity.EmployeeDepartment;
import com.NewEmployeeManagement.Repository.DepartmentRepository;
import com.NewEmployeeManagement.Service.DepartmentService;
import com.NewEmployeeManagement.Service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private StaffService staffService;

    @Override
    public EmployeeDepartment createDepartment(EmployeeDepartment employeeDepartment, String role, String email) {
        if (!permissionService.hasPermission(role, email, "POST")) {
            throw new AccessDeniedException("No permission to create department");
        }

        String branchCode = permissionService.fetchBranchCode(role, email);
        employeeDepartment.setRole(role);
        employeeDepartment.setCreatedByEmail(email);
        employeeDepartment.setBranchCode(branchCode);

        boolean exists = departmentRepository.existsByDepartmentAndBranchCode(
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
        if ("Superadmin".equalsIgnoreCase(role)) {
            boolean emailExists = staffService.isClientEmailExist(email);
            if (!emailExists) {
                throw new AccessDeniedException("Institute email does not exist: " + email);
            }

            List<String> branchCodes = staffService.getBranchCodesByInstituteEmail(email);

            if (branchCodes == null || branchCodes.isEmpty()) {
                return Collections.emptyList();
            }

            List<EmployeeDepartment> list = departmentRepository.findAllByBranchCodeIn(branchCodes);

            return list;
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

        if (!existing.getEmployees().isEmpty()) {
            throw new IllegalStateException("Cannot delete department with active employees");
        }
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