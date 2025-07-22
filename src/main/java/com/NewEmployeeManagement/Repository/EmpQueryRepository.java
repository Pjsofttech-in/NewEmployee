package com.NewEmployeeManagement.Repository;

import com.NewEmployeeManagement.Entity.EmployeeQuery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EmpQueryRepository extends JpaRepository<EmployeeQuery, Long> {

    @Query("SELECT q FROM EmployeeQuery q WHERE q.branchCode = :branchCode ORDER BY q.id DESC")
    List<EmployeeQuery> findAllByBranchCode(String branchCode);

}