package com.aziarets.vividapp.rest.facade;

import com.aziarets.vividapp.model.Gift;
import com.aziarets.vividapp.rest.dto.GiftDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GiftFacade {

    private BotUserFacade botUserFacade;

    @Autowired
    public GiftFacade(BotUserFacade botUserFacade) {
        this.botUserFacade = botUserFacade;
    }

    public GiftDTO giftToGiftDTO(Gift gift) {
        GiftDTO giftDTO = new GiftDTO();
        giftDTO.setId(gift.getId());
        giftDTO.setName(gift.getName());
        giftDTO.setDescription(gift.getDescription());
        giftDTO.setGiftPhotoURL(gift.getGiftPhotoURL());
        if (gift.getOccupiedBy() != null) {
            giftDTO.setOccupiedBy(botUserFacade.botUserToBotUserDTO(gift.getOccupiedBy()));
        }

        return giftDTO;

    }
}
