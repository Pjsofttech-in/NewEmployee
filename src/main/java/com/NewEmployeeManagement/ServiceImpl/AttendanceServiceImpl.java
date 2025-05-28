package com.NewEmployeeManagement.ServiceImpl;

import com.NewEmployeeManagement.Entity.Attendence;
import com.NewEmployeeManagement.Entity.Employee;
import com.NewEmployeeManagement.Repository.AttendenceRepository;
import com.NewEmployeeManagement.Repository.EmployeeRepository;
import com.NewEmployeeManagement.Service.AttendenceService;
import com.NewEmployeeManagement.Service.EmployeeService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AttendanceServiceImpl implements AttendenceService {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private AttendenceRepository attendenceRepo;

    @Override
    public String handleFaceRecognitionAndAttendance(MultipartFile selfieImage, HttpServletRequest request) {
        try {
            // Step 1: Call Face Recognition API
            String apiUrl = "https://pjsofttech.in:38443/login";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(selfieImage.getBytes()) {
                @Override
                public String getFilename() {
                    return "selfie.jpg";
                }
            });

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.POST, requestEntity, Map.class);

            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                throw new RuntimeException("Face recognition API returned invalid response.");
            }

            Map<String, Object> result = response.getBody();
            if (!"success".equals(result.get("status"))) {
                throw new AccessDeniedException("Face recognition failed: No match found");
            }

            Map<String, Object> matchMap = (Map<String, Object>) result.get("match");
            Object empIdObj = matchMap.get("emp_id");
            if (empIdObj == null) {
                throw new IllegalArgumentException("emp_id not found in face recognition response");
            }

            int empID = Integer.parseInt(empIdObj.toString());

            // Step 2: Get Employee
            Employee employee = employeeRepository.findById(empID).orElseThrow(()-> new RuntimeException("Employee not found"));
            if (employee == null) {
                throw new IllegalArgumentException("Employee not found for ID: " + empID);
            }

            // Step 3: Check if attendance already exists
            LocalDate today = LocalDate.now();
            List<Attendence> existingAttendance = attendenceRepo.findByEmpIDAndTodaysDate(empID, today);
            if (existingAttendance != null && !existingAttendance.isEmpty()) {
                throw new IllegalArgumentException("Attendance already recorded for today");
            }

            // Step 4: Attendance creation
            LocalTime shiftStartTime = LocalTime.parse(employee.getShiftStartTime());
            LocalTime shiftEndTime = LocalTime.parse(employee.getShiftEndTime());
            long shiftDuration = ChronoUnit.MINUTES.between(shiftStartTime, shiftEndTime);

            ZoneId istZoneId = ZoneId.of("Asia/Kolkata");
            LocalTime nowIST = LocalTime.now(istZoneId).truncatedTo(ChronoUnit.MINUTES);

            String wifiIP = Optional.ofNullable(request.getHeader("X-Forwarded-For")).orElse(request.getRemoteAddr());
            String systemIP = Optional.ofNullable(request.getRemoteAddr()).orElse("N/A");

            Attendence attendance = new Attendence();
            attendance.setEmpID(empID);
            attendance.setBranchCode(employee.getBranchCode());
            attendance.setName(employee.getFullName());
            attendance.setTodaysDate(today);
            attendance.setWorkType("Work From Office");
            attendance.setShift(employee.getShift());
            attendance.setLoginTime(nowIST);

            attendance.setShiftMinutes(shiftDuration);
            attendance.setSystemIP(systemIP);
            attendance.setIP(wifiIP);
            attendance.setStatus(nowIST.isAfter(shiftStartTime) ? "Late" : "On time");

            attendenceRepo.save(attendance);

            return "Attendance recorded successfully for empID: " + empID;
        } catch (IOException e) {
            throw new RuntimeException("Failed to login due to IO error: " + e.getMessage());
        }
    }

}
