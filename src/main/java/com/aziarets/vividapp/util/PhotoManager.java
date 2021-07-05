package com.aziarets.vividapp.util;

import com.aziarets.vividapp.model.Gift;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

    private GiftTelegramUrlGenerator giftTelegramUrlGenerator;

    @Autowired
    public PhotoManager(GiftTelegramUrlGenerator giftTelegramUrlGenerator) {
        this.giftTelegramUrlGenerator = giftTelegramUrlGenerator;
    }

    private final Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
        "cloud_name", "dirdwzm1g",
        "api_key", "434845759426257",
        "api_secret", "GcHlP3Kn5BIr4I3oZcLRRGMJVSs"));

    private Map uploadPhoto(File file) throws IOException {
        return cloudinary.uploader().upload(file, ObjectUtils.emptyMap());
    }

    public String getGiftPhotoLink(Gift gift,  List<PhotoSize> photoSizes){
        URL photoUrlInTelegram = null;
        Map uploadPhotoResponse = null;
        String photoIdInTelegram = photoSizes.get(photoSizes.size()-1).getFileId();
        try {
            photoUrlInTelegram = new URL(giftTelegramUrlGenerator.getGiftPhotoTelegramURL(photoIdInTelegram));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        File file = new File("src/main/webapp/resources/download/giftId" + gift.getId());
        try {
            file.createNewFile();
            FileUtils.copyURLToFile(photoUrlInTelegram, file);
            uploadPhotoResponse = uploadPhoto(file);
            file.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return uploadPhotoResponse.get("url").toString();
    }
}
