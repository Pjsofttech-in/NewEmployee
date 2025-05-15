package com.NewEmployeeManagement.Repository;

import com.NewEmployeeManagement.Entity.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long>
{
    @Query("SELECT l FROM LeaveRequest l WHERE l.branchCode = :branchCode ORDER BY l.id DESC")
    List<LeaveRequest> findAllByBranchCode(String branchCode);
}