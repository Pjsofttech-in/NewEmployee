package com.NewEmployeeManagement.Pageination;


import com.NewEmployeeManagement.Entity.Attendence;
import com.NewEmployeeManagement.Entity.Employee;
import com.NewEmployeeManagement.Repository.AttendenceRepository;
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
    private final AttendenceRepository attendenceRepository;

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

    public Page<Attendence> filterAttendence(String status,
                                             String todaysDateFilter,
                                             LocalDate startDate,
                                             LocalDate endDate,
                                             String branchCode,
                                             int page,
                                             int size) {

        LocalDate today = LocalDate.now();
        final LocalDate fromDate;
        final LocalDate toDate;

        if (todaysDateFilter != null) {
            switch (todaysDateFilter.toLowerCase()) {
                case "today" -> {
                    fromDate = today;
                    toDate = today;
                }
                case "yesterday" -> {
                    fromDate = today.minusDays(1);
                    toDate = today.minusDays(1);
                }
                case "last7days" -> {
                    fromDate = today.minusDays(6);
                    toDate = today;
                }
                case "last30days" -> {
                    fromDate = today.minusDays(29);
                    toDate = today;
                }
                case "last365days" -> {
                    fromDate = today.minusDays(364);
                    toDate = today;
                }
                case "custom" -> {
                    if (startDate != null && endDate != null) {
                        fromDate = startDate;
                        toDate = endDate;
                    } else {
                        fromDate = today;
                        toDate = today;
                    }
                }
                default -> {
                    fromDate = today;
                    toDate = today;
                }
            }
        } else {
            fromDate = today;
            toDate = today;
        }


        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "todaysDate"));

        if ("present".equalsIgnoreCase(status)) {
            Specification<Attendence> spec = (root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(cb.between(root.get("todaysDate"), fromDate, toDate));
                predicates.add(cb.equal(root.get("branchCode"), branchCode));
                predicates.add(cb.or(
                        cb.equal(root.get("status"), "late"),
                        cb.equal(root.get("status"), "on time")
                ));
                query.orderBy(cb.asc(root.get("status")));
                return cb.and(predicates.toArray(new Predicate[0]));
            };
            return attendenceRepository.findAll(spec, pageable);
        }

        if ("absent".equalsIgnoreCase(status)) {
            final List<Employee> joinedEmployees = employeeRepository.findByBranchCodeAndStatus(branchCode, "Joined");
            final List<String> presentEmails = attendenceRepository.findEmailsByBranchCodeAndDate(fromDate, branchCode);

            List<Attendence> absentRecords = joinedEmployees.stream()
                    .filter(emp -> !presentEmails.contains(emp.getEmpEmail()))
                    .map(emp -> {
                        Attendence att = new Attendence();
                        att.setEmail(emp.getEmpEmail());
                        att.setName(emp.getFullName());
                        att.setTodaysDate(fromDate);
                        att.setStatus("absent");
                        att.setBranchCode(emp.getBranchCode());
                        att.setEmployee(emp);
                        return att;
                    })
                    .toList();

            int start = Math.min(page * size, absentRecords.size());
            int end = Math.min(start + size, absentRecords.size());
            List<Attendence> pageContent = absentRecords.subList(start, end);

            return new org.springframework.data.domain.PageImpl<>(pageContent, pageable, absentRecords.size());
        }

        Specification<Attendence> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) predicates.add(cb.equal(root.get("status"), status));
            if (branchCode != null) predicates.add(cb.equal(root.get("branchCode"), branchCode));
            predicates.add(cb.between(root.get("todaysDate"), fromDate, toDate));
            query.orderBy(cb.asc(root.get("status")));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return attendenceRepository.findAll(spec, pageable);
    }


}