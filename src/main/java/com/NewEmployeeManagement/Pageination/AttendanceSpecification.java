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
    public static Specification<EmployeeAttendence> build(AttendenceFilterDTO filter,
                                                          String timeFrame,
                                                          List<String> branchCodes,
                                                          LocalDate customStartDate,
                                                          LocalDate customEndDate) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Name filter
            if (filter != null && filter.getName() != null && !filter.getName().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + filter.getName().toLowerCase() + "%"));
            }

            // Status filter (honor "All" meaning no status filter)
            if (filter != null && filter.getStatus() != null && !filter.getStatus().isBlank()
                    && !"All".equalsIgnoreCase(filter.getStatus().trim())) {
                predicates.add(cb.equal(cb.lower(root.get("status")), filter.getStatus().toLowerCase().trim()));
            }

            // Branch handling: if branchCodes provided -> use IN predicate; otherwise skip branch predicate.
            if (branchCodes != null && !branchCodes.isEmpty()) {
                Path<String> branchPath = root.get("branchCode"); // adapt if your field name differs
                CriteriaBuilder.In<String> in = cb.in(branchPath);
                for (String bc : branchCodes) {
                    if (bc != null) in.value(bc);
                }
                predicates.add(in);
            }

            // Timeframe / date predicates
            LocalDate today = LocalDate.now();
            String tf = timeFrame != null ? timeFrame.toLowerCase() : "all";
            switch (tf) {
                case "today" -> predicates.add(cb.equal(root.get("todaysDate"), today));
                case "7days" -> predicates.add(cb.between(root.get("todaysDate"), today.minusDays(6), today));
                case "30days" -> predicates.add(cb.between(root.get("todaysDate"), today.minusDays(29), today));
                case "365days" -> predicates.add(cb.between(root.get("todaysDate"), today.minusDays(364), today));
                case "custom" -> {
                    if (customStartDate != null && customEndDate != null) {
                        predicates.add(cb.between(root.get("todaysDate"), customStartDate, customEndDate));
                    }
                }
                case "all" -> {
                    // no date predicate for "all"
                }
                default -> {
                    // unknown timeframe - treat as "all" (no predicate)
                }
            }

            // If no predicates added, return conjunction (no-op)
            if (predicates.isEmpty()) {
                return cb.conjunction();
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }



    public static Specification<EmployeeAttendence> build(AttendenceFilterDTO filter,
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

    public static Specification<EmployeeAttendence> branchCodesIn(List<String> branchCodes) {
        if (branchCodes == null || branchCodes.isEmpty()) return null;
        return (Root<EmployeeAttendence> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            Path<String> branchPath = root.get("branchCode"); // adapt field name if different
            CriteriaBuilder.In<String> in = cb.in(branchPath);
            for (String bc : branchCodes) {
                in.value(bc);
            }
            return in;
        };
    }
}