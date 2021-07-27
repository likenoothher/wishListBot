package com.aziarets.vividapp.util;

import com.aziarets.vividapp.model.Password;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Component
public class PasswordGenerator {
    private static final Logger logger = LoggerFactory.getLogger(PasswordGenerator.class);

    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public PasswordGenerator(RestTemplate restTemplate, ObjectMapper objectMapper, PasswordEncoder passwordEncoder) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.passwordEncoder = passwordEncoder;
    }

    private String getRandomPassword() {
        logger.info("Handle get password request");
        Password password = null;
        String passwordGeneratorURL = "https://makemeapassword.ligos.net/api/v1/alphanumeric/json?c=1&l=5";
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(passwordGeneratorURL, String.class);
        try {
            password = objectMapper.readValue(responseEntity.getBody(), Password.class);
        } catch (IOException e) {
            logger.warn("IOException during parsing JSON answer: " + e.getMessage());
        }
        if (password != null && password.getValues().size() >= 1) {
            logger.info("Return generated password");
            return password.getValues().get(0);
        }
        logger.warn("Password is null. Return default value");
        return "def";
    }

    public String getEncodedPassword(){
        return passwordEncoder.encode(getRandomPassword());
    }
}
