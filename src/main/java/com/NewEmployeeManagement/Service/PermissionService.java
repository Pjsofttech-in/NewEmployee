package com.NewEmployeeManagement.Service;

public interface PermissionService {
    boolean hasPermission(String module, String action, String role, String email);
}