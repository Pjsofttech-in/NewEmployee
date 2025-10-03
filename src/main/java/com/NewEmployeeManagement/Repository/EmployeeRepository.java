package com.NewEmployeeManagement.Repository;

import com.NewEmployeeManagement.DTO.BirthdayDTO;
import com.NewEmployeeManagement.DTO.EmployeeBirthdayDTO;
import com.NewEmployeeManagement.Entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {

    @Query("SELECT e FROM Employee e WHERE e.branchCode = :branchCode AND e.isDeleted = false ORDER BY e.id DESC")
    List<Employee> findAllByBranchCode(@Param("branchCode")String branchCode);

    @Query("SELECT e FROM Employee e WHERE e.branchCode = :branchCode AND e.isDeleted = false")
    List<Employee> findAllByBranchCodeAndIsDeletedFalse(String branchCode);


    @Query("SELECT e FROM Employee e WHERE e.empEmail = :email")
    Optional<Employee> findEmployeeByEmail(@Param("email") String email);

    Optional<Employee> findByIdAndIsDeletedFalse(Long id);

    List<Employee> findByIsDeletedFalse();

    Optional<Employee> findByEmpEmail(String empEmail);

    boolean existsByEmpEmail(String empEmail);

    @Query("SELECT e FROM Employee e JOIN e.employeeDocument d WHERE d.employeePhoto = :photo")
    Optional<Employee> findByEmployeePhoto(@Param("photo") String photo);

    List<Employee> findByBranchCodeAndStatus(String branchCode, String status);

    @Query("SELECT e FROM Employee e WHERE e.branchCode = :branchCode AND e.joiningDate <= :endDate AND e.isDeleted = false")
    List<Employee> findActiveEmployeesByBranchCodeAndJoiningDate(@Param("branchCode") String branchCode, @Param("endDate") LocalDate endDate);

    @Query("SELECT MIN(e.joiningDate) FROM Employee e WHERE e.isDeleted = false")
    Optional<LocalDate> findEarliestJoiningDate();

    @Query(value = """
            SELECT e.full_name as fullName, e.dob as dob, e.department as department, e.category_name as categoryName
            FROM employee e
            WHERE e.branch_code = :branchCode
              AND DATE_FORMAT(e.dob, '%m-%d') 
                  BETWEEN DATE_FORMAT(CURDATE(), '%m-%d') 
                  AND DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL 30 DAY), '%m-%d')
            """, nativeQuery = true)
    List<EmployeeBirthdayDTO> findUpcomingBirthdays(@Param("branchCode") String branchCode);


    @Query("""
        SELECT e FROM Employee e
        WHERE e.branchCode = :branchCode
          AND e.isDeleted = false
          AND (
               (e.joiningDate IS NOT NULL AND e.joiningDate <= :endDate)
               OR (e.rejoiningData IS NOT NULL AND e.rejoiningData <= :endDate)
          )
          AND (e.terminatDate IS NULL OR e.terminatDate >= :startDate)
        """)
    List<Employee> findEmployeesActiveBetween(@Param("branchCode") String branchCode,
                                              @Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);



}
