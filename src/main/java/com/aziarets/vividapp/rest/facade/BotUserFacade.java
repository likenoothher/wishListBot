package com.aziarets.vividapp.rest.facade;

import com.aziarets.vividapp.model.BotUser;
import com.aziarets.vividapp.rest.dto.BotUserDTO;
import org.springframework.stereotype.Component;

@Component
public class BotUserFacade {

    public BotUserDTO botUserToBotUserDTO(BotUser user) {
        BotUserDTO userDTO = new BotUserDTO();
        userDTO.setId(user.getId());
        userDTO.setUserName(user.getUserName());
        userDTO.setFirstName(user.getFirstName());
        userDTO.setLastName(user.getLastName());
        userDTO.setUserAvatarPhotoURL(user.getUserAvatarPhotoURL());

        return userDTO;
    }
}
