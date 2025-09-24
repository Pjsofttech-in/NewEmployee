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

    Optional<Employee> findByIdAndIsDeletedFalse(Long id);

    List<Employee> findByIsDeletedFalse();

    Optional<Employee> findByEmpEmail(String empEmail);

    boolean existsByEmpEmail(String empEmail);

    @Query("SELECT e FROM Employee e JOIN e.employeeDocument d WHERE d.employeePhoto = :photo")
    Optional<Employee> findByEmployeePhoto(@Param("photo") String photo);

    List<Employee> findByBranchCodeAndStatus(String branchCode, String status);

    @Query("SELECT e.status, COUNT(e) " +
            "FROM Employee e " +
            "WHERE e.isDeleted = false " +
            "AND e.createAt BETWEEN :startDate AND :endDate " +
            "AND e.branchCode = :branchCode " +
            "GROUP BY e.status")
    List<Object[]> countByStatusBetweenDatesAndBranchCode(LocalDateTime startDate, LocalDateTime endDate, String branchCode);


    @Query("SELECT e.department, COUNT(e) " +
            "FROM Employee e " +
            "WHERE e.status = 'Joined' " +
            "AND e.isDeleted = false " +
            "AND e.createAt BETWEEN :startDate AND :endDate " +
            "AND e.branchCode = :branchCode " +
            "GROUP BY e.department")
    List<Object[]> countByDepartmentJoinedBetweenDatesAndBranchCode(LocalDateTime startDate, LocalDateTime endDate, String branchCode);


    @Query("SELECT e.categoryName, COUNT(e) " +
            "FROM Employee e " +
            "WHERE e.status = 'Joined' " +
            "AND e.isDeleted = false " +
            "AND e.createAt BETWEEN :startDate AND :endDate " +
            "AND e.branchCode = :branchCode " +
            "GROUP BY e.categoryName")
    List<Object[]> countByCategoryJoinedBetweenDatesAndBranchCode(LocalDateTime startDate, LocalDateTime endDate, String branchCode);


    @Query("SELECT COUNT(e) FROM Employee e WHERE e.isDeleted = false AND e.createAt BETWEEN :startDate AND :endDate AND e.branchCode = :branchCode")
    Long countTotalEmployeesBetweenDatesAndBranchCode(LocalDateTime startDate, LocalDateTime endDate, String branchCode);


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
                  AND DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL 7 DAY), '%m-%d')
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


    @Query("SELECT e.status, COUNT(e) FROM Employee e " +
            "WHERE (:branchCode IS NULL OR e.branchCode = :branchCode) " +
            "AND (:startDate IS NULL OR e.joiningDate >= :startDate OR e.rejoiningData >= :startDate OR e.terminatDate >= :startDate) " +
            "AND (:endDate IS NULL OR e.joiningDate <= :endDate OR e.rejoiningData <= :endDate OR e.terminatDate <= :endDate) " +
            "GROUP BY e.status")
    List<Object[]> countByStatusWithBranch(@Param("branchCode") String branchCode,
                                           @Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);



    @Query("SELECT e.department, COUNT(e) FROM Employee e " +
            "WHERE (:branchCode IS NULL OR e.branchCode = :branchCode) " +
            "AND (:startDate IS NULL OR ((e.joiningDate >= :startDate AND e.status IN ('Joined','Rejoined')) " +
            "OR (e.rejoiningData >= :startDate AND e.status='Rejoined') " +
            "OR (e.terminatDate >= :startDate AND e.status='Terminated'))) " +
            "AND (:endDate IS NULL OR ((e.joiningDate <= :endDate AND e.status IN ('Joined','Rejoined')) " +
            "OR (e.rejoiningData <= :endDate AND e.status='Rejoined') " +
            "OR (e.terminatDate <= :endDate AND e.status='Terminated'))) " +
            "GROUP BY e.department")
    List<Object[]> countByDepartmentWithBranch(@Param("branchCode") String branchCode,
                                               @Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);

    @Query("SELECT e.categoryName, COUNT(e) FROM Employee e " +
            "WHERE (:branchCode IS NULL OR e.branchCode = :branchCode) " +
            "AND (:startDate IS NULL OR ((e.joiningDate >= :startDate AND e.status IN ('Joined','Rejoined')) " +
            "OR (e.rejoiningData >= :startDate AND e.status='Rejoined') " +
            "OR (e.terminatDate >= :startDate AND e.status='Terminated'))) " +
            "AND (:endDate IS NULL OR ((e.joiningDate <= :endDate AND e.status IN ('Joined','Rejoined')) " +
            "OR (e.rejoiningData <= :endDate AND e.status='Rejoined') " +
            "OR (e.terminatDate <= :endDate AND e.status='Terminated'))) " +
            "GROUP BY e.categoryName")
    List<Object[]> countByCategoryWithBranch(@Param("branchCode") String branchCode,
                                             @Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate);

}
