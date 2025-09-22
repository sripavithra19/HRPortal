package com.ciberspring.portal.hr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@Controller
public class HRPortalApplication {

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/home")
    public String index() {
        return "home";
    }
   

    public static void main(String[] args) {
        SpringApplication.run(HRPortalApplication.class, args);
    }
}
