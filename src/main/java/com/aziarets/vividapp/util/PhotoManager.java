package com.aziarets.vividapp.util;

import com.aziarets.vividapp.model.Gift;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

@Component

public class PhotoManager {
    private static final Logger logger = LoggerFactory.getLogger(PhotoManager.class);

    private GiftTelegramUrlGenerator giftTelegramUrlGenerator;
    private Cloudinary cloudinary;

    @Autowired
    public PhotoManager(GiftTelegramUrlGenerator giftTelegramUrlGenerator, Cloudinary cloudinary) {
        this.giftTelegramUrlGenerator = giftTelegramUrlGenerator;
        this.cloudinary = cloudinary;
    }

    private Map uploadPhoto(File file) throws IOException {
        logger.info("Uploading file with name: " + file.getName());
        return cloudinary.uploader().upload(file, ObjectUtils.emptyMap());
    }

    public String getGiftPhotoLink(Gift gift, List<PhotoSize> photoSizes) {
        logger.info("Create link to photo of gift with id : " + gift.getId());
        URL photoUrlInTelegram = null;
        Map uploadPhotoResponse = null;
        String photoIdInTelegram = photoSizes.get(photoSizes.size() - 1).getFileId();
        try {
            photoUrlInTelegram = new URL(giftTelegramUrlGenerator.getGiftPhotoTelegramURL(photoIdInTelegram));
        } catch (MalformedURLException e) {
            logger.warn("Exception during creating photo URL in telegram: " + e.getMessage());
        }
        File file = new File("src/main/webapp/resources/download/giftId" + gift.getId());
        try {
            file.createNewFile();
            FileUtils.copyURLToFile(photoUrlInTelegram, file);
            uploadPhotoResponse = uploadPhoto(file);
            file.delete();
        } catch (IOException e) {
            logger.warn("Exception during copying photo from telegram: " + e.getMessage());
        }
        if (uploadPhotoResponse != null && uploadPhotoResponse.containsKey("url")) {
            logger.info("Return link to photo of gift with id : " + gift.getId());
            return uploadPhotoResponse.get("url").toString();
        } else {
            logger.warn("Bad upload photo response. Return null");
            return null;
        }


    }
}
