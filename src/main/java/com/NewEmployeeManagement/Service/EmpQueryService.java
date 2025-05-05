package com.NewEmployeeManagement.Service;

import com.NewEmployeeManagement.Entity.EmpQuery;

import java.util.List;

public interface EmpQueryService {
    EmpQuery createQuery(EmpQuery query, String role, String email);
    List<EmpQuery> getAllQueries(String role, String email, String branchCode);
    EmpQuery updateQuery(int id, EmpQuery query, String role, String email);
    void deleteQuery(int id, String role, String email);
    EmpQuery getQueryById(int id, String role, String email);
}