package com.NewEmployeeManagement.Service;

import com.NewEmployeeManagement.Entity.Employee;
import com.NewEmployeeManagement.Repository.EmployeeRepository;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class S3Service {

    private static final Logger logger = LoggerFactory.getLogger(S3Service.class);

    private final AmazonS3 s3Client;
    private final EmployeeRepository employeeRepository;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    public S3Service(AmazonS3 s3Client, EmployeeRepository employeeRepository) {
        this.s3Client = s3Client;
        this.employeeRepository = employeeRepository;
    }


    public String uploadEmployeeDocument(MultipartFile file, String branchCode, String systemName) throws IOException {
        if (branchCode == null || branchCode.isEmpty()) {
            logger.error("BranchCode is required");
            throw new IllegalArgumentException("BranchCode is required");
        }

        if (systemName == null || systemName.isEmpty()) {
            logger.error("SystemName is required");
            throw new IllegalArgumentException("SystemName is required");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            logger.error("File original filename is null");
            throw new IllegalArgumentException("File must have a valid name");
        }

        String fileExtension = "";
        int dotIndex = originalFilename.lastIndexOf(".");
        if (dotIndex > 0) {
            fileExtension = originalFilename.substring(dotIndex);
        }

        String uuidFileName = java.util.UUID.randomUUID().toString() + fileExtension;

        String key = branchCode + "/" + systemName.toLowerCase() + "/doct/" + uuidFileName;

        try (InputStream inputStream = file.getInputStream()) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            s3Client.putObject(new PutObjectRequest(bucketName, key, inputStream, metadata));
            logger.info("File uploaded successfully with key: {}", key);
        } catch (SdkClientException e) {
            logger.error("Error uploading to S3", e);
            throw new RuntimeException("Error uploading to S3", e);
        }

        return String.format("https://%s.s3.amazonaws.com/%s", bucketName, key);
    }

    public boolean deleteImage(String bucketName, String fileUrl) {
        try {
            String key = fileUrl;

            // Extract key from full URL
            if (fileUrl.startsWith("http")) {
                int index = fileUrl.indexOf(bucketName) + bucketName.length() + 1;
                key = fileUrl.substring(index); // e.g., BCH193/employee-sys/attendance_faces/2.JPG
            }

            DeleteObjectRequest request = new DeleteObjectRequest(bucketName, key);
            s3Client.deleteObject(request);

            System.out.println("✅ Deleted from S3: " + key);
            return true;
        } catch (AmazonServiceException e) {
            System.err.println("❌ S3 Delete Error: " + e.getMessage());
            return false;
        }
    }



    // ✅ Upload face image directly  to attendance_faces/
    public String uploadEmployeeFaceImage(MultipartFile file, String branchCode, Long employeeId) throws IOException {
        if (branchCode == null || branchCode.isEmpty()) {
            logger.error("BranchCode is required");
            throw new IllegalArgumentException("BranchCode is required");
        }

        if (file == null || file.isEmpty()) {
            logger.error("File is missing or empty");
            throw new IllegalArgumentException("File is missing or empty");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            logger.error("File original filename is null");
            throw new IllegalArgumentException("File must have a valid name");
        }

        String fileExtension = "";
        int dotIndex = originalFilename.lastIndexOf(".");
        if (dotIndex > 0) {
            fileExtension = originalFilename.substring(dotIndex);
        }

        String systemName = "employee-sys";
        String key = branchCode + "/" + systemName + "/attendance_faces/" + employeeId + fileExtension;

        try (InputStream inputStream = file.getInputStream()) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            s3Client.putObject(new PutObjectRequest(bucketName, key, inputStream, metadata));
            logger.info("Employee face image uploaded with key: {}", key);
        } catch (SdkClientException e) {
            logger.error("Failed to upload to S3", e);
            throw new RuntimeException("Failed to upload to S3", e);
        }

        String fileUrl = String.format("https://%s.s3.amazonaws.com/%s", bucketName, key);

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
        employee.setFaceEncoding(fileUrl);
        employeeRepository.save(employee);

        return fileUrl;
    }
}