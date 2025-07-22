package com.NewEmployeeManagement.Controller;

import com.NewEmployeeManagement.Entity.EmployeeQuery;
import com.NewEmployeeManagement.Service.EmpQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "https://pjsofttech.in")
public class EmpQueryController {

    @Autowired
    private EmpQueryService service;

    @PostMapping("/createEmpQuery")
    public ResponseEntity<EmployeeQuery> createQuery(@RequestBody EmployeeQuery query,
                                                     @RequestParam String role,
                                                     @RequestParam String email) {
        return ResponseEntity.ok(service.createQuery(query, role, email));
    }

    @GetMapping("/getAllEmpQueries")
    public ResponseEntity<List<EmployeeQuery>> getAllQueries(@RequestParam String role,
                                                             @RequestParam String email) {
        return ResponseEntity.ok(service.getAllQueries(role, email));
    }

    @GetMapping("/getEmpQueryById/{id}")
    public ResponseEntity<EmployeeQuery> getQueryById(@PathVariable Long id,
                                                      @RequestParam String role,
                                                      @RequestParam String email) {
        return ResponseEntity.ok(service.getQueryById(id, role, email));
    }

    @PutMapping("/updateEmpQuery/{id}")
    public ResponseEntity<EmployeeQuery> updateQuery(@PathVariable Long id,
                                                     @RequestBody EmployeeQuery query,
                                                     @RequestParam String role,
                                                     @RequestParam String email) {
        return ResponseEntity.ok(service.updateQuery(id, query, role, email));
    }

    @DeleteMapping("/deleteEmpQuery/{id}")
    public ResponseEntity<String> deleteQuery(@PathVariable Long id,
                                              @RequestParam String role,
                                              @RequestParam String email) {
        service.deleteQuery(id, role, email);
        return ResponseEntity.ok("Query deleted successfully");
    }
}