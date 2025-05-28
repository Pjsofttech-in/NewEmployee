package com.NewEmployeeManagement.Repository;

import com.NewEmployeeManagement.Entity.Attendence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AttendenceRepository extends JpaRepository<Attendence, Integer> {
    List<Attendence> findByEmpIDAndTodaysDate(int empID, LocalDate today);

}