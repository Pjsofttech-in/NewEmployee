package com.NewEmployeeManagement.Controller;

import com.NewEmployeeManagement.Entity.EmpQuery;
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
    public ResponseEntity<EmpQuery> createQuery(@RequestBody EmpQuery query,
                                                @RequestParam String role,
                                                @RequestParam String email) {
        return ResponseEntity.ok(service.createQuery(query, role, email));
    }

    @GetMapping("/getAllEmpQueries")
    public ResponseEntity<List<EmpQuery>> getAllQueries(@RequestParam String role,
                                                        @RequestParam String email) {
        return ResponseEntity.ok(service.getAllQueries(role, email));
    }

    @GetMapping("/getEmpQueryById/{id}")
    public ResponseEntity<EmpQuery> getQueryById(@PathVariable int id,
                                                 @RequestParam String role,
                                                 @RequestParam String email) {
        return ResponseEntity.ok(service.getQueryById(id, role, email));
    }

    @PutMapping("/updateEmpQuery/{id}")
    public ResponseEntity<EmpQuery> updateQuery(@PathVariable int id,
                                                @RequestBody EmpQuery query,
                                                @RequestParam String role,
                                                @RequestParam String email) {
        return ResponseEntity.ok(service.updateQuery(id, query, role, email));
    }

    @DeleteMapping("/deleteEmpQuery/{id}")
    public ResponseEntity<String> deleteQuery(@PathVariable int id,
                                              @RequestParam String role,
                                              @RequestParam String email) {
        service.deleteQuery(id, role, email);
        return ResponseEntity.ok("Query deleted successfully");
    }
}