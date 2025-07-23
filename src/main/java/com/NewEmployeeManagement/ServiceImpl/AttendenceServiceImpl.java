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
    public ResponseEntity<String> markAttendance(MultipartFile image, String branchCode, String systemName, HttpServletRequest request, String workType) {

        try {
            if (!("Work From Home".equalsIgnoreCase(workType) || "Work From Office".equalsIgnoreCase(workType))) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Invalid work type. Please select either 'Work From Home' or 'Work From Office'.");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        try {
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
            body.add("system_name", systemName);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(pythonApiUrl, requestEntity, String.class);

            String responseBody = response.getBody();
            System.out.println("Python API response: " + responseBody);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(responseBody);

            String status = root.path("status").asText(null);
            String empIdStr = root.path("empid").asText(null);

            if (!"success".equalsIgnoreCase(status) || empIdStr == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Face not recognized or missing employee ID.");
            }

            Long empId = Long.parseLong(empIdStr);
            Optional<Employee> employeeOpt = employeeRepository.findById(empId);
            if (employeeOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Employee not found for ID: " + empId);
            }

            Employee employee = employeeOpt.get();
            if (employee.isDeleted()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Employee not found for ID: " + empId);
            }

            if (attendenceRepository.existsByEmployee_IdAndTodaysDateAndStatus(employee.getId(), LocalDate.now(), "login")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Attendance already marked for today.");
            }

            LocalTime loginTime = LocalTime.now(ZoneId.of("Asia/Kolkata"));

            EmployeeAttendence attendance = new EmployeeAttendence();
            attendance.setName(employee.getFullName());
            attendance.setTodaysDate(LocalDate.now());
            attendance.setLoginTime(loginTime);
            attendance.setSystemName(systemName);
            attendance.setIP(request.getRemoteAddr());
            attendance.setShift(employee.getShift());
            attendance.setEmployee(employee);
            attendance.setShiftStartTime(employee.getShiftStartTime());
            attendance.setShiftEndTime(employee.getShiftEndTime());
            attendance.setBranchCode(employee.getBranchCode());
            attendance.setEmail(employee.getEmpEmail());
            attendance.setWorkType(workType);

            try {
                String shiftStart = employee.getShiftStartTime(); // e.g., "09:30"
                if (shiftStart != null && !shiftStart.isEmpty()) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                    LocalTime shiftStartTime = LocalTime.parse(shiftStart, formatter);
                    if (loginTime.isAfter(shiftStartTime)) {
                        attendance.setStatus("Late");
                    } else {
                        attendance.setStatus("On Time");
                    }
                } else {
                    attendance.setStatus("N/A");
                }
            } catch (Exception e) {
                attendance.setStatus("Error");
                System.err.println("Late mark calculation failed: " + e.getMessage());
            }

            attendenceRepository.save(attendance);

            return ResponseEntity.ok("Attendance marked successfully for: " + employee.getFullName());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error while marking attendance: " + e.getMessage());
        }
    }


    @Override
    public EmployeeAttendence markLogout(MultipartFile image, String branchCode, String systemName, HttpServletRequest request) {
        try {
            // Call Python API
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("image", new ByteArrayResource(image.getBytes()) {
                @Override
                public String getFilename() {
                    return image.getOriginalFilename();
                }
            });
            body.add("system_name", systemName);
            body.add("branch_code", branchCode);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(pythonLogoutApiUrl, requestEntity, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Python API logout failed with status: " + response.getStatusCode());
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());

            String status = root.path("status").asText(null);
            String empIdStr = root.path("empid").asText(null);
            String logoutTimeStr = root.path("logout_time").asText(null);

            if (!"success".equalsIgnoreCase(status) || empIdStr == null) {
                throw new RuntimeException("Logout failed: Face not recognized or missing employee ID.");
            }

            Long empId = Long.parseLong(empIdStr);
            Optional<Employee> employeeOpt = employeeRepository.findById(empId);
            if (employeeOpt.isEmpty()) {
                throw new RuntimeException("Employee not found for ID: " + empId);
            }

            Employee employee = employeeOpt.get();

            // Fetch today's attendance record by employee ID and date
            EmployeeAttendence attendance = attendenceRepository.findByEmailAndTodaysDate(employee.getEmpEmail(), LocalDate.now())
                    .orElseThrow(() -> new RuntimeException("Attendance not found for today."));

            if (attendance.getLogoutTime() != null) {
                throw new RuntimeException("Logout already marked for today.");
            }

            // Use provided logout time or current time
            LocalTime logoutTime = (logoutTimeStr != null && !logoutTimeStr.isEmpty())
                    ? LocalTime.parse(logoutTimeStr)
                    : LocalTime.now(ZoneId.of("Asia/Kolkata"));

            attendance.setLogoutTime(logoutTime);
            attendance.setLogoutIP(request.getRemoteAddr());
            attendance.setStatus("Logout");

            // Calculate shift duration
            if (attendance.getLoginTime() != null) {
                long minutes = ChronoUnit.MINUTES.between(attendance.getLoginTime(), logoutTime);
                attendance.setShiftMinutes((int) minutes);
            } else {
                attendance.setShiftMinutes(0);
            }

            // After setting attendance.setShiftMinutes(...)
            // After calculating actual shiftMinutes
            if (attendance.getShiftStartTime() != null && attendance.getShiftEndTime() != null) {
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                    LocalTime shiftStart = LocalTime.parse(attendance.getShiftStartTime(), formatter);
                    LocalTime shiftEnd = LocalTime.parse(attendance.getShiftEndTime(), formatter);

                    long expectedShiftMinutes = ChronoUnit.MINUTES.between(shiftStart, shiftEnd);
                    long actualShiftMinutes = attendance.getShiftMinutes() != null ? attendance.getShiftMinutes() : 0;

                    // Set overTime
                    long overtime = actualShiftMinutes > expectedShiftMinutes
                            ? actualShiftMinutes - expectedShiftMinutes
                            : 0;
                    attendance.setOverTime(overtime);

                    // Calculate day
                    if (actualShiftMinutes >= expectedShiftMinutes) {
                        attendance.setDay(1.0);
                    } else if (actualShiftMinutes >= (expectedShiftMinutes - 30)) {
                        attendance.setDay(0.5);
                    } else {
                        attendance.setDay(0.0);
                    }

                } catch (Exception ex) {
                    System.err.println("Error parsing shift times for overtime/day: " + ex.getMessage());
                    attendance.setOverTime(0L);
                    attendance.setDay(0.0);
                }
            } else {
                // If shift times are not set
                attendance.setDay(0.0);
            }



            return attendenceRepository.save(attendance);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error during logout: " + e.getMessage());
        }

    }

    @Override
    public ResponseEntity<String> markBreakIn(MultipartFile image, String branchCode, String systemName, HttpServletRequest request) {
        try {
            // Prepare request for Python API
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
            body.add("system_name", systemName);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // Call Python API
            ResponseEntity<String> response = restTemplate.postForEntity(pythonBreakInApiUrl, requestEntity, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Python break-in API failed.");
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());

            String status = root.path("status").asText(null);
            String empIdStr = root.path("empid").asText(null);

            if (!"success".equalsIgnoreCase(status) || empIdStr == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Break-In failed: Face not recognized or employee ID missing.");
            }

            Long empId = Long.parseLong(empIdStr);
            Optional<Employee> employeeOpt = employeeRepository.findById(empId);
            if (employeeOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Employee not found for ID: " + empId);
            }

            Employee employee = employeeOpt.get();

            EmployeeAttendence attendance = attendenceRepository.findByEmailAndTodaysDate(employee.getEmpEmail(), LocalDate.now())
                    .orElseThrow(() -> new RuntimeException("Attendance record not found for today."));

            if (attendance.getBreakIn() != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Break-In already marked for today.");
            }

            // Set current time as Break-In time
            LocalTime breakInTime = LocalTime.now(ZoneId.of("Asia/Kolkata"));
            attendance.setBreakIn(breakInTime);

            attendenceRepository.save(attendance);

            return ResponseEntity.ok("Break-In time recorded successfully for employee ID: " + empId);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error during Break-In: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<String> markBreakOut(MultipartFile image, String branchCode, String systemName, HttpServletRequest request) {
        try {
            // Prepare request to Python API
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
            body.add("system_name", systemName);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // Call Python API
            ResponseEntity<String> response = restTemplate.postForEntity(pythonBreakOutApiUrl, requestEntity, String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Python Break-Out API failed.");
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());

            String status = root.path("status").asText(null);
            String empIdStr = root.path("empid").asText(null);

            if (!"success".equalsIgnoreCase(status) || empIdStr == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Face not recognized or employee ID missing.");
            }

            Long empId = Long.parseLong(empIdStr);

            Optional<Employee> employeeOpt = employeeRepository.findById(empId);
            if (employeeOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Employee not found for ID: " + empId);
            }

            Employee employee = employeeOpt.get();
            EmployeeAttendence attendance = attendenceRepository.findByEmailAndTodaysDate(employee.getEmpEmail(), LocalDate.now())
                    .orElseThrow(() -> new RuntimeException("Attendance record not found for today."));

            if (attendance.getBreakIn() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Break-In must be marked before Break-Out.");
            }

            if (attendance.getBreakOut() != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Break-Out already marked for today.");
            }

            LocalTime breakOutTime = LocalTime.now(ZoneId.of("Asia/Kolkata"));
            attendance.setBreakOut(breakOutTime);

            // Calculate break minutes
            long breakMinutes = ChronoUnit.MINUTES.between(attendance.getBreakIn(), breakOutTime);
            attendance.setBreakMinutes(breakMinutes);

            attendenceRepository.save(attendance);
            return ResponseEntity.ok("Break-Out marked successfully. Break duration: " + breakMinutes + " minutes.");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error during Break-Out: " + e.getMessage());
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