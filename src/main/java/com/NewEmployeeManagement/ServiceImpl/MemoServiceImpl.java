package com.NewEmployeeManagement.ServiceImpl;

import com.NewEmployeeManagement.Entity.Memo;
import com.NewEmployeeManagement.Repository.MemoRepository;
import com.NewEmployeeManagement.Service.MemoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class MemoServiceImpl implements MemoService {

    @Autowired
    private MemoRepository repository;

    @Autowired
    private WebClient webClient;

    @Autowired
    private StaffService staffService;

    @Value("${client.superadmin.base-url}")
    private String superAdminBaseUrl;

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
    public Memo createMemo(Memo memo, String role, String email) {
        if (!hasPermission(role, email, "POST")) {
            throw new AccessDeniedException("No permission to create memo");
        }

        String branchCode = fetchBranchCodeByRole(role, email);
        memo.setRole(role);
        memo.setCreatedByEmail(email);
        memo.setBranchCode(branchCode);
        return repository.save(memo);
    }

    @Override
    public List<Memo> getAllMemos(String role, String email, String branchCode) {
        if (!hasPermission(role, email, "GET")) {
            throw new AccessDeniedException("No permission to view memos");
        }
        return repository.findAllByBranchCode(branchCode);
    }

    @Override
    public Memo getMemoById(int id, String role, String email) {
        if (!hasPermission(role, email, "GET")) {
            throw new AccessDeniedException("No permission to view memo");
        }

        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Memo not found"));
    }

    @Override
    public Memo updateMemo(int id, Memo memo, String role, String email) {
        if (!hasPermission(role, email, "PUT")) {
            throw new AccessDeniedException("No permission to update memo");
        }

        Memo existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Memo not found"));

        existing.setMemoName(memo.getMemoName() != null ? memo.getMemoName() : existing.getMemoName());
        existing.setMemoDescription(memo.getMemoDescription() != null ? memo.getMemoDescription() : existing.getMemoDescription());
        existing.setCreatedAt(memo.getCreatedAt() != null ? memo.getCreatedAt() : existing.getCreatedAt());
        existing.setEmail(memo.getEmail() != null ? memo.getEmail() : existing.getEmail());
        existing.setFullName(memo.getFullName() != null ? memo.getFullName() : existing.getFullName());

        return repository.save(existing);
    }

    @Override
    public void deleteMemo(int id, String role, String email) {
        if (!hasPermission(role, email, "DELETE")) {
            throw new AccessDeniedException("No permission to delete memo");
        }

        Memo memo = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Memo not found"));

        memo.setDeleted(true);
        repository.deleteById(id);
    }
}