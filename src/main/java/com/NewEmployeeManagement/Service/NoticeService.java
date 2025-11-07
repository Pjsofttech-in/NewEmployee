package com.NewEmployeeManagement.Service;

import com.NewEmployeeManagement.Entity.EmployeeNotice;
import jakarta.annotation.Nullable;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;

public interface NoticeService {
    EmployeeNotice createNotice(EmployeeNotice employeeNotice, String role, String email);
    Page<EmployeeNotice> getAllNotices(String role, String email,
                                       LocalDate startDate, LocalDate endDate,
                                       @Nullable String branchFilter, int page, int size);
    EmployeeNotice updateNotice(Long id, EmployeeNotice employeeNotice, String role, String email);
    void deleteNotice(Long id, String role, String email);
    EmployeeNotice getNoticeById(Long id, String role, String email);
}