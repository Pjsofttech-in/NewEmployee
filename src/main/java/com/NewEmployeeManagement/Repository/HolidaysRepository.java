package com.NewEmployeeManagement.Repository;

import com.NewEmployeeManagement.Entity.Holidays;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HolidaysRepository extends JpaRepository<Holidays, Long> {
    @Query("SELECT h FROM Holidays h WHERE h.branchCode = :branchCode ORDER BY h.id DESC")
    List<Holidays> findAllByBranchCode(@Param("branchCode") String branchCode);
}