package com.NewEmployeeManagement.ServiceImpl;

import com.NewEmployeeManagement.DTO.EmployeeLeaveSummaryDTO;
import com.NewEmployeeManagement.Entity.Employee;
import com.NewEmployeeManagement.Entity.EmployeeCategory;
import com.NewEmployeeManagement.Entity.EmployeeLeaveRequest;
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
    public EmployeeLeaveRequest createLeaveRequest(EmployeeLeaveRequest employeeLeaveRequest, Long employeeId, String role, String email) {
        if (!permissionService.hasPermission(role, email, "POST")) {

            throw new AccessDeniedException("No permission to create leave request");
        }

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + employeeId));

        if (employee.isDeleted()) {
            throw new RuntimeException("Employee not present (soft deleted), cannot create leave request");
        }

        employeeLeaveRequest.setEmployee(employee);

        employeeLeaveRequest.setEmpID(employee.getId());
        employeeLeaveRequest.setFullName(employee.getFullName());
        employeeLeaveRequest.setCategoryName(employee.getCategoryName());

        String branchCode = permissionService.fetchBranchCode(role, email);
        employeeLeaveRequest.setRole(role);
        employeeLeaveRequest.setCreatedByEmail(email);
        employeeLeaveRequest.setBranchCode(branchCode);

        employeeLeaveRequest.calculateToDateAndLeaveRequestDate();

        // Save the leave request
        EmployeeLeaveRequest savedRequest = repository.save(employeeLeaveRequest);

        // Determine leave duration (0.5 for half-day, 1.0 for full-day)
        double days = "half-day".equalsIgnoreCase(employeeLeaveRequest.getDuration()) ? 0.5 : 1.0;

        return savedRequest;
    }


    @Override
    public List<EmployeeLeaveRequest> getAllLeaveRequests(String role, String email) {
        if (!permissionService.hasPermission(role, email, "GET")) {
            throw new AccessDeniedException("No permission to view leave requests");
        }
        String branchCode = permissionService.fetchBranchCode(role, email);
        return repository.findAllByBranchCode(branchCode);
    }

    @Override
    public EmployeeLeaveRequest updateLeaveRequest(Long id, EmployeeLeaveRequest employeeLeaveRequest, String role, String email) {
        if (!permissionService.hasPermission(role, email, "PUT")) {
            throw new AccessDeniedException("No permission to update leave request");
        }

        EmployeeLeaveRequest existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("LeaveRequest not found"));

        existing.setFromDate(employeeLeaveRequest.getFromDate() != null ? employeeLeaveRequest.getFromDate() : existing.getFromDate());
        existing.setToDate(employeeLeaveRequest.getToDate() != null ? employeeLeaveRequest.getToDate() : existing.getToDate());
        existing.setCategoryName(employeeLeaveRequest.getCategoryName() != null ? employeeLeaveRequest.getCategoryName() : existing.getCategoryName());
        existing.setReasondescription(employeeLeaveRequest.getReasondescription() != null ? employeeLeaveRequest.getReasondescription() : existing.getReasondescription());
        existing.setPaidleave(employeeLeaveRequest.getPaidleave() != null ? employeeLeaveRequest.getPaidleave() : existing.getPaidleave());
        existing.setUnpaidleave(employeeLeaveRequest.getUnpaidleave() != null ? employeeLeaveRequest.getUnpaidleave() : existing.getUnpaidleave());
        existing.setLeaveRequired(employeeLeaveRequest.getLeaveRequired() != null ? employeeLeaveRequest.getLeaveRequired() : existing.getLeaveRequired());
        existing.setAppliedPaidLeaves(employeeLeaveRequest.getAppliedPaidLeaves() != null ? employeeLeaveRequest.getAppliedPaidLeaves() : existing.getAppliedPaidLeaves());
        existing.setStatus(employeeLeaveRequest.getStatus() != null ? employeeLeaveRequest.getStatus() : existing.getStatus());
        existing.setLeaveType(employeeLeaveRequest.getLeaveType() !=null ? employeeLeaveRequest.getLeaveType():existing.getLeaveType());
        existing.setDuration(employeeLeaveRequest.getDuration() !=null ? employeeLeaveRequest.getDuration():existing.getDuration());
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
    public EmployeeLeaveRequest getLeaveRequestById(Long id, String role, String email) {
        if (!permissionService.hasPermission(role, email, "GET")) {
            throw new AccessDeniedException("No permission to view leave request");
        }

        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("LeaveRequest not found"));
    }

    @Override
    public EmployeeLeaveRequest approveOrRejectLeave(Long leaveRequestId, String action, String role, String email) {
        if (!permissionService.hasPermission(role, email, "POST")) {
            throw new AccessDeniedException("No permission to approve/reject leave");
        }

        EmployeeLeaveRequest employeeLeaveRequest = repository.findById(leaveRequestId)
                .orElseThrow(() -> new RuntimeException("LeaveRequest not found"));

        String normalizedAction = action.trim().toLowerCase();
        if (!normalizedAction.equals("approve") && !normalizedAction.equals("reject")) {
            throw new IllegalArgumentException("Invalid action. Must be 'approve' or 'reject'");
        }

        Employee employee = employeeLeaveRequest.getEmployee();

        // Determine leave days: 0.5 for half-day, otherwise full leaveRequired or default 1.0
        double leaveDays = "half-day".equalsIgnoreCase(employeeLeaveRequest.getDuration()) ? 0.5 :
                employeeLeaveRequest.getLeaveRequired() != null ? employeeLeaveRequest.getLeaveRequired() : 1.0;

        if ("approve".equals(normalizedAction)) {
            if ("Paid".equalsIgnoreCase(employeeLeaveRequest.getLeaveType())) {
                double availablePaidLeave = employee.getPaidleaves() != null ? employee.getPaidleaves() : 0.0;

                if (availablePaidLeave < leaveDays) {
                    throw new IllegalArgumentException("Not enough paid leave balance to approve");
                }

                employee.setPaidleaves(availablePaidLeave - leaveDays);
                employeeLeaveRequest.setPaidleave(leaveDays);
                employeeLeaveRequest.setUnpaidleave(0.0);

            } else if ("Unpaid".equalsIgnoreCase(employeeLeaveRequest.getLeaveType())) {
                double availableUnpaidLeave = employee.getUnpaidleaves() != null ? employee.getUnpaidleaves() : 0.0;

                if (availableUnpaidLeave < leaveDays) {
                    throw new IllegalArgumentException("Not enough unpaid leave balance to approve");
                }

                employee.setUnpaidleaves(availableUnpaidLeave - leaveDays);
                employeeLeaveRequest.setUnpaidleave(leaveDays);
                employeeLeaveRequest.setPaidleave(0.0);

            } else {
                throw new IllegalArgumentException("Invalid leave type");
            }

            employeeLeaveRequest.setLeaveRequired(leaveDays);
            employeeLeaveRequest.setStatus("Approved");
            employeeLeaveRequest.setTotalleavecount(leaveDays);
            employeeLeaveRequest.calculateToDateAndLeaveRequestDate();

        } else {
            employeeLeaveRequest.setStatus("Rejected");
            employeeLeaveRequest.setPaidleave(0.0);
            employeeLeaveRequest.setUnpaidleave(0.0);
            employeeLeaveRequest.setTotalleavecount(0.0);
        }

        employeeRepository.save(employee);
        return repository.save(employeeLeaveRequest);
    }


    @Override
    public EmployeeLeaveSummaryDTO getLeaveSummary(String role, String email, Long employeeId)
    {
        if (!permissionService.hasPermission(role, email, "GET")) {
            throw new AccessDeniedException("No permission to approve/reject leave");
        }
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + employeeId));

        EmployeeCategory category = employee.getEmployeeCategory();

        double totalPaidLeavesFromCategory = category != null && category.getTotalPaidLeave() != null ? category.getTotalPaidLeave() : 0.0;
        double totalUnpaidLeavesFromCategory = category != null && category.getTotalUnpaidLeave() != null ? category.getTotalUnpaidLeave() : 0.0;

        double totalAppliedLeaves = 0.0;
        double paidLeave = 0.0;
        double unpaidLeave = 0.0;

        // Get all non-deleted leave requests
        List<EmployeeLeaveRequest> employeeLeaveRequests = repository.findByEmpIDAndIsDeletedFalse(employeeId);

        for (EmployeeLeaveRequest request : employeeLeaveRequests) {
            Double leaveRequired = request.getLeaveRequired() != null ? request.getLeaveRequired() : 0.0;
            totalAppliedLeaves += leaveRequired;

            if ("approved".equalsIgnoreCase(request.getStatus())) {
                // Use actual paid/unpaid values saved during approval
                paidLeave += request.getPaidleave() != null ? request.getPaidleave() : 0.0;
                unpaidLeave += request.getUnpaidleave() != null ? request.getUnpaidleave() : 0.0;
            }
        }

        double remainingPaidLeave = totalPaidLeavesFromCategory - paidLeave;
        if (remainingPaidLeave < 0) remainingPaidLeave = 0;

        return new EmployeeLeaveSummaryDTO(
                totalPaidLeavesFromCategory,
                totalUnpaidLeavesFromCategory,
                totalAppliedLeaves,
                paidLeave,
                unpaidLeave,
                remainingPaidLeave
        );
    }

}