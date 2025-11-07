package com.NewEmployeeManagement.Service;

import com.NewEmployeeManagement.Entity.EmployeeMemo;
import jakarta.annotation.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MemoService {
    EmployeeMemo createMemo(EmployeeMemo employeeMemo, String role, String email);
    Page<EmployeeMemo> getAllMemos(String role, String email, @Nullable String nameFilter,
                                   @Nullable String emailFilter, @Nullable String branchFilter, Pageable pageable);

    EmployeeMemo getMemoById(Long id, String role, String email);
    EmployeeMemo updateMemo(Long id, EmployeeMemo employeeMemo, String role, String email);
    void deleteMemo(Long id, String role, String email);
}