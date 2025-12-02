package com.NewEmployeeManagement.Pageination;

import com.NewEmployeeManagement.DTO.AttendenceFilterDTO;
import com.NewEmployeeManagement.Entity.EmployeeAttendence;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AttendanceSpecification
{
    public static Specification<EmployeeAttendence> build(
            AttendenceFilterDTO filter,
            String timeFrame,
            String branchCode,
            LocalDate customStartDate,
            LocalDate customEndDate) {

        List<String> branchCodes = null;
        if (branchCode != null && !branchCode.isBlank()) {
            branchCodes = List.of(branchCode);
        }
        return build(filter, timeFrame, branchCodes, customStartDate, customEndDate);
    }


    public static Specification<EmployeeAttendence> build(
            AttendenceFilterDTO filter,
            String timeFrame,
            List<String> branchCodes,
            LocalDate customStartDate,
            LocalDate customEndDate) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter != null && filter.getName() != null && !filter.getName().isBlank()) {
                predicates.add(
                        cb.like(cb.lower(root.get("name")), "%" + filter.getName().toLowerCase() + "%")
                );
            }

            if (filter != null && filter.getStatus() != null &&
                    !filter.getStatus().equalsIgnoreCase("All") &&
                    !filter.getStatus().equalsIgnoreCase("Absent")) {

                predicates.add(
                        cb.equal(cb.lower(root.get("status")), filter.getStatus().toLowerCase().trim())
                );
            }

            if (branchCodes != null && !branchCodes.isEmpty()) {
                CriteriaBuilder.In<String> inClause = cb.in(root.get("branchCode"));
                branchCodes.forEach(inClause::value);
                predicates.add(inClause);
            }

            LocalDate today = LocalDate.now();

            switch (timeFrame != null ? timeFrame.toLowerCase() : "all") {
                case "today" -> predicates.add(cb.equal(root.get("todaysDate"), today));
                case "7days" -> predicates.add(cb.between(root.get("todaysDate"), today.minusDays(6), today));
                case "30days" -> predicates.add(cb.between(root.get("todaysDate"), today.minusDays(29), today));
                case "365days" -> predicates.add(cb.between(root.get("todaysDate"), today.minusDays(364), today));
                case "custom" -> {
                    if (customStartDate != null && customEndDate != null) {
                        predicates.add(cb.between(root.get("todaysDate"), customStartDate, customEndDate));
                    }
                }
                case "all" -> {}
            }

            return predicates.isEmpty()
                    ? cb.conjunction()
                    : cb.and(predicates.toArray(new Predicate[0]));
        };
    }


    public static Specification<EmployeeAttendence> branchCodesIn(List<String> branchCodes) {
        if (branchCodes == null || branchCodes.isEmpty()) return null;

        return (root, query, cb) -> {
            CriteriaBuilder.In<String> inClause = cb.in(root.get("branchCode"));
            branchCodes.forEach(inClause::value);
            return inClause;
        };
    }
}