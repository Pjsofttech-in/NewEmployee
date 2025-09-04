package com.NewEmployeeManagement.Repository;

import com.NewEmployeeManagement.Entity.EmployeeHolidays;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HolidaysRepository extends JpaRepository<EmployeeHolidays, Long> {
    @Query("SELECT h FROM EmployeeHolidays h WHERE h.branchCode = :branchCode ORDER BY h.id DESC")
    List<EmployeeHolidays> findAllByBranchCode(@Param("branchCode") String branchCode);


    @Query("SELECT COUNT(h) " +
            "FROM EmployeeHolidays h " +
            "WHERE h.branchCode = :branchCode " +
            "AND FUNCTION('MONTH', h.date) = :month " +
            "AND FUNCTION('YEAR', h.date) = :year " +
            "AND h.paidHoliday = true")
    Long getPaidHolidaysForMonth(@Param("branchCode") String branchCode,
                                 @Param("month") int month,
                                 @Param("year") int year);



}