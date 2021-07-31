package com.aziarets.vividapp.rest.dto;

import lombok.Data;

@Data
public class BotUserDTO {
    private long id;
    private String userName;
    private String firstName;
    private String lastName;
    private String userAvatarPhotoURL;
}
