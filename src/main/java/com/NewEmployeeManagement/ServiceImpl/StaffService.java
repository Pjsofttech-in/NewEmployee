package com.NewEmployeeManagement.ServiceImpl;

import com.NewEmployeeManagement.DTO.BranchAddressDTO;
import com.NewEmployeeManagement.DTO.InstituteClientWrapperResponse;
import com.NewEmployeeManagement.DTO.InstituteLoginResponse;
import com.NewEmployeeManagement.JWT.LoginRequest;
import com.NewEmployeeManagement.JWT.LoginResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class StaffService
{
    private final WebClient webClient;

    @Autowired
    public StaffService(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<LoginResponse> loginStaff(LoginRequest request) {
        return webClient.post()
                .uri("/stafflogin")
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(error -> Mono.error(new RuntimeException("Login Failed: " + error)))
                )
                .bodyToMono(LoginResponse.class);
    }


    public Map<String, Boolean> getPermissionsByEmail(String email) {

        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        String token = request.getHeader(HttpHeaders.AUTHORIZATION);

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/permissionForStaff")
                        .queryParam("staffEmail", email)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, token)  // pass it as-is
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Boolean>>() {})
                .block();
    }

    public Map<String, Object> getCrudPermissionForDepartmentByEmail(String email) {

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String token = request.getHeader(HttpHeaders.AUTHORIZATION);

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/permissionForDepartment")
                        .queryParam("email", email)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, token)  // Pass token directly
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();

    }

    public List<InstituteLoginResponse> getInstituteDetailsOnly(String email) {
        InstituteClientWrapperResponse response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/getLayerClientByClientEmail")
                        .queryParam("email", email)
                        .build())
                .retrieve()
                .bodyToMono(InstituteClientWrapperResponse.class)
                .block();

        return response != null ? response.getInstituteResponseDTOS() : Collections.emptyList();
    }


    public String getInstituteEmailByBranchCode(String branchCode) {
        try {
            String emailResponse = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/instituteEmailByBranchCode")
                            .queryParam("branchCode", branchCode)
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(); // blocking, like your getInstituteDetails

            return emailResponse != null ? emailResponse : "No email found";

        } catch (Exception e) {
            e.printStackTrace();
            return "Error fetching institute email: " + e.getMessage();
        }
    }

    public BranchAddressDTO getBranchAddressDetails(String branchCode) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/branchAddressDetailsByBranchCode")
                        .queryParam("branchCode", branchCode)
                        .build())
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .flatMap(error -> {
                                    System.err.println("SuperAdmin error: " + error);
                                    return Mono.error(new RuntimeException("Failed to fetch branch details"));
                                })
                )
                .bodyToMono(BranchAddressDTO.class)
                .block(); // ✅ convert reactive response to blocking for MVC app
    }

}
