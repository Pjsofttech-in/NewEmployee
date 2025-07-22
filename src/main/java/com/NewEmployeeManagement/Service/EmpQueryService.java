package com.NewEmployeeManagement.Service;

import com.NewEmployeeManagement.Entity.EmployeeQuery;

import java.util.List;

public interface EmpQueryService {
    EmployeeQuery createQuery(EmployeeQuery query, String role, String email);
    List<EmployeeQuery> getAllQueries(String role, String email);
    EmployeeQuery updateQuery(Long id, EmployeeQuery query, String role, String email);
    void deleteQuery(Long id, String role, String email);
    EmployeeQuery getQueryById(Long id, String role, String email);
}