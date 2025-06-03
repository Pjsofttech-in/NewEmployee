package com.NewEmployeeManagement.ServiceImpl;

import com.NewEmployeeManagement.Entity.EmpQuery;
import com.NewEmployeeManagement.Repository.EmpQueryRepository;
import com.NewEmployeeManagement.Service.EmpQueryService;
import com.NewEmployeeManagement.Service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class EmpQueryServiceImpl implements EmpQueryService {

    @Autowired
    private EmpQueryRepository repository;

    @Autowired
    private PermissionService permissionService;

    @Override
    public EmpQuery createQuery(EmpQuery query, String role, String email) {
        if (!permissionService.hasPermission(role, email, "POST")) {
            throw new AccessDeniedException("No permission to create query");
        }

        String branchCode = permissionService.fetchBranchCode(role, email);
        query.setRole(role);
        query.setCreatedByEmail(email);
        query.setBranchCode(branchCode);
        query.setDate(LocalDate.now());
        return repository.save(query);
    }

    @Override
    public List<EmpQuery> getAllQueries(String role, String email) {
        if (!permissionService.hasPermission(role, email, "GET")) {
            throw new AccessDeniedException("No permission to view queries");
        }
        String branchCode = permissionService.fetchBranchCode(role, email);
        return repository.findAllByBranchCode(branchCode);
    }

    @Override
    public EmpQuery updateQuery(int id, EmpQuery query, String role, String email) {
        if (!permissionService.hasPermission(role, email, "PUT")) {
            throw new AccessDeniedException("No permission to update query");
        }

        EmpQuery existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Query not found"));

        existing.setEmail(query.getEmail() != null ? query.getEmail() : existing.getEmail());
        existing.setQuery(query.getQuery() != null ? query.getQuery() : existing.getQuery());

        return repository.save(existing);
    }


    @Override
    public void deleteQuery(int id, String role, String email) {
        if (!permissionService.hasPermission(role, email, "DELETE")) {
            throw new AccessDeniedException("No permission to delete query");
        }

        repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Query not found"));

        repository.deleteById(id);
    }

    @Override
    public EmpQuery getQueryById(int id, String role, String email) {
        if (!permissionService.hasPermission(role, email, "GET")) {
            throw new AccessDeniedException("No permission to view query");
        }

        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Query not found"));
    }
}