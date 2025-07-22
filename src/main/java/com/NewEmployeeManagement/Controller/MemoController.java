package com.NewEmployeeManagement.Controller;

import com.NewEmployeeManagement.Entity.EmployeeMemo;
import com.NewEmployeeManagement.Service.MemoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "https://pjsofttech.in")
public class MemoController {

    @Autowired
    private MemoService service;

    @PostMapping("/createMemo")
    public ResponseEntity<EmployeeMemo> createMemo(@RequestBody EmployeeMemo employeeMemo,
                                                   @RequestParam String role,
                                                   @RequestParam String email) {
        return ResponseEntity.ok(service.createMemo(employeeMemo, role, email));
    }

    @GetMapping("/getAllMemos")
    public ResponseEntity<List<EmployeeMemo>> getAllMemos(@RequestParam String role,
                                                          @RequestParam String email) {
        return ResponseEntity.ok(service.getAllMemos(role, email));
    }

    @GetMapping("/getMemoById/{id}")
    public ResponseEntity<EmployeeMemo> getMemoById(@PathVariable Long id,
                                                    @RequestParam String role,
                                                    @RequestParam String email) {
        return ResponseEntity.ok(service.getMemoById(id, role, email));
    }

    @PutMapping("/updateMemo/{id}")
    public ResponseEntity<EmployeeMemo> updateMemo(@PathVariable Long id,
                                                   @RequestBody EmployeeMemo employeeMemo,
                                                   @RequestParam String role,
                                                   @RequestParam String email) {
        return ResponseEntity.ok(service.updateMemo(id, employeeMemo, role, email));
    }

    @DeleteMapping("/deleteMemo/{id}")
    public ResponseEntity<String> deleteMemo(@PathVariable Long id,
                                             @RequestParam String role,
                                             @RequestParam String email) {
        service.deleteMemo(id, role, email);
        return ResponseEntity.ok("Memo deleted successfully");
    }
}