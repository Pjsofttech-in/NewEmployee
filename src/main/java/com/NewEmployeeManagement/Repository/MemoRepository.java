package com.NewEmployeeManagement.Repository;

import com.NewEmployeeManagement.Entity.EmployeeMemo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MemoRepository extends JpaRepository<EmployeeMemo, Long> {

    @Query("SELECT m FROM EmployeeMemo m WHERE m.branchCode = :branchCode AND m.isDeleted = false ORDER BY m.id DESC")
    List<EmployeeMemo> findAllByBranchCode(@Param("branchCode") String branchCode);

    List<EmployeeMemo> findAllByEmail(String email);
}