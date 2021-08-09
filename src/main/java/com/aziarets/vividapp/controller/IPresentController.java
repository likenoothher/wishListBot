package com.aziarets.vividapp.controller;

import com.aziarets.vividapp.model.BotUser;
import com.aziarets.vividapp.model.Gift;
import com.aziarets.vividapp.service.BotService;
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
import java.util.Map;

@Controller
@RequestMapping(value = "/i_present")
public class IPresentController {
    private static final Logger logger = LoggerFactory.getLogger(IPresentController.class);

    private BotService botService;

    @Autowired
    public IPresentController(BotService botService) {
        this.botService = botService;
    }

    @GetMapping({"", "/"})
    public String showIPresentList(Model model, Principal principal) {
        BotUser botUser = botService.findUserByUserName(principal.getName()).get();
        Map<Gift, BotUser> userGifts = botService.getUserPresentsMap(botUser);
        model.addAttribute("user", botUser);
        model.addAttribute("gifts", userGifts);
        logger.info("Returning i present page for " + principal.getName());
        return "i_present";
    }

    @PostMapping("/refuse")
    public String refuse(@RequestParam(value = "giftId") long giftId,
                         @RequestParam(value = "userId") long userId,
                         Principal principal) {
        logger.info("Handling refuse from donate gift request from user " + principal.getName());
        BotUser botUser = botService.findUserById(userId).get();
        boolean isRefused = botService.refuseFromDonate(giftId, botUser);
        logger.info("Gift with id " + giftId + " deleting from i present list of to user " + principal.getName()
            + " result - " + isRefused);
        return "redirect:/i_present";
    }
}
