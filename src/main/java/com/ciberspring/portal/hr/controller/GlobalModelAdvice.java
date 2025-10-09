package com.ciberspring.portal.hr.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import java.util.Collections;
import java.util.List;

@ControllerAdvice
public class GlobalModelAdvice {

	private static final Logger logger = LoggerFactory.getLogger(GlobalModelAdvice.class);

	private static final List<String> HR_GROUP_NAMES = List.of("HR_EMPLOYEES_ACCESS", "HR_EMPLOYEE_ACCESS", "HR_ACCESS",
			"HR", "HR Employees Access", "HR_EMPLOYEES");

	@ModelAttribute("user")
	public OidcUser oidcUser(@AuthenticationPrincipal OidcUser user) {
		return user;
	}

	@ModelAttribute("userGroups")
	public List<String> userGroups(@AuthenticationPrincipal OidcUser user) {
		if (user != null) {
			List<String> groups = user.getClaimAsStringList("groups");
			if (groups == null) {
				groups = Collections.emptyList();
			}

			logger.info("User groups: {}", groups);
			return groups;
		}
		return Collections.emptyList();
	}

	@ModelAttribute("isHRUser")
	public boolean isHRUser(@AuthenticationPrincipal OidcUser user) {
		if (user != null) {
			List<String> groups = userGroups(user);
			boolean isHR = groups.stream()
					.anyMatch(group -> HR_GROUP_NAMES.stream().anyMatch(hrGroup -> hrGroup.equalsIgnoreCase(group)));

			// Enhanced logging
			logger.info("=== HR ACCESS DEBUG ===");
			logger.info("User: {}", user.getEmail());
			logger.info("All claims: {}", user.getClaims());
			logger.info("Groups found: {}", groups);
			logger.info("HR Group names we're checking: {}", HR_GROUP_NAMES);
			logger.info("HR Access granted: {}", isHR);
			logger.info("=== END DEBUG ===");

			return isHR;
		}
		return false;
	}
}