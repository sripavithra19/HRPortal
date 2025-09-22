package com.ciberspring.portal.hr.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAdvice {

    @ModelAttribute("user")
    public OidcUser oidcUser(@AuthenticationPrincipal OidcUser user) {
        return user; // available as ${user} in every Thymeleaf template
    }
}