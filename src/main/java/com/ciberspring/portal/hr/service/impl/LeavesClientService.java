package com.ciberspring.portal.hr.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class LeavesClientService {
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final String apiUrl;
    private final ObjectMapper objectMapper;
    
    public LeavesClientService(OAuth2AuthorizedClientService authorizedClientService,
            @Value("${leaves.api.url}") String apiUrl,
            ObjectMapper objectMapper) {
        this.authorizedClientService = authorizedClientService;
        this.apiUrl = apiUrl;
        this.objectMapper = objectMapper;
    }
    
    public String getLeaves(OAuth2AuthenticationToken authentication) {
        String accessToken = getUserAccessToken(authentication);
        HttpHeaders headers = createHeaders(accessToken);
       
        try {
            ResponseEntity<String> response = new RestTemplate()
                    .exchange(apiUrl + "/leaves", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        
            // Validate JSON response
            objectMapper.readTree(response.getBody());
            return response.getBody();
        } catch (Exception e) {
            return "[]"; // Return empty array if invalid JSON
        }
    }
    public String getLeaveDetails(Long employeeId, OAuth2AuthenticationToken authentication) {
        String accessToken = getUserAccessToken(authentication);
        HttpHeaders headers = createHeaders(accessToken);
        
        try {
            String url = apiUrl + "/leaves/leaveDetails/" + employeeId;
            System.out.println("DEBUG: Calling URL: " + url);
            
            ResponseEntity<String> response = new RestTemplate()
                    .exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            
            System.out.println("DEBUG: Response status: " + response.getStatusCode());
            System.out.println("DEBUG: Response body: " + response.getBody());
            
            // Handle null response body
            if (response.getBody() == null) {
                return "{\"error\": \"No data found for employee ID: " + employeeId + "\"}";
            }
            
            // Validate JSON response
            objectMapper.readTree(response.getBody());
            return response.getBody();
        } catch (Exception e) {
            System.out.println("DEBUG: Error occurred: " + e.getMessage());
            e.printStackTrace();
            return "{\"error\": \"Unable to fetch leave details: " + e.getMessage().replace("\"", "\\\"") + "\"}";
        }
    }

 
    public String getAllLeaveDetails(OAuth2AuthenticationToken authentication) {
        String accessToken = getUserAccessToken(authentication);
        HttpHeaders headers = createHeaders(accessToken);
       
        try {
            ResponseEntity<String> response = new RestTemplate()
                    .exchange(apiUrl + "/leaves/allLeaveDetails", 
                             HttpMethod.GET, new HttpEntity<>(headers), String.class);
        
            // Validate JSON response
            objectMapper.readTree(response.getBody());
            return response.getBody();
        } catch (Exception e) {
            return "[]"; // Return empty array if invalid JSON or error
        }
    }
    
    private String getUserAccessToken(OAuth2AuthenticationToken authentication) {
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                authentication.getAuthorizedClientRegistrationId(),
                authentication.getName()
        );
        OAuth2AccessToken token = client.getAccessToken();
        return token.getTokenValue();
    }

    private HttpHeaders createHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}