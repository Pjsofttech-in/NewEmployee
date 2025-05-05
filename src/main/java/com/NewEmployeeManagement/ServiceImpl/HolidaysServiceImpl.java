package com.NewEmployeeManagement.ServiceImpl;

import com.NewEmployeeManagement.Entity.Holidays;
import com.NewEmployeeManagement.Repository.HolidaysRepository;
import com.NewEmployeeManagement.Service.HolidaysService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class HolidaysServiceImpl implements HolidaysService {

    private final HolidaysRepository repository;
    private final WebClient webClient;
    private final StaffService staffService;

    @Value("${client.superadmin.base-url}")
    private String superAdminBaseUrl;

    @Autowired
    public HolidaysServiceImpl(HolidaysRepository repository,
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
    public Holidays createHoliday(Holidays holiday, String role, String email) {
        if (!hasPermission(role, email, "POST")) {
            throw new AccessDeniedException("No permission to create holiday");
        }

        String branchCode = fetchBranchCodeByRole(role, email);
        holiday.setCreatedByEmail(email);
        holiday.setBranchCode(branchCode);
        holiday.setRole(role);

        return repository.save(holiday);
    }

    @Override
    public List<Holidays> getAllHolidays(String role, String email, String branchCode) {
        if (!hasPermission(role, email, "GET")) {
            throw new AccessDeniedException("No permission to view holidays");
        }

        return repository.findAllByBranchCode(branchCode);
    }

    @Override
    public Holidays updateHoliday(Long id, Holidays holiday, String role, String email) {
        if (!hasPermission(role, email, "PUT")) {
            throw new AccessDeniedException("No permission to update holiday");
        }

        Holidays existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Holiday not found"));

        existing.setHolidayName(holiday.getHolidayName() != null ? holiday.getHolidayName() : existing.getHolidayName());
        existing.setDate(holiday.getDate() != null ? holiday.getDate() : existing.getDate());
        existing.setDay(holiday.getDay() != null ? holiday.getDay() : existing.getDay());
        existing.setPaidHoliday(holiday.isPaidHoliday() != existing.isPaidHoliday() ? holiday.isPaidHoliday() : existing.isPaidHoliday());

        return repository.save(existing);
    }


    @Override
    public void deleteHoliday(Long id, String role, String email) {
        if (!hasPermission(role, email, "DELETE")) {
            throw new AccessDeniedException("No permission to delete holiday");
        }

        repository.findById(id).orElseThrow(() -> new RuntimeException("Holiday not found"));
        repository.deleteById(id);
    }

    @Override
    public Holidays getHolidayById(Long id, String role, String email) {
        if (!hasPermission(role, email, "GET")) {
            throw new AccessDeniedException("No permission to view holiday");
        }

        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Holiday not found"));
    }
}