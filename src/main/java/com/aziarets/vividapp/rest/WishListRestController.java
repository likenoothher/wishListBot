package com.aziarets.vividapp.rest;

import com.aziarets.vividapp.exception.IllegalOperationException;
import com.aziarets.vividapp.exceptionhandling.ApiResponse;
import com.aziarets.vividapp.model.BotUser;
import com.aziarets.vividapp.model.Gift;
import com.aziarets.vividapp.rest.dto.GiftDTO;
import com.aziarets.vividapp.rest.facade.GiftFacade;
import com.aziarets.vividapp.service.BotService;
import com.aziarets.vividapp.util.NotificationSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/wishlist")
public class WishListRestController {
    private static final Logger logger = LoggerFactory.getLogger(WishListRestController.class);

    private BotService botService;
    private NotificationSender notificationSender;


    @Autowired
    public WishListRestController(BotService botService, NotificationSender notificationSender) {
        this.botService = botService;
        this.notificationSender = notificationSender;
    }

    @GetMapping({"", "/"})
    public List<GiftDTO> getWishlist(Principal principal) {
        logger.info("User " + principal.getName() + " requests wishlist");
        BotUser user = botService.findUserByUserName(principal.getName()).get();

        List<Gift> gifts = botService.getUserWishListGifts(user.getId());
        List<GiftDTO> giftsDTO = botService.convertGiftListToDTO(gifts);

        logger.info("Returning wishlist of user with user name " + user.getId());
        return giftsDTO;
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addGift(@ModelAttribute GiftDTO giftDTO,
                                               @RequestParam(value = "image", required = false) MultipartFile file,
                                               Principal principal) {
        logger.info("User " + principal.getName() + " adds gift");
        Gift gift = Gift.GiftBuilder
            .newGift()
            .withName(giftDTO.getName())
            .withDescription(giftDTO.getDescription())
            .withUrl(giftDTO.getUrl())
            .build();

        if (file != null) {
            botService.assignPhotoURLToGift(gift, file);
        }

        BotUser botUser = botService.findUserByUserName(principal.getName()).get();
        boolean isAdded = botService.addGiftToUser(gift, botUser);
        if (isAdded) {
            notificationSender.sendUserAddGiftNotification(botUser);
            return new ResponseEntity<>(new ApiResponse("Gift was added"), HttpStatus.OK);
        }
        return new ResponseEntity<>(new ApiResponse("Gift wasn't added"), HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @PostMapping("/update")
    public ResponseEntity<ApiResponse> updateGift(@ModelAttribute GiftDTO giftDTO, // переделать под requestbody
                                                  @RequestParam(value = "image", required = false) MultipartFile file,
                                                  Principal principal) {
        logger.info("User " + principal.getName() + " updates gift with id " + giftDTO.getId());
        Optional<Gift> gift = botService.findGiftById(giftDTO.getId());
        BotUser user = botService.findUserByUserName(principal.getName()).get();

        if (gift.isPresent()) {
            if (!user.getUserName().equals(principal.getName())) {
                logger.warn("User {} tried to update not his own gift. Throw forbidden message", principal.getName());
                throw new IllegalOperationException("Forbidden");
            }
        }
        Gift updatedGift = gift.get();
        if (giftDTO.getDescription() != null) {
            updatedGift.setDescription(giftDTO.getDescription());
        }
        if (giftDTO.getUrl() != null) {
            updatedGift.setUrl(giftDTO.getUrl());
        }
        if (file != null) {
            botService.assignPhotoURLToGift(updatedGift, file);
        } else {
            botService.deleteGiftPhoto(updatedGift);
            updatedGift.setGiftPhotoURL(null);
            updatedGift.setGiftPhotoCloudinaryId(null);
        }

        boolean isUpdated = botService.updateGift(updatedGift);
        if (isUpdated) {
            return new ResponseEntity<>(new ApiResponse("Gift was updated"), HttpStatus.OK);
        }
        return new ResponseEntity<>(new ApiResponse("Gift wasn't updated"), HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @DeleteMapping("/{giftId}/delete")
    public ResponseEntity<ApiResponse> deleteGift(@PathVariable("giftId") long id,
                                                  Principal principal) {
        logger.info("User " + principal.getName() + " deletes gift with id " + id);
        Optional<Gift> gift = botService.findGiftById(id);
        BotUser giftHolder = botService.findGiftHolderByGiftId(id).get();

        if (gift.isPresent()) {
            if (!giftHolder.getUserName().equals(principal.getName())) {
                logger.warn("User {} tried to delete not his own gift. Throw forbidden message", principal.getName());
                throw new IllegalOperationException("Forbidden");
            }
        }

        boolean isRemoved = botService.deleteGift(id);
        if (isRemoved) {
            if (gift.get().getOccupiedBy() != null) {
                notificationSender.sendUserDeletedGiftYouDonateNotification(gift.get(), giftHolder);
            }
            return new ResponseEntity<>(new ApiResponse("Gift was deleted"), HttpStatus.OK);
        }
        return new ResponseEntity<>(new ApiResponse("Gift wasn't deleted"), HttpStatus.INTERNAL_SERVER_ERROR);

    }
}
