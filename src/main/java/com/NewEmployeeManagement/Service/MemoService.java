package com.NewEmployeeManagement.Service;

import com.NewEmployeeManagement.Entity.EmployeeMemo;

import java.util.List;

public interface MemoService {
    EmployeeMemo createMemo(EmployeeMemo employeeMemo, String role, String email);
    List<EmployeeMemo> getAllMemos(String role, String email);
    EmployeeMemo getMemoById(Long id, String role, String email);
    EmployeeMemo updateMemo(Long id, EmployeeMemo employeeMemo, String role, String email);
    void deleteMemo(Long id, String role, String email);
}