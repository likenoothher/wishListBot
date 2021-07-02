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
@RequestMapping(value = "/i_present")
public class IPresentController {

    private BotService botService;

    @Autowired
    public IPresentController(BotService botService) {
        this.botService = botService;
    }

    @GetMapping("")
    public String showIPresentList(Principal principal, Model model){
        BotUser botUser = botService.findUserByUserName(principal.getName()).get();
        Map<Gift, BotUser> userGifts = botService.getUserPresentsMap(botUser);
        System.out.println(userGifts.toString());
        model.addAttribute("user", botUser);
        model.addAttribute("gifts", userGifts);
        return "i_present";
    }

    @PostMapping("/refuse")
    public String showIPresentList(@RequestParam(value = "giftId") long giftId,
                                   @RequestParam(value = "userId") long userId){
        BotUser botUser = botService.findUserByTelegramId(userId).get();
        botService.refuseFromDonate(giftId, botUser);
        return "redirect:/i_present";
    }
}
