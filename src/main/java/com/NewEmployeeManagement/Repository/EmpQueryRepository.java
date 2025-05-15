package com.NewEmployeeManagement.Repository;

import com.NewEmployeeManagement.Entity.EmpQuery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EmpQueryRepository extends JpaRepository<EmpQuery, Integer> {

    @Query("SELECT q FROM EmpQuery q WHERE q.branchCode = :branchCode ORDER BY e.id DESC")
    List<EmpQuery> findAllByBranchCode(String branchCode);
}