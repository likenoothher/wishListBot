package com.aziarets.vividapp.rest;

import com.aziarets.vividapp.exceptionhandling.ApiResponse;
import com.aziarets.vividapp.model.BotUser;
import com.aziarets.vividapp.service.BotService;
import com.aziarets.vividapp.util.NotificationSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.security.Principal;

@RestController
@RequestMapping("api/settings")
@Validated
public class SettingsRestController {
    private static final Logger logger = LoggerFactory.getLogger(SettingsRestController.class);

    private BotService botService;
    private NotificationSender notificationSender;

    @Autowired
    public SettingsRestController(BotService botService, NotificationSender notificationSender) {
        this.botService = botService;
        this.notificationSender = notificationSender;
    }

    @PostMapping({"", "/"})
    public ResponseEntity<ApiResponse> setSettings(@RequestParam(value = "updateReady") boolean updateReady,
                                                   @RequestParam(value = "allCanSeeMyWishList") boolean allCanSeeMyWishList,
                                                   @RequestParam(value = "giftLimit") @Min(1) @Max(10) Integer giftLimit,
                                                   Principal principal) {
        logger.info("User {} updates settings", principal.getName());
        BotUser botUser = botService.findUserByUserName(principal.getName()).get();

        botUser.setReadyReceiveUpdates(updateReady);
        botUser.setAllCanSeeMyWishList(allCanSeeMyWishList);
        botUser.setGiftLimit(giftLimit);

        boolean isUpdated = botService.updateUser(botUser);
        if (isUpdated) {
            return new ResponseEntity<>(new ApiResponse("Settings was updated"), HttpStatus.OK);
        }
        return new ResponseEntity<>(new ApiResponse("Settings wasn't updated"), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping("/send_message")
    public ResponseEntity<ApiResponse> sendMessage(@RequestParam(value = "message") String message,
                                                   Principal principal) {
        logger.info("User {} sends message to admin", principal.getName());
        BotUser botUser = botService.findUserByUserName(principal.getName()).get();

        notificationSender.sendMessageToDeveloper(botUser, message);

        return new ResponseEntity<>(new ApiResponse("Message was sent"), HttpStatus.OK);
    }


}
