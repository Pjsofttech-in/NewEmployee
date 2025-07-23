package com.NewEmployeeManagement.Controller;

import com.NewEmployeeManagement.DTO.AttendanceSummaryDTO;
import com.NewEmployeeManagement.DTO.AttendenceFilterDTO;
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
    public ResponseEntity<String> markAttendance(
            @RequestParam("image") MultipartFile image,
            @RequestParam("branch_code") String branchCode,
            @RequestParam("system_name") String systemName,
            @RequestParam("work_type") String workType,
            HttpServletRequest request) {

        return attendenceService.markAttendance(image, branchCode, systemName, request,workType);
    }

    @PostMapping(value = "/employeeLogout", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> markLogout(
            @RequestParam("image") MultipartFile image,
            @RequestParam("branch_code") String branchCode,
            @RequestParam("system_name") String systemName,
            HttpServletRequest request) {

        try {
            EmployeeAttendence attendance = attendenceService.markLogout(image, branchCode, systemName, request);
            return ResponseEntity.ok("Logout successfully of id:"+attendance.getEmployee().getId());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Logout failed: " + e.getMessage());
        }
    }

    @PostMapping(value = "/employeeBreakIn", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> markBreakIn(
            @RequestParam("image") MultipartFile image,
            @RequestParam("branch_code") String branchCode,
            @RequestParam("system_name") String systemName,
            HttpServletRequest request) {

        return attendenceService.markBreakIn(image, branchCode, systemName, request);
    }

    @PostMapping(value = "/employeeBreakOut", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> markBreakOut(
            @RequestParam("image") MultipartFile image,
            @RequestParam("branch_code") String branchCode,
            @RequestParam("system_name") String systemName,
            HttpServletRequest request) {

        return attendenceService.markBreakOut(image, branchCode, systemName, request);
    }

    @PostMapping("/AttendanceFilter")
    public ResponseEntity<Page<EmployeeAttendence>> getAttendanceList(
            @RequestParam(required = false) String timeFrame,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestBody(required = false) AttendenceFilterDTO filter) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

        Page<EmployeeAttendence> result = attendenceService.getFilteredAttendance(
                filter, timeFrame, startDate, endDate, pageable
        );

        return ResponseEntity.ok(result);
    }

    @GetMapping("/TodayAttendaceSummary")
    public ResponseEntity<AttendanceSummaryDTO> getTodayAttendanceSummary(
            @RequestParam("branch_code") String branchCode) {
        AttendanceSummaryDTO summary = attendenceService.getTodayAttendanceSummary(branchCode);
        return ResponseEntity.ok(summary);
    }

}