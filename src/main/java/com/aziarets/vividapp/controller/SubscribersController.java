package com.aziarets.vividapp.controller;

import com.aziarets.vividapp.model.BotUser;
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

@Controller
@RequestMapping(value = "/subscribers")
public class SubscribersController {
    private static final Logger logger = LoggerFactory.getLogger(SubscribersController.class);

    private BotService botService;
    private NotificationSender notificationSender;

    @Autowired
    public SubscribersController(BotService botService, NotificationSender notificationSender) {
        this.botService = botService;
        this.notificationSender = notificationSender;
    }

    @GetMapping({"", "/"})
    public String showSubscriberList(Model model, Principal principal) {
        BotUser botUser = botService.findUserByUserName(principal.getName()).get();
        List<BotUser> subscribers = botService.getUserSubscribers(botUser);
        model.addAttribute("user", botUser);
        model.addAttribute("subscribers", subscribers);
        logger.info("Returning subscribers page for " + principal.getName());
        return "subscribers";
    }

    @PostMapping("/delete")
    public String delete(@RequestParam(value = "deletedUserId") long deletedUserId,
                         @RequestParam(value = "userId") long userId,
                         Principal principal) {
        logger.info("Handling delete subscriber with id " + deletedUserId +
            "  request from user  " + principal.getName());
        BotUser subscribedTo = botService.findUserById(userId).get();
        BotUser subscriber = botService.findUserByTelegramId(deletedUserId).get();
        boolean isDeleted = botService.removeSubscriberFromSubscriptions(subscriber, subscribedTo);
        if (isDeleted) {
            notificationSender.sendSubscriberDeletedNotification(subscriber, subscribedTo);
        }
        logger.info("Subscriber with id " + deletedUserId + " deleting from user " + principal.getName()
            + " result - " + isDeleted);
        return "redirect:/subscribers";
    }
}
