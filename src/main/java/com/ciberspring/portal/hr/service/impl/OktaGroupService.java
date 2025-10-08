package com.ciberspring.portal.hr.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
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
public class OktaGroupService {

    private final OAuth2AuthorizedClientService authorizedClientService;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${okta.api.url}")
    private String oktaApiUrl;

    @Value("${okta.api.token}")
    private String oktaApiToken;

    public OktaGroupService(OAuth2AuthorizedClientService authorizedClientService, ObjectMapper objectMapper) {
        this.authorizedClientService = authorizedClientService;
        this.objectMapper = objectMapper;
    }

    /**
     * Get user groups using Okta API (requires API token)
     */
    public List<String> getUserGroups(String userId) {
        String url = oktaApiUrl + "/api/v1/users/" + userId + "/groups";

        HttpHeaders headers = createHeadersWithApiToken();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {}
        );

        return response.getBody().stream()
                .map(group -> (String) ((Map<String, Object>) group.get("profile")).get("name"))
                .collect(Collectors.toList());
    }

    /**
     * Get user groups using OAuth2 access token (from logged-in user)
     */
    public List<String> getUserGroups(OAuth2AuthenticationToken authentication) {
        String accessToken = getUserAccessToken(authentication);
        String userId = getUserIdFromAuthentication(authentication);
        
        String url = oktaApiUrl + "/api/v1/users/" + userId + "/groups";

        HttpHeaders headers = createHeadersWithAccessToken(accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {}
        );

        return response.getBody().stream()
                .map(group -> (String) ((Map<String, Object>) group.get("profile")).get("name"))
                .collect(Collectors.toList());
    }

    /**
     * Get access token from OAuth2 authentication
     */
    private String getUserAccessToken(OAuth2AuthenticationToken authentication) {
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                authentication.getAuthorizedClientRegistrationId(),
                authentication.getName()
        );
        OAuth2AccessToken token = client.getAccessToken();
        return token.getTokenValue();
    }

    /**
     * Get user ID from authentication
     */
    private String getUserIdFromAuthentication(OAuth2AuthenticationToken authentication) {
        // Get user ID from the principal (OidcUser)
        return authentication.getPrincipal().getName(); // or get subject from claims
    }

    /**
     * Create headers with API token (for server-to-server calls)
     */
    private HttpHeaders createHeadersWithApiToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(oktaApiToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    /**
     * Create headers with OAuth2 access token (for user-context calls)
     */
    private HttpHeaders createHeadersWithAccessToken(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    /**
     * Get user groups with fallback - tries access token first, then API token
     */
    public List<String> getUserGroupsWithFallback(OAuth2AuthenticationToken authentication, String userId) {
        try {
            // First try with OAuth2 access token
            return getUserGroups(authentication);
        } catch (Exception e) {
            // Fallback to API token method
            return getUserGroups(userId);
        }
    }

    /**
     * Check if user has specific group
     */
    public boolean hasGroup(OAuth2AuthenticationToken authentication, String groupName) {
        List<String> groups = getUserGroups(authentication);
        return groups.stream().anyMatch(group -> group.equalsIgnoreCase(groupName));
    }

    /**
     * Check if user has any of the specified groups
     */
    public boolean hasAnyGroup(OAuth2AuthenticationToken authentication, List<String> groupNames) {
        List<String> groups = getUserGroups(authentication);
        return groups.stream()
                .anyMatch(userGroup -> groupNames.stream()
                        .anyMatch(requiredGroup -> requiredGroup.equalsIgnoreCase(userGroup)));
    }
}