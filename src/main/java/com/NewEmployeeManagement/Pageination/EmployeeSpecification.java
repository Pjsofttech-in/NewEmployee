package com.NewEmployeeManagement.Pageination;

import com.NewEmployeeManagement.DTO.EmployeeFilterDTO;
import com.NewEmployeeManagement.Entity.Employee;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EmployeeSpecification
{
    public static Specification<Employee> build(EmployeeFilterDTO filter,
                                                String branchCode,
                                                String timeFrame,
                                                LocalDate startDate,
                                                LocalDate endDate) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Required filters
            if (branchCode != null && !branchCode.isEmpty()) {
                predicates.add(cb.equal(root.get("branchCode"), branchCode));
            }
            predicates.add(cb.equal(root.get("isDeleted"), false));

            // Optional filters
            if (filter != null) {
                if (filter.getDepartment() != null) {
                    predicates.add(cb.equal(root.get("department"), filter.getDepartment()));
                }
                if (filter.getCategoryName() != null) {
                    predicates.add(cb.equal(root.get("categoryName"), filter.getCategoryName()));
                }
                if (filter.getDesignation() != null) {
                    predicates.add(cb.like(cb.lower(root.get("designation")),
                            "%" + filter.getDesignation().toLowerCase() + "%"));
                }
                if (filter.getStatus() != null) {
                    predicates.add(cb.equal(root.get("status"), filter.getStatus()));
                }
                if (filter.getFullName() != null) {
                    predicates.add(cb.like(cb.lower(root.get("fullName")),
                            "%" + filter.getFullName().toLowerCase() + "%"));
                }
                if (filter.getDutyType() != null) {
                    predicates.add(cb.equal(root.get("dutyType"), filter.getDutyType()));
                }
                if (filter.getShift() != null) {
                    predicates.add(cb.equal(root.get("shift"), filter.getShift()));
                }
                if (filter.getEmployeeType() != null) {
                    predicates.add(cb.equal(root.get("employeeType"), filter.getEmployeeType()));
                }
            }

            // Time filtering logic
            LocalDate today = LocalDate.now();

            if (timeFrame != null && !timeFrame.equalsIgnoreCase("all")) {
                switch (timeFrame.toLowerCase()) {
                    case "today" -> predicates.add(cb.equal(root.get("joiningDate"), today));
                    case "7days" -> predicates.add(cb.between(root.get("joiningDate"),
                            today.minusDays(6), today));
                    case "30days" -> predicates.add(cb.between(root.get("joiningDate"),
                            today.minusDays(29), today));
                    case "365days" -> predicates.add(cb.between(root.get("joiningDate"),
                            today.minusDays(364), today));
                    case "custom" -> {
                        if (startDate != null && endDate != null) {
                            predicates.add(cb.between(root.get("joiningDate"), startDate, endDate));
                        }
                    }
                    default -> {
                        // Treat unknown timeFrame same as "all" (no date filtering)
                    }
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

}
