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
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
public class AttendanceServiceImpl implements AttendenceService {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private AttendenceRepository attendenceRepo;

    private static final String FACE_API_URL = "https://pjsofttech.in:38443/login";

    @Override
    public String handleFaceRecognitionAndAttendance(MultipartFile selfieImage, HttpServletRequest request) {
        try {
            // Step 1: Call Face Recognition API
            Map<String, Object> faceResponse = callFaceRecognitionAPI(selfieImage);
            Map<String, Object> matchData = getMatchData(faceResponse);

            int empID = Integer.parseInt(Objects.requireNonNull(matchData.get("emp_id")).toString());

            // Step 2: Fetch Employee
            Employee employee = employeeRepository.findById(empID)
                    .orElseThrow(() -> new RuntimeException("Employee not found for ID: " + empID));

            // Step 3: Check existing attendance
            LocalDate today = LocalDate.now();
            boolean alreadyPresent = !attendenceRepo.findByEmpIDAndTodaysDate(empID, today).isEmpty();
            if (alreadyPresent) {
                throw new IllegalArgumentException("Attendance already recorded for today");
            }

            // Step 4: Record Attendance
            Attendence attendance = createAttendance(employee, request);
            attendenceRepo.save(attendance);

            return "Attendance recorded successfully for empID: " + empID;

        } catch (IOException e) {
            throw new RuntimeException("Failed to process selfie image: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> callFaceRecognitionAPI(MultipartFile selfieImage) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        ByteArrayResource imageResource = new ByteArrayResource(selfieImage.getBytes()) {
            @Override
            public String getFilename() {
                return "selfie.jpg";
            }
        };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", imageResource);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = new RestTemplate().exchange(FACE_API_URL, HttpMethod.POST, requestEntity, Map.class);

        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            throw new RuntimeException("Face recognition API error or empty response");
        }

        return response.getBody();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getMatchData(Map<String, Object> faceResponse) {
        if (!"success".equals(faceResponse.get("status"))) {
            throw new AccessDeniedException("Face recognition failed: No match found");
        }

        Object match = faceResponse.get("match");
        if (!(match instanceof Map)) {
            throw new RuntimeException("Invalid match data from face recognition response");
        }

        return (Map<String, Object>) match;
    }

    private Attendence createAttendance(Employee employee, HttpServletRequest request) {
        ZoneId istZoneId = ZoneId.of("Asia/Kolkata");
        LocalTime nowIST = LocalTime.now(istZoneId).truncatedTo(ChronoUnit.MINUTES);
        LocalDate today = LocalDate.now(istZoneId);

        LocalTime shiftStart = LocalTime.parse(employee.getShiftStartTime());
        LocalTime shiftEnd = LocalTime.parse(employee.getShiftEndTime());
        long shiftDurationMinutes = ChronoUnit.MINUTES.between(shiftStart, shiftEnd);

        String wifiIP = Optional.ofNullable(request.getHeader("X-Forwarded-For")).orElse(request.getRemoteAddr());
        String systemIP = Optional.ofNullable(request.getRemoteAddr()).orElse("N/A");

        Attendence attendance = new Attendence();
        attendance.setEmpID(employee.getId());
        attendance.setName(employee.getFullName());
        attendance.setBranchCode(employee.getBranchCode());
        attendance.setTodaysDate(today);
        attendance.setWorkType("Work From Office");
        attendance.setShift(employee.getShift());
        attendance.setLoginTime(nowIST);
        attendance.setShiftMinutes(shiftDurationMinutes);
        attendance.setSystemIP(systemIP);
        attendance.setIP(wifiIP);
        attendance.setStatus(nowIST.isAfter(shiftStart) ? "Late" : "On time");

        return attendance;
    }
}