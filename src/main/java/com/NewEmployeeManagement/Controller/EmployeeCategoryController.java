package com.NewEmployeeManagement.Controller;

import com.NewEmployeeManagement.Entity.EmployeeCategory;
import com.NewEmployeeManagement.Service.EmployeeCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "https://pjsofttech.in")
public class EmployeeCategoryController {

    @Autowired
    private EmployeeCategoryService service;

    @PostMapping("/createCategory")
    public ResponseEntity<EmployeeCategory> createCategory(@RequestParam String role,
                                                           @RequestParam String email,
                                                           @RequestBody EmployeeCategory category) {
        return ResponseEntity.ok(service.createCategory(category, role, email));
    }

    @GetMapping("/getAllCategories")
    public ResponseEntity<List<EmployeeCategory>> getAllCategories(@RequestParam String role,
                                                                   @RequestParam String email,
                                                                   @RequestParam String branchCode) {
        return ResponseEntity.ok(service.getAllCategories(role, email, branchCode));
    }

    @GetMapping("/getCategoryById/{id}")
    public ResponseEntity<EmployeeCategory> getCategoryById(@PathVariable Long id,
                                                            @RequestParam String role,
                                                            @RequestParam String email) {
        return ResponseEntity.ok(service.getCategoryById(id, role, email));
    }

    @PutMapping("/updateCategory/{id}")
    public ResponseEntity<EmployeeCategory> updateCategory(@PathVariable Long id,
                                                           @RequestParam String role,
                                                           @RequestParam String email,
                                                           @RequestBody EmployeeCategory category) {
        return ResponseEntity.ok(service.updateCategory(id, category, role, email));
    }

    @DeleteMapping("/deleteCategory/{id}")
    public ResponseEntity<String> deleteCategory(@PathVariable Long id,
                                                 @RequestParam String role,
                                                 @RequestParam String email) {
        service.deleteCategory(id, role, email);
        return ResponseEntity.ok("Category deleted successfully");
    }

    @GetMapping("/getCategoryByName/{categoryName}")
    public ResponseEntity<Optional<EmployeeCategory>> getCategoryByName(@PathVariable String categoryName,
                                                                        @RequestParam String role,
                                                                        @RequestParam String email,
                                                                        @RequestParam String branchCode) {
        return ResponseEntity.ok(service.getCategoryByName(categoryName, branchCode));
    }

    @GetMapping("/getTotalCategory")
    public ResponseEntity<Long> getTotalCategory(@RequestParam String role,
                                                 @RequestParam String email,
                                                 @RequestParam String branchCode) {
        return ResponseEntity.ok(service.getTotalCategory(branchCode));
    }

    @GetMapping("/getNonDeletedCategories")
    public ResponseEntity<List<EmployeeCategory>> getNonDeletedCategories(@RequestParam String role,
                                                                          @RequestParam String email,
                                                                          @RequestParam String branchCode) {
        return ResponseEntity.ok(service.getNonDeletedCategories(branchCode));
    }

//    @DeleteMapping("/softDeleteCategory/{id}")
//    public ResponseEntity<String> softDeleteCategory(@PathVariable Long id,
//                                                     @RequestParam String role,
//                                                     @RequestParam String email) {
//        service.softDeleteCategory(id, role, email);
//        return ResponseEntity.ok("Category soft-deleted successfully");
//    }
}