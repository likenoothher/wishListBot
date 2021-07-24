package com.aziarets.vividapp.controller;

import com.aziarets.vividapp.model.BotUser;
import com.aziarets.vividapp.model.Gift;
import com.aziarets.vividapp.service.BotService;
import com.aziarets.vividapp.util.NotificationSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping(value = "/settings")
public class SettingsController {
    private static final Logger logger = LoggerFactory.getLogger(SettingsController.class);

    private BotService botService;
    private NotificationSender notificationSender;

    @Autowired
    public SettingsController(BotService botService, NotificationSender notificationSender) {
        this.botService = botService;
        this.notificationSender = notificationSender;
    }

    @GetMapping({"", "/"})
    public String showSettings(Principal principal, Model model) {
        BotUser botUser = botService.findUserByUserName(principal.getName()).get();
        model.addAttribute("user", botUser);
        return "settings";
    }

    @PostMapping("/send_message")
    public String sendMessageToDeveloper(@RequestParam(value = "userId") long id,
                                         @RequestParam(value = "message") String message) {
        logger.info("User with id " + id + " send message to developer");
        Optional<BotUser> user = botService.findUserById(id);
        if (user.isPresent()) {
            notificationSender.sendMessageToDeveloper(user.get(), message);
        }
        return "redirect:/settings";
    }

    @PostMapping("/refresh")
    public String refreshUserSettings(@RequestParam(value = "userId", required = true) long id,
                                      @RequestParam(value = "readyReceiveUpdates", required = false) boolean readyReceiveUpdates,
                                      @RequestParam(value = "allCanSeeWishlist", required = false) boolean allCanSeeWishlist,
                                      @RequestParam(value = "giftLimit", required = true) int giftLimit) {
        logger.info("User with id " + id + " updates settings");

        Optional<BotUser> user = botService.findUserById(id);
        if (user.isPresent()) {
            BotUser updatedUser = user.get();
            updatedUser.setReadyReceiveUpdates(readyReceiveUpdates);
            updatedUser.setAllCanSeeMyWishList(allCanSeeWishlist);
            updatedUser.setGiftLimit(giftLimit);
            botService.updateUser(updatedUser);
        }

        return "redirect:/settings";
    }
}
