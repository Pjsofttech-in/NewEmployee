package com.NewEmployeeManagement.Repository;

import com.NewEmployeeManagement.Entity.Employee;
import com.NewEmployeeManagement.Entity.EmployeeFeedBackForm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeedBackRepository extends JpaRepository<EmployeeFeedBackForm,Long>
{

    @Query("SELECT e FROM EmployeeFeedBackForm e WHERE e.branchCode = :branchCode ORDER BY e.id DESC")
    List<EmployeeFeedBackForm> findAllByBranchCode(String branchCode);

    @Query("SELECT e FROM EmployeeFeedBackForm e WHERE e.createdByEmail = :email")
    List<EmployeeFeedBackForm> findFeedBackByEmail(@Param("email") String email);
}
