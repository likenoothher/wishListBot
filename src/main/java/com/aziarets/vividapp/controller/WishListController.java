package com.aziarets.vividapp.controller;

import com.aziarets.vividapp.service.BotService;
import com.aziarets.vividapp.model.BotUser;
import com.aziarets.vividapp.model.Gift;
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

    @Autowired
    public WishListController(BotService botService) {
        this.botService = botService;
    }

    @GetMapping({"", "/"})
    public String showWishList(Principal principal, Model model) {
        BotUser botUser = botService.findUserByUserName(principal.getName()).get();
        List<Gift> userGifts = botService.getUserWishListGifts(botUser.getTgAccountId());
        model.addAttribute("user", botUser);
        model.addAttribute("gifts", userGifts);
        logger.info("Returning wish list page for " + principal.getName());
        return "wishlist";
    }

//    @PostMapping("")
//    public String showWishList(@RequestParam(value = "id") long id, Model model, Principal principal) {
//        BotUser botUser = botService.findUserByTelegramId(id).get();
//        List<Gift> userGifts = botService.getUserWishListGifts(botUser.getTgAccountId());
//        model.addAttribute("user", botUser);
//        model.addAttribute("gifts", userGifts);
//        logger.info("Returning wish list page for " + principal.getName());
//        return "wishlist";
//    }

    @GetMapping("/add_gift")
    public String addGift(Model model, Principal principal) {
        logger.info("Returning add gift form for user " + principal.getName());
        Gift gift = new Gift();
        model.addAttribute("gift", gift);
        return "addGift";
    }

    @PostMapping("/add_gift")
    public String addGift(@RequestParam("giftName") String giftName,
                          @RequestParam("giftDescription") String giftDescription,
                          @RequestParam("giftURL") String giftURL,
                          Principal principal) {
        logger.info("Handling add gift request from user " + principal.getName());
        Gift gift = new Gift();
        gift.setName(giftName);
        gift.setDescription(giftDescription);
        gift.setUrl(giftURL);
        BotUser botUser = botService.findUserByUserName(principal.getName()).get();
        botService.addGiftToUser(gift, botUser);
        logger.info("Gift with name "+ giftName+" added to user " + principal.getName());
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
            botService.deleteGift(gift.getId());
            logger.info("Gift with id " + giftId + " deleted to user " + principal.getName());
            return "redirect:/wishlist";
        } else {
            logger.warn("User with name " + principal.getName() + " tried to delete not his gift with id " + giftId);
            return "forbidden";
        }
    }
}
