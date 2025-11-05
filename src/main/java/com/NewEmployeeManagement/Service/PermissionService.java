package com.NewEmployeeManagement.Service;

public interface PermissionService {

    boolean hasPermission(String role, String email, String action);
    String fetchBranchCode(String role, String email);
    String fetchEmployeeStatusByEmail(String email);
}