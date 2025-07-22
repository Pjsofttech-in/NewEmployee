package com.NewEmployeeManagement.Repository;

import com.NewEmployeeManagement.Entity.EmployeeDepartment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepartmentRepository extends JpaRepository<EmployeeDepartment, Long> {

    @Query("SELECT d FROM EmployeeDepartment d WHERE d.branchCode = :branchCode ORDER BY d.id DESC")
    List<EmployeeDepartment> findAllByBranchCode(@Param("branchCode") String branchCode);


}