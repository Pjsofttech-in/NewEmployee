package com.NewEmployeeManagement.ServiceImpl;

import com.NewEmployeeManagement.DTO.EmployeeCountResponse;
import com.NewEmployeeManagement.Repository.EmployeeRepository;
import com.NewEmployeeManagement.Service.DashboardService;
import com.NewEmployeeManagement.Service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardServiceImpl implements DashboardService
{

    @Autowired
    EmployeeRepository employeeRepository;

    @Autowired
    PermissionService permissionService;

    @Override
    public EmployeeCountResponse getEmployeeCounts(String role, String email,String filter, LocalDate startDate, LocalDate endDate)
    {
        if (!permissionService.hasPermission(role, email, "GET")) {
            throw new AccessDeniedException("No permission to view Get Count");
        }
        String branchCode = permissionService.fetchBranchCode(role, email);
        LocalDate now = LocalDate.now();
        switch (filter.toLowerCase()) {
            case "today" -> {
                startDate = now;
                endDate = now;
            }
            case "7days" -> {
                startDate = now.minusDays(6);
                endDate = now;
            }
            case "30days" -> {
                startDate = now.minusDays(29);
                endDate = now;
            }
            case "365days" -> {
                startDate = now.minusDays(364);
                endDate = now;
            }
            case "all" -> {
                startDate = LocalDate.of(2000, 1, 1);
                endDate = now;
            }
            case "custom" -> {
                if (startDate == null || endDate == null)
                    throw new IllegalArgumentException("Custom filter requires startDate and endDate.");
            }
            default -> throw new IllegalArgumentException("Invalid filter type: " + filter);
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        Long total = employeeRepository.countTotalEmployeesBetweenDatesAndBranchCode(startDateTime, endDateTime, branchCode);

        List<Object[]> statusList = employeeRepository.countByStatusBetweenDatesAndBranchCode(startDateTime, endDateTime,branchCode);
        List<Object[]> deptList = employeeRepository.countByDepartmentJoinedBetweenDatesAndBranchCode(startDateTime, endDateTime,branchCode);
        List<Object[]> categoryList = employeeRepository.countByCategoryJoinedBetweenDatesAndBranchCode(startDateTime, endDateTime,branchCode);

        Map<String, Long> statusMap = new HashMap<>();
        statusMap.put("Joined", 0L);
        statusMap.put("Terminated", 0L);

        for (Object[] obj : statusList) {
            String status = (String) obj[0];
            Long count = (Long) obj[1];
            statusMap.put(status, count);
        }

        statusMap.put("total", total);

        Map<String, Long> deptMap = new HashMap<>();
        Map<String, Long> categoryMap = new HashMap<>();

        statusList.forEach(obj -> statusMap.put((String) obj[0], (Long) obj[1]));
        deptList.forEach(obj -> deptMap.put((String) obj[0], (Long) obj[1]));
        categoryList.forEach(obj -> categoryMap.put((String) obj[0], (Long) obj[1]));

        EmployeeCountResponse response = new EmployeeCountResponse();
        response.setStatusCounts(statusMap);
        response.setDepartmentCounts(deptMap);
        response.setCategoryCounts(categoryMap);

        return response;
    }

}
