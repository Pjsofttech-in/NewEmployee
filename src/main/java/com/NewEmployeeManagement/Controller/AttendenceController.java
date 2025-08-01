package com.NewEmployeeManagement.Controller;

import com.NewEmployeeManagement.DTO.AttendanceSummaryDTO;
import com.NewEmployeeManagement.DTO.AttendenceFilterDTO;
import com.NewEmployeeManagement.DTO.EmployeeAttendanceDTO;
import com.NewEmployeeManagement.Entity.EmployeeAttendence;
import com.NewEmployeeManagement.Service.AttendenceService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;


@RestController
//@CrossOrigin(origins = "http://localhost:3000")
@CrossOrigin(origins = "https://pjsofttech.in")
public class AttendenceController {

    @Autowired
    private AttendenceService attendenceService;


    @PostMapping(value = "/markAttendanceForEmployee", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> markEmployeeAttendanceFromFace(
            @RequestParam("image") MultipartFile image,
            @RequestParam("branchCode") String branchCode) {
        String result = attendenceService.markEmployeeAttendanceFromFace(image, branchCode);
        return ResponseEntity.ok(result);
    }

    @PostMapping(value = "/employeeLogout", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> logoutEmployee(
        @RequestParam MultipartFile image,
        @RequestParam String branchCode,
        HttpServletRequest request) {

    String logoutIp = request.getRemoteAddr();
    String result = attendenceService.logoutEmployeeFromFace(image, branchCode, logoutIp);
    return ResponseEntity.ok(result);
}

    @PostMapping(value = "/employeeBreakIn", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> breakIn(
            @RequestParam MultipartFile image,
            @RequestParam String branchCode)
    {
        return ResponseEntity.ok(attendenceService.breakInEmployeeFromFace(image, branchCode));
    }

    @PostMapping(value = "/employeeBreakOut", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> markBreakOut(
            @RequestParam MultipartFile image,
            @RequestParam String branchCode)  {

        return ResponseEntity.ok(attendenceService.breakOutEmployeeFromFace(image, branchCode));
    }

    @PostMapping("/AttendanceFilter")
    public ResponseEntity<Page<EmployeeAttendanceDTO>> getAllEmployeeAttendance(
            @RequestParam String role,
            @RequestParam String email,
            @RequestParam(required = false) String timeFrame,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate customStartDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate customEndDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestBody(required = false) AttendenceFilterDTO filterDTO) {

        Pageable pageable = PageRequest.of(page, size);
        Page<EmployeeAttendanceDTO> result = attendenceService.getFilteredEmployeeAttendance(
                filterDTO, role, email, timeFrame, customStartDate, customEndDate, pageable);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/TodayAttendaceSummary")
    public ResponseEntity<AttendanceSummaryDTO> getTodayAttendanceSummary(
            @RequestParam String branchCode) {
        AttendanceSummaryDTO summary = attendenceService.getTodayAttendanceSummary(branchCode);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/getAttendanceByEmpId")
    public ResponseEntity<Page<EmployeeAttendence>> getAttendanceByEmpId(
            @RequestParam Long empId,
            @RequestParam(required = false) String timeFrame,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate customStartDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate customEndDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("todaysDate").descending());
        Page<EmployeeAttendence> attendances = attendenceService.getAttendanceByEmpId(empId, timeFrame, customStartDate, customEndDate, pageable);
        return ResponseEntity.ok(attendances);
    }


}