package com.NewEmployeeManagement.Repository;

import com.NewEmployeeManagement.Entity.EmployeeSalary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface SalaryRepository extends JpaRepository<EmployeeSalary,Long>, JpaSpecificationExecutor<EmployeeSalary>
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

    @Query("SELECT s FROM EmployeeSalary s WHERE s.branchCode = :branchCode AND s.isDeleted = false")
    Page<EmployeeSalary> findAllByBranchCode(@Param("branchCode") String branchCode, Pageable pageable);

    @Query("SELECT s FROM EmployeeSalary s WHERE s.empId = :empId AND s.isDeleted = false ORDER BY s.year DESC, s.month DESC")
    List<EmployeeSalary> findAllByEmpId(@Param("empId") Long empId);

    @Modifying
    @Query("UPDATE EmployeeSalary s SET s.status = :status WHERE s.id = :salaryId AND s.isDeleted = false")
    int updateSalaryStatus(@Param("salaryId") Long salaryId, @Param("status") String status);


    @Query("SELECT COUNT(s) FROM EmployeeSalary s " +
            "WHERE (:month IS NULL OR s.month = :month) " +
            "AND (:year IS NULL OR s.year = :year) " +
            "AND s.isDeleted = false")
    long countAllSalaries(@Param("month") Integer month, @Param("year") Integer year);

    @Query("SELECT COUNT(s) FROM EmployeeSalary s " +
            "WHERE s.status = 'Paid' " +
            "AND (:month IS NULL OR s.month = :month) " +
            "AND (:year IS NULL OR s.year = :year) " +
            "AND s.isDeleted = false")
    long countPaidSalaries(@Param("month") Integer month, @Param("year") Integer year);

    @Query("SELECT COUNT(s) FROM EmployeeSalary s " +
            "WHERE s.status = 'Pending' " +
            "AND (:month IS NULL OR s.month = :month) " +
            "AND (:year IS NULL OR s.year = :year) " +
            "AND s.isDeleted = false")
    long countPendingSalaries(@Param("month") Integer month, @Param("year") Integer year);

    @Query("SELECT COALESCE(SUM(s.finalNetSalary), 0) FROM EmployeeSalary s " +
            "WHERE (:month IS NULL OR s.month = :month) " +
            "AND (:year IS NULL OR s.year = :year) " +
            "AND s.isDeleted = false")
    BigDecimal sumAllFinalNetSalary(@Param("month") Integer month, @Param("year") Integer year);

    @Query("SELECT COALESCE(SUM(s.finalNetSalary), 0) FROM EmployeeSalary s " +
            "WHERE s.status = 'Paid' " +
            "AND (:month IS NULL OR s.month = :month) " +
            "AND (:year IS NULL OR s.year = :year) " +
            "AND s.isDeleted = false")
    BigDecimal sumPaidFinalNetSalary(@Param("month") Integer month, @Param("year") Integer year);

    @Query("SELECT COALESCE(SUM(s.finalNetSalary), 0) FROM EmployeeSalary s " +
            "WHERE s.status = 'Pending' " +
            "AND (:month IS NULL OR s.month = :month) " +
            "AND (:year IS NULL OR s.year = :year) " +
            "AND s.isDeleted = false")
    BigDecimal sumPendingFinalNetSalary(@Param("month") Integer month, @Param("year") Integer year);
}
