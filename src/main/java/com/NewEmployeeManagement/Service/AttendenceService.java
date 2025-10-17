package com.NewEmployeeManagement.Service;

import com.NewEmployeeManagement.DTO.AttendanceSummaryDTO;
import com.NewEmployeeManagement.DTO.AttendenceFilterDTO;
import com.NewEmployeeManagement.DTO.EmployeeAttendanceDTO;
import com.NewEmployeeManagement.Entity.EmployeeAttendence;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface AttendenceService {

    String markEmployeeAttendanceFromFace(MultipartFile image, String branchCode, String clientIp);
    String logoutEmployeeFromFace(MultipartFile image, String branchCode, String logoutIp);
    String breakInEmployeeFromFace(MultipartFile image, String branchCode);
    String breakOutEmployeeFromFace(MultipartFile image, String branchCode);
    AttendanceSummaryDTO getTodayAttendanceSummary(String branchCode);
    Page<EmployeeAttendence> getAttendanceByEmpId(Long empId, String role, String email,String timeFrame, LocalDate customStartDate, LocalDate customEndDate, Pageable pageable);
    Page<EmployeeAttendanceDTO> getFilteredEmployeeAttendance(AttendenceFilterDTO filterDTO, String role, String email, String timeFrame, LocalDate customStartDate, LocalDate customEndDate, Pageable pageable);
    Long getAttendanceCount(Long empId, int month, int year);
    String markEmployeeAttendanceManually(List<Long> empIds, String role, String email);



}