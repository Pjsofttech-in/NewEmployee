package com.NewEmployeeManagement.Repository;

import com.NewEmployeeManagement.Entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Integer>, JpaSpecificationExecutor<Employee> {

    @Query("SELECT e FROM Employee e WHERE e.branchCode = :branchCode AND e.isDeleted = false ORDER BY e.id DESC")
    List<Employee> findAllByBranchCode(@Param("branchCode")String branchCode);
    List<Employee> findAllByBranchCodeAndIsDeletedFalse(String branchCode);

    Optional<Employee> findByIdAndIsDeletedFalse(int id);

    List<Employee> findByIsDeletedFalse();

    Optional<Employee> findByEmpEmail(String empEmail);

    Optional<Employee> findBySystemName(String systemName);

    Optional<Employee> findByFullName(String fullName);

    @Query("SELECT e FROM Employee e JOIN e.employeeDocument d WHERE d.employeePhoto = :photo")
    Optional<Employee> findByEmployeePhoto(@Param("photo") String photo);

    List<Employee> findByBranchCodeAndStatus(String branchCode, String status);

}
