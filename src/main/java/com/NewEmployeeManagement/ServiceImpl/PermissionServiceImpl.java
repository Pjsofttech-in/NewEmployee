package com.NewEmployeeManagement.ServiceImpl;

import com.NewEmployeeManagement.Entity.Employee;
import com.NewEmployeeManagement.Repository.EmployeeRepository;
import com.NewEmployeeManagement.Service.EmployeeService;
import com.NewEmployeeManagement.Service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.Optional;

@Service
public class PermissionServiceImpl implements PermissionService {

    @Autowired
    StaffService staffService;

    @Autowired
    @Lazy
    EmployeeRepository employeeRepository;

    @Autowired
    WebClient webClient;

    @Autowired
    public PermissionServiceImpl(WebClient webClient)
    {
        this.webClient = webClient;
    }

    public boolean hasPermission(String role, String email, String action)
    {
        System.out.println("Checking permission for role: " + role + ", email: " + email + ", action: " + action);
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
            case "USER" -> {
                boolean emailExists = employeeRepository.existsByEmpEmail(email);

                if (!emailExists) {
                    System.out.println("Email does not exist: " + email);
                    yield false; // Deny permission
                }

                System.out.println("Permissions for USER: All allowed for existing email " + email);

                yield switch (action.toUpperCase()) {
                    case "GET", "POST", "PUT", "DELETE" -> true;
                    default -> false;
                };
            }

            default -> false;
        };
    }


    public String fetchBranchCode(String role, String email) {
        Map<String, String> roleToEndpoint = Map.of(
                "branch", "/branch/getbranchcode",
                "department", "/department/getbranchcode",
                "staff", "/staff/getbranchcode"
        );

        String lowerRole = role.toLowerCase();

        if ("user".equals(lowerRole)) {
            Optional<Employee> employee = employeeRepository.findByEmpEmail(email);
            if (employee.isPresent()) {
                return employee.get().getBranchCode(); // ✅ correct field accessor
            } else {
                throw new RuntimeException("Employee not found for email: " + email);
            }
        }


        String endpoint = roleToEndpoint.get(lowerRole);
        if (endpoint == null) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }

        return callWebClientForBranchCode(endpoint, email);
    }

    private String callWebClientForBranchCode(String endpoint, String email) {
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