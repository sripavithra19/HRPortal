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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class AllEmployeesLeaveBalanceService {
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final String apiUrl;
    private final ObjectMapper objectMapper;

    public AllEmployeesLeaveBalanceService(OAuth2AuthorizedClientService authorizedClientService,
                                  @Value("${leaves.api.url}") String apiUrl,
                                  ObjectMapper objectMapper) {
        this.authorizedClientService = authorizedClientService;
        this.apiUrl = apiUrl;
        this.objectMapper = objectMapper;
    }
    public String getEmployeesLeaveBalance(OAuth2AuthenticationToken authentication) {
        try {
            String accessToken = getUserAccessToken(authentication);
            HttpHeaders headers = createHeaders(accessToken);
           
            ResponseEntity<String> response = new RestTemplate()
                    .exchange(apiUrl + "/AllLeaveBalance", HttpMethod.GET, new HttpEntity<>(headers), String.class);
            System.out.println("=== DEBUG: API Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Body: " + response.getBody());
            return response.getBody();
            
        } catch (HttpClientErrorException.Forbidden e) {
            // Handle 403 Forbidden specifically
            return """
            {
                "error": "access_denied",
                "message": "You are not allowed to access employee data. This is only for Management HR team access.",
                "contact": "Please contact HR administrator if you need access."
            }
            """;
        } catch (Exception e) {
        	System.out.println("=== DEBUG: Exception occurred ===");
            e.printStackTrace();
            return "{\"error\": \"Failed to fetch data: " + e.getMessage() + "\"}";
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
