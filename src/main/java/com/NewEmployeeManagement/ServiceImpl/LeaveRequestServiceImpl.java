package com.NewEmployeeManagement.ServiceImpl;

import com.NewEmployeeManagement.DTO.EmployeeLeaveSummaryDTO;
import com.NewEmployeeManagement.Entity.Employee;
import com.NewEmployeeManagement.Entity.EmployeeCategory;
import com.NewEmployeeManagement.Entity.LeaveRequest;
import com.NewEmployeeManagement.Repository.EmployeeRepository;
import com.NewEmployeeManagement.Repository.LeaveRequestRepository;
import com.NewEmployeeManagement.Service.LeaveRequestService;
import com.NewEmployeeManagement.Service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

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

        if (employee.isDeleted()) {
            throw new RuntimeException("Employee not present (soft deleted), cannot create leave request");
        }

        leaveRequest.setEmployee(employee);

        leaveRequest.setEmpID(employee.getId());
        leaveRequest.setFullName(employee.getFullName());
        leaveRequest.setCategoryName(employee.getCategoryName());

        String branchCode = permissionService.fetchBranchCode(role, email);
        leaveRequest.setRole(role);
        leaveRequest.setCreatedByEmail(email);
        leaveRequest.setBranchCode(branchCode);

        leaveRequest.calculateToDateAndLeaveRequestDate();

        // Save the leave request
        LeaveRequest savedRequest = repository.save(leaveRequest);

        // Determine leave duration (0.5 for half-day, 1.0 for full-day)
        double days = "half-day".equalsIgnoreCase(leaveRequest.getDuration()) ? 0.5 : 1.0;

        return savedRequest;
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
        existing.setDuration(leaveRequest.getDuration() !=null ? leaveRequest.getDuration():existing.getDuration());
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

        Employee employee = leaveRequest.getEmployee();

        // Determine leave days: 0.5 for half-day, otherwise full leaveRequired or default 1.0
        double leaveDays = "half-day".equalsIgnoreCase(leaveRequest.getDuration()) ? 0.5 :
                leaveRequest.getLeaveRequired() != null ? leaveRequest.getLeaveRequired() : 1.0;

        if ("approve".equals(normalizedAction)) {
            if ("Paid".equalsIgnoreCase(leaveRequest.getLeaveType())) {
                double availablePaidLeave = employee.getPaidleaves() != null ? employee.getPaidleaves() : 0.0;

                if (availablePaidLeave < leaveDays) {
                    throw new IllegalArgumentException("Not enough paid leave balance to approve");
                }

                employee.setPaidleaves(availablePaidLeave - leaveDays);
                leaveRequest.setPaidleave(leaveDays);
                leaveRequest.setUnpaidleave(0.0);

            } else if ("Unpaid".equalsIgnoreCase(leaveRequest.getLeaveType())) {
                double availableUnpaidLeave = employee.getUnpaidleaves() != null ? employee.getUnpaidleaves() : 0.0;

                if (availableUnpaidLeave < leaveDays) {
                    throw new IllegalArgumentException("Not enough unpaid leave balance to approve");
                }

                employee.setUnpaidleaves(availableUnpaidLeave - leaveDays);
                leaveRequest.setUnpaidleave(leaveDays);
                leaveRequest.setPaidleave(0.0);

            } else {
                throw new IllegalArgumentException("Invalid leave type");
            }

            leaveRequest.setLeaveRequired(leaveDays);
            leaveRequest.setStatus("Approved");
            leaveRequest.setTotalleavecount(leaveDays);
            leaveRequest.calculateToDateAndLeaveRequestDate();

        } else {
            leaveRequest.setStatus("Rejected");
            leaveRequest.setPaidleave(0.0);
            leaveRequest.setUnpaidleave(0.0);
            leaveRequest.setTotalleavecount(0.0);
        }

        employeeRepository.save(employee);
        return repository.save(leaveRequest);
    }


    @Override
    public EmployeeLeaveSummaryDTO getLeaveSummary(int employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + employeeId));

        EmployeeCategory category = employee.getEmployeeCategory();
        Double totalPaidLeavesFromCategory = category != null && category.getTotalPaidLeave() != null ? category.getTotalPaidLeave() : 0.0;
        Double totalUnpaidLeavesFromCategory = category != null && category.getTotalUnpaidLeave() != null ? category.getTotalUnpaidLeave() : 0.0;

        Double paidLeave = 0.0;
        Double unpaidLeave = 0.0;
        Double totalLeaveRequired = 0.0;

        List<LeaveRequest> leaveRequests = repository.findByEmployeeIdAndIsDeletedFalse(employeeId);
        for (LeaveRequest request : leaveRequests) {
            paidLeave += request.getPaidleave() != null ? request.getPaidleave() : 0.0;
            unpaidLeave += request.getUnpaidleave() != null ? request.getUnpaidleave() : 0.0;
            totalLeaveRequired += request.getLeaveRequired() != null ? request.getLeaveRequired() : 0.0;
        }

        Double remainingPaidLeave = employee.getPaidleaves() != null ? employee.getPaidleaves() : 0.0;

        return new EmployeeLeaveSummaryDTO(
                totalPaidLeavesFromCategory,
                totalUnpaidLeavesFromCategory,
                totalLeaveRequired,
                paidLeave,
                unpaidLeave,
                remainingPaidLeave
        );
    }


}