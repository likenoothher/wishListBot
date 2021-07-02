package com.aziarets.vividapp.controller;

import com.aziarets.vividapp.model.BotUser;
import com.aziarets.vividapp.model.Gift;
import com.aziarets.vividapp.service.BotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.PathParam;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping(value = "/subscriptions")
public class SubscriptionsController {

    private BotService botService;

    @Autowired
    public SubscriptionsController(BotService botService) {
        this.botService = botService;
    }

    @GetMapping("")
    public String showSubscriptionsList(Principal principal, Model model){
        BotUser botUser = botService.findUserByUserName(principal.getName()).get();
        List<BotUser> subscriptions = botService.getUserSubscriptions(botUser);
        model.addAttribute("user", botUser);
        model.addAttribute("subscriptions", subscriptions);
        return "subscriptions";
    }

    @GetMapping("/{userTelegramId}")
    public String showSubscriptionWishList(@PathVariable Long userTelegramId, Model model){
        List<Gift> availableToDonateGifts = botService.getAvailableToDonateGifts(userTelegramId);
        BotUser botUser = botService.findUserByTelegramId(userTelegramId).get();
        model.addAttribute("user", botUser);
        model.addAttribute("availableGifts", availableToDonateGifts);
        return "subscriptionWishList";
    }

    @PostMapping("/donate")
    public String goingDonatePresent(Principal principal,
                                     @RequestParam(value = "giftId") Long giftId){
        BotUser botUser = botService.findUserByUserName(principal.getName()).get();
        BotUser giftHolder = botService.findGiftHolderByGiftId(giftId).get();
        botService.donate(giftId, botUser);
        return "redirect:/subscriptions/" + giftHolder.getTgAccountId();
    }

    @PostMapping("/unsubscribe")
    public String unsubscribe(@RequestParam(value = "userId") Long userId,
                              @RequestParam(value = "unsubscribedFrom") Long unsubscribedFrom) {
        BotUser byUserDeleted = botService.findUserByTelegramId(userId).get();
        BotUser deletedUser = botService.findUserByTelegramId(unsubscribedFrom).get();
        botService.removeSubscriptionFromSubscriber(deletedUser, byUserDeleted);
        return "redirect:/subscriptions/";
    }
}
