package com.aziarets.vividapp.util;

import com.aziarets.vividapp.model.Password;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Component
public class PasswordGenerator {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public String getRandomPassword() {
        Password password = null;
        String passwordGeneratorURL = "https://makemeapassword.ligos.net/api/v1/alphanumeric/json?c=1&l=5";
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(passwordGeneratorURL, String.class);
        try {
            password = objectMapper.readValue(responseEntity.getBody(), Password.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (password != null) {
            return password.getValues().get(0);
        }
        return "default";
    }
}
