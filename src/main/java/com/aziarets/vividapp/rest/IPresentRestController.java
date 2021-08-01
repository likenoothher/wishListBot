package com.aziarets.vividapp.rest;

import com.aziarets.vividapp.controller.WishListController;
import com.aziarets.vividapp.exception.IllegalOperationException;
import com.aziarets.vividapp.exceptionhandling.ApiResponse;
import com.aziarets.vividapp.model.BotUser;
import com.aziarets.vividapp.model.Gift;
import com.aziarets.vividapp.rest.dto.BotUserDTO;
import com.aziarets.vividapp.rest.dto.GiftDTO;
import com.aziarets.vividapp.rest.facade.BotUserFacade;
import com.aziarets.vividapp.rest.facade.GiftFacade;
import com.aziarets.vividapp.service.BotService;
import com.aziarets.vividapp.util.NotificationSender;
import org.checkerframework.checker.units.qual.A;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/present")
public class IPresentRestController {
    private static final Logger logger = LoggerFactory.getLogger(IPresentRestController.class);

    private BotService botService;

    @Autowired
    public IPresentRestController(BotService botService) {
        this.botService = botService;
    }

    @GetMapping({"", "/"})
    public Map<GiftDTO, BotUserDTO> showIPresentList(Principal principal) {
        logger.info("User " + principal.getName() + " requests I present list");
        BotUser botUser = botService.findUserByUserName(principal.getName()).get();
        Map<Gift, BotUser> userGifts = botService.getUserPresentsMap(botUser);
        Map<GiftDTO, BotUserDTO> userGiftsDTO = botService.convertIPresentMapToDTO(userGifts);
        logger.info("Returning I present list of user with user name " + botUser.getUserName());

        return userGiftsDTO;
    }

    @GetMapping("/{giftId}/refuse")
    public ResponseEntity<ApiResponse> refuse(@PathVariable(value = "giftId") long giftId,
                                              Principal principal) {
        logger.info("Handling refuse from donate gift request from user " + principal.getName());
        BotUser botUser = botService.findUserByUserName(principal.getName()).get();
        Optional<Gift> gift = botService.findGiftById(giftId);
        if (!gift.isPresent()) {
            logger.warn("Gift with id " + " doesn't exist. Throw forbidden message");
            throw new IllegalOperationException("Forbidden");

        }

        if (gift.get().getOccupiedBy() == null || !gift.get().getOccupiedBy().getUserName().equals(principal.getName())) {
            logger.warn("User {} tried to refuse from gift which is not in his I present list." +
                " Throw forbidden message", principal.getName());
            throw new IllegalOperationException("Forbidden");
        }

        boolean isRefused = botService.refuseFromDonate(giftId, botUser);
        if (isRefused) {
            return new ResponseEntity<>(new ApiResponse("Gift was deleted from your I present list"), HttpStatus.OK);
        }
        return new ResponseEntity<>(new ApiResponse("Gift wasn't deleted from your I present list"),
            HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
