package com.NewEmployeeManagement.Controller;

import com.NewEmployeeManagement.Entity.Holidays;
import com.NewEmployeeManagement.Service.HolidaysService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "https://pjsofttech.in")
public class HolidaysController {

    @Autowired
    private HolidaysService service;

    @PostMapping("/createHoliday")
    public ResponseEntity<Holidays> createHoliday(@RequestBody Holidays holiday,
                                                  @RequestParam String role,
                                                  @RequestParam String email) {
        return ResponseEntity.ok(service.createHoliday(holiday, role, email));
    }

    @GetMapping("/getAllHolidays")
    public ResponseEntity<List<Holidays>> getAllHolidays(@RequestParam String role,
                                                         @RequestParam String email) {
        return ResponseEntity.ok(service.getAllHolidays(role, email));
    }

    @GetMapping("/getHolidayById/{id}")
    public ResponseEntity<Holidays> getHolidayById(@PathVariable Long id,
                                                   @RequestParam String role,
                                                   @RequestParam String email) {
        return ResponseEntity.ok(service.getHolidayById(id, role, email));
    }

    @PutMapping("/updateHoliday/{id}")
    public ResponseEntity<Holidays> updateHoliday(@PathVariable Long id,
                                                  @RequestBody Holidays holiday,
                                                  @RequestParam String role,
                                                  @RequestParam String email) {
        return ResponseEntity.ok(service.updateHoliday(id, holiday, role, email));
    }

    @DeleteMapping("/deleteHoliday/{id}")
    public ResponseEntity<String> deleteHoliday(@PathVariable Long id,
                                                @RequestParam String role,
                                                @RequestParam String email) {
        service.deleteHoliday(id, role, email);
        return ResponseEntity.ok("Holiday deleted successfully");
    }
}