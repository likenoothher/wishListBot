package com.aziarets.vividapp.util;

import com.aziarets.vividapp.model.Gift;
import com.aziarets.vividapp.model.Password;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Component
public class GiftTelegramUrlGenerator {

    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;

    @Autowired
    public GiftTelegramUrlGenerator(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public String getGiftPhotoTelegramURL(String giftPhotoTelegramId) {
        String telegramGetPhotoPathURL =
            "https://api.telegram.org/bot1899375504:AAE-M6_miu3OytFn9pt_otNdniFK82Pb7Kg/getFile?file_id=" +
                giftPhotoTelegramId;

        ResponseEntity<String> responseEntity = restTemplate.getForEntity(telegramGetPhotoPathURL, String.class);
        try {
            ObjectNode objectNode = objectMapper.readValue(responseEntity.getBody(), ObjectNode.class);
            System.out.println(objectNode.get("result"));
            if(objectNode.has("result")){
                return "https://api.telegram.org/file/bot1899375504:AAE-M6_miu3OytFn9pt_otNdniFK82Pb7Kg/"
                    + objectNode.findValue("file_path").asText();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
