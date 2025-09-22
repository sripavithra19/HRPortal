package com.ciberspring.portal.hr.config;
 
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
 
import com.fasterxml.jackson.databind.ObjectMapper;
 
import static org.springframework.security.config.Customizer.withDefaults;
 
import org.springframework.beans.factory.annotation.Value;
 
@Configuration
@EnableWebSecurity
public class SecurityConfig {
	
	 @Value("${okta.domain}")
	    private String oktaDomain;
 
	    @Value("${app.redirect-uri}")
	    private String redirectUri;
 
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
				.oauth2Login(oauth2 -> oauth2.defaultSuccessUrl("/home", true) 
				).logout(logout -> logout.logoutSuccessHandler(oidcLogoutSuccessHandler()) 
				);
 
		return http.build();
	}
 
	@Bean
	public LogoutSuccessHandler oidcLogoutSuccessHandler() {
        return (request, response, authentication) -> {
            String idToken = "";
 
            if (authentication != null && authentication.getPrincipal() instanceof DefaultOidcUser oidcUser) {
                idToken = oidcUser.getIdToken().getTokenValue();
            }
 
            String logoutUrl = oktaDomain + "/oauth2/default/v1/logout?id_token_hint=" + idToken
                    + "&post_logout_redirect_uri=" + redirectUri;
 
            response.sendRedirect(logoutUrl);
        };
    }
}