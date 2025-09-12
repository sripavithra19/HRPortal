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

	public ClientController(EmployeeClientService employeeClientService, LeavesClientService leavesClientService,
			ObjectMapper objectMapper) {
		this.employeeClientService = employeeClientService;
		this.leavesClientService = leavesClientService;
		this.objectMapper = objectMapper;
	}

	@GetMapping("/employees")
	public String getAllEmployees() {
		return "employees";
	}

	@GetMapping("/leaves")
	public String getAllLeaves() {
		return "leaves";
	}

	@GetMapping("/employees/json")
	@ResponseBody
	public String getEmployeesJson(OAuth2AuthenticationToken authentication) {
		return employeeClientService.getEmployees(authentication);
	}

	@GetMapping("/leaves/json")
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

	@GetMapping("/leaveDetails/{employeeId}")
	@ResponseBody
	public String getLeaveDetails(@PathVariable Long employeeId, OAuth2AuthenticationToken authentication) {
		return leavesClientService.getLeaveDetails(employeeId, authentication);
	}

	// NEW ENDPOINT: For all leave details (optional)
	@GetMapping("/allLeaveDetails")
	@ResponseBody
	public String getAllLeaveDetails(OAuth2AuthenticationToken authentication) {
		return leavesClientService.getAllLeaveDetails(authentication);
	}

	// NEW ENDPOINT: Page for leaves availed
	@GetMapping("/leaves-availed")
	public String getLeavesAvailedPage() {
		return "leaves-availed"; 
	}
}
