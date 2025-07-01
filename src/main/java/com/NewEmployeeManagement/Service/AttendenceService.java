package com.NewEmployeeManagement.Service;

import com.NewEmployeeManagement.DTO.AttendanceSummaryDTO;
import com.NewEmployeeManagement.Entity.Attendence;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface AttendenceService {

    ResponseEntity<String> markAttendance(MultipartFile image, String branchCode, String systemName, HttpServletRequest request,String workType);

    Attendence markLogout(MultipartFile image, String branchCode, String systemName, HttpServletRequest request);

    ResponseEntity<String> markBreakIn(MultipartFile image, String branchCode, String systemName, HttpServletRequest request);

    ResponseEntity<String> markBreakOut(MultipartFile image, String branchCode, String systemName, HttpServletRequest request);

    AttendanceSummaryDTO getTodayAttendanceSummary(String branchCode);

}