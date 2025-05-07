package com.NewEmployeeManagement.Pageination;


import com.NewEmployeeManagement.Entity.Employee;
import com.NewEmployeeManagement.Repository.EmployeeRepository;
import com.NewEmployeeManagement.Service.PermissionService;
import com.NewEmployeeManagement.ServiceImpl.StaffService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SpecializationService {
    private final EmployeeRepository employeeRepository;
    private final PermissionService permissionService;

    @Autowired
    private WebClient webClient;

    @Autowired
    private StaffService staffService;

    public Page<Employee> filterEmployees(String department, String categoryName, String designation,
                                          String status, String branchCode, String role, String email,
                                          int page, int size) {

        if (!hasPermission(role, email, "POST")) throw new AccessDeniedException("No permission");



        Specification<Employee> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("isDeleted")));

            if (branchCode != null) predicates.add(cb.equal(root.get("branchCode"), branchCode));
            if (department != null) predicates.add(cb.equal(root.get("department"), department));
            if (categoryName != null) predicates.add(cb.equal(root.get("categoryName"), categoryName));
            if (designation != null) predicates.add(cb.equal(root.get("designation"), designation));
            if (status != null) predicates.add(cb.equal(root.get("status"), status));

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Pageable pageable = PageRequest.of(page, size);
        return employeeRepository.findAll(spec, pageable);
    }

    private boolean hasPermission(String role, String email, String action) {
        if ("BRANCH".equalsIgnoreCase(role)) {
            Boolean exists = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/existBranchbyemail")
                            .queryParam("email", email)
                            .build())
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();
            return Boolean.TRUE.equals(exists);
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

    private String fetchBranchCode(String role, String email) {
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


}
