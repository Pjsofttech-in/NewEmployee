package com.NewEmployeeManagement.ServiceImpl;

import com.NewEmployeeManagement.Entity.Department;
import com.NewEmployeeManagement.Repository.DepartmentRepository;
import com.NewEmployeeManagement.Service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository repository;
    private final WebClient webClient;
    private final StaffService staffService;

    @Value("${client.superadmin.base-url}")
    private String superAdminBaseUrl;

    @Autowired
    public DepartmentServiceImpl(DepartmentRepository repository,
                                 WebClient webClient,
                                 StaffService staffService) {
        this.repository = repository;
        this.webClient = webClient;
        this.staffService = staffService;
    }

    private boolean hasPermission(String role, String email, String action) {
        if ("BRANCH".equalsIgnoreCase(role)) {
            try {
                Boolean exists = webClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/existBranchbyemail")
                                .queryParam("email", email)
                                .build())
                        .retrieve()
                        .bodyToMono(Boolean.class)
                        .block();

                return Boolean.TRUE.equals(exists);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        return switch (role.toUpperCase()) {
            case "STAFF" -> {
                Map<String, Boolean> perms = staffService.getPermissionsByEmail(email);
                yield switch (action.toUpperCase()) {
                    case "GET" -> Boolean.TRUE.equals(perms.get("cansGet"));
                    case "POST" -> Boolean.TRUE.equals(perms.get("cansPost"));
                    case "PUT" -> Boolean.TRUE.equals(perms.get("cansPut"));
                    case "DELETE" -> Boolean.TRUE.equals(perms.get("cansDelete"));
                    default -> false;
                };
            }
            case "DEPARTMENT" -> {
                Map<String, Object> perms = staffService.getCrudPermissionForDepartmentByEmail(email);
                yield switch (action.toUpperCase()) {
                    case "GET" -> Boolean.TRUE.equals(perms.get("candGet"));
                    case "POST" -> Boolean.TRUE.equals(perms.get("candPost"));
                    case "PUT" -> Boolean.TRUE.equals(perms.get("candPut"));
                    case "DELETE" -> Boolean.TRUE.equals(perms.get("candDelete"));
                    default -> false;
                };
            }
            default -> false;
        };
    }

    private String fetchBranchCodeByRole(String role, String email) {
        String endpoint = switch (role.toLowerCase()) {
            case "branch" -> "/branch/getbranchcode";
            case "department" -> "/department/getbranchcode";
            case "staff" -> "/staff/getbranchcode";
            default -> throw new IllegalArgumentException("Invalid role: " + role);
        };

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(endpoint)
                        .queryParam("email", email)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    @Override
    public Department createDepartment(Department department, String role, String email) {
        if (!hasPermission(role, email, "POST")) {
            throw new AccessDeniedException("No permission to create department");
        }

        String branchCode = fetchBranchCodeByRole(role, email);
        department.setRole(role);
        department.setCreatedByEmail(email);
        department.setBranchCode(branchCode);
        department.setDeleted(false);
        return repository.save(department);
    }

    @Override
    public List<Department> getAllDepartments(String role, String email, String branchCode) {
        if (!hasPermission(role, email, "GET")) {
            throw new AccessDeniedException("No permission to view departments");
        }

        return repository.findAllByBranchCode(branchCode);
    }

    @Override
    public Department updateDepartment(int id, Department department, String role, String email) {
        if (!hasPermission(role, email, "PUT")) {
            throw new AccessDeniedException("No permission to update department");
        }

        Department existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found"));

        existing.setDepartment(department.getDepartment());
        return repository.save(existing);
    }

    @Override
    public void deleteDepartment(int id, String role, String email) {
        if (!hasPermission(role, email, "DELETE")) {
            throw new AccessDeniedException("No permission to delete department");
        }

        Department existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found"));

        existing.setDeleted(true);
        repository.save(existing);
    }

    @Override
    public Department getDepartmentById(int id, String role, String email) {
        if (!hasPermission(role, email, "GET")) {
            throw new AccessDeniedException("No permission to view department");
        }

        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found"));
    }
}