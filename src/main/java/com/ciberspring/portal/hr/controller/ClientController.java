package com.ciberspring.portal.hr.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ciberspring.portal.hr.service.impl.AllEmployeesLeaveBalanceService;
import com.ciberspring.portal.hr.service.impl.EmployeeClientService;
import com.ciberspring.portal.hr.service.impl.LeavesClientService;
import com.ciberspring.portal.hr.service.impl.MyLeavesClientService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@RequestMapping("/client")
public class ClientController {

	private final EmployeeClientService employeeClientService;
	private final LeavesClientService leavesClientService;
	private final MyLeavesClientService myLeavesClientService;
	private final AllEmployeesLeaveBalanceService allEmployeesLeaveBalanceService;
	private final ObjectMapper objectMapper;

	public ClientController(EmployeeClientService employeeClientService, LeavesClientService leavesClientService,
			AllEmployeesLeaveBalanceService allEmployeesLeaveBalanceService,
			MyLeavesClientService myLeavesClientService, ObjectMapper objectMapper) {
		this.employeeClientService = employeeClientService;
		this.leavesClientService = leavesClientService;
		this.myLeavesClientService = myLeavesClientService;
		this.allEmployeesLeaveBalanceService = allEmployeesLeaveBalanceService;
		this.objectMapper = objectMapper;
	}

	@GetMapping("/employees")
	public String getAllEmployees() {
		return "employees";
	}

	@GetMapping("/localholidaylist")
	public String getAllLeaves() {
		return "localholidaylist";
	}

	@GetMapping("/add-employee")
	public String showAddEmployeeForm() {
		return "add-employee";
	}

	@GetMapping("/employees/json")
	@ResponseBody
	public String getEmployeesJson(OAuth2AuthenticationToken authentication) {
		return employeeClientService.getEmployees(authentication);
	}

	@GetMapping("/localholidaylist/json")
	@ResponseBody
	public String getLeavesJson(OAuth2AuthenticationToken authentication) {
		return leavesClientService.getLeaves(authentication);
	}

	@GetMapping("/employees/{id}")
	public String getEmployeeById(@PathVariable String id, OAuth2AuthenticationToken authentication, Model model) {
		try {
			String employeeJson = employeeClientService.getEmployeeById(id, authentication);
			JsonNode employeeNode = objectMapper.readTree(employeeJson);
			model.addAttribute("employee", employeeNode);
			model.addAttribute("employeeJson", employeeJson);
			model.addAttribute("employeeId", id);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "employee-detail";
	}

	@GetMapping("/myleaves")
	public String getMyLeavesByEmail(OAuth2AuthenticationToken authentication, Model model) {
		try {
			// Get the logged-in user's email from Okta
			OidcUser user = (OidcUser) authentication.getPrincipal();
			String email = user.getEmail(); // This gets the email from Okta token
			System.out.println(email);

			if (email == null || email.isEmpty()) {
				throw new RuntimeException("Email not found in user token");
			}

			// Call the service with the email
			String leavesJson = myLeavesClientService.getLeaveBalancesByEmail(email, authentication);
			JsonNode leavesNode = objectMapper.readTree(leavesJson);

			model.addAttribute("leaves", leavesNode);
			model.addAttribute("leavesJson", leavesJson);
			model.addAttribute("email", email);

		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("error", "Failed to fetch your leave balances: " + e.getMessage());
		}
		return "my-leaves-detail";
	}

	@GetMapping("/myleaves/json")
	@ResponseBody
	public String getMyLeavesJsonByEmail(OAuth2AuthenticationToken authentication) {
		try {
			OidcUser user = (OidcUser) authentication.getPrincipal();
			String email = user.getEmail();

			System.out.println("=== OKTA EMAIL DEBUG ===");
			System.out.println("Okta email: " + email);
			System.out.println("All user attributes: " + user.getAttributes());

			// Also check other possible email fields
			String preferredUsername = user.getPreferredUsername();
			System.out.println("Preferred username: " + preferredUsername);

			if (email == null || email.isEmpty()) {
				return "{\"error\": \"Email not found in user token\"}";
			}

			String result = myLeavesClientService.getLeaveBalancesByEmail(email, authentication);
			System.out.println("Service response: " + result);
			return result;

		} catch (Exception e) {
			e.printStackTrace();
			return "{\"error\": \"Failed to fetch leave balances: " + e.getMessage() + "\"}";
		}
	}

	@GetMapping("/AllLeaveBalance")
	public String getAllEmployeesLeaves() {
		return "allEmployeesLeave";
	}

	@GetMapping("/AllLeaveBalance/json")
	@ResponseBody
	public String getAllEmployeesLeavesJson(OAuth2AuthenticationToken authentication) {
		return allEmployeesLeaveBalanceService.getEmployeesLeaveBalance(authentication);
	}

	/**
	 * Handle Google Places API address predictions
	 */
	@GetMapping("/address/predictions")
	@ResponseBody
	public String getAddressPredictions(@RequestParam String input, OAuth2AuthenticationToken authentication) {
		try {
			// You can add authentication checks here if needed
			return employeeClientService.getAddressPredictions(input, authentication);
		} catch (Exception e) {
			e.printStackTrace();
			return "{\"error\": \"Failed to fetch address predictions: " + e.getMessage() + "\"}";
		}
	}

	/**
	 * Handle Google Places API address details
	 */
	@GetMapping("/address/details")
	@ResponseBody
	public String getAddressDetails(@RequestParam String placeId, OAuth2AuthenticationToken authentication) {
		try {
			return employeeClientService.getAddressDetails(placeId, authentication);
		} catch (Exception e) {
			e.printStackTrace();
			return "{\"error\": \"Failed to fetch address details: " + e.getMessage() + "\"}";
		}
	}

	@PostMapping("/employees/create")
	@ResponseBody
	public String createEmployee(@RequestBody Map<String, String> employeeData,
			OAuth2AuthenticationToken authentication) {
		try {
			System.out.println("=== CREATE EMPLOYEE DEBUG ===");
			System.out.println("Employee Data: " + employeeData);
			System.out.println("Authentication: " + (authentication != null ? "Present" : "Null"));
			
			if (authentication != null) {
				System.out.println("User: " + authentication.getName());
				System.out.println("Authorities: " + authentication.getAuthorities());
			}
			System.out.println("=== END DEBUG ===");
			
			return employeeClientService.createEmployee(employeeData, authentication);
		} catch (Exception e) {
			e.printStackTrace();
			return "{\"error\": \"Failed to create employee: " + e.getMessage() + "\"}";
		}
	}
	
	@GetMapping("/personal-details")
	public String redirectToPersonalDetails(OAuth2AuthenticationToken authentication) {
	    try {
	        // Get the logged-in user's email from Okta
	        OidcUser user = (OidcUser) authentication.getPrincipal();
	        String email = user.getEmail();
	        
	        if (email == null || email.isEmpty()) {
	            throw new RuntimeException("Email not found in user token");
	        }

	        // Get access token
	        String accessToken = employeeClientService.getUserAccessToken(authentication);
	        
	        System.out.println("=== PERSONAL DETAILS REDIRECT ===");
	        System.out.println("User Email: " + email);
	        System.out.println("Access Token available: " + (accessToken != null));
	        System.out.println("=== END DEBUG ===");
	        
	        if (accessToken == null) {
	            throw new RuntimeException("Access token not available");
	        }

	        // URL encode both email and token
	        String encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8);
	        String encodedToken = URLEncoder.encode(accessToken, StandardCharsets.UTF_8);
	        
	        // Redirect with both email and token parameters
	        return "redirect:http://localhost:8081/personal-details?email=" + encodedEmail + "&token=" + encodedToken;
	        
	    } catch (Exception e) {
	        e.printStackTrace();
	        return "redirect:/home?error=Failed to access personal details: " + e.getMessage();
	    }
	}

	@GetMapping("/personal-details-with-token")
	public String personalDetailsWithToken(OAuth2AuthenticationToken authentication, Model model) {
	    try {
	        OidcUser user = (OidcUser) authentication.getPrincipal();
	        String email = user.getEmail();
	        String accessToken = employeeClientService.getUserAccessToken(authentication);
	        
	        model.addAttribute("accessToken", accessToken);
	        model.addAttribute("userEmail", email);
	        model.addAttribute("targetUrl", "http://localhost:8081/personal-details");
	        
	        return "token-redirect";
	        
	    } catch (Exception e) {
	        e.printStackTrace();
	        return "redirect:/home?error=Failed to access personal details: " + e.getMessage();
	    }
	}

}