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
    @Query("UPDATE EmployeeSalary s " +
            "SET s.status = :status, " +
            "    s.transactionId = CASE WHEN :transactionId IS NULL THEN s.transactionId ELSE :transactionId END " +
            "WHERE s.id = :salaryId AND s.isDeleted = false")
    int updateSalaryStatus(@Param("salaryId") Long salaryId,
                           @Param("status") String status,
                           @Param("transactionId") Long transactionId);


    @Query("SELECT COUNT(s) FROM EmployeeSalary s " +
            "WHERE (:month IS NULL OR s.month = :month) " +
            "AND (:year IS NULL OR s.year = :year) " +
            "AND s.isDeleted = false " +
            "AND s.branchCode = :branchCode")
    long countAllSalaries(@Param("month") Integer month,
                          @Param("year") Integer year,
                          @Param("branchCode") String branchCode);

    @Query("SELECT COUNT(s) FROM EmployeeSalary s " +
            "WHERE s.status = 'Paid' " +
            "AND (:month IS NULL OR s.month = :month) " +
            "AND (:year IS NULL OR s.year = :year) " +
            "AND s.isDeleted = false " +
            "AND s.branchCode = :branchCode")
    long countPaidSalaries(@Param("month") Integer month,
                           @Param("year") Integer year,
                           @Param("branchCode") String branchCode);

    @Query("SELECT COUNT(s) FROM EmployeeSalary s " +
            "WHERE s.status = 'Pending' " +
            "AND (:month IS NULL OR s.month = :month) " +
            "AND (:year IS NULL OR s.year = :year) " +
            "AND s.isDeleted = false " +
            "AND s.branchCode = :branchCode")
    long countPendingSalaries(@Param("month") Integer month,
                              @Param("year") Integer year,
                              @Param("branchCode") String branchCode);

    @Query("SELECT COALESCE(SUM(s.finalNetSalary), 0) FROM EmployeeSalary s " +
            "WHERE (:month IS NULL OR s.month = :month) " +
            "AND (:year IS NULL OR s.year = :year) " +
            "AND s.isDeleted = false " +
            "AND s.branchCode = :branchCode")
    BigDecimal sumAllFinalNetSalary(@Param("month") Integer month,
                                    @Param("year") Integer year,
                                    @Param("branchCode") String branchCode);

    @Query("SELECT COALESCE(SUM(s.finalNetSalary), 0) FROM EmployeeSalary s " +
            "WHERE s.status = 'Paid' " +
            "AND (:month IS NULL OR s.month = :month) " +
            "AND (:year IS NULL OR s.year = :year) " +
            "AND s.isDeleted = false " +
            "AND s.branchCode = :branchCode")
    BigDecimal sumPaidFinalNetSalary(@Param("month") Integer month,
                                     @Param("year") Integer year,
                                     @Param("branchCode") String branchCode);

    @Query("SELECT COALESCE(SUM(s.finalNetSalary), 0) FROM EmployeeSalary s " +
            "WHERE s.status = 'Pending' " +
            "AND (:month IS NULL OR s.month = :month) " +
            "AND (:year IS NULL OR s.year = :year) " +
            "AND s.isDeleted = false " +
            "AND s.branchCode = :branchCode")
    BigDecimal sumPendingFinalNetSalary(@Param("month") Integer month,
                                        @Param("year") Integer year,
                                        @Param("branchCode") String branchCode);

    @Query("SELECT COALESCE(SUM(e.finalNetSalary), 0) " +
            "FROM EmployeeSalary e " +
            "WHERE e.month = :month AND e.year = :year " +
            "AND e.isDeleted = false " +
            "AND e.branchCode = :branchCode")
    BigDecimal getTotalNetSalaryByMonthAndYear(@Param("month") int month,
                                               @Param("year") int year,
                                               @Param("branchCode") String branchCode);

    @Query("SELECT COALESCE(SUM(e.finalNetSalary), 0) " +
            "FROM EmployeeSalary e " +
            "WHERE e.year = :year " +
            "AND e.isDeleted = false " +
            "AND e.branchCode = :branchCode")
    BigDecimal getTotalNetSalaryByYear(@Param("year") int year,
                                       @Param("branchCode") String branchCode);

    @Query("SELECT e.month, COALESCE(SUM(e.finalNetSalary), 0) " +
            "FROM EmployeeSalary e " +
            "WHERE e.year = :year " +
            "AND e.isDeleted = false " +
            "AND e.branchCode = :branchCode " +
            "GROUP BY e.month " +
            "ORDER BY e.branchCode ASC, e.month ASC")
    List<Object[]> getMonthlySalaryTotalsByYear(@Param("year") int year,
                                                @Param("branchCode") String branchCode);



    @Query("SELECT e.month, SUM(e.finalNetSalary) " +
            "FROM EmployeeSalary e " +
            "WHERE e.empId = :empId AND e.year = :year AND e.isDeleted = false " +
            "GROUP BY e.month")
    List<Object[]> getSalaryByYear(@Param("empId") Long empId, @Param("year") int year);


    @Query("SELECT e FROM EmployeeSalary e WHERE e.month = :month AND e.year = :year AND e.isDeleted = false")
    List<EmployeeSalary> findByMonthAndYear(int month, int year);
}
