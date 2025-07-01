package com.NewEmployeeManagement.Repository;

import com.NewEmployeeManagement.Entity.Attendence;
import com.NewEmployeeManagement.Entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface AttendenceRepository extends JpaRepository<Attendence, Integer> , JpaSpecificationExecutor<Attendence> {

    boolean existsByEmployee_IdAndTodaysDateAndStatus(int empid, LocalDate todaysDate, String status);

    Optional<Attendence> findByEmailAndTodaysDate(String email, LocalDate todaysDate);

    // Custom Query to find all employee IDs marked present today
    @Query("SELECT e FROM Attendence e WHERE e.branchCode = :branchCode ORDER BY e.id DESC")
    List<Attendence> findAllByBranchCode(@Param("branchCode")String branchCode);

    @Query("SELECT a.email FROM Attendence a WHERE a.todaysDate = :date AND a.branchCode = :branchCode AND (a.status = 'late' OR a.status = 'on time')")
    List<String> findEmailsByBranchCodeAndDate(@Param("date") LocalDate date, @Param("branchCode") String branchCode);

    @Query("SELECT a FROM Attendence a WHERE a.todaysDate = :today AND a.status IN ('Late', 'On Time')")
    List<Attendence> findPresentAttendances(@Param("today") LocalDate today);

//    @Query("SELECT e.name FROM Employee e WHERE e.id NOT IN (SELECT a.employee.id FROM Attendence a WHERE a.todaysDate = :today)")
//    List<String> findAbsentEmployeeNames(@Param("today") LocalDate today);

    @Query("SELECT e FROM Employee e WHERE e.branchCode = :branchCode AND e.isDeleted = false AND e.id NOT IN (" +
            "SELECT a.employee.id FROM Attendence a WHERE a.todaysDate = :today AND a.employee.branchCode = :branchCode)")
    List<Employee> findAbsentEmployees(@Param("today") LocalDate today, @Param("branchCode") String branchCode);

}