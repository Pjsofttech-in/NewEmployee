package com.NewEmployeeManagement.ServiceImpl;

import com.NewEmployeeManagement.Entity.EmployeeNotice;
import com.NewEmployeeManagement.Repository.NoticeRepository;
import com.NewEmployeeManagement.Service.NoticeService;
import com.NewEmployeeManagement.Service.PermissionService;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class NoticeServiceImpl implements NoticeService {

    @Autowired
    NoticeRepository repository;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private StaffService staffService;


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
    public Page<EmployeeNotice> getAllNotices(String role, String email,
                                              LocalDate startDate, LocalDate endDate, @Nullable String branchFilter,
                                              int page, int size) {

        if (!permissionService.hasPermission(role, email, "GET")) {
            throw new AccessDeniedException("No permission to view notices");
        }

        Pageable pageable = PageRequest.of(page, size);

        if ("User".equalsIgnoreCase(role)) {
            String branchCode = permissionService.fetchBranchCode(role, email);
            String employeeStatus = permissionService.fetchEmployeeStatusByEmail(email);

            if (employeeStatus == null)
                throw new RuntimeException("Employee not found");

            if (!employeeStatus.equalsIgnoreCase("Joined") && !employeeStatus.equalsIgnoreCase("Rejoined"))
                throw new RuntimeException("Employee is terminated or not active");

            return repository.findAllByBranchCode(branchCode, startDate, endDate, pageable);
        }

        if ("SuperAdmin".equalsIgnoreCase(role)) {
            if (!staffService.isClientEmailExist(email)) {
                throw new RuntimeException("Invalid SuperAdmin email — institute not found");
            }

            List<String> branchCodes = staffService.getBranchCodesByInstituteEmail(email);

            if (branchFilter != null && !branchFilter.isBlank()) {
                branchCodes = branchCodes.stream()
                        .filter(code -> code.equalsIgnoreCase(branchFilter))
                        .toList();
            }

            return repository.findAllByBranchCodes(branchCodes, startDate, endDate, pageable);
        }

        String branchCode = permissionService.fetchBranchCode(role, email);
        return repository.findAllByBranchCode(branchCode, startDate, endDate, pageable);
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


}