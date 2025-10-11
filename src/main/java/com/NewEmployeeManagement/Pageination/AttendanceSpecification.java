package com.NewEmployeeManagement.Pageination;

import com.NewEmployeeManagement.DTO.AttendenceFilterDTO;
import com.NewEmployeeManagement.Entity.EmployeeAttendence;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AttendanceSpecification
{
    public static Specification<EmployeeAttendence> build(AttendenceFilterDTO filter,
                                                          String timeFrame, String branchCode,
                                                          LocalDate customStartDate,
                                                          LocalDate customEndDate) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter != null) {
                if (filter.getName() != null && !filter.getName().isBlank()) {
                    predicates.add(cb.like(cb.lower(root.get("name")), "%" + filter.getName().toLowerCase() + "%"));
                }
//                if (filter.getStatus() != null && !filter.getStatus().equalsIgnoreCase("All")) {
//                    predicates.add(cb.equal(cb.lower(root.get("status")), filter.getStatus().toLowerCase()));
//                }
            }
            predicates.add(cb.equal(root.get("branchCode"), branchCode));

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

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}