package com.NewEmployeeManagement.ServiceImpl;

import com.NewEmployeeManagement.Entity.EmployeeFeedBackForm;
import com.NewEmployeeManagement.Repository.FeedBackRepository;
import com.NewEmployeeManagement.Service.FeedBackService;
import com.NewEmployeeManagement.Service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class FeedBackServiceImpl implements FeedBackService
{

    @Autowired
    FeedBackRepository feedBackRepository;

    @Autowired
    PermissionService permissionService;


    @Override
    public EmployeeFeedBackForm createfeedBack(EmployeeFeedBackForm feedBackForm, String role, String email) {

        if (!permissionService.hasPermission(role, email, "POST")) {
            throw new AccessDeniedException("No permission to view FeedBack");
        }

        feedBackForm.setCreatedByEmail(email);
        feedBackForm.setRole(role);

        String branchCode = permissionService.fetchBranchCode(role, email);
        feedBackForm.setDate(LocalDate.now());

        feedBackForm.setBranchCode(branchCode);
        return feedBackRepository.save(feedBackForm);
    }

    @Override
    public EmployeeFeedBackForm updateFeedBack(Long id, EmployeeFeedBackForm feedBackForm, String role, String email) {

        if (!permissionService.hasPermission(role, email, "PUT")) {
            throw new AccessDeniedException("No permission to view FeedBack");
        }

        EmployeeFeedBackForm existing = feedBackRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Feedback not found"));

        if (feedBackForm.getName()!= null && !feedBackForm.getName().isEmpty())
        {
            existing.setName(feedBackForm.getName());
        }
        if (feedBackForm.getSubject()!= null && !feedBackForm.getSubject().isEmpty())
        {
            existing.setSubject(feedBackForm.getSubject());
        }
        if (feedBackForm.getDepartment()!= null && !feedBackForm.getDepartment().isEmpty())
        {
            existing.setDepartment(feedBackForm.getDepartment());
        }
        if (feedBackForm.getDescription()!= null && !feedBackForm.getDescription().isEmpty())
        {
            existing.setDescription(feedBackForm.getDescription());
        }
        if(feedBackForm.getStatus()!= null && !feedBackForm.getStatus().isEmpty())
        {
            existing.setStatus(feedBackForm.getStatus());
        }

        if (feedBackForm.getRemark()!= null && !feedBackForm.getRemark().isEmpty())
        {
            existing.setRemark(feedBackForm.getRemark());
        }


        return feedBackRepository.save(existing);
    }

    @Override
    public List<EmployeeFeedBackForm> getAllFeedback(String role, String email)
    {
        if (!permissionService.hasPermission(role, email, "GET")) {
            throw new AccessDeniedException("No permission to view FeedBack");
        }

        String branchCode = permissionService.fetchBranchCode(role, email);
        return feedBackRepository.findAllByBranchCode(branchCode);
    }

    @Override
    public EmployeeFeedBackForm getFeedbackById(Long id, String role, String email) {
        if (!permissionService.hasPermission(role, email, "GET")) {
            throw new AccessDeniedException("No permission to view FeedBack");
        }
        return feedBackRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Feedback not found"));
    }

    @Override
    public List<EmployeeFeedBackForm> getFeedbackbyEmail(String role, String email)
    {
        if (!permissionService.hasPermission(role, email, "GET")) {
            throw new AccessDeniedException("No permission to view FeedBack");
        }

        List<EmployeeFeedBackForm> list = feedBackRepository.findFeedBackByEmail(email);

        if (list == null || list.isEmpty()) {
            throw new RuntimeException("FeedBack Not Found For this Email");
        }

        return list;
    }

    @Override
    public String deleteFeedback(Long id, String role, String email) {
        EmployeeFeedBackForm existing = feedBackRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Feedback not found"));

        feedBackRepository.delete(existing);

        return "Feedback deleted successfully";
    }



}
