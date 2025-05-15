package com.NewEmployeeManagement.ServiceImpl;

import com.NewEmployeeManagement.Entity.Employee;
import com.NewEmployeeManagement.Entity.LeaveRequest;
import com.NewEmployeeManagement.Repository.EmployeeRepository;
import com.NewEmployeeManagement.Repository.LeaveRequestRepository;
import com.NewEmployeeManagement.Service.LeaveRequestService;
import com.NewEmployeeManagement.Service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class LeaveRequestServiceImpl implements LeaveRequestService {

    @Autowired
    LeaveRequestRepository repository;

    @Autowired
    PermissionService permissionService;

    @Autowired
    private EmployeeRepository employeeRepository;



    @Override
    public LeaveRequest createLeaveRequest(LeaveRequest leaveRequest, int employeeId, String role, String email) {
        if (!permissionService.hasPermission(role, email, "POST")) {
            throw new AccessDeniedException("No permission to create leave request");
        }

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + employeeId));
        leaveRequest.setEmployee(employee);

        leaveRequest.setEmpID(employee.getId());
        leaveRequest.setFullName(employee.getFullName());
        leaveRequest.setCategoryName(employee.getCategoryName());
        String branchCode = permissionService.fetchBranchCode(role, email);
        leaveRequest.setRole(role);
        leaveRequest.setCreatedByEmail(email);
        leaveRequest.setBranchCode(branchCode);
        leaveRequest.calculateToDateAndLeaveRequestDate();

        return repository.save(leaveRequest);
    }

    @Override
    public List<LeaveRequest> getAllLeaveRequests(String role, String email) {
        if (!permissionService.hasPermission(role, email, "GET")) {
            throw new AccessDeniedException("No permission to view leave requests");
        }
        String branchCode = permissionService.fetchBranchCode(role, email);
        return repository.findAllByBranchCode(branchCode);
    }

    @Override
    public LeaveRequest updateLeaveRequest(Long id, LeaveRequest leaveRequest, String role, String email) {
        if (!permissionService.hasPermission(role, email, "PUT")) {
            throw new AccessDeniedException("No permission to update leave request");
        }

        LeaveRequest existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("LeaveRequest not found"));

        existing.setFromDate(leaveRequest.getFromDate() != null ? leaveRequest.getFromDate() : existing.getFromDate());
        existing.setToDate(leaveRequest.getToDate() != null ? leaveRequest.getToDate() : existing.getToDate());
        existing.setCategoryName(leaveRequest.getCategoryName() != null ? leaveRequest.getCategoryName() : existing.getCategoryName());
        existing.setReasondescription(leaveRequest.getReasondescription() != null ? leaveRequest.getReasondescription() : existing.getReasondescription());
        existing.setPaidleave(leaveRequest.getPaidleave() != null ? leaveRequest.getPaidleave() : existing.getPaidleave());
        existing.setUnpaidleave(leaveRequest.getUnpaidleave() != null ? leaveRequest.getUnpaidleave() : existing.getUnpaidleave());
        existing.setLeaveRequired(leaveRequest.getLeaveRequired() != null ? leaveRequest.getLeaveRequired() : existing.getLeaveRequired());
        existing.setAppliedPaidLeaves(leaveRequest.getAppliedPaidLeaves() != null ? leaveRequest.getAppliedPaidLeaves() : existing.getAppliedPaidLeaves());
        existing.setStatus(leaveRequest.getStatus() != null ? leaveRequest.getStatus() : existing.getStatus());
        existing.setLeaveType(leaveRequest.getLeaveType() !=null ? leaveRequest.getLeaveType():existing.getLeaveType());

        existing.calculateToDateAndLeaveRequestDate();

        return repository.save(existing);
    }


    @Override
    public void deleteLeaveRequest(Long id, String role, String email) {
        if (!permissionService.hasPermission(role, email, "DELETE")) {
            throw new AccessDeniedException("No permission to delete leave request");
        }

        repository.findById(id)
                .orElseThrow(() -> new RuntimeException("LeaveRequest not found"));

        repository.deleteById(id);
    }

    @Override
    public LeaveRequest getLeaveRequestById(Long id, String role, String email) {
        if (!permissionService.hasPermission(role, email, "GET")) {
            throw new AccessDeniedException("No permission to view leave request");
        }

        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("LeaveRequest not found"));
    }

    @Override
    public LeaveRequest approveOrRejectLeave(Long leaveRequestId, String action, String role, String email) {
        if (!permissionService.hasPermission(role, email, "POST")) {
            throw new AccessDeniedException("No permission to approve/reject leave");
        }

        LeaveRequest leaveRequest = repository.findById(leaveRequestId)
                .orElseThrow(() -> new RuntimeException("LeaveRequest not found"));

        String normalizedAction = action.trim().toLowerCase();
        if (!normalizedAction.equals("approve") && !normalizedAction.equals("reject")) {
            throw new IllegalArgumentException("Invalid action. Must be 'approve' or 'reject'");
        }

        leaveRequest.setStatus(normalizedAction.equals("approve") ? "Approved" : "Rejected");

        if (normalizedAction.equals("reject")) {
            return repository.save(leaveRequest);
        }

        double leaveDays = leaveRequest.getLeaveRequired() != null ? leaveRequest.getLeaveRequired() : 0.0;

        if ("Paid".equalsIgnoreCase(leaveRequest.getLeaveType())) {
            leaveRequest.setPaidleave(
                    (leaveRequest.getPaidleave() != null ? leaveRequest.getPaidleave() : 0.0) + leaveDays
            );
        } else if ("Unpaid".equalsIgnoreCase(leaveRequest.getLeaveType())) {
            leaveRequest.setUnpaidleave(
                    (leaveRequest.getUnpaidleave() != null ? leaveRequest.getUnpaidleave() : 0.0) + leaveDays
            );
        }

        leaveRequest.calculateToDateAndLeaveRequestDate();

        return repository.save(leaveRequest);
    }

}