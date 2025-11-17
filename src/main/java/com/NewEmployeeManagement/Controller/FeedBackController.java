package com.NewEmployeeManagement.Controller;

import com.NewEmployeeManagement.Entity.EmployeeFeedBackForm;
import com.NewEmployeeManagement.Service.FeedBackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class FeedBackController
{

    @Autowired
    FeedBackService feedBackService;

    @PostMapping("/createFeedBack")
    public ResponseEntity<EmployeeFeedBackForm> createFeedback(
            @RequestParam String role,
            @RequestParam String email,
            @RequestBody EmployeeFeedBackForm feedBackForm
    ) {
        return ResponseEntity.ok(feedBackService.createfeedBack(feedBackForm, role, email));
    }

    @PutMapping("/updateFeedBack/{id}")
    public ResponseEntity<EmployeeFeedBackForm> updateFeedback(
            @PathVariable Long id,
            @RequestParam String role,
            @RequestParam String email,
            @RequestBody EmployeeFeedBackForm feedBackForm
    ) {
        return ResponseEntity.ok(feedBackService.updateFeedBack(id, feedBackForm, role, email));
    }

    @GetMapping("/allFeedBack")
    public ResponseEntity<List<EmployeeFeedBackForm>> getAllFeedback(
            @RequestParam String role,
            @RequestParam String email) {

        return ResponseEntity.ok(feedBackService.getAllFeedback(role, email));
    }

    @GetMapping("/getFeedBackbyId/{id}")
    public ResponseEntity<EmployeeFeedBackForm> getById(
            @PathVariable Long id,
            @RequestParam String role,
            @RequestParam String email
    ) {
        return ResponseEntity.ok(feedBackService.getFeedbackById(id, role, email));
    }


    @GetMapping("/getFeedBackbyEmail")
    public ResponseEntity<List<EmployeeFeedBackForm>> getFeedbackByEmail(
            @RequestParam String role,
            @RequestParam String email
    ) {
        return ResponseEntity.ok(feedBackService.getFeedbackbyEmail(role, email));
    }

    @DeleteMapping("/deleteFeedBack/{id}")
    public ResponseEntity<String> deleteFeedback(
            @PathVariable Long id,
            @RequestParam String role,
            @RequestParam String email
    ) {
        return ResponseEntity.ok(feedBackService.deleteFeedback(id, role, email));
    }

}
