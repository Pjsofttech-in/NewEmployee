package com.NewEmployeeManagement.Service;

import com.NewEmployeeManagement.Entity.EmployeeCategory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface EmployeeCategoryService {

    EmployeeCategory createCategory(EmployeeCategory category, String role, String email);

    List<EmployeeCategory> getAllCategories(String role, String email);

    EmployeeCategory updateCategory(Long id, EmployeeCategory category, String role, String email);

    void deleteCategory(Long id, String role, String email);

    EmployeeCategory getCategoryById(Long id, String role, String email);

    BigDecimal convertToPercent(BigDecimal value);

    BigDecimal convertIfNeeded(BigDecimal value);

    BigDecimal convertToInteger(BigDecimal value);

    void convertToPercentage(EmployeeCategory category);

    void convertToPercentageIfNeeded(EmployeeCategory category);

    void convertToIntegerValues(EmployeeCategory category);

    Optional<EmployeeCategory> getCategoryByName(String categoryName, String branchCode);

    long getTotalCategory(String branchCode);

    List<EmployeeCategory> getNonDeletedCategories(String branchCode);


}