package com.NewEmployeeManagement.ServiceImpl;

import com.NewEmployeeManagement.Entity.Notice;
import com.NewEmployeeManagement.Repository.NoticeRepository;
import com.NewEmployeeManagement.Service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class NoticeServiceImpl implements NoticeService {

    private final NoticeRepository repository;
    private final WebClient webClient;
    private final StaffService staffService;

    @Value("${client.superadmin.base-url}")
    private String superAdminBaseUrl;

    @Autowired
    public NoticeServiceImpl(NoticeRepository repository, WebClient webClient, StaffService staffService) {
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
    public Notice createNotice(Notice notice, String role, String email) {
        if (!hasPermission(role, email, "POST")) {
            throw new AccessDeniedException("No permission to create notice");
        }

        String branchCode = fetchBranchCodeByRole(role, email);
        notice.setBranchCode(branchCode);
        notice.setRole(role);
        notice.setCreatedByEmail(email);
        return repository.save(notice);
    }

    @Override
    public List<Notice> getAllNotices(String role, String email, String branchCode) {
        if (!hasPermission(role, email, "GET")) {
            throw new AccessDeniedException("No permission to view notices");
        }

        return repository.findAllByBranchCode(branchCode);
    }

    @Override
    public Notice updateNotice(int id, Notice notice, String role, String email) {
        if (!hasPermission(role, email, "PUT")) {
            throw new AccessDeniedException("No permission to update notice");
        }

        Notice existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notice not found"));

        existing.setNoticeName(
                notice.getNoticeName() != null && !notice.getNoticeName().isBlank()
                        ? notice.getNoticeName()
                        : existing.getNoticeName()
        );

        existing.setNoticeDescription(
                notice.getNoticeDescription() != null && !notice.getNoticeDescription().isBlank()
                        ? notice.getNoticeDescription()
                        : existing.getNoticeDescription()
        );

        return repository.save(existing);
    }


    @Override
    public void deleteNotice(int id, String role, String email) {
        if (!hasPermission(role, email, "DELETE")) {
            throw new AccessDeniedException("No permission to delete notice");
        }

        Notice notice = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notice not found"));

        notice.setDeleted(true);
        repository.deleteById(id);
    }

    @Override
    public Notice getNoticeById(int id, String role, String email) {
        if (!hasPermission(role, email, "GET")) {
            throw new AccessDeniedException("No permission to view notice");
        }

        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notice not found"));
    }
}