package com.NewEmployeeManagement.Service;

import com.NewEmployeeManagement.DTO.EmployeeLeaveSummaryDTO;
import com.NewEmployeeManagement.Entity.EmployeeLeaveRequest;

import java.util.List;

public interface LeaveRequestService {
    EmployeeLeaveRequest createLeaveRequest(EmployeeLeaveRequest employeeLeaveRequest, Long employeeId, String role, String email);
    List<EmployeeLeaveRequest> getAllLeaveRequests(String role, String email);
    EmployeeLeaveRequest updateLeaveRequest(Long id, EmployeeLeaveRequest employeeLeaveRequest, String role, String email);
    void deleteLeaveRequest(Long id, String role, String email);
    EmployeeLeaveRequest getLeaveRequestById(Long id, String role, String email);
    EmployeeLeaveRequest updateLeaveStatus(Long leaveRequestId, String status, String role, String email);

    EmployeeLeaveSummaryDTO getLeaveSummary(String role, String email,Long employeeId);

}