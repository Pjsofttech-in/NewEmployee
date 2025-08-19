package com.NewEmployeeManagement.Repository;

import com.NewEmployeeManagement.Entity.EmployeeAttendence;
import com.NewEmployeeManagement.Entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendenceRepository extends JpaRepository<EmployeeAttendence, Long> , JpaSpecificationExecutor<EmployeeAttendence> {

    boolean existsByEmployee_IdAndTodaysDateAndStatus(Long empid, LocalDate todaysDate, String status);

    Optional<EmployeeAttendence> findByEmailAndTodaysDate(String email, LocalDate todaysDate);

    // Custom Query to find all employee IDs marked present today
    @Query("SELECT e FROM EmployeeAttendence e WHERE e.branchCode = :branchCode ORDER BY e.id DESC")
    List<EmployeeAttendence> findAllByBranchCode(@Param("branchCode")String branchCode);

    @Query("SELECT a.email FROM EmployeeAttendence a WHERE a.todaysDate = :date AND a.branchCode = :branchCode AND (a.status = 'late' OR a.status = 'on time')")
    List<String> findEmailsByBranchCodeAndDate(@Param("date") LocalDate date, @Param("branchCode") String branchCode);

    @Query("SELECT a FROM EmployeeAttendence a WHERE a.todaysDate = :today AND a.status IN ('Late', 'On Time')")
    List<EmployeeAttendence> findPresentAttendances(@Param("today") LocalDate today);

    @Query("SELECT e FROM EmployeeAttendence e WHERE e.employee = :employee AND e.todaysDate = :date")
    Optional<EmployeeAttendence> findByEmployeeAndTodaysDate(@Param("employee") Employee employee,
                                                             @Param("date") LocalDate date);
    @Query("SELECT e FROM Employee e WHERE e.branchCode = :branchCode AND e.isDeleted = false AND e.id NOT IN (" +
            "SELECT a.employee.id FROM EmployeeAttendence a WHERE a.todaysDate = :today AND a.employee.branchCode = :branchCode)")
    List<Employee> findAbsentEmployees(@Param("today") LocalDate today, @Param("branchCode") String branchCode);


    @Query("SELECT a FROM EmployeeAttendence a WHERE a.employee.id = :empid")
    Page<EmployeeAttendence> findByEmployeeId(@Param("empid") Long empid, Pageable pageable);

    @Query("SELECT a FROM EmployeeAttendence a WHERE a.employee.id = :empid AND a.todaysDate BETWEEN :startDate AND :endDate")
    Page<EmployeeAttendence> findByEmployeeIdAndTodaysDateBetween(@Param("empid") Long empid,
                                                                  @Param("startDate") LocalDate startDate,
                                                                  @Param("endDate") LocalDate endDate,
                                                                  Pageable pageable);

    @Query("SELECT COUNT(e) FROM EmployeeAttendence e " +
            "WHERE e.employee.id = :empId " +
            "AND FUNCTION('MONTH', e.todaysDate) = :month " +
            "AND FUNCTION('YEAR', e.todaysDate) = :year")
    Long getAttendanceCountByEmpIdAndMonthYear(@Param("empId") Long empId, @Param("month") int month, @Param("year") int year);

}