package com.NewEmployeeManagement.Repository;

import com.NewEmployeeManagement.Entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, Integer> {

    @Query("SELECT n FROM Notice n WHERE n.branchCode = :branchCode AND n.isDeleted = false ORDER BY e.id DESC")
    List<Notice> findAllByBranchCode(String branchCode);
}
