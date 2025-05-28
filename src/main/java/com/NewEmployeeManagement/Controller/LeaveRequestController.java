package com.NewEmployeeManagement.Controller;

import com.NewEmployeeManagement.DTO.EmployeeLeaveSummaryDTO;
import com.NewEmployeeManagement.Entity.LeaveRequest;
import com.NewEmployeeManagement.Service.LeaveRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    @Autowired
    public LeaveRequestController(LeaveRequestService leaveRequestService) {
        this.leaveRequestService = leaveRequestService;
    }

    @PostMapping("/createLeaveRequest")
    public LeaveRequest createLeaveRequest(@RequestBody LeaveRequest leaveRequest,
                                           @RequestParam int employeeId,
                                           @RequestParam String role,
                                           @RequestParam String email) {
        return leaveRequestService.createLeaveRequest(leaveRequest, employeeId, role, email);
    }

    @GetMapping("/getAllLeaveRequests")
    public List<LeaveRequest> getAllLeaveRequests(@RequestParam String role,
                                                  @RequestParam String email) {
        return leaveRequestService.getAllLeaveRequests(role, email);
    }

    @GetMapping("/getLeaveRequestById/{id}")
    public LeaveRequest getLeaveRequestById(@PathVariable Long id,
                                            @RequestParam String role, @RequestParam String email) {
        return leaveRequestService.getLeaveRequestById(id, role, email);
    }

    @PutMapping("/updateLeaveRequest/{id}")
    public LeaveRequest updateLeaveRequest(@PathVariable Long id,
                                           @RequestBody LeaveRequest leaveRequest,
                                           @RequestParam String role, @RequestParam String email) {
        return leaveRequestService.updateLeaveRequest(id, leaveRequest, role, email);
    }

    @DeleteMapping("/deleteLeaveRequest/{id}")
    public void deleteLeaveRequest(@PathVariable Long id, @RequestParam String role, @RequestParam String email) {
        leaveRequestService.deleteLeaveRequest(id, role, email);
    }

    @PostMapping("/leaveStatus/{id}")
    public LeaveRequest approveOrRejectLeave(@PathVariable Long id,
                                             @RequestParam String action,
                                             @RequestParam String role,
                                             @RequestParam String email) {
        return leaveRequestService.approveOrRejectLeave(id, action, role, email);
    }

    @GetMapping("/getLeaveSummary/{employeeId}")
    public ResponseEntity<EmployeeLeaveSummaryDTO> getLeaveSummary(@PathVariable int employeeId) {
        EmployeeLeaveSummaryDTO summary = leaveRequestService.getLeaveSummary(employeeId);
        return ResponseEntity.ok(summary);
    }

}