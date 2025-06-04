package com.NewEmployeeManagement.Pageination;


import com.NewEmployeeManagement.Entity.Employee;
import com.NewEmployeeManagement.Repository.EmployeeRepository;
import com.NewEmployeeManagement.Service.PermissionService;
import com.NewEmployeeManagement.ServiceImpl.StaffService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SpecializationService {
    private final EmployeeRepository employeeRepository;
    private final PermissionService permissionService;

    @Autowired
    private WebClient webClient;

    @Autowired
    private StaffService staffService;

    public Page<Employee> filterEmployees(String department, String categoryName, String designation,
                                          String status, String branchCode, String role, String email,
                                          String fullName, String joiningDateFilter,
                                          LocalDate startDate, LocalDate endDate,
                                          int page, int size) {

        if (!permissionService.hasPermission(role, email, "POST"))
            throw new AccessDeniedException("No permission");

        Specification<Employee> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Only employees who are not deleted and are currently joined
            predicates.add(cb.isFalse(root.get("isDeleted")));
            predicates.add(cb.equal(root.get("status"), "Joined"));

            // Filter by branchCode, department, etc.
            if (branchCode != null) predicates.add(cb.equal(root.get("branchCode"), branchCode));
            if (department != null) predicates.add(cb.equal(root.get("department"), department));
            if (categoryName != null) predicates.add(cb.equal(root.get("categoryName"), categoryName));
            if (designation != null) predicates.add(cb.equal(root.get("designation"), designation));
            if (status != null) predicates.add(cb.equal(root.get("status"), status));
            if (fullName != null && !fullName.trim().isEmpty())
                predicates.add(cb.like(cb.lower(root.get("fullName")), "%" + fullName.toLowerCase() + "%"));

            // Handle joining date filters
            if (joiningDateFilter != null) {
                LocalDate today = LocalDate.now();
                switch (joiningDateFilter.toLowerCase()) {
                    case "today" -> predicates.add(cb.equal(root.get("joiningDate"), today));
                    case "last7days" -> predicates.add(cb.between(root.get("joiningDate"), today.minusDays(6), today));
                    case "last30days" ->
                            predicates.add(cb.between(root.get("joiningDate"), today.minusDays(29), today));
                    case "last365days" ->
                            predicates.add(cb.between(root.get("joiningDate"), today.minusDays(364), today));
                    case "custom" -> {
                        if (startDate != null && endDate != null) {
                            predicates.add(cb.between(root.get("joiningDate"), startDate, endDate));
                        }
                    }
                }
            }

            query.orderBy(cb.desc(root.get("joiningDate"))); // Sort by joiningDate descending
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Pageable pageable = PageRequest.of(page, size);
        return employeeRepository.findAll(spec, pageable);
    }
}