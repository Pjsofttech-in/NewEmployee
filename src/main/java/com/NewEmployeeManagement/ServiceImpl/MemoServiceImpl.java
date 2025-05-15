package com.NewEmployeeManagement.ServiceImpl;

import com.NewEmployeeManagement.Entity.Memo;
import com.NewEmployeeManagement.Repository.MemoRepository;
import com.NewEmployeeManagement.Service.MemoService;
import com.NewEmployeeManagement.Service.PermissionService;
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
    private PermissionService permissionService;

    @Override
    public Memo createMemo(Memo memo, String role, String email) {
        if (!permissionService.hasPermission(role, email, "POST")) {
            throw new AccessDeniedException("No permission to create memo");
        }

        String branchCode = permissionService.fetchBranchCode(role, email);
        memo.setRole(role);
        memo.setCreatedByEmail(email);
        memo.setBranchCode(branchCode);
        return repository.save(memo);
    }

    @Override
    public List<Memo> getAllMemos(String role, String email) {
        if (!permissionService.hasPermission(role, email, "GET")) {
            throw new AccessDeniedException("No permission to view memos");
        }
        String branchCode = permissionService.fetchBranchCode(role, email);
        return repository.findAllByBranchCode(branchCode);
    }

    @Override
    public Memo getMemoById(int id, String role, String email) {
        if (!permissionService.hasPermission(role, email, "GET")) {
            throw new AccessDeniedException("No permission to view memo");
        }

        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Memo not found"));
    }

    @Override
    public Memo updateMemo(int id, Memo memo, String role, String email) {
        if (!permissionService.hasPermission(role, email, "PUT")) {
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
        if (!permissionService.hasPermission(role, email, "DELETE")) {
            throw new AccessDeniedException("No permission to delete memo");
        }

        Memo memo = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Memo not found"));

        memo.setDeleted(true);
        repository.deleteById(id);
    }
}