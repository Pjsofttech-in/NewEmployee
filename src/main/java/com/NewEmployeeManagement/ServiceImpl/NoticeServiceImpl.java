package com.NewEmployeeManagement.ServiceImpl;

import com.NewEmployeeManagement.Entity.EmployeeNotice;
import com.NewEmployeeManagement.Repository.NoticeRepository;
import com.NewEmployeeManagement.Service.NoticeService;
import com.NewEmployeeManagement.Service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NoticeServiceImpl implements NoticeService {

    @Autowired
    NoticeRepository repository;

    @Autowired
    private PermissionService permissionService;


    @Override
    public EmployeeNotice createNotice(EmployeeNotice employeeNotice, String role, String email) {
        if (!permissionService.hasPermission(role, email, "POST")) {
            throw new AccessDeniedException("No permission to create notice");
        }

        String branchCode = permissionService.fetchBranchCode(role, email);
        employeeNotice.setBranchCode(branchCode);
        employeeNotice.setRole(role);
        employeeNotice.setCreatedByEmail(email);
        return repository.save(employeeNotice);
    }

    @Override
    public List<EmployeeNotice> getAllNotices(String role, String email) {
        if (!permissionService.hasPermission(role, email, "GET")) {
            throw new AccessDeniedException("No permission to view notices");
        }
        String branchCode = permissionService.fetchBranchCode(role, email);
        return repository.findAllByBranchCode(branchCode);
    }

    @Override
    public EmployeeNotice updateNotice(Long id, EmployeeNotice employeeNotice, String role, String email) {
        if (!permissionService.hasPermission(role, email, "PUT")) {
            throw new AccessDeniedException("No permission to update notice");
        }

        EmployeeNotice existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notice not found"));

        existing.setNoticeName(
                employeeNotice.getNoticeName() != null && !employeeNotice.getNoticeName().isBlank()
                        ? employeeNotice.getNoticeName()
                        : existing.getNoticeName()
        );

        existing.setNoticeDescription(
                employeeNotice.getNoticeDescription() != null && !employeeNotice.getNoticeDescription().isBlank()
                        ? employeeNotice.getNoticeDescription()
                        : existing.getNoticeDescription()
        );

        return repository.save(existing);
    }


    @Override
    public void deleteNotice(Long id, String role, String email) {
        if (!permissionService.hasPermission(role, email, "DELETE")) {
            throw new AccessDeniedException("No permission to delete notice");
        }

        EmployeeNotice employeeNotice = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notice not found"));

        employeeNotice.setDeleted(true);
        repository.deleteById(id);
    }

    @Override
    public EmployeeNotice getNoticeById(Long id, String role, String email) {
        if (!permissionService.hasPermission(role, email, "GET")) {
            throw new AccessDeniedException("No permission to view notice");
        }

        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notice not found"));
    }

    @Override
    public List<EmployeeNotice> getNoticesByEmail(String role, String email)
    {
        if (!permissionService.hasPermission(role, email, "GET")) {
            throw new AccessDeniedException("No permission to view notice");
        }

        return repository.findAllByEmailAndNotDeleted(email);
    }
}