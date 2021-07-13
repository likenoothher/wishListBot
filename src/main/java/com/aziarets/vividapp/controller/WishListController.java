package com.aziarets.vividapp.controller;

import com.aziarets.vividapp.service.BotService;
import com.aziarets.vividapp.model.BotUser;
import com.aziarets.vividapp.model.Gift;
import com.aziarets.vividapp.util.NotificationSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping(value = "/wishlist")
public class WishListController {
    private static final Logger logger = LoggerFactory.getLogger(WishListController.class);

    private BotService botService;
    private NotificationSender notificationSender;

    @Autowired
    public WishListController(BotService botService, NotificationSender notificationSender) {
        this.botService = botService;
        this.notificationSender = notificationSender;
    }

    @GetMapping({"", "/"})
    public String showWishList(Model model, Principal principal) {
        BotUser botUser = botService.findUserByUserName(principal.getName()).get();
        List<Gift> userGifts = botService.getUserWishListGifts(botUser.getTgAccountId());
        model.addAttribute("user", botUser);
        model.addAttribute("gifts", userGifts);
        logger.info("Returning wish list page for " + principal.getName());
        return "wishlist";
    }

    @GetMapping("/add_gift")
    public String addGift(Model model, Principal principal) {
        logger.info("Returning add gift form for user " + principal.getName());
        Gift gift = new Gift();
        model.addAttribute("gift", gift);
        return "addGift";
    }

    @PostMapping("/add_gift")
    public String addGift(@ModelAttribute("gift") Gift gift, Principal principal) {
        logger.info("Handling add gift request from user " + principal.getName());
        BotUser botUser = botService.findUserByUserName(principal.getName()).get();
        boolean isAdded = botService.addGiftToUser(gift, botUser);
        if(isAdded) {
            notificationSender.sendUserAddGiftNotification(botUser);
        }
        logger.info("Gift with name "+ gift.getName() +" added to user " + principal.getName());
        return "redirect:/wishlist";
    }

    @GetMapping("/update_gift")
    public String updateGift(@RequestParam(value = "giftId") long giftId, Model model, Principal principal) {
        logger.info("Returning update gift form for user " + principal.getName() + ", updated gift id: " + giftId);
        BotUser giftHolder = botService.findGiftHolderByGiftId(giftId).get();
        if (giftHolder.getUserName().equals(principal.getName())) {
            model.addAttribute("giftHolderId", giftHolder.getId());
            Gift gift = botService.findGiftById(giftId).get();
            model.addAttribute("gift", gift);
            System.out.println(gift.toString());
            return "updateGift";
        } else {
            logger.warn("User with name " + principal.getName() + " tried to update not his gift with id " + giftId);
            return "forbidden";
        }
    }

    @PostMapping("/update_gift")
    public String updateGift(@RequestParam(value = "giftId") long giftId,
                             @RequestParam("giftDescription") String giftDescription,
                             @RequestParam("giftURL") String giftURL,
                             Principal principal) {
        logger.info("Handling updating gift request for user " + principal.getName() + ", updated gift id: " + giftId);
        Gift gift = botService.findGiftById(giftId).get();
        BotUser giftHolder = botService.findGiftHolderByGiftId(giftId).get();
        if (giftHolder.getUserName().equals(principal.getName())) {
            gift.setDescription(giftDescription);
            gift.setUrl(giftURL);
            botService.updateGift(gift);
            logger.info("Gift with id " + giftId + " updated to user " + principal.getName());
            return "redirect:/wishlist";
        } else {
            logger.warn("User with name " + principal.getName() + " tried to update not his gift with id " + giftId);
            return "forbidden";
        }
    }

    @PostMapping("/delete_gift")
    public String deleteGift(@RequestParam(value = "giftId") long giftId, Principal principal) {
        logger.info("Handling deleting gift request for user " + principal.getName() + ", deleted gift id: " + giftId);
        Gift gift = botService.findGiftById(giftId).get();
        BotUser giftHolder = botService.findGiftHolderByGiftId(giftId).get();
        if (giftHolder.getUserName().equals(principal.getName())) {
            boolean isRemoved = botService.deleteGift(gift.getId());
            if (isRemoved && gift.getOccupiedBy() != null) {
                notificationSender.sendUserDeletedGiftYouDonateNotification(gift, giftHolder);
            }
            logger.info("Gift with id " + giftId + " deleted to user " + principal.getName());
            return "redirect:/wishlist";
        } else {
            logger.warn("User with name " + principal.getName() + " tried to delete not his gift with id " + giftId);
            return "forbidden";
        }
    }
}
