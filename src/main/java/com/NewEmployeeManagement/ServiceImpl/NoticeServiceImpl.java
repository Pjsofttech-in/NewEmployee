package com.NewEmployeeManagement.ServiceImpl;

import com.NewEmployeeManagement.Entity.Notice;
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
    public Notice createNotice(Notice notice, String role, String email) {
        if (!permissionService.hasPermission(role, email, "POST")) {
            throw new AccessDeniedException("No permission to create notice");
        }

        String branchCode = permissionService.fetchBranchCode(role, email);
        notice.setBranchCode(branchCode);
        notice.setRole(role);
        notice.setCreatedByEmail(email);
        return repository.save(notice);
    }

    @Override
    public List<Notice> getAllNotices(String role, String email) {
        if (!permissionService.hasPermission(role, email, "GET")) {
            throw new AccessDeniedException("No permission to view notices");
        }
        String branchCode = permissionService.fetchBranchCode(role, email);
        return repository.findAllByBranchCode(branchCode);
    }

    @Override
    public Notice updateNotice(int id, Notice notice, String role, String email) {
        if (!permissionService.hasPermission(role, email, "PUT")) {
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
        if (!permissionService.hasPermission(role, email, "DELETE")) {
            throw new AccessDeniedException("No permission to delete notice");
        }

        Notice notice = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notice not found"));

        notice.setDeleted(true);
        repository.deleteById(id);
    }

    @Override
    public Notice getNoticeById(int id, String role, String email) {
        if (!permissionService.hasPermission(role, email, "GET")) {
            throw new AccessDeniedException("No permission to view notice");
        }

        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notice not found"));
    }
}