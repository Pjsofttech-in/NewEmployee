package com.NewEmployeeManagement.Service;

import com.NewEmployeeManagement.Entity.EmployeeNotice;

import java.util.List;

public interface NoticeService {
    EmployeeNotice createNotice(EmployeeNotice employeeNotice, String role, String email);
    List<EmployeeNotice> getAllNotices(String role, String email);
    EmployeeNotice updateNotice(Long id, EmployeeNotice employeeNotice, String role, String email);
    void deleteNotice(Long id, String role, String email);
    EmployeeNotice getNoticeById(Long id, String role, String email);
}