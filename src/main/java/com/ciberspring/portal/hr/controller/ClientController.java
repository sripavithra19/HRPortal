package com.ciberspring.portal.hr.controller;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ciberspring.portal.hr.service.impl.EmployeeClientService;
import com.ciberspring.portal.hr.service.impl.LeavesClientService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@RequestMapping("/client")
public class ClientController {

	private final EmployeeClientService employeeClientService;
	private final LeavesClientService leavesClientService;
	private final ObjectMapper objectMapper;

	public ClientController(EmployeeClientService employeeClientService,LeavesClientService leavesClientService, ObjectMapper objectMapper) {
		this.employeeClientService = employeeClientService;
		this.leavesClientService=leavesClientService;
		this.objectMapper = objectMapper;
	}

	@GetMapping("/employees")
	public String getAllEmployees(OAuth2AuthenticationToken authentication, Model model) {
		// Just return the view, data will be loaded via AJAX
		return "employees";
	}
   
	@GetMapping("/leaves")
	public String getAllLeaves(OAuth2AuthenticationToken authentication, Model model) {
		// Just return the view, data will be loaded via AJAX
		return "leaves";
	}
	@GetMapping("/employees/json")
	@ResponseBody
	public String getEmployeesJson(OAuth2AuthenticationToken authentication) {
		String response = employeeClientService.getEmployees(authentication);
		System.out.println("API Response: " + response); 
		return response;
	}
	
	@GetMapping("/leaves/json")
	@ResponseBody
	public String getLeavesJson(OAuth2AuthenticationToken authentication) {
		String response = leavesClientService.getLeaves(authentication);
		return response;
	}

	@GetMapping("/employees/{id}")
	public String getEmployeeById(@PathVariable String id, OAuth2AuthenticationToken authentication, Model model) {
		System.out.println("Fetching employee with ID: " + id);

		String employeeJson = employeeClientService.getEmployeeById(id, authentication);
		System.out.println("Raw API response: " + employeeJson);

		try {
			JsonNode employeeNode = objectMapper.readTree(employeeJson);
			System.out.println("Parsed JSON: " + employeeNode.toString());

			model.addAttribute("employee", employeeNode);
			model.addAttribute("employeeJson", employeeJson); // The JSON string
			model.addAttribute("employeeId", id);

			return "employee-detail";
		} catch (Exception e) {
			System.err.println("Error parsing JSON: " + e.getMessage());
			e.printStackTrace();

			// Still add the raw JSON even if parsing failed
			model.addAttribute("employeeJson", employeeJson);
			model.addAttribute("employeeId", id);
			return "employee-detail";
		}
	}

}