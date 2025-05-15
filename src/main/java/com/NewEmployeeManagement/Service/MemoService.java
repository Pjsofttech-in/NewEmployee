package com.NewEmployeeManagement.Service;

import com.NewEmployeeManagement.Entity.Memo;

import java.util.List;

public interface MemoService {
    Memo createMemo(Memo memo, String role, String email);
    List<Memo> getAllMemos(String role, String email);
    Memo getMemoById(int id, String role, String email);
    Memo updateMemo(int id, Memo memo, String role, String email);
    void deleteMemo(int id, String role, String email);
}