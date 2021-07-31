package com.aziarets.vividapp.rest.dto;


import lombok.Data;

@Data
public class GiftDTO {
    private long id;
    private String name;
    private String url;
    private BotUserDTO occupiedBy;
    private String description;
    private String giftPhotoURL;
}
