package com.NewEmployeeManagement.Controller;

import com.NewEmployeeManagement.Entity.Memo;
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
    public ResponseEntity<Memo> createMemo(@RequestBody Memo memo,
                                           @RequestParam String role,
                                           @RequestParam String email) {
        return ResponseEntity.ok(service.createMemo(memo, role, email));
    }

    @GetMapping("/getAllMemos")
    public ResponseEntity<List<Memo>> getAllMemos(@RequestParam String role,
                                                  @RequestParam String email,
                                                  @RequestParam String branchCode) {
        return ResponseEntity.ok(service.getAllMemos(role, email, branchCode));
    }

    @GetMapping("/getMemoById/{id}")
    public ResponseEntity<Memo> getMemoById(@PathVariable int id,
                                            @RequestParam String role,
                                            @RequestParam String email) {
        return ResponseEntity.ok(service.getMemoById(id, role, email));
    }

    @PutMapping("/updateMemo/{id}")
    public ResponseEntity<Memo> updateMemo(@PathVariable int id,
                                           @RequestBody Memo memo,
                                           @RequestParam String role,
                                           @RequestParam String email) {
        return ResponseEntity.ok(service.updateMemo(id, memo, role, email));
    }

    @DeleteMapping("/deleteMemo/{id}")
    public ResponseEntity<String> deleteMemo(@PathVariable int id,
                                             @RequestParam String role,
                                             @RequestParam String email) {
        service.deleteMemo(id, role, email);
        return ResponseEntity.ok("Memo deleted successfully");
    }
}