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

import java.util.Optional;

@Controller
@RequestMapping(value = "/admin")
public class AdminController {
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private BotService botService;
    private NotificationSender notificationSender;

    @Autowired
    public AdminController(BotService botService, NotificationSender notificationSender) {
        this.botService = botService;
        this.notificationSender = notificationSender;
    }

    @GetMapping(value = "/search_user")
    public String searchUser() {
        logger.info("Returning search user page for admin");
        return "searchUser";

    }

    @PostMapping(value = "/search_user")
    public String searchUser(@RequestParam(value = "userName", required = false) String username, Model model) {
        if (username != null) {
            logger.info("Admin search user by user name - " + username);
            Optional<BotUser> botUser = botService.findUserByUserName(username);
            if (botUser.isPresent()) {
                model.addAttribute("user", botUser.get());
            }
            return "searchUser";
        } else {
            logger.info("Returning search user page for admin");
            return "searchUser";
        }
    }

    @PostMapping(value = "/send_message")
    public String sendMessage(@RequestParam(value = "userId") long id,
                              @RequestParam(value = "message") String message,
                              Model model) {
        logger.info("Admin sends message to user with id - " + id);
        Optional<BotUser> botUser = botService.findUserById(id);
        if (botUser.isPresent()) {
            notificationSender.sendMessage(botUser.get(), message);
            logger.info("Message to user with id  " + id + " was sent");
            model.addAttribute("user", botUser.get());
        }
        return "searchUser";
    }

    @PostMapping(value = "/block")
    public String block(@RequestParam(value = "userId") long id,
                        Model model) {
        logger.info("Admin blocks user with id - " + id);
        BotUser botUser = botService.findUserById(id).get();
        botUser.setEnabled(false);
        botService.updateUser(botUser);
        model.addAttribute("user", botUser);

        return "searchUser";
    }

    @PostMapping(value = "/unblock")
    public String unblock(@RequestParam(value = "userId") long id,
                          Model model) {
        logger.info("Admin blocks user with id - " + id);
        BotUser botUser = botService.findUserById(id).get();
        botUser.setEnabled(true);
        botService.updateUser(botUser);
        model.addAttribute("user", botUser);

        return "searchUser";
    }
}
