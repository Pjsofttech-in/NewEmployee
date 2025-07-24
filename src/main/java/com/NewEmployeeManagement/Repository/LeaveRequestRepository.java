package com.NewEmployeeManagement.Repository;

import com.NewEmployeeManagement.Entity.EmployeeLeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<EmployeeLeaveRequest, Long>
{
    @Query("SELECT l FROM EmployeeLeaveRequest l WHERE l.branchCode = :branchCode ORDER BY l.id DESC")
    List<EmployeeLeaveRequest> findAllByBranchCode(@Param("branchCode")String branchCode);

    @Query("SELECT e FROM EmployeeLeaveRequest e WHERE e.empID = :empId AND e.isDeleted = false")
    List<EmployeeLeaveRequest> findByEmpIDAndIsDeletedFalse(@Param("empId") Long empId);
}