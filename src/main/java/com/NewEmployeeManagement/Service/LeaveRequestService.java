package com.NewEmployeeManagement.Service;

import com.NewEmployeeManagement.DTO.EmployeeLeaveSummaryDTO;
import com.NewEmployeeManagement.Entity.LeaveRequest;

import java.util.List;

public interface LeaveRequestService {
    LeaveRequest createLeaveRequest(LeaveRequest leaveRequest, int employeeId, String role, String email);
    List<LeaveRequest> getAllLeaveRequests(String role, String email);
    LeaveRequest updateLeaveRequest(Long id, LeaveRequest leaveRequest, String role, String email);
    void deleteLeaveRequest(Long id, String role, String email);
    LeaveRequest getLeaveRequestById(Long id, String role, String email);

    LeaveRequest approveOrRejectLeave(Long leaveRequestId, String action, String role, String email);

    EmployeeLeaveSummaryDTO getLeaveSummary(int employeeId);

}