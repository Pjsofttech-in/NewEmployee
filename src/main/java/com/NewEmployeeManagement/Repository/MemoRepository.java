package com.NewEmployeeManagement.Repository;

import com.NewEmployeeManagement.Entity.Memo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MemoRepository extends JpaRepository<Memo, Integer> {

    @Query("SELECT m FROM Memo m WHERE m.branchCode = :branchCode AND m.isDeleted = false")
    List<Memo> findAllByBranchCode(String branchCode);
}