package com.NewEmployeeManagement.ServiceImpl;

import com.NewEmployeeManagement.DTO.EmployeeCountResponse;
import com.NewEmployeeManagement.Repository.EmployeeRepository;
import com.NewEmployeeManagement.Service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Override
    public EmployeeCountResponse getEmployeeCounts(String filter, LocalDate startDate, LocalDate endDate) {
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

        // Convert to LocalDateTime range
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        // Call updated methods
        List<Object[]> statusList = employeeRepository.countByStatusBetweenDates(startDateTime, endDateTime);
        List<Object[]> deptList = employeeRepository.countByDepartmentJoinedBetweenDates(startDateTime, endDateTime);
        List<Object[]> categoryList = employeeRepository.countByCategoryJoinedBetweenDates(startDateTime, endDateTime);

        Map<String, Long> statusMap = new HashMap<>();
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
