package com.NewEmployeeManagement.ServiceImpl;

import com.NewEmployeeManagement.DTO.AttendanceSummaryDTO;
import com.NewEmployeeManagement.DTO.AttendenceFilterDTO;
import com.NewEmployeeManagement.Entity.EmployeeAttendence;
import com.NewEmployeeManagement.Entity.Employee;
import com.NewEmployeeManagement.Pageination.AttendanceSpecification;
import com.NewEmployeeManagement.Repository.AttendenceRepository;
import com.NewEmployeeManagement.Repository.EmployeeRepository;
import com.NewEmployeeManagement.Service.AttendenceService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AttendenceServiceImpl implements AttendenceService {

    @Value("${employee.attendance.python-api-url}")
    private String pythonApiUrl;

    @Value("${employee.attendance-logout.python-api-url}")
    private String pythonLogoutApiUrl;

    @Value("${employee.attendance-breakin.python-api-url}")
    private String pythonBreakInApiUrl;

    @Value("${employee.attendance-breakout.python-api-url}")
    private String pythonBreakOutApiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private AttendenceRepository attendenceRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public String markEmployeeAttendanceFromFace(MultipartFile image, String branchCode) {
        try {
            String fastApiUrl = "https://pjsofttech.in:51443/auto-branch-scan";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // Prepare multipart body
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

            // Fetch Employee
            Employee employee = employeeRepository.findById(empId)
                    .orElseThrow(() -> new RuntimeException("Employee not found"));

            // Check if attendance already marked
            Optional<EmployeeAttendence> existingAttendance = attendenceRepository.findByEmployeeAndTodaysDate(employee, today);
            if (existingAttendance.isPresent()) {
                return "Attendance already marked for: " + employee.getFullName() +"With empId :" + empId;
            }

            LocalTime loginTime = LocalTime.now();
            String shiftStartTimeStr = employee.getShiftStartTime(); // Example: "09:00"
            LocalTime shiftStartTime = LocalTime.parse(shiftStartTimeStr); // Assumes correct format
            LocalTime allowedTime = shiftStartTime.plusMinutes(5);
            String status = loginTime.isAfter(allowedTime) ? "Late" : "OnTime";

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

            return "Attendance marked for employee: " + employee.getFullName() + "With empId :" + empId ;

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

            // 🔍 Fetch the Employee object first
            Employee employee = employeeRepository.findById(empId)
                    .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + empId));

            LocalDate today = LocalDate.now();

            // ✅ Use the correct repository method with Employee object
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
    public Page<EmployeeAttendence> getFilteredAttendance(AttendenceFilterDTO filter, String timeFrame, LocalDate startDate, LocalDate endDate, Pageable pageable) {

        if (timeFrame == null || timeFrame.isBlank()) {
            timeFrame = "all";
        }

        Specification<EmployeeAttendence> spec =
                AttendanceSpecification.build(filter, timeFrame, startDate, endDate);

        return attendenceRepository.findAll(spec, pageable);
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

}