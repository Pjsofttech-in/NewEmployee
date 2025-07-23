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
    public static Specification<EmployeeAttendence> build(
            AttendenceFilterDTO filter,
                String timeFrame,
                LocalDate startDate,
                LocalDate endDate
        ) {
            return (root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();

                if (filter != null) {
//                    if (filter.getEmpID() != null) {
//                        predicates.add(cb.equal(root.get("empID"), filter.getEmpID()));
//                    }

                    if (filter.getName() != null && !filter.getName().isBlank()) {
                        predicates.add(cb.like(cb.lower(root.get("name")), "%" + filter.getName().toLowerCase() + "%"));
                    }

                    if (filter.getStatus() != null && !filter.getStatus().isBlank()) {
                        predicates.add(cb.equal(cb.lower(root.get("status")), filter.getStatus().toLowerCase()));
                    }

                }

                LocalDate today = LocalDate.now();

                switch (timeFrame.toLowerCase()) {
                    case "today" -> predicates.add(cb.equal(root.get("todaysDate"), today));
                    case "7days" -> predicates.add(cb.between(root.get("todaysDate"), today.minusDays(6), today));
                    case "30days" -> predicates.add(cb.between(root.get("todaysDate"), today.minusDays(29), today));
                    case "365days" -> predicates.add(cb.between(root.get("todaysDate"), today.minusDays(364), today));
                    case "custom" -> {
                        if (startDate != null && endDate != null) {
                            predicates.add(cb.between(root.get("todaysDate"), startDate, endDate));
                        }
                    }
                    case "all" -> {} // no filter
                }

                return cb.and(predicates.toArray(new Predicate[0]));
            };
        }


}
