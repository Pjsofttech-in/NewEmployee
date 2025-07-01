package com.NewEmployeeManagement.Controller;

import com.NewEmployeeManagement.DTO.AttendanceSummaryDTO;
import com.NewEmployeeManagement.Entity.Attendence;
import com.NewEmployeeManagement.Pageination.SpecializationService;
import com.NewEmployeeManagement.Service.AttendenceService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/attendance")
public class AttendenceController {

    @Autowired
    private AttendenceService attendenceService;

    @Autowired
    private SpecializationService specializationService;

    @PostMapping(value = "/login", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> markAttendance(
            @RequestParam("image") MultipartFile image,
            @RequestParam("branch_code") String branchCode,
            @RequestParam("system_name") String systemName,
            @RequestParam("work_type") String workType,
            HttpServletRequest request) {

        return attendenceService.markAttendance(image, branchCode, systemName, request,workType);
    }

    @PostMapping(value = "/logout", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> markLogout(
            @RequestParam("image") MultipartFile image,
            @RequestParam("branch_code") String branchCode,
            @RequestParam("system_name") String systemName,
            HttpServletRequest request) {

        try {
            Attendence attendance = attendenceService.markLogout(image, branchCode, systemName, request);
            return ResponseEntity.ok("Logout successfully of id:"+attendance.getEmployee().getId());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Logout failed: " + e.getMessage());
        }
    }

    @PostMapping(value = "/break-in", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> markBreakIn(
            @RequestParam("image") MultipartFile image,
            @RequestParam("branch_code") String branchCode,
            @RequestParam("system_name") String systemName,
            HttpServletRequest request) {

        return attendenceService.markBreakIn(image, branchCode, systemName, request);
    }

    @PostMapping(value = "/break-out", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> markBreakOut(
            @RequestParam("image") MultipartFile image,
            @RequestParam("branch_code") String branchCode,
            @RequestParam("system_name") String systemName,
            HttpServletRequest request) {

        return attendenceService.markBreakOut(image, branchCode, systemName, request);
    }

    @PostMapping("/AttendanceFilter")
    public Page<Attendence> filterAttendence(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String todaysDateFilter,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam String branchCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return specializationService.filterAttendence(status, todaysDateFilter, startDate, endDate,
                branchCode, page, size);
    }

    @GetMapping("/today-summary")
    public ResponseEntity<AttendanceSummaryDTO> getTodayAttendanceSummary(
            @RequestParam("branch_code") String branchCode) {
        AttendanceSummaryDTO summary = attendenceService.getTodayAttendanceSummary(branchCode);
        return ResponseEntity.ok(summary);
    }

}