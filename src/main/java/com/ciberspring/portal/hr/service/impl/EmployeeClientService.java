package com.ciberspring.portal.hr.service.impl;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class EmployeeClientService {

	  private final OAuth2AuthorizedClientService authorizedClientService;
	    private final String apiUrl;
	    private final ObjectMapper objectMapper;

	    public EmployeeClientService(OAuth2AuthorizedClientService authorizedClientService,
	            @Value("${employee.api.url}") String apiUrl, ObjectMapper objectMapper) {
	        this.authorizedClientService = authorizedClientService;
	        this.apiUrl = apiUrl;
	        this.objectMapper = objectMapper;
	    }

	public String getEmployees(OAuth2AuthenticationToken authentication) {
		try {
			String accessToken = getUserAccessToken(authentication);
			HttpHeaders headers = createHeaders(accessToken);

			ResponseEntity<String> response = new RestTemplate().exchange(apiUrl + "/employees", HttpMethod.GET,
					new HttpEntity<>(headers), String.class);

			return response.getBody();

		} catch (HttpClientErrorException.Forbidden e) {
			// Handle 403 Forbidden - user doesn't have required groups/scopes
			return """
					{
					    "error": "access_denied",
					    "message": "You are not allowed to access employee data. This requires HR_EMPLOYEES_ACCESS group or employees.read scope.",
					    "contact": "Please contact HR administrator if you need access."
					}
					""";
		} catch (Exception e) {
			System.out.println("=== DEBUG: Exception occurred ===");
			e.printStackTrace();
			return "{\"error\": \"Failed to fetch data: " + e.getMessage() + "\"}";
		}
	}

	public String getEmployeeById(String id, OAuth2AuthenticationToken authentication) {
		String accessToken = getUserAccessToken(authentication);
		HttpHeaders headers = createHeaders(accessToken);

		try {
			ResponseEntity<String> response = new RestTemplate().exchange(apiUrl + "/employees/" + id, HttpMethod.GET,
					new HttpEntity<>(headers), String.class);

			// Validate JSON response
			objectMapper.readTree(response.getBody());
			return response.getBody();
		} catch (Exception e) {
			return "{}"; // Return empty object if invalid JSON
		}
	}

	public String getAddressPredictions(String input, OAuth2AuthenticationToken authentication) {
		try {
			String accessToken = getUserAccessToken(authentication);
			HttpHeaders headers = createHeaders(accessToken);

			// Call the Employee Management API for address predictions
			String url = apiUrl + "/employees/address/predictions?input="
					+ URLEncoder.encode(input, StandardCharsets.UTF_8);

			ResponseEntity<String> response = new RestTemplate().exchange(url, HttpMethod.GET,
					new HttpEntity<>(headers), String.class);

			return response.getBody();

		} catch (Exception e) {
			e.printStackTrace();
			return "{\"error\": \"Failed to fetch address predictions: " + e.getMessage() + "\"}";
		}
	}

	public String getAddressDetails(String placeId, OAuth2AuthenticationToken authentication) {
		try {
			String accessToken = getUserAccessToken(authentication);
			HttpHeaders headers = createHeaders(accessToken);

			// Call the Employee Management API for address details
			String url = apiUrl + "/employees/address/details?placeId="
					+ URLEncoder.encode(placeId, StandardCharsets.UTF_8);

			ResponseEntity<String> response = new RestTemplate().exchange(url, HttpMethod.GET,
					new HttpEntity<>(headers), String.class);

			return response.getBody();

		} catch (Exception e) {
			e.printStackTrace();
			return "{\"error\": \"Failed to fetch address details: " + e.getMessage() + "\"}";
		}
	}

	
	public String createEmployee(Map<String, String> employeeData, OAuth2AuthenticationToken authentication) {
	    try {
	        System.out.println("=== EMPLOYEE CLIENT SERVICE - CREATE EMPLOYEE ===");
	        System.out.println("API URL: " + apiUrl);
	        System.out.println("Employee Data: " + employeeData);
	        
	        String accessToken = getUserAccessToken(authentication);
			System.out.println(accessToken);
	        
	        // Call the Employee Management API to create employee
			System.out.println("Access Token (first 100 chars): " + accessToken.substring(0, 100));
	        String url = apiUrl + "/employees";

	        // Convert map to JSON
	        ObjectMapper mapper = new ObjectMapper();
	        String requestBody = mapper.writeValueAsString(employeeData);
	        
	        System.out.println("Request URL: " + url);
	        System.out.println("Request Body: " + requestBody);

	        // Create headers (empty since security is disabled)
	       HttpHeaders headers = new HttpHeaders();
	       headers.setContentType(MediaType.APPLICATION_JSON);
	       headers.setBearerAuth(accessToken);
	       //HttpHeaders headers = createHeaders(accessToken);
	        
	        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

	        System.out.println("Sending POST request...");
	        
	        ResponseEntity<String> response = new RestTemplate().exchange(url, HttpMethod.POST, request, String.class);

	        System.out.println("Response Status: " + response.getStatusCode());
	        System.out.println("Response Body: " + response.getBody());
	        System.out.println("=== END DEBUG ===");

	        return response.getBody();

	    } catch (Exception e) {
	        System.out.println("=== ERROR IN EMPLOYEE CLIENT SERVICE ===");
	        System.out.println("Error: " + e.getMessage());
	        e.printStackTrace();
	        System.out.println("=== END ERROR ===");
	        return "{\"error\": \"Failed to create employee: " + e.getMessage() + "\"}";
	    }
	}

	public String getUserAccessToken(OAuth2AuthenticationToken authentication) {
		OAuth2AuthorizedClient client = authorizedClientService
				.loadAuthorizedClient(authentication.getAuthorizedClientRegistrationId(), authentication.getName());
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