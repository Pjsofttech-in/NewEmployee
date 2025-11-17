package com.NewEmployeeManagement.Service;

import com.NewEmployeeManagement.Entity.EmployeeFeedBackForm;

import java.util.List;

public interface FeedBackService
{
    EmployeeFeedBackForm createfeedBack(EmployeeFeedBackForm feedBackForm, String role, String email);

    EmployeeFeedBackForm updateFeedBack(Long id, EmployeeFeedBackForm feedBackForm, String role, String email);

    List<EmployeeFeedBackForm> getAllFeedback(String role, String email);

    EmployeeFeedBackForm getFeedbackById(Long id, String role, String email);

    String deleteFeedback(Long id, String role, String email);

    List<EmployeeFeedBackForm> getFeedbackbyEmail(String role, String email);


}
