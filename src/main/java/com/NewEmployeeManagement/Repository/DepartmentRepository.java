package com.NewEmployeeManagement.Repository;

import com.NewEmployeeManagement.Entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Integer> {

    @Query("SELECT d FROM Department d WHERE d.branchCode = :branchCode ORDER BY d.id DESC")
    List<Department> findAllByBranchCode(@Param("branchCode") String branchCode);


}