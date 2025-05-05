package com.NewEmployeeManagement.ServiceImpl;

import com.NewEmployeeManagement.Service.PermissionService;
import org.springframework.stereotype.Service;

@Service
public class PermissionServiceImpl implements PermissionService {

    @Override
    public boolean hasPermission(String module, String action, String role, String email) {
        // Implement permission logic here based on your requirements
        // For example:
        if ("employee_category".equals(module)) {
            if ("delete".equals(action) && "ADMIN".equals(role)) {
                return true; // Admin can delete
            }
        }
        // Add more cases or more sophisticated logic here
        return false; // Deny access by default
    }
}