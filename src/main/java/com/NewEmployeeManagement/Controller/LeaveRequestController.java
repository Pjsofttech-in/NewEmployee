package com.NewEmployeeManagement.Controller;

import com.NewEmployeeManagement.DTO.EmployeeLeaveSummaryDTO;
import com.NewEmployeeManagement.DTO.LeaveRequestsResponse;
import com.NewEmployeeManagement.Entity.EmployeeLeaveRequest;
import com.NewEmployeeManagement.Service.LeaveRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
//@CrossOrigin(origins = "http://localhost:3000")
@CrossOrigin(origins = "https://pjsofttech.in")
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    @Autowired
    public LeaveRequestController(LeaveRequestService leaveRequestService) {
        this.leaveRequestService = leaveRequestService;
    }

    @PostMapping("/createLeaveRequest")
    public EmployeeLeaveRequest createLeaveRequest(@RequestBody EmployeeLeaveRequest employeeLeaveRequest,
                                                   @RequestParam Long employeeId,
                                                   @RequestParam String role,
                                                   @RequestParam String email) {
        return leaveRequestService.createLeaveRequest(employeeLeaveRequest, employeeId, role, email);
    }

    @GetMapping("/getAllLeaveRequests")
    public ResponseEntity<LeaveRequestsResponse> getAllLeaveRequests(
            @RequestParam String role,
            @RequestParam String email,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String branchCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("fromDate").descending());
        LeaveRequestsResponse resp = leaveRequestService.getAllLeaveRequests(role, email, name, status, branchCode, pageable);
        return ResponseEntity.ok(resp);
    }
    @GetMapping("/getLeaveRequestById/{id}")
    public EmployeeLeaveRequest getLeaveRequestById(@PathVariable Long id,
                                                    @RequestParam String role, @RequestParam String email) {
        return leaveRequestService.getLeaveRequestById(id, role, email);
    }

    @PutMapping("/updateLeaveRequest/{id}")
    public EmployeeLeaveRequest updateLeaveRequest(@PathVariable Long id,
                                                   @RequestBody EmployeeLeaveRequest employeeLeaveRequest,
                                                   @RequestParam String role, @RequestParam String email) {
        return leaveRequestService.updateLeaveRequest(id, employeeLeaveRequest, role, email);
    }

    @DeleteMapping("/deleteLeaveRequest/{id}")
    public void deleteLeaveRequest(@PathVariable Long id, @RequestParam String role, @RequestParam String email) {
        leaveRequestService.deleteLeaveRequest(id, role, email);
    }

    @PostMapping("/leaveStatus/{id}")
    public EmployeeLeaveRequest approveOrRejectLeave(@PathVariable Long id,
                                                     @RequestParam String status,
                                                     @RequestParam String role,
                                                     @RequestParam String email) {
        return leaveRequestService.updateLeaveStatus(id, status, role, email);
    }

    @GetMapping("/getLeaveSummary/{employeeId}")
    public ResponseEntity<EmployeeLeaveSummaryDTO> getLeaveSummary(@RequestParam String role, @RequestParam String email,@PathVariable Long employeeId) {
        EmployeeLeaveSummaryDTO summary = leaveRequestService.getLeaveSummary(role,email,employeeId);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/getAllLeavesByEmployeeId")
    public ResponseEntity<List<EmployeeLeaveRequest>> getLeavesByEmployee(@RequestParam String role, @RequestParam String email,@RequestParam Long empID)
    {
        List<EmployeeLeaveRequest> leaveList = leaveRequestService.getAllLeaveRequestsByEmpId(role, email, empID);
        return ResponseEntity.ok(leaveList);
    }


}