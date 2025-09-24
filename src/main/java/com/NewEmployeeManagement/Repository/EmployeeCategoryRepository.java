package com.NewEmployeeManagement.Repository;

import com.NewEmployeeManagement.Entity.EmployeeCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeCategoryRepository extends JpaRepository<EmployeeCategory, Long> {

    @Query("SELECT e FROM EmployeeCategory e WHERE e.branchCode = :branchCode AND e.isDeleted = false ORDER BY e.id DESC")
    List<EmployeeCategory> findAllByBranchCode(@Param("branchCode") String branchCode);

    @Query("SELECT e FROM EmployeeCategory e WHERE e.isDeleted = false AND e.categoryName = :categoryName AND e.branchCode = :branchCode")
    Optional<EmployeeCategory> findByCategoryName(@Param("categoryName") String categoryName, @Param("branchCode") String branchCode);

    @Query("SELECT COUNT(e) FROM EmployeeCategory e WHERE e.isDeleted = false AND e.branchCode = :branchCode")
    long countTotalCategory(@Param("branchCode") String branchCode);

    @Query("SELECT e FROM EmployeeCategory e WHERE e.isDeleted = false AND e.categoryName = :categoryName AND e.branchCode = :branchCode")
    EmployeeCategory findCategoryByCategoryName(@Param("categoryName") String categoryName, @Param("branchCode") String branchCode);

    @Query("SELECT e FROM EmployeeCategory e WHERE e.isDeleted = false AND e.branchCode = :branchCode ORDER BY e.id DESC")
    List<EmployeeCategory> findByIsDeletedFalseAndBranchCode(@Param("branchCode") String branchCode);

    Optional<EmployeeCategory> findByCategoryNameAndBranchCode(String categoryName, String branchCode);

    long countByBranchCode(String branchCode);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
            "FROM EmployeeCategory c " +
            "WHERE c.categoryName = :categoryName AND c.branchCode = :branchCode")
    boolean existsByCategoryNameAndBranchCode(@Param("categoryName") String categoryName,
                                              @Param("branchCode") String branchCode);
}