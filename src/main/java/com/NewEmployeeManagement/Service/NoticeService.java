package com.NewEmployeeManagement.Service;

import com.NewEmployeeManagement.Entity.Notice;

import java.util.List;

public interface NoticeService {
    Notice createNotice(Notice notice, String role, String email);
    List<Notice> getAllNotices(String role, String email, String branchCode);
    Notice updateNotice(int id, Notice notice, String role, String email);
    void deleteNotice(int id, String role, String email);
    Notice getNoticeById(int id, String role, String email);
}