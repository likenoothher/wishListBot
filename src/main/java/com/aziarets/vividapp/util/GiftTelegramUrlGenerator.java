package com.aziarets.vividapp.util;

import com.aziarets.vividapp.model.Gift;
import com.aziarets.vividapp.model.Password;
import com.aziarets.vividapp.service.BotService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Component
@PropertySource(value= {"classpath:application.properties"})
public class GiftTelegramUrlGenerator {
    private static final Logger logger = LoggerFactory.getLogger(GiftTelegramUrlGenerator.class);

    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;
    private String telegramPhotoPathURL;
    private String telegramDownloadURL;

    @Autowired
    public GiftTelegramUrlGenerator(RestTemplate restTemplate, ObjectMapper objectMapper,
                                    @Value("${telegram.photo_path_url}") String photoPathUrl,
                                    @Value("${telegram.download_photo_url}") String downloadURL) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.telegramPhotoPathURL = photoPathUrl;
        this.telegramDownloadURL = downloadURL;
    }

    public String getGiftPhotoTelegramURL(String photoIdInTelegram) {
        logger.info("Start to create link for gift's picture with telegram id: " +
            photoIdInTelegram);
        String telegramGetPhotoPathURL = this.telegramPhotoPathURL  + photoIdInTelegram;

        ResponseEntity<String> responseEntity = restTemplate.getForEntity(telegramGetPhotoPathURL, String.class);
        try {
            ObjectNode objectNode = objectMapper.readValue(responseEntity.getBody(), ObjectNode.class);

            if (objectNode.has("result")) {
                logger.info("Return link of gift's picture with telegram id: " +
                    photoIdInTelegram);
                return this.telegramDownloadURL + objectNode.findValue("file_path").asText();
            }
        } catch (IOException e) {
           logger.warn("Exception during creating link of gift's picture with telegram id: " + photoIdInTelegram
           + "\n" + e.getMessage());
        }
        logger.warn("Exception during creating link of gift's picture with telegram id: " + photoIdInTelegram
            + ". JSON answer doesn't contain \"result\" field");
        return null;
    }
}
