package com.NewEmployeeManagement.ServiceImpl;

import com.NewEmployeeManagement.Entity.Employee;
import com.NewEmployeeManagement.Entity.LeaveRequest;
import com.NewEmployeeManagement.Repository.EmployeeRepository;
import com.NewEmployeeManagement.Repository.LeaveRequestRepository;
import com.NewEmployeeManagement.Service.LeaveRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class LeaveRequestServiceImpl implements LeaveRequestService {

    private final LeaveRequestRepository repository;
    private final WebClient webClient;
    private final StaffService staffService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Value("${client.superadmin.base-url}")
    private String superAdminBaseUrl;

    @Autowired
    public LeaveRequestServiceImpl(LeaveRequestRepository repository,
                                   WebClient webClient,
                                   StaffService staffService) {
        this.repository = repository;
        this.webClient = webClient;
        this.staffService = staffService;
    }

    private boolean hasPermission(String role, String email, String action) {
        if ("BRANCH".equalsIgnoreCase(role)) {
            Boolean exists = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/existBranchbyemail")
                            .queryParam("email", email)
                            .build())
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();
            return Boolean.TRUE.equals(exists);
        }

        return switch (role.toUpperCase()) {
            case "STAFF" -> {
                Map<String, Boolean> perms = staffService.getPermissionsByEmail(email);
                yield switch (action.toUpperCase()) {
                    case "GET" -> Boolean.TRUE.equals(perms.get("cansGet"));
                    case "POST" -> Boolean.TRUE.equals(perms.get("cansPost"));
                    case "PUT" -> Boolean.TRUE.equals(perms.get("cansPut"));
                    case "DELETE" -> Boolean.TRUE.equals(perms.get("cansDelete"));
                    default -> false;
                };
            }
            case "DEPARTMENT" -> {
                Map<String, Object> perms = staffService.getCrudPermissionForDepartmentByEmail(email);
                yield switch (action.toUpperCase()) {
                    case "GET" -> Boolean.TRUE.equals(perms.get("candGet"));
                    case "POST" -> Boolean.TRUE.equals(perms.get("candPost"));
                    case "PUT" -> Boolean.TRUE.equals(perms.get("candPut"));
                    case "DELETE" -> Boolean.TRUE.equals(perms.get("candDelete"));
                    default -> false;
                };
            }
            default -> false;
        };
    }

    private String fetchBranchCode(String role, String email) {
        String endpoint = switch (role.toLowerCase()) {
            case "branch" -> "/branch/getbranchcode";
            case "department" -> "/department/getbranchcode";
            case "staff" -> "/staff/getbranchcode";
            default -> throw new IllegalArgumentException("Invalid role: " + role);
        };
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(endpoint)
                        .queryParam("email", email)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    @Override
    public LeaveRequest createLeaveRequest(LeaveRequest leaveRequest, int employeeId, String role, String email) {
        if (!hasPermission(role, email, "POST")) {
            throw new AccessDeniedException("No permission to create leave request");
        }

        // Fetch employee and set the relationship
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + employeeId));
        leaveRequest.setEmployee(employee);

        // Optionally populate some fields from employee
        leaveRequest.setEmpID(employee.getId());
        leaveRequest.setFullName(employee.getFullName());
        leaveRequest.setCategoryName(employee.getCategoryName());

        // Set role/email/branchCode from the creator
        String branchCode = fetchBranchCode(role, email);
        leaveRequest.setRole(role);
        leaveRequest.setCreatedByEmail(email);
        leaveRequest.setBranchCode(branchCode);

        // Calculate derived fields
        leaveRequest.calculateToDateAndLeaveRequestDate();

        return repository.save(leaveRequest);
    }



    @Override
    public List<LeaveRequest> getAllLeaveRequests(String role, String email, String branchCode) {
        if (!hasPermission(role, email, "GET")) {
            throw new AccessDeniedException("No permission to view leave requests");
        }

        return repository.findAllByBranchCode(branchCode);
    }

    @Override
    public LeaveRequest updateLeaveRequest(Long id, LeaveRequest leaveRequest, String role, String email) {
        if (!hasPermission(role, email, "PUT")) {
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

        // Recalculate totalleavecount and toDate if needed
        existing.calculateToDateAndLeaveRequestDate();

        return repository.save(existing);
    }


    @Override
    public void deleteLeaveRequest(Long id, String role, String email) {
        if (!hasPermission(role, email, "DELETE")) {
            throw new AccessDeniedException("No permission to delete leave request");
        }

        repository.findById(id)
                .orElseThrow(() -> new RuntimeException("LeaveRequest not found"));

        repository.deleteById(id);
    }

    @Override
    public LeaveRequest getLeaveRequestById(Long id, String role, String email) {
        if (!hasPermission(role, email, "GET")) {
            throw new AccessDeniedException("No permission to view leave request");
        }

        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("LeaveRequest not found"));
    }

    @Override
    public LeaveRequest approveOrRejectLeave(Long leaveRequestId, String action, String role, String email) {
        if (!hasPermission(role, email, "POST")) {
            throw new AccessDeniedException("No permission to approve/reject leave");
        }

        LeaveRequest leaveRequest = repository.findById(leaveRequestId)
                .orElseThrow(() -> new RuntimeException("LeaveRequest not found"));

        // Normalize and validate action
        String normalizedAction = action.trim().toLowerCase();
        if (!normalizedAction.equals("approve") && !normalizedAction.equals("reject")) {
            throw new IllegalArgumentException("Invalid action. Must be 'approve' or 'reject'");
        }

        // Set status dynamically
        leaveRequest.setStatus(normalizedAction.equals("approve") ? "Approved" : "Rejected");

        // If rejected, no need to process further
        if (normalizedAction.equals("reject")) {
            return repository.save(leaveRequest);
        }

        // Handle approved logic
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

        // Update calculated fields
        leaveRequest.calculateToDateAndLeaveRequestDate();

        return repository.save(leaveRequest);
    }

}