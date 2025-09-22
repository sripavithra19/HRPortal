package com.ciberspring.portal.hr.service.impl;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class MyLeavesClientService {

    private final OAuth2AuthorizedClientService authorizedClientService;
    private final String apiUrl;
    private final ObjectMapper objectMapper;
    
    public MyLeavesClientService(OAuth2AuthorizedClientService authorizedClientService,
            @Value("${leaves.api.url}") String apiUrl,
            ObjectMapper objectMapper) {
        this.authorizedClientService = authorizedClientService;
        this.apiUrl = apiUrl;
        this.objectMapper = objectMapper;
    }
    public String getLeaveBalancesByEmail(String email, OAuth2AuthenticationToken authentication) {
        String accessToken = getUserAccessToken(authentication);
        HttpHeaders headers = createHeaders(accessToken);
        
        try {
            // Use UriComponentsBuilder for proper URL construction
            String url = UriComponentsBuilder.fromHttpUrl(apiUrl)
                .path("/myLeaves/by-email")
                .queryParam("email", email)
                .toUriString();
            
            System.out.println("Calling URL: " + url);
            
            ResponseEntity<String> response = new RestTemplate()
                .exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            
            System.out.println("Response status: " + response.getStatusCode());
            System.out.println("Response body: " + response.getBody());
            
            objectMapper.readTree(response.getBody());
            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"Failed to fetch leave balances: " + e.getMessage() + "\"}";
        }
    }
    
    public String getLeaveBalancesById(String id, OAuth2AuthenticationToken authentication) {
        String accessToken = getUserAccessToken(authentication);
        HttpHeaders headers = createHeaders(accessToken);
        
        try {
            ResponseEntity<String> response = new RestTemplate()
                .exchange(apiUrl + "/myLeaves/" + id, 
                         HttpMethod.GET, 
                         new HttpEntity<>(headers), 
                         String.class);
            
            // Validate JSON response
            objectMapper.readTree(response.getBody());
            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"Failed to fetch leave balances: " + e.getMessage() + "\"}";
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