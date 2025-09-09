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

    @Query("SELECT e FROM EmployeeLeaveRequest e WHERE e.empId = :empId AND e.isDeleted = false")
    List<EmployeeLeaveRequest> findByEmpIDAndIsDeletedFalse(@Param("empId") Long empId);

    @Query("SELECT COALESCE(SUM(l.paidleave), 0) " +
            "FROM EmployeeLeaveRequest l " +
            "WHERE l.empId = :empId " +
            "AND l.status = 'APPROVED' " +
            "AND MONTH(l.fromDate) = :month " +
            "AND YEAR(l.fromDate) = :year")
    Double getPaidLeaveForMonth(@Param("empId") Long empId,
                                @Param("month") int month,
                                @Param("year") int year);


    @Query("SELECT MONTH(e.fromDate), SUM(e.leaveRequired) " +
            "FROM EmployeeLeaveRequest e " +
            "WHERE e.empId = :empId " +
            "AND YEAR(e.fromDate) = :year " +
            "AND e.isDeleted = false " +
            "AND e.status = 'Approved' " +
            "GROUP BY MONTH(e.fromDate)")
    List<Object[]> getLeaveByYear(@Param("empId") Long empId, @Param("year") int year);

}