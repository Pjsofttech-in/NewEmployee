package com.NewEmployeeManagement.ServiceImpl;

import com.NewEmployeeManagement.Entity.EmployeeQuery;
import com.NewEmployeeManagement.Entity.Employee;
import com.NewEmployeeManagement.Repository.EmpQueryRepository;
import com.NewEmployeeManagement.Repository.EmployeeRepository;
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
    private EmployeeRepository employeeRepository;

    @Autowired
    private PermissionService permissionService;

    @Override
    public EmployeeQuery createQuery(EmployeeQuery query, String role, String email) {
        if (!permissionService.hasPermission(role, email, "POST")) {
            throw new AccessDeniedException("No permission to create query");
        }

        String branchCode = permissionService.fetchBranchCode(role, email);

        // 🔍 Check Employee existence
        Employee employee = employeeRepository.findByEmpEmail(query.getEmail())
                .orElseThrow(() -> new RuntimeException("Employee with given email not found"));

        if (employee.isDeleted()) {
            throw new RuntimeException("Employee with given email is deleted");
        }

        query.setRole(role);
        query.setCreatedByEmail(email);
        query.setBranchCode(branchCode);
        query.setDate(LocalDate.now());
        query.setEmployee(employee); // ✅ Set employee object

        return repository.save(query);
    }


    @Override
    public List<EmployeeQuery> getAllQueries(String role, String email) {
        if (!permissionService.hasPermission(role, email, "GET")) {
            throw new AccessDeniedException("No permission to view queries");
        }
        if("USER".equalsIgnoreCase(role)){
            return repository.findAllByEmail(email);
        }
        else {
            String branchCode = permissionService.fetchBranchCode(role, email);
            return repository.findAllByBranchCode(branchCode);
        }
    }

    @Override
    public EmployeeQuery updateQuery(Long id, EmployeeQuery query, String role, String email) {
        if (!permissionService.hasPermission(role, email, "PUT")) {
            throw new AccessDeniedException("No permission to update query");
        }

        EmployeeQuery existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Query not found"));

        existing.setEmail(query.getEmail() != null ? query.getEmail() : existing.getEmail());
        existing.setQuery(query.getQuery() != null ? query.getQuery() : existing.getQuery());

        return repository.save(existing);
    }


    @Override
    public void deleteQuery(Long id, String role, String email) {
        if (!permissionService.hasPermission(role, email, "DELETE")) {
            throw new AccessDeniedException("No permission to delete query");
        }

        repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Query not found"));

        repository.deleteById(id);
    }

    @Override
    public EmployeeQuery getQueryById(Long id, String role, String email) {
        if (!permissionService.hasPermission(role, email, "GET")) {
            throw new AccessDeniedException("No permission to view query");
        }

        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Query not found"));
    }
}