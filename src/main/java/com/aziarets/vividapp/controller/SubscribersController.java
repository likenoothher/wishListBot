package com.aziarets.vividapp.controller;

import com.aziarets.vividapp.model.BotUser;
import com.aziarets.vividapp.model.Gift;
import com.aziarets.vividapp.service.BotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(value = "/subscribers")
public class SubscribersController {

    private BotService botService;

    @Autowired
    public SubscribersController(BotService botService) {
        this.botService = botService;
    }

    @GetMapping("")
    public String showSubscriberList(Principal principal, Model model){
        BotUser botUser = botService.findUserByUserName(principal.getName()).get();
        List<BotUser> subscribers = botService.getUserSubscribers(botUser);
        model.addAttribute("user", botUser);
        model.addAttribute("subscribers", subscribers);
        return "subscribers";
    }

    @PostMapping("/delete")
    public String showIPresentList(@RequestParam(value = "deletedUserId") long deletedUserId,
                                   @RequestParam(value = "userId") long userId){
        BotUser subscribedTo = botService.findUserByTelegramId(userId).get();
        BotUser subscriber = botService.findUserByTelegramId(deletedUserId).get();
        botService.removeSubscriberFromSubscriptions(subscriber, subscribedTo);
        return "redirect:/subscribers";
    }
}
