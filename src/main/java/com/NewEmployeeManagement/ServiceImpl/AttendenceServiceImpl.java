package com.NewEmployeeManagement.ServiceImpl;

import com.NewEmployeeManagement.DTO.AttendanceSummaryDTO;
import com.NewEmployeeManagement.DTO.AttendenceFilterDTO;
import com.NewEmployeeManagement.DTO.EmployeeAttendanceDTO;
import com.NewEmployeeManagement.Entity.EmployeeAttendence;
import com.NewEmployeeManagement.Entity.Employee;
import com.NewEmployeeManagement.Pageination.AttendanceSpecification;
import com.NewEmployeeManagement.Repository.AttendenceRepository;
import com.NewEmployeeManagement.Repository.EmployeeRepository;
import com.NewEmployeeManagement.Service.AttendenceService;
import com.NewEmployeeManagement.Service.PermissionService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AttendenceServiceImpl implements AttendenceService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private AttendenceRepository attendenceRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PermissionService permissionService;

    @Override
    public String markEmployeeAttendanceFromFace(MultipartFile image, String branchCode) {
        try {
            String fastApiUrl = "https://pjsofttech.in:51443/auto-branch-scan";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("image", new ByteArrayResource(image.getBytes()) {
                @Override
                public String getFilename() {
                    return image.getOriginalFilename();
                }
            });
            body.add("branch_code", branchCode);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> response = restTemplate.postForEntity(fastApiUrl, requestEntity, Map.class);

            Map<String, Object> responseBody = response.getBody();

            if (responseBody == null || !"success".equals(responseBody.get("status"))) {
                return "Face recognition failed";
            }

            List<Map<String, Object>> matches = (List<Map<String, Object>>) responseBody.get("matches");
            if (matches == null || matches.isEmpty()) {
                return "No face match found";
            }

            Map<String, Object> firstMatch = matches.get(0);
            String empIdStr = String.valueOf(firstMatch.get("empid"));
            Long empId = Long.parseLong(empIdStr);

            LocalDate today = LocalDate.now();

            Employee employee = employeeRepository.findById(empId)
                    .orElseThrow(() -> new RuntimeException("Employee not found"));

            Optional<EmployeeAttendence> existingAttendance =
                    attendenceRepository.findByEmployeeAndTodaysDate(employee, today);
            if (existingAttendance.isPresent()) {
                return "Attendance already marked for: " + employee.getFullName() + " With empId :" + empId;
            }

            LocalTime loginTime = LocalTime.now();

            String shiftStartTimeStr = employee.getShiftStartTime(); // Example: "09:00"
            LocalTime shiftStartTime = null;
            LocalTime allowedTime = null;

            if (shiftStartTimeStr != null && !shiftStartTimeStr.isBlank()) {
                try {
                    shiftStartTime = LocalTime.parse(shiftStartTimeStr);
                    allowedTime = shiftStartTime.plusMinutes(5);
                } catch (Exception ex) {
                    // Log the parsing issue and fallback
                    System.err.println("Invalid shiftStartTime for empId " + empId + ": " + shiftStartTimeStr);
                }
            }

            String status;
            if (allowedTime != null) {
                status = loginTime.isAfter(allowedTime) ? "Late" : "OnTime";
            } else {

                status = "OnTime";
            }

            String dayName = LocalDate.now().getDayOfWeek().toString();

            // Create new attendance entry
            EmployeeAttendence attendance = new EmployeeAttendence();
            attendance.setEmployee(employee);
            attendance.setTodaysDate(today);
            attendance.setLoginTime(loginTime);
            attendance.setStatus(status);
            attendance.setDay(dayName.charAt(0) + dayName.substring(1).toLowerCase());
            attendance.setBranchCode(branchCode);
            attendance.setName(employee.getFullName());
            attendance.setEmail(employee.getEmpEmail());
            attendance.setShift(employee.getShift());
            attendance.setShiftStartTime(employee.getShiftStartTime());
            attendance.setShiftEndTime(employee.getShiftEndTime());

            attendenceRepository.save(attendance);

            return "Attendance marked for employee: " + employee.getFullName() + " With empId :" + empId;

        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to mark attendance: " + e.getMessage();
        }
    }

    @Override
    public String logoutEmployeeFromFace(MultipartFile image, String branchCode, String logoutIp) {
        try {
            String fastApiUrl = "https://pjsofttech.in:51443/auto-branch-scan";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("image", new ByteArrayResource(image.getBytes()) {
                @Override
                public String getFilename() {
                    return image.getOriginalFilename();
                }
            });
            body.add("branch_code", branchCode);
            body.add("classroom_scan", "false"); // Optional, set to false for employee only

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> response = restTemplate.postForEntity(fastApiUrl, requestEntity, Map.class);

            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null || !"success".equalsIgnoreCase((String) responseBody.get("status"))) {
                return "Face recognition failed or no success status from server.";
            }

            List<Map<String, Object>> matches = (List<Map<String, Object>>) responseBody.get("matches");
            if (matches == null || matches.isEmpty()) {
                return "No face match found";
            }

            Map<String, Object> firstMatch = matches.get(0);
            String empIdStr = String.valueOf(firstMatch.get("empid"));
            Long empId = Long.parseLong(empIdStr);

            Employee employee = employeeRepository.findById(empId)
                    .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + empId));

            LocalDate today = LocalDate.now();

            Optional<EmployeeAttendence> optional = attendenceRepository.findByEmployeeAndTodaysDate(employee, today);

            if (optional.isEmpty()) {
                return "No attendance record found for Emp ID: " + empId;
            }

            EmployeeAttendence attendance = optional.get();

            if (attendance.getLogoutTime() != null) {
                return "Already logged out for Emp ID: " + empId;
            }

            LocalTime logoutTime = LocalTime.now();
            attendance.setLogoutTime(logoutTime);
            attendance.setIP(logoutIp); // 👈 set logout IP (or system IP field if applicable)

            if (attendance.getLoginTime() != null) {
                Integer workedMinutes = Math.toIntExact(Duration.between(attendance.getLoginTime(), logoutTime).toMinutes());
                attendance.setTotalMinutesWorked(workedMinutes);
            }

            attendenceRepository.save(attendance);

            return "Logout successful for Emp ID: " + empId + " Name: " + attendance.getName();

        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to logout employee: " + e.getMessage();
        }
    }


    @Override
    public String breakInEmployeeFromFace(MultipartFile image, String branchCode) {
        try {
            String fastApiUrl = "https://pjsofttech.in:51443/auto-branch-scan";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("image", new ByteArrayResource(image.getBytes()) {
                @Override
                public String getFilename() {
                    return image.getOriginalFilename();
                }
            });
            body.add("branch_code", branchCode);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> response = restTemplate.postForEntity(fastApiUrl, requestEntity, Map.class);

            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null || !"success".equalsIgnoreCase((String) responseBody.get("status"))) {
                return "Face recognition failed or no success status.";
            }

            List<Map<String, Object>> matches = (List<Map<String, Object>>) responseBody.get("matches");
            if (matches == null || matches.isEmpty()) return "No face match found";

            Map<String, Object> match = matches.get(0);
            Long empId = Long.parseLong(String.valueOf(match.get("empid")));

            Employee employee = employeeRepository.findById(empId)
                    .orElseThrow(() -> new RuntimeException("Employee not found"));

            LocalDate today = LocalDate.now();
            Optional<EmployeeAttendence> optional = attendenceRepository.findByEmployeeAndTodaysDate(employee, today);

            if (optional.isEmpty()) return "Attendance not found for Emp ID: " + empId;

            EmployeeAttendence attendance = optional.get();

            if (attendance.getBreakIn() != null) return "Already breaked-in today";

            attendance.setBreakIn(LocalTime.now());
            attendenceRepository.save(attendance);

            return "Break-In recorded for Emp ID: " + empId + " Name: " + attendance.getName();

        } catch (Exception e) {
            e.printStackTrace();
            return "Break-In failed: " + e.getMessage();
        }
    }

    @Override
    public String breakOutEmployeeFromFace(MultipartFile image, String branchCode) {
        try {
            String fastApiUrl = "https://pjsofttech.in:51443/auto-branch-scan";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("image", new ByteArrayResource(image.getBytes()) {
                @Override
                public String getFilename() {
                    return image.getOriginalFilename();
                }
            });
            body.add("branch_code", branchCode);
            body.add("classroom_scan", "false");

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> response = restTemplate.postForEntity(fastApiUrl, requestEntity, Map.class);

            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null || !"success".equalsIgnoreCase((String) responseBody.get("status"))) {
                return "Face recognition failed or no success status.";
            }

            List<Map<String, Object>> matches = (List<Map<String, Object>>) responseBody.get("matches");
            if (matches == null || matches.isEmpty()) return "No face match found";

            Map<String, Object> match = matches.get(0);
            Long empId = Long.parseLong(String.valueOf(match.get("empid")));

            Employee employee = employeeRepository.findById(empId)
                    .orElseThrow(() -> new RuntimeException("Employee not found"));

            LocalDate today = LocalDate.now();
            Optional<EmployeeAttendence> optional = attendenceRepository.findByEmployeeAndTodaysDate(employee, today);

            if (optional.isEmpty()) return "Attendance not found for Emp ID: " + empId;

            EmployeeAttendence attendance = optional.get();

            if (attendance.getBreakIn() == null) return "Break-In not done yet";
            if (attendance.getBreakOut() != null) return "Already Breaked-Out";

            LocalTime breakOut = LocalTime.now();
            attendance.setBreakOut(breakOut);

            long breakMinutes = Duration.between(attendance.getBreakIn(), breakOut).toMinutes();
            attendance.setBreakMinutes(breakMinutes);

            attendenceRepository.save(attendance);

            return "Break-Out recorded for Emp ID: " + empId + " | Break Duration: " + breakMinutes + " min";

        } catch (Exception e) {
            e.printStackTrace();
            return "Break-Out failed: " + e.getMessage();
        }
    }

    @Override
    public Page<EmployeeAttendanceDTO> getFilteredEmployeeAttendance(
            AttendenceFilterDTO filterDTO,
            String role, String email,
            String timeFrame,
            LocalDate customStartDate,
            LocalDate customEndDate,
            Pageable pageable)
    {
        if (!permissionService.hasPermission(role, email, "Post")) {
            throw new AccessDeniedException("No permission to view Get Attendace");
        }

        // 1. Determine date range
        LocalDate today = LocalDate.now();
        LocalDate startDate = today;
        LocalDate endDate = today;

        String branchCode = permissionService.fetchBranchCode(role, email);

        switch (timeFrame != null ? timeFrame.toLowerCase() : "all") {
            case "today" -> {
                startDate = today;
                endDate = today;
            }
            case "7days" -> {
                startDate = today.minusDays(6);
                endDate = today;
            }
            case "30days" -> {
                startDate = today.minusDays(29);
                endDate = today;
            }
            case "365days" -> {
                startDate = today.minusDays(364);
                endDate = today;
            }
            case "custom" -> {
                if (customStartDate != null && customEndDate != null) {
                    startDate = customStartDate;
                    endDate = customEndDate;
                }
            }
            case "all" -> {
                startDate = employeeRepository.findEarliestJoiningDate()
                        .orElse(today.minusYears(1));
                endDate = today;
            }
        }


        // 2. Fetch employees
        List<Employee> employees = employeeRepository.findActiveEmployeesByBranchCodeAndJoiningDate(branchCode, endDate);

        // 3. Filter by name (optional)
        if (filterDTO != null && filterDTO.getName() != null && !filterDTO.getName().isBlank()) {
            String name = filterDTO.getName().toLowerCase();
            employees = employees.stream()
                    .filter(emp -> emp.getFullName() != null && emp.getFullName().toLowerCase().contains(name))
                    .collect(Collectors.toList());
        }

        // 4. Attendance from DB
        var spec = AttendanceSpecification.build(filterDTO, timeFrame, branchCode,customStartDate, customEndDate);
        List<EmployeeAttendence> attendances = attendenceRepository.findAll(spec);

        // 5. Map attendance
        Map<String, EmployeeAttendence> attendanceMap = attendances.stream()
                .collect(Collectors.toMap(
                        a -> a.getEmployee().getId() + "_" + a.getTodaysDate(),
                        a -> a
                ));

        List<EmployeeAttendanceDTO> combinedList = new ArrayList<>();

        // 6. Combine employee + attendance for each date
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            for (Employee emp : employees) {
                if (emp.getJoiningDate() != null && !date.isBefore(emp.getJoiningDate())) {
                    String key = emp.getId() + "_" + date;
                    EmployeeAttendence att = attendanceMap.get(key);

                    EmployeeAttendanceDTO dto;
                    if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
                        dto = new EmployeeAttendanceDTO(emp.getId(), emp.getFullName(), emp.getEmpEmail(), date,
                                "Sunday", null, null, null, null, 0L);
                    } else if (att != null) {
                        dto = new EmployeeAttendanceDTO(
                                att.getEmployee().getId(),
                                att.getName(),
                                att.getEmail(),
                                att.getTodaysDate(),
                                att.getStatus(),
                                att.getLoginTime(),
                                att.getLogoutTime(),
                                att.getBreakIn(),
                                att.getBreakOut(),
                                att.getBreakMinutes()
                        );
                    } else {
                        dto = new EmployeeAttendanceDTO(emp.getId(), emp.getFullName(), emp.getEmpEmail(), date,
                                "Absent", null, null, null, null, 0L);
                    }

                    combinedList.add(dto);
                }
            }
        }


        // 7. Filter by status
        if (filterDTO != null && filterDTO.getStatus() != null && !filterDTO.getStatus().equalsIgnoreCase("All")) {
            String status = filterDTO.getStatus().toLowerCase();
            combinedList = combinedList.stream()
                    .filter(dto -> dto.getStatus() != null && dto.getStatus().toLowerCase().equals(status))
                    .toList();
        }

        // 8. Pagination
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), combinedList.size());
        List<EmployeeAttendanceDTO> paged = (start < end) ? combinedList.subList(start, end) : List.of();


        return new PageImpl<>(paged, pageable, combinedList.size());
    }


    @Override
    public AttendanceSummaryDTO getTodayAttendanceSummary(String branchCode) {
        LocalDate today = LocalDate.now();

        // Fetch only present attendances (Late or On Time)
        List<EmployeeAttendence> presentAttendances = attendenceRepository.findPresentAttendances(today);

        List<String> presentNames = presentAttendances.stream()
                .filter(att -> att.getEmployee() != null
                        && !att.getEmployee().isDeleted()
                        && branchCode.equalsIgnoreCase(att.getEmployee().getBranchCode()))
                .map(att -> att.getEmployee().getFullName())
                .distinct() // Optional: if someone has multiple records
                .collect(Collectors.toList());

        // Get absent employees
        List<Employee> absentEmployees = attendenceRepository.findAbsentEmployees(today, branchCode);
        List<String> absentNames = absentEmployees.stream()
                .map(Employee::getFullName)
                .collect(Collectors.toList());

        return new AttendanceSummaryDTO(
                branchCode,
                presentNames.size(),
                presentNames,
                absentNames.size(),
                absentNames
        );
    }

    @Override
    public Page<EmployeeAttendence> getAttendanceByEmpId(Long empId, String role, String email,String timeFrame, LocalDate customStartDate, LocalDate customEndDate, Pageable pageable) {

        if (!permissionService.hasPermission(role, email, "Get")) {
            throw new AccessDeniedException("No permission to view Get Attendace");
        }

        LocalDate today = LocalDate.now();
        LocalDate startDate = null;
        LocalDate endDate = today;

        if (timeFrame == null || timeFrame.equalsIgnoreCase("all")) {
            return attendenceRepository.findByEmployeeId(empId, pageable);
        }

        switch (timeFrame.toLowerCase()) {
            case "today" -> {
                startDate = today;
            }
            case "7days" -> {
                startDate = today.minusDays(6);
            }
            case "30days" -> {
                startDate = today.minusDays(29);
            }
            case "365days" -> {
                startDate = today.minusDays(364);
            }
            case "custom" -> {
                if (customStartDate != null && customEndDate != null) {
                    startDate = customStartDate;
                    endDate = customEndDate;
                } else {
                    throw new IllegalArgumentException("Custom date range requires both start and end dates.");
                }
            }
            default -> throw new IllegalArgumentException("Invalid time frame value.");
        }

        return attendenceRepository.findByEmployeeIdAndTodaysDateBetween(empId, startDate, endDate, pageable);
    }

    @Override
    public Long getAttendanceCount(Long empId, int month, int year) {
        return attendenceRepository.getAttendanceCount(empId, month, year);
    }



    @Override
    public String markEmployeeAttendanceManually(List<Long> empIds, String role, String email)
    {
        if (!permissionService.hasPermission(role, email, "Get")) {
            throw new AccessDeniedException("No permission to view Get Attendace");
        }

        String branchCode = permissionService.fetchBranchCode(role, email);
        try {
            LocalDate today = LocalDate.now();
            LocalTime loginTime = LocalTime.now();
            String dayName = today.getDayOfWeek().toString();
            String formattedDay = dayName.charAt(0) + dayName.substring(1).toLowerCase();

            StringBuilder resultMessage = new StringBuilder();

            for (Long empId : empIds) {
                // Fetch employee
                Employee employee = employeeRepository.findById(empId)
                        .orElseThrow(() -> new RuntimeException("Employee not found with empId: " + empId));

                // Check if attendance already marked
                Optional<EmployeeAttendence> existingAttendance =
                        attendenceRepository.findByEmployeeAndTodaysDate(employee, today);

                if (existingAttendance.isPresent()) {
                    resultMessage.append("Attendance already marked for: ")
                            .append(employee.getFullName())
                            .append(" (empId: ").append(empId).append(")\n");
                    continue;
                }

                String shiftStartTimeStr = employee.getShiftStartTime(); // e.g. "09:00"
                LocalTime shiftStartTime = LocalTime.parse(shiftStartTimeStr);
                LocalTime allowedTime = shiftStartTime.plusMinutes(5);
                String status = loginTime.isAfter(allowedTime) ? "Late" : "OnTime";

                EmployeeAttendence attendance = new EmployeeAttendence();
                attendance.setEmployee(employee);
                attendance.setTodaysDate(today);
                attendance.setLoginTime(loginTime);
                attendance.setStatus(status);
                attendance.setDay(formattedDay);
                attendance.setBranchCode(branchCode);
                attendance.setName(employee.getFullName());
                attendance.setEmail(employee.getEmpEmail());
                attendance.setShift(employee.getShift());
                attendance.setShiftStartTime(employee.getShiftStartTime());
                attendance.setShiftEndTime(employee.getShiftEndTime());

                attendenceRepository.save(attendance);

                resultMessage.append("Attendance marked for: ")
                        .append(employee.getFullName())
                        .append(" (empId: ").append(empId).append(")\n");
            }

            return resultMessage.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to mark attendance: " + e.getMessage();
        }
    }


}