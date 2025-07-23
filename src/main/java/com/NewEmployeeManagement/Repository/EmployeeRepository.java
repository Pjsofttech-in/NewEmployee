package com.NewEmployeeManagement.Repository;

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

    @Query("SELECT e FROM Employee e JOIN e.employeeDocument d WHERE d.employeePhoto = :photo")
    Optional<Employee> findByEmployeePhoto(@Param("photo") String photo);

    List<Employee> findByBranchCodeAndStatus(String branchCode, String status);

    @Query("SELECT e.status, COUNT(e) FROM Employee e WHERE e.isDeleted = false AND e.createAt BETWEEN :startDate AND :endDate GROUP BY e.status")
    List<Object[]> countByStatusBetweenDates(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT e.department, COUNT(e) FROM Employee e WHERE e.status = 'Joined' AND e.isDeleted = false AND e.createAt BETWEEN :startDate AND :endDate GROUP BY e.department")
    List<Object[]> countByDepartmentJoinedBetweenDates(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT e.categoryName, COUNT(e) FROM Employee e WHERE e.status = 'Joined' AND e.isDeleted = false AND e.createAt BETWEEN :startDate AND :endDate GROUP BY e.categoryName")
    List<Object[]> countByCategoryJoinedBetweenDates(LocalDateTime startDate, LocalDateTime endDate);

}
