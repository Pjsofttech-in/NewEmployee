package com.NewEmployeeManagement.Repository;

import com.NewEmployeeManagement.Entity.EmployeeSalary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalaryRepository extends JpaRepository<EmployeeSalary,Long>
{
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END " +
            "FROM EmployeeSalary s " +
            "WHERE s.empId = :empId AND s.month = :month AND s.year = :year AND s.isDeleted = false")
    boolean existsByEmpIdAndMonthAndYear(@Param("empId") Long empId,
                                         @Param("month") int month,
                                         @Param("year") int year);

    @Query("SELECT s FROM EmployeeSalary s " +
            "WHERE s.empId = :empId AND s.month = :month AND s.year = :year AND s.isDeleted = false")
    EmployeeSalary findByEmpIdAndMonthAndYear(@Param("empId") Long empId,
                                              @Param("month") int month,
                                              @Param("year") int year);



    @Query("SELECT s FROM EmployeeSalary s WHERE s.branchCode = :branchCode ORDER BY s.id DESC")
    List<EmployeeSalary> findAllByBranchCode(@Param("branchCode") String branchCode);


}
