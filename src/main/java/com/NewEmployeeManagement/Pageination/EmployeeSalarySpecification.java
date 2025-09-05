package com.NewEmployeeManagement.Pageination;

import com.NewEmployeeManagement.Entity.EmployeeSalary;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class EmployeeSalarySpecification
{
    public static Specification<EmployeeSalary> filterSalaries(Long empId, String fullName,
                                                               String department, String status, String employeecategory,
                                                               Integer month, Integer year,
                                                               String branchCode) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (branchCode != null) {
                predicates.add(cb.equal(root.get("branchCode"), branchCode));
            }

            if (empId != null) {
                predicates.add(cb.equal(root.get("empId"), empId));
            }
            if (fullName != null && !fullName.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("fullName")), "%" + fullName.toLowerCase() + "%"));
            }
            if (department != null && !department.isEmpty()) {
                predicates.add(cb.equal(root.get("department"), department));
            }
            if (status != null && !status.isEmpty()) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (employeecategory != null && !employeecategory.isEmpty()) {
                predicates.add(cb.equal(root.get("employeecategory"), employeecategory));
            }
            if (month != null) {
                predicates.add(cb.equal(root.get("month"), month));
            }
            if (year != null) {
                predicates.add(cb.equal(root.get("year"), year));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<EmployeeSalary> filterByEmpIdMonthYear(Long empId, Integer month, Integer year) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (empId != null) {
                predicates.add(cb.equal(root.get("empId"), empId));
            }
            if (month != null) {
                predicates.add(cb.equal(root.get("month"), month));
            }
            if (year != null) {
                predicates.add(cb.equal(root.get("year"), year));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
