package com.NewEmployeeManagement.Repository;

import com.NewEmployeeManagement.Entity.EmployeeNotice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NoticeRepository extends JpaRepository<EmployeeNotice, Long> {

    @Query("SELECT n FROM EmployeeNotice n WHERE n.branchCode = :branchCode AND n.isDeleted = false ORDER BY n.id DESC")
    List<EmployeeNotice> findAllByBranchCode(@Param("branchCode") String branchCode);
}
