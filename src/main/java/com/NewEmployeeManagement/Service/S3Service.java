package com.NewEmployeeManagement.Service;

import com.NewEmployeeManagement.Entity.Employee;
import com.NewEmployeeManagement.Repository.EmployeeRepository;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// other imports...

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

        // ✅ Generate a UUID-based filename
        String fileExtension = "";
        int dotIndex = originalFilename.lastIndexOf(".");
        if (dotIndex > 0) {
            fileExtension = originalFilename.substring(dotIndex);
        }

        String uuidFileName = java.util.UUID.randomUUID().toString() + fileExtension;

        // ✅ Construct path like your existing logic
        String key = branchCode + "/" + systemName.toLowerCase() + "/doct/" + uuidFileName;

        try (InputStream inputStream = file.getInputStream()) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType()); // Optional but helpful

            s3Client.putObject(new PutObjectRequest(bucketName, key, inputStream, metadata));
            logger.info("File uploaded successfully with key: {}", key);
        } catch (SdkClientException e) {
            logger.error("Error uploading to S3", e);
            throw new RuntimeException("Error uploading to S3", e);
        }

        // ✅ Return formatted public S3 URL
        return String.format("https://%s.s3.amazonaws.com/%s", bucketName, key);
    }


    public String copyImageToAttendanceFolderWithEmpId(int employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        String currentImageUrl = employee.getFaceEncoding();
        if (currentImageUrl == null || currentImageUrl.isEmpty()) {
            logger.error("Employee image URL is empty");
            throw new IllegalArgumentException("Employee image URL is empty");
        }

        String branchCode = employee.getBranchCode();
        if (branchCode == null || branchCode.isEmpty()) {
            logger.error("Branch code missing");
            throw new IllegalArgumentException("Branch code missing");
        }

        String systemName = "newemployee"; // fixed system name

        String marker = ".amazonaws.com/";
        int markerIndex = currentImageUrl.indexOf(marker);
        if (markerIndex == -1) {
            logger.error("Invalid S3 URL: {}", currentImageUrl);
            throw new IllegalArgumentException("Invalid S3 URL: " + currentImageUrl);
        }

        String sourceKey = currentImageUrl.substring(markerIndex + marker.length());
        String originalFileName = sourceKey.substring(sourceKey.lastIndexOf("/") + 1);
        String extension = "";
        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex > 0) extension = originalFileName.substring(dotIndex);

        String destKey = branchCode + "/" + systemName + "/attendancy/" + employeeId + extension;

        try {
            s3Client.copyObject(bucketName, sourceKey, bucketName, destKey);
            logger.info("Copied image from {} to {}", sourceKey, destKey);
        } catch (AmazonServiceException e) {
            logger.error("Failed to copy S3 object", e);
            throw new RuntimeException("Failed to copy S3 object", e);
        }

        String newUrl = s3Client.getUrl(bucketName, destKey).toString();
        employee.setFaceEncoding(newUrl);
        employeeRepository.save(employee);

        return newUrl;
    }

}