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
        if (role == null || role.isBlank()) {
            System.out.println("Role is null or empty");
            return false;
        }
        role = role.trim().toUpperCase();
        action = (action == null) ? "" : action.trim().toUpperCase();

        if (role.equals("SUPERADMIN")) {

            boolean emailExists = staffService.isClientEmailExist(email);
            if (!emailExists) {
                System.out.println("SuperAdmin email does not exist: " + email);
                return false;
            }

            System.out.println("SuperAdmin email verified: " + email);

            return switch (action) {
                case "GET" -> true;
                default -> {
                    System.out.println("SuperAdmin allowed only GET");
                    yield false;
                }
            };
        }

        if ("BRANCH".equals(role)) {
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

        return switch (role) {
            case "STAFF" -> {
                Map<String, Boolean> perms = staffService.getPermissionsByEmail(email);
                yield switch (action) {
                    case "GET" -> Boolean.TRUE.equals(perms.get("cansGet"));
                    case "POST" -> Boolean.TRUE.equals(perms.get("cansPost"));
                    case "PUT" -> Boolean.TRUE.equals(perms.get("cansPut"));
                    case "DELETE" -> Boolean.TRUE.equals(perms.get("cansDelete"));
                    default -> false;
                };
            }
            case "DEPARTMENT" -> {
                Map<String, Object> perms = staffService.getCrudPermissionForDepartmentByEmail(email);
                yield switch (action) {
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
                    yield false;
                }

                System.out.println("Permissions for USER: All allowed for existing email " + email);

                yield switch (action) {
                    case "GET", "POST", "PUT", "DELETE" -> true;
                    default -> false;
                };
            }
            default -> {
                System.out.println("Invalid role detected: " + role);
                yield false;
            }
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

    @Override
    public String fetchEmployeeStatusByEmail(String email) {
        return employeeRepository.findEmployeeByEmail(email)
                .map(Employee::getStatus)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
    }

}