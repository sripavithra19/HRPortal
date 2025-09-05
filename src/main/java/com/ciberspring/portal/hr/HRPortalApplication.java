package com.ciberspring.portal.hr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@SpringBootApplication
@Controller
public class HRPortalApplication {
	@GetMapping("/")
	public String home() {
		return "home";
	}

	@GetMapping("/index")
	public String index() {
		return "index";
	}

	@GetMapping("/user")
	public String user(@AuthenticationPrincipal OidcUser user, Model model) {
		if (user == null) {
			return "redirect:/";
		}
		model.addAttribute("user", user);
		return "user";
	}

	public static void main(String[] args) {
		SpringApplication.run(HRPortalApplication.class, args);
	}
}