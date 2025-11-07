package com.NewEmployeeManagement.Repository;

import com.NewEmployeeManagement.Entity.EmployeeMemo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MemoRepository extends JpaRepository<EmployeeMemo, Long> {

    @Query("SELECT m FROM EmployeeMemo m WHERE m.branchCode = :branchCode AND m.isDeleted = false ORDER BY m.id DESC")
    List<EmployeeMemo> findAllByBranchCode(@Param("branchCode") String branchCode);

    @Query("""
        SELECT m FROM EmployeeMemo m
        WHERE m.isDeleted = false
        AND m.branchCode IN :branchCodes
        AND (:nameFilter IS NULL OR LOWER(m.fullName) LIKE LOWER(CONCAT('%', :nameFilter, '%')))
        AND (:emailFilter IS NULL OR LOWER(m.email) LIKE LOWER(CONCAT('%', :emailFilter, '%')))
        """)
    Page<EmployeeMemo> findAllByFilters(@Param("branchCodes") List<String> branchCodes,
                                        @Param("nameFilter") String nameFilter,
                                        @Param("emailFilter") String emailFilter,
                                        Pageable pageable);

    @Query("""
        SELECT m FROM EmployeeMemo m
        WHERE LOWER(m.email) = LOWER(:email)
        AND m.isDeleted = false
        """)
    Page<EmployeeMemo> findAllByEmail(@Param("email") String email, Pageable pageable);

}