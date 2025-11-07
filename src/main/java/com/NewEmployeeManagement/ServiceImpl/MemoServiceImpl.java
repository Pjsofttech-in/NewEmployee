package com.NewEmployeeManagement.ServiceImpl;

import com.NewEmployeeManagement.Entity.EmployeeMemo;
import com.NewEmployeeManagement.Repository.MemoRepository;
import com.NewEmployeeManagement.Service.MemoService;
import com.NewEmployeeManagement.Service.PermissionService;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MemoServiceImpl implements MemoService {

    @Autowired
    private MemoRepository repository;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private StaffService staffService;

    @Override
    public EmployeeMemo createMemo(EmployeeMemo employeeMemo, String role, String email) {
        if (!permissionService.hasPermission(role, email, "POST")) {
            throw new AccessDeniedException("No permission to create memo");
        }

        String branchCode = permissionService.fetchBranchCode(role, email);
        employeeMemo.setRole(role);
        employeeMemo.setCreatedByEmail(email);
        employeeMemo.setBranchCode(branchCode);
        return repository.save(employeeMemo);
    }

    @Override
    public Page<EmployeeMemo> getAllMemos(String role, String email, @Nullable String nameFilter,
                                          @Nullable String emailFilter, @Nullable String branchFilter, Pageable pageable) {

        if (!permissionService.hasPermission(role, email, "GET")) {
            throw new AccessDeniedException("No permission to view memos");
        }

        if ("USER".equalsIgnoreCase(role)) {
            return repository.findAllByEmail(email, pageable);
        }
        boolean isSuperAdmin = role != null && role.equalsIgnoreCase("superadmin");
        List<String> branchCodes = new ArrayList<>();
            if (branchFilter != null && !branchFilter.isBlank()) {
                branchCodes.add(branchFilter);
            } else if (isSuperAdmin) {
                boolean exists = staffService.isClientEmailExist(email);
                if (!exists) {
                    throw new AccessDeniedException("Institute email not found or no permission for SuperAdmin with this email");
                }
                List<String> fetched = staffService.getBranchCodesByInstituteEmail(email);
                if (fetched != null && !fetched.isEmpty()) {
                    branchCodes.addAll(fetched);
                }
            }  else {
            branchCodes.add(permissionService.fetchBranchCode(role, email));
        }

        return repository.findAllByFilters(branchCodes, nameFilter, emailFilter, pageable);
    }

    @Override
    public EmployeeMemo getMemoById(Long id, String role, String email) {
        if (!permissionService.hasPermission(role, email, "GET")) {
            throw new AccessDeniedException("No permission to view memo");
        }

        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Memo not found"));
    }

    @Override
    public EmployeeMemo updateMemo(Long id, EmployeeMemo employeeMemo, String role, String email) {
        if (!permissionService.hasPermission(role, email, "PUT")) {
            throw new AccessDeniedException("No permission to update memo");
        }

        EmployeeMemo existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Memo not found"));

        existing.setMemoName(employeeMemo.getMemoName() != null ? employeeMemo.getMemoName() : existing.getMemoName());
        existing.setMemoDescription(employeeMemo.getMemoDescription() != null ? employeeMemo.getMemoDescription() : existing.getMemoDescription());
        existing.setCreatedAt(employeeMemo.getCreatedAt() != null ? employeeMemo.getCreatedAt() : existing.getCreatedAt());
        existing.setEmail(employeeMemo.getEmail() != null ? employeeMemo.getEmail() : existing.getEmail());
        existing.setFullName(employeeMemo.getFullName() != null ? employeeMemo.getFullName() : existing.getFullName());

        return repository.save(existing);
    }

    @Override
    public void deleteMemo(Long id, String role, String email) {
        if (!permissionService.hasPermission(role, email, "DELETE")) {
            throw new AccessDeniedException("No permission to delete memo");
        }

        EmployeeMemo employeeMemo = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Memo not found"));

        employeeMemo.setDeleted(true);
        repository.deleteById(id);
    }
}