package com.aziarets.vividapp.controller;

import com.aziarets.vividapp.model.BotUser;
import com.aziarets.vividapp.model.Gift;
import com.aziarets.vividapp.service.BotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;

@Controller
public class SettingsController {
    private BotService botService;

    @Autowired
    public SettingsController(BotService botService) {
        this.botService = botService;
    }

    @GetMapping("")
    public String showWishList(Principal principal, Model model){
        BotUser botUser = botService.findUserByUserName(principal.getName()).get();
        model.addAttribute("user", botUser);
        return "settings";
    }

    @PostMapping("")
    public String showWishList(@RequestParam(value = "id") long id, Model model){
        BotUser botUser = botService.findUserByTelegramId(id).get();
        List<Gift> userGifts = botService.getUserWishListGifts(botUser.getTgAccountId());
        model.addAttribute("user", botUser);
        model.addAttribute("gifts", userGifts);
        return "wishlist";
    }
}
