package com.NewEmployeeManagement.Service;

import com.NewEmployeeManagement.DTO.AttendanceSummaryDTO;
import com.NewEmployeeManagement.DTO.AttendenceFilterDTO;
import com.NewEmployeeManagement.Entity.EmployeeAttendence;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

public interface AttendenceService {

    ResponseEntity<String> markAttendance(MultipartFile image, String branchCode, String systemName, HttpServletRequest request,String workType);

    EmployeeAttendence markLogout(MultipartFile image, String branchCode, String systemName, HttpServletRequest request);

    ResponseEntity<String> markBreakIn(MultipartFile image, String branchCode, String systemName, HttpServletRequest request);

    ResponseEntity<String> markBreakOut(MultipartFile image, String branchCode, String systemName, HttpServletRequest request);

    AttendanceSummaryDTO getTodayAttendanceSummary(String branchCode);

    Page<EmployeeAttendence> getFilteredAttendance(AttendenceFilterDTO filter, String timeFrame, LocalDate startDate, LocalDate endDate, Pageable pageable);
}