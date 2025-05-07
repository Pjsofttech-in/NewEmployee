package com.NewEmployeeManagement.Repository;

import com.NewEmployeeManagement.Entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Integer>, JpaSpecificationExecutor<Employee> {
    List<Employee> findAllByBranchCode(String branchCode);
    List<Employee> findAllByBranchCodeAndIsDeletedFalse(String branchCode);

    Optional<Employee> findByIdAndIsDeletedFalse(int id);
}
