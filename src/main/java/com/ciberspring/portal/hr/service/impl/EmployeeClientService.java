package com.ciberspring.portal.hr.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class EmployeeClientService {

    private final OAuth2AuthorizedClientService authorizedClientService;
    private final String apiUrl;
    private final ObjectMapper objectMapper;

    public EmployeeClientService(OAuth2AuthorizedClientService authorizedClientService,
                                  @Value("${employee.api.url}") String apiUrl,
                                  ObjectMapper objectMapper) {
        this.authorizedClientService = authorizedClientService;
        this.apiUrl = apiUrl;
        this.objectMapper = objectMapper;
    }

    public String getEmployees(OAuth2AuthenticationToken authentication) {
        String accessToken = getUserAccessToken(authentication);
        HttpHeaders headers = createHeaders(accessToken);
       
        try {
            ResponseEntity<String> response = new RestTemplate()
                    .exchange(apiUrl + "/employees", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        
            // Validate JSON response
            objectMapper.readTree(response.getBody());
            return response.getBody();
        } catch (Exception e) {
            return "[]"; // Return empty array if invalid JSON
        }
    }

    public String getEmployeeById(String id, OAuth2AuthenticationToken authentication) {
        String accessToken = getUserAccessToken(authentication);
        HttpHeaders headers = createHeaders(accessToken);
        
        try {
            ResponseEntity<String> response = new RestTemplate()
                    .exchange(apiUrl + "/employees/" + id, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            
            // Validate JSON response
            objectMapper.readTree(response.getBody());
            return response.getBody();
        } catch (Exception e) {
            return "{}"; // Return empty object if invalid JSON
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