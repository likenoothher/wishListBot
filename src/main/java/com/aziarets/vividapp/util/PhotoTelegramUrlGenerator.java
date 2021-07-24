package com.aziarets.vividapp.util;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.telegram.telegrambots.meta.api.objects.UserProfilePhotos;

import java.io.IOException;

@Component
@PropertySource(value = {"classpath:application.properties"})
public class PhotoTelegramUrlGenerator {
    private static final Logger logger = LoggerFactory.getLogger(PhotoTelegramUrlGenerator.class);

    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;
    private String telegramPhotoPathURL;
    private String telegramDownloadFileURL;
    private String telegramGetAvatarIdURL;

    @Autowired
    public PhotoTelegramUrlGenerator(RestTemplate restTemplate, ObjectMapper objectMapper,
                                     @Value("${telegram.photo_path_url}") String photoPathUrl,
                                     @Value("${telegram.download_photo_url}") String fileURL,
                                     @Value("${telegram.avatar_id_path}") String avatarURL) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.telegramPhotoPathURL = photoPathUrl;
        this.telegramDownloadFileURL = fileURL;
        this.telegramGetAvatarIdURL = avatarURL;
    }

    public String getGiftPhotoTelegramURL(String photoIdInTelegram) {
        logger.info("Start to create link for gift's picture with telegram id: " +
            photoIdInTelegram);
        String telegramGetPhotoPathURL = this.telegramPhotoPathURL + photoIdInTelegram;

        ResponseEntity<String> responseEntity = restTemplate.getForEntity(telegramGetPhotoPathURL, String.class);
        try {
            ObjectNode objectNode = objectMapper.readValue(responseEntity.getBody(), ObjectNode.class);

            if (objectNode.has("result")) {
                logger.info("Return link of gift's picture with telegram id: " +
                    photoIdInTelegram);
                return this.telegramDownloadFileURL + objectNode.findValue("file_path").asText();
            }
        } catch (IOException e) {
            logger.warn("Exception during creating link of gift's picture with telegram id: " + photoIdInTelegram
                + "\n" + e.getMessage());
        }
        logger.warn("Exception during creating link of gift's picture with telegram id: " + photoIdInTelegram
            + ". JSON answer doesn't contain \"result\" field");
        return null;
    }

    public String getUserAvatarPhotoTelegramURL(long userTelegramId) {
        logger.info("Start to create link for user's avatar with telegram id: " + userTelegramId);

        try {
            String telegramAvatarIdURL = telegramGetAvatarIdURL + userTelegramId;
            ResponseEntity<String> avatarIDResponseEntity = restTemplate.getForEntity(telegramAvatarIdURL, String.class);

            ObjectNode avatarObjectNode = objectMapper.readValue(avatarIDResponseEntity.getBody(), ObjectNode.class);
            if( avatarObjectNode.findValue("photos").isEmpty() ) {
                logger.info("User with telegram id " + userTelegramId + " doesn't have any photo. Return null");
                return null;
            }
            String telegramAvatarDownloadURL = telegramPhotoPathURL
                + avatarObjectNode.findValue("photos").get(0).findValue("file_id").asText();

            ResponseEntity<String> avatarPathsResponseEntity =
                restTemplate.getForEntity(telegramAvatarDownloadURL, String.class);

            ObjectNode avatarPathsObjectNode =
                objectMapper.readValue(avatarPathsResponseEntity.getBody(), ObjectNode.class);

            if (avatarPathsObjectNode.has("result")) {
                logger.info("Return link for user's avatar with telegram id: " +
                    userTelegramId);
                return this.telegramDownloadFileURL + avatarPathsObjectNode.findValue("file_path").asText();
            }

        } catch (JsonProcessingException e) {
            logger.warn(e.getMessage());
        } catch (Exception e){
            logger.warn(e.getMessage());
        }

        logger.warn("Exception during creating link of avatar's picture for user with telegram id: " + userTelegramId);
        return null;

    }
}
