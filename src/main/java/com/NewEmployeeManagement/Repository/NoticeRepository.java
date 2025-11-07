package com.NewEmployeeManagement.Repository;

import com.NewEmployeeManagement.Entity.EmployeeNotice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface NoticeRepository extends JpaRepository<EmployeeNotice, Long> {

    @Query("SELECT n FROM EmployeeNotice n WHERE n.branchCode = :branchCode AND n.isDeleted = false ORDER BY n.id DESC")
    List<EmployeeNotice> findAllByBranchCode(@Param("branchCode") String branchCode);
    @Query("""
        SELECT n FROM EmployeeNotice n
        WHERE n.isDeleted = false
        AND n.branchCode = :branchCode
        AND (:startDate IS NULL OR n.createdAt >= :startDate)
        AND (:endDate IS NULL OR n.createdAt <= :endDate)
        """)
    Page<EmployeeNotice> findAllByBranchCode(@Param("branchCode") String branchCode,
                                             @Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate,
                                             Pageable pageable);

    @Query("""
        SELECT n FROM EmployeeNotice n
        WHERE n.isDeleted = false
        AND n.branchCode IN :branchCodes
        AND (:startDate IS NULL OR n.createdAt >= :startDate)
        AND (:endDate IS NULL OR n.createdAt <= :endDate)
        """)
    Page<EmployeeNotice> findAllByBranchCodes(@Param("branchCodes") List<String> branchCodes,
                                              @Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate,
                                              Pageable pageable);

}
