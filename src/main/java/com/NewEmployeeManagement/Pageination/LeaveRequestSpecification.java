package com.NewEmployeeManagement.Pageination;

import com.NewEmployeeManagement.Entity.EmployeeLeaveRequest;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class LeaveRequestSpecification
{
    public static Specification<EmployeeLeaveRequest> build(String name, String status, List<String> branchCodes) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("isDeleted"), false));

            if (name != null && !name.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("fullName")), "%" + name.trim().toLowerCase() + "%"));
            }

            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("status")), status.trim().toLowerCase()));
            }

            if (branchCodes != null && !branchCodes.isEmpty()) {
                if (branchCodes.size() == 1) {
                    predicates.add(cb.equal(root.get("branchCode"), branchCodes.get(0)));
                } else {
                    CriteriaBuilder.In<String> inClause = cb.in(root.get("branchCode"));
                    for (String bc : branchCodes) {
                        inClause.value(bc);
                    }
                    predicates.add(inClause);
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
