package com.NewEmployeeManagement.ServiceImpl;

import com.NewEmployeeManagement.DTO.AttendanceDTO;
import com.NewEmployeeManagement.DTO.AttendanceSummaryDTO;
import com.NewEmployeeManagement.DTO.AttendenceFilterDTO;
import com.NewEmployeeManagement.DTO.EmployeeAttendanceDTO;
import com.NewEmployeeManagement.Entity.Employee;
import com.NewEmployeeManagement.Entity.EmployeeAttendence;
import com.NewEmployeeManagement.Pageination.AttendanceSpecification;
import com.NewEmployeeManagement.Repository.AttendenceRepository;
import com.NewEmployeeManagement.Repository.EmployeeRepository;
import com.NewEmployeeManagement.Repository.LeaveRequestRepository;
import com.NewEmployeeManagement.Service.AttendenceService;
import com.NewEmployeeManagement.Service.PermissionService;
import io.micrometer.common.util.StringUtils;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.net.InetAddress;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AttendenceServiceImpl implements AttendenceService {

    private final RestTemplate restTemplate = new RestTemplate();
    @Autowired
    LeaveRequestRepository leaveRequestRepository;
    @Autowired
    private AttendenceRepository attendenceRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private StaffService staffService;
    @Autowired
    private PermissionService permissionService;

    @Override
    public String markEmployeeAttendanceFromFace(MultipartFile image, AttendanceDTO reqDTO, String clientIp) {
        try {
            String fastApiUrl = "https://pjsofttech.in:51443/auto-branch-scan";

            String branchCode = reqDTO.getBranchCode();
            Long empId = reqDTO.getEmpId();
            String workMode = reqDTO.getWorkMode();

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

            Map<String, Object> firstMatch = matches.getFirst();
            String empIdStr = String.valueOf(firstMatch.get("empid"));
            Long detectedEmpId = Long.parseLong(empIdStr);

            LocalDate today = LocalDate.now();

            String locationVerify = verifyGeoLocation(reqDTO, empId, detectedEmpId);

            if (StringUtils.isBlank(locationVerify) || !"success".equalsIgnoreCase(locationVerify))
                return locationVerify;

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
            attendance.setSystemIP(InetAddress.getLocalHost().getHostAddress()); // server IP
            attendance.setIP(clientIp);

            attendance.setLatitude(reqDTO.getLatitude());
            attendance.setLongitude(reqDTO.getLongitude());
            attendance.setWorkMode(workMode);

            attendenceRepository.save(attendance);

            return "Attendance marked for employee: " + employee.getFullName() + " With empId :" + empId;

        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to mark attendance: " + e.getMessage();
        }
    }

    private String verifyGeoLocation(AttendanceDTO reqDTO, Long empId, Long detectedEmpId) {
        if (!empId.equals(detectedEmpId)) {
            return "Face recognition failed";
        }

        if (StringUtils.isNotBlank(reqDTO.getWorkMode()) && "WFO".equalsIgnoreCase(reqDTO.getWorkMode())) {
            String locationVerificationResult = staffService.verifyGeoLocationForAttendance(reqDTO);
            if (StringUtils.isBlank(locationVerificationResult) || !locationVerificationResult.equalsIgnoreCase("success")) {
                return "Employee attendance is not allowed from outside of company";
            }
        }
        return "Success";
    }

    @Override
    @Transactional
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
            body.add("classroom_scan", "false");

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> response = restTemplate.postForEntity(fastApiUrl, new HttpEntity<>(body, headers), Map.class);

            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null || !"success".equalsIgnoreCase(String.valueOf(responseBody.get("status")))) {
                return "Face recognition failed or no success status.";
            }

            List<Map<String, Object>> matches = (List<Map<String, Object>>) responseBody.get("matches");
            if (matches == null || matches.isEmpty()) {
                return "No face match found.";
            }

            Long empId = Long.parseLong(String.valueOf(matches.get(0).get("empid")));
            Employee employee = employeeRepository.findById(empId)
                    .orElseThrow(() -> new RuntimeException("Employee not found"));

            LocalDate today = LocalDate.now();
            EmployeeAttendence attendance = attendenceRepository.findByEmployeeAndTodaysDate(employee, today)
                    .orElseThrow(() -> new RuntimeException("No attendance found for Emp ID: " + empId));

            if (attendance.getLogoutTime() != null) {
                return "Already logged out for Emp ID: " + empId;
            }

            LocalTime logoutTime = LocalTime.now();
            attendance.setLogoutTime(logoutTime);
            attendance.setLogoutIP(logoutIp);

            if (attendance.getLoginTime() != null) {
                long workedMinutes;
                if (!logoutTime.isBefore(attendance.getLoginTime())) {
                    workedMinutes = Duration.between(attendance.getLoginTime(), logoutTime).toMinutes();
                } else {

                    workedMinutes = Duration.between(attendance.getLoginTime(), logoutTime.plusHours(24)).toMinutes();
                }
                if (attendance.getBreakMinutes() != null) {
                    workedMinutes -= attendance.getBreakMinutes();
                }
                if (workedMinutes < 0) workedMinutes = 0;
                attendance.setTotalMinutesWorked((int) workedMinutes);
            }

            if (attendance.getShiftStartTime() != null && attendance.getShiftEndTime() != null) {
                try {
                    LocalTime shiftStart = parseFlexibleTime(attendance.getShiftStartTime());
                    LocalTime shiftEnd = parseFlexibleTime(attendance.getShiftEndTime());

                    if (shiftEnd.isBefore(shiftStart) || shiftEnd.equals(shiftStart)) {
                        LocalTime endPlus12 = shiftEnd.plusHours(12);
                        if (endPlus12.isAfter(shiftStart)) {
                            shiftEnd = endPlus12;
                        } else {
                            shiftEnd = shiftEnd.plusHours(24);
                        }
                    }

                    long shiftMinutes = Duration.between(shiftStart, shiftEnd).toMinutes();
                    if (shiftMinutes < 0) shiftMinutes = 0;

                    attendance.setShiftMinutes(shiftMinutes);

                    long workedMinutes;
                    if (attendance.getLoginTime() != null && attendance.getLogoutTime() != null) {
                        LocalTime login = attendance.getLoginTime();
                        LocalTime logout = attendance.getLogoutTime();
                        if (!logout.isBefore(login)) {
                            workedMinutes = Duration.between(login, logout).toMinutes();
                        } else {
                            // logout next day
                            workedMinutes = Duration.between(login, logout.plusHours(24)).toMinutes();
                        }
                        if (attendance.getBreakMinutes() != null) {
                            workedMinutes -= attendance.getBreakMinutes();
                        }
                        if (workedMinutes < 0) workedMinutes = 0;
                    } else {
                        workedMinutes = attendance.getTotalMinutesWorked() != null ? attendance.getTotalMinutesWorked() : 0;
                    }

                    long overtime = workedMinutes - shiftMinutes;
                    attendance.setOverTime(Math.max(0, overtime));
                } catch (Exception e) {

                    System.err.println("Error parsing shift times for Emp ID " + empId + " : " + e.getMessage());
                    attendance.setShiftMinutes(0L);
                    attendance.setOverTime(0L);
                }
            } else {
                attendance.setOverTime(0L);
                if (attendance.getShiftMinutes() == null) attendance.setShiftMinutes(0L);
            }

            attendenceRepository.save(attendance);

            return "Logout successful for Emp ID: " + empId + " Name: " + attendance.getName();

        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to logout employee: " + e.getMessage();
        }
    }


    private LocalTime parseFlexibleTime(String time) {
        if (time == null || time.trim().isEmpty()) throw new RuntimeException("Time is empty");
        String t = time.trim();

        DateTimeFormatter[] formatters = new DateTimeFormatter[]{
                DateTimeFormatter.ofPattern("H:mm"),           // 9:30 or 09:30
                DateTimeFormatter.ofPattern("HH:mm"),          // 09:30
                DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH), // 9:30 AM
                DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH),
                DateTimeFormatter.ofPattern("H:mm:ss"),
                DateTimeFormatter.ofPattern("HH:mm:ss"),
                DateTimeFormatter.ofPattern("h:mm:ss a", Locale.ENGLISH),
                DateTimeFormatter.ofPattern("hh:mm:ss a", Locale.ENGLISH)
        };

        for (DateTimeFormatter f : formatters) {
            try {
                return LocalTime.parse(t, f);
            } catch (Exception ignored) {
            }
        }
        for (DateTimeFormatter f : formatters) {
            try {
                return LocalTime.parse(t.toUpperCase().replaceAll("\\.", ""), f);
            } catch (Exception ignored) {
            }
        }
        try {
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d{1,2}:\\d{2})").matcher(t);
            if (m.find()) {
                return LocalTime.parse(m.group(1), DateTimeFormatter.ofPattern("H:mm"));
            }
        } catch (Exception ignored) {
        }

        throw new RuntimeException("Unsupported time format: '" + time + "'");
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
            Pageable pageable) {
        if (!permissionService.hasPermission(role, email, "GET")) {
            throw new AccessDeniedException("No permission to view Get Attendance");
        }

        LocalDate today = LocalDate.now();
        LocalDate startDate = today;
        LocalDate endDate = today;

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
                startDate = employeeRepository.findEarliestJoiningDate().orElse(today.minusYears(1));
                endDate = today;
            }
        }

        boolean isSuperAdmin = role != null && role.equalsIgnoreCase("superadmin");

        List<String> branchCodeList = new ArrayList<>();
        String branchCodeForSpec = null;

        if (isSuperAdmin) {
            boolean exists = staffService.isClientEmailExist(email);
            if (!exists) {
                throw new AccessDeniedException("Institute email not found or no permission for SuperAdmin with this email");
            }

            if (filterDTO != null && filterDTO.getBranchCode() != null && !filterDTO.getBranchCode().isBlank()) {
                branchCodeList.add(filterDTO.getBranchCode());
                branchCodeForSpec = filterDTO.getBranchCode();
            } else {
                List<String> result = staffService.getBranchCodesByInstituteEmail(email);
                if (result != null) branchCodeList.addAll(result);

                if (branchCodeList.size() == 1) {
                    branchCodeForSpec = branchCodeList.get(0);
                } else {
                    branchCodeForSpec = null;
                }
            }
        } else {
            String branchCode = permissionService.fetchBranchCode(role, email);
            if (branchCode != null) branchCodeList.add(branchCode);

            branchCodeForSpec = branchCodeList.isEmpty() ? null : branchCodeList.get(0);
        }

        Map<Long, Employee> employeeMap = new LinkedHashMap<>();
        for (String bc : branchCodeList) {
            if (bc == null) continue;

            List<Employee> empList = employeeRepository.findActiveEmployeesByBranchCodeAndJoiningDate(bc, endDate);
            if (empList != null) {
                for (Employee e : empList) {
                    if (e != null && e.getId() != null) {
                        employeeMap.putIfAbsent(e.getId(), e);
                    }
                }
            }
        }

        if (branchCodeList.isEmpty()) {
            String fallbackBranch = permissionService.fetchBranchCode(role, email);
            if (fallbackBranch != null) {
                List<Employee> empList = employeeRepository.findActiveEmployeesByBranchCodeAndJoiningDate(fallbackBranch, endDate);
                if (empList != null) {
                    for (Employee e : empList) {
                        if (e != null && e.getId() != null) {
                            employeeMap.putIfAbsent(e.getId(), e);
                        }
                    }
                }
                branchCodeForSpec = fallbackBranch;
                branchCodeList.add(fallbackBranch);
            }
        }

        List<Employee> employees = new ArrayList<>(employeeMap.values());

        if (filterDTO != null && filterDTO.getName() != null && !filterDTO.getName().isBlank()) {
            String name = filterDTO.getName().toLowerCase();
            employees = employees.stream()
                    .filter(emp -> emp.getFullName() != null && emp.getFullName().toLowerCase().contains(name))
                    .collect(Collectors.toList());
        }

        var spec = AttendanceSpecification.build(filterDTO, timeFrame, branchCodeForSpec, customStartDate, customEndDate);

        if (branchCodeList != null && branchCodeList.size() > 1) {
            var branchSpec = AttendanceSpecification.branchCodesIn(branchCodeList);
            spec = (spec == null) ? branchSpec : spec.and(branchSpec);
        }

        List<EmployeeAttendence> attendances = attendenceRepository.findAll(spec);

        Map<String, EmployeeAttendence> attendanceMap = attendances.stream()
                .filter(a -> a.getEmployee() != null && a.getEmployee().getId() != null && a.getTodaysDate() != null)
                .collect(Collectors.toMap(
                        a -> a.getEmployee().getId().toString() + "_" + a.getTodaysDate(),
                        a -> a,
                        (existing, replacement) -> existing
                ));

        List<EmployeeAttendanceDTO> combinedList = new ArrayList<>();

        boolean filterAbsentOnly =
                filterDTO != null &&
                        filterDTO.getStatus() != null &&
                        filterDTO.getStatus().equalsIgnoreCase("Absent");

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            for (Employee emp : employees) {

                if (emp.getJoiningDate() != null && !date.isBefore(emp.getJoiningDate())) {

                    LocalDate terminatedDate = emp.getTerminatDate();
                    LocalDate rejoiningDate = emp.getRejoiningData();
                    String empStatus = emp.getStatus() != null ? emp.getStatus().trim().toLowerCase() : "";

                    if (terminatedDate != null && !"rejoined".equalsIgnoreCase(empStatus)
                            && date.isAfter(terminatedDate)) {
                        continue;
                    }
                    if ("rejoined".equalsIgnoreCase(empStatus)
                            && rejoiningDate != null
                            && date.isBefore(rejoiningDate)) {
                        continue;
                    }

                    String key = emp.getId() + "_" + date;
                    EmployeeAttendence att = attendanceMap.get(key);

                    EmployeeAttendanceDTO dto;

                    if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
                        dto = new EmployeeAttendanceDTO(emp.getId(), emp.getFullName(), emp.getEmpEmail(), date,
                                "Sunday", null, null, null, null, 0L);

                    } else if (att != null) {

                        // employee has real attendance
                        if (filterAbsentOnly) continue;  // FIX: do not include present data when filtering Absent

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
                        // employee has NO attendance record
                        // FIX: only include absent when filter == Absent OR no status filter
                        if (!filterAbsentOnly &&
                                filterDTO != null &&
                                filterDTO.getStatus() != null &&
                                !filterDTO.getStatus().equalsIgnoreCase("All")) {
                            continue;
                        }

                        dto = new EmployeeAttendanceDTO(
                                emp.getId(),
                                emp.getFullName(),
                                emp.getEmpEmail(),
                                date,
                                "Absent",
                                null, null, null, null, 0L
                        );
                    }

                    combinedList.add(dto);
                }
            }
        }


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
    public Map<String, Object> getAttendanceByEmpId(Long empId, String role, String email,
                                                    String timeFrame, LocalDate customStartDate,
                                                    LocalDate customEndDate, Pageable pageable) {

        if (!permissionService.hasPermission(role, email, "Get")) {
            throw new AccessDeniedException("No permission to view Get Attendance");
        }

        LocalDate today = LocalDate.now();
        LocalDate startDate = null;
        LocalDate endDate = today;

        if (timeFrame == null || timeFrame.equalsIgnoreCase("all")) {
            startDate = LocalDate.of(1970, 1, 1); // very old date
            endDate = today;
        } else {
            switch (timeFrame.toLowerCase()) {
                case "today" -> startDate = today;
                case "7days" -> startDate = today.minusDays(6);
                case "30days" -> startDate = today.minusDays(29);
                case "365days" -> startDate = today.minusDays(364);
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
        }

        // Fetch paginated attendance
        Page<EmployeeAttendence> attendancePage =
                attendenceRepository.findByEmployeeIdAndTodaysDateBetween(empId, startDate, endDate, pageable);

        // Count OnTime / Late
        long onTimeCount = attendenceRepository.countByEmployeeIdAndStatusIgnoreCaseAndTodaysDateBetween(
                empId, "OnTime", startDate, endDate);

        long lateCount = attendenceRepository.countByEmployeeIdAndStatusIgnoreCaseAndTodaysDateBetween(
                empId, "Late", startDate, endDate);

        // Count leave using date range
        long leaveCount = leaveRequestRepository.countByEmpIdAndDateRange(empId, startDate, endDate);

        // Wrap in a single response map
        Map<String, Object> response = new HashMap<>();
        response.put("attendance", attendancePage.getContent());
        response.put("onTimeCount", onTimeCount);
        response.put("lateCount", lateCount);
        response.put("leaveCount", leaveCount);
        response.put("totalPages", attendancePage.getTotalPages());
        response.put("totalElements", attendancePage.getTotalElements());
        response.put("currentPage", attendancePage.getNumber());

        return response;
    }


    @Override
    public Long getAttendanceCount(Long empId, int month, int year) {
        return attendenceRepository.getAttendanceCount(empId, month, year);
    }


    @Override
    public String markEmployeeAttendanceManually(List<Long> empIds, String role, String email) {
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