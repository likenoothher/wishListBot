package com.aziarets.vividapp.util;

import com.aziarets.vividapp.model.BotUser;
import com.aziarets.vividapp.model.Gift;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class PhotoManager {
    private static final Logger logger = LoggerFactory.getLogger(PhotoManager.class);

    private PhotoTelegramUrlGenerator photoTelegramUrlGenerator;
    private Cloudinary cloudinary;

    @Autowired
    public PhotoManager(PhotoTelegramUrlGenerator photoTelegramUrlGenerator, Cloudinary cloudinary) {
        this.photoTelegramUrlGenerator = photoTelegramUrlGenerator;
        this.cloudinary = cloudinary;
    }

    private Map uploadPhoto(File file) throws IOException {
        logger.info("Uploading file with name: " + file.getName());
        return cloudinary.uploader().upload(file, ObjectUtils.emptyMap());
    }

    public Map deletePhoto(Gift gift) {
        logger.info("Deleting photo of gift with id: " + gift.getId());
        if (gift.getGiftPhotoCloudinaryId() == null) {
            logger.info("Gift with id: " + gift.getId() + " doesn't have a photo. Return");
            return Collections.emptyMap();
        }
        try {
            return cloudinary.uploader().destroy(gift.getGiftPhotoCloudinaryId(), cloudinary.config.properties);
        } catch (IOException e) {
            logger.warn("Exception during deleting photo of gift with id" + gift.getId());
        }
        return Collections.emptyMap();
    }

    public boolean assignGiftPhotoParameters(Gift gift, List<PhotoSize> photoSizes) {
        logger.info("Create link to photo of gift with id : " + gift.getId());
        URL photoUrlInTelegram = null;
        Map uploadPhotoResponse = null;
        String photoIdInTelegram = photoSizes.get(photoSizes.size() - 1).getFileId();
        try {
            photoUrlInTelegram = new URL(photoTelegramUrlGenerator.getGiftPhotoTelegramURL(photoIdInTelegram));
        } catch (MalformedURLException e) {
            logger.warn("Exception during creating photo URL in telegram: " + e.getMessage());
        }
        File file = new File("giftId" + gift.getId());
        try {
            file.createNewFile();
            FileUtils.copyURLToFile(photoUrlInTelegram, file);
            uploadPhotoResponse = uploadPhoto(file);
        } catch (IOException e) {
            logger.warn("Exception during copying photo from telegram: " + e.getMessage());
        } finally {
            file.delete();
        }
        if (uploadPhotoResponse != null && uploadPhotoResponse.containsKey("url")
            && uploadPhotoResponse.containsKey("public_id")) {
            logger.info("Return link to photo of gift with id : " + gift.getId());
            gift.setGiftPhotoURL(uploadPhotoResponse.get("url").toString());
            gift.setGiftPhotoCloudinaryId(uploadPhotoResponse.get("public_id").toString());
            return true;
        } else {
            logger.warn("Bad upload photo response. Return null");
            return false;
        }
    }

    public boolean assignGiftPhotoParameters(Gift gift, MultipartFile uploadFile) {
        logger.info("Create link to photo of gift with id : " + (gift.getId() == 0 ? " new gift" : gift.getId()));
        Map uploadPhotoResponse = null;
        File file = null;
        try {
            file = File.createTempFile(uploadFile.getOriginalFilename(), "/");
            uploadFile.transferTo(file);
        } catch (IOException e) {
            logger.warn("Exception during transferring multipart file to file: " + e.getMessage());
        }
        try {
            uploadPhotoResponse = uploadPhoto(file);
        } catch (IOException e) {
            logger.warn("Exception during uploading photo to cloudinary: " + e.getMessage());
        } finally {
            file.delete();
        }
        if (uploadPhotoResponse != null && uploadPhotoResponse.containsKey("url")
            && uploadPhotoResponse.containsKey("public_id")) {
            logger.info("Return link to photo of gift with id : " + (gift.getId() == 0 ? " new gift" : gift.getId()));
            gift.setGiftPhotoURL(uploadPhotoResponse.get("url").toString());
            gift.setGiftPhotoCloudinaryId(uploadPhotoResponse.get("public_id").toString());
            return true;
        } else {
            logger.warn("Bad upload photo response. Return null");
            return false;
        }
    }

    public String getAvatarPhotoURL(long telegramId) {
        logger.info("Create link to avatar of user with telegram id : " + telegramId);
        Map uploadPhotoResponse = null;
        URL photoUrlInTelegram = null;
        String userAvatarPhotoTelegramURL = photoTelegramUrlGenerator.getUserAvatarPhotoTelegramURL(telegramId);
        if (userAvatarPhotoTelegramURL == null) {
            return null;
        }

        try {
            photoUrlInTelegram = new URL(userAvatarPhotoTelegramURL);
        } catch (MalformedURLException e) {
            logger.warn("Exception during creating avatar URL for user with telegram id : " + telegramId + ". Message:"
                + e.getMessage());
        }
        File file = new File("avatar");
        try {
            file.createNewFile();
            FileUtils.copyURLToFile(photoUrlInTelegram, file);
            uploadPhotoResponse = uploadPhoto(file);
        } catch (IOException e) {
            logger.warn("Exception during copying photo from telegram: " + e.getMessage());
        } finally {
            file.delete();
        }
        if (uploadPhotoResponse != null && uploadPhotoResponse.containsKey("url")
            && uploadPhotoResponse.containsKey("public_id")) {
            logger.info("Return link to avatar photo of user with telegram id : " + telegramId);
            return (uploadPhotoResponse.get("url").toString());
        } else {
            logger.warn("Bad upload photo response. Return null");
            return "false";
        }
    }
}
