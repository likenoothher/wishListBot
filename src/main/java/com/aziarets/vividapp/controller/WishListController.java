package com.aziarets.vividapp.controller;

import com.aziarets.vividapp.service.BotService;
import com.aziarets.vividapp.model.BotUser;
import com.aziarets.vividapp.model.Gift;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class WishListController {
    private BotService botService;

    @Autowired
    public WishListController(BotService botService) {
        this.botService = botService;
    }

    @GetMapping("/")
    public String findBotUserById(Model model){
       BotUser botUser = botService.findUserByUserName("john_yoy").get();
       List<Gift> userGifts = botService.getUserWishListGifts(botUser.getTgAccountId());
       model.addAttribute("user", botUser);
       model.addAttribute("gifts", userGifts);
       return "wishlist";
    }

    @PostMapping("/")
    public String showMain(@RequestParam(value = "id") long id, Model model){
        BotUser botUser = botService.findUserByTelegramId(id).get();
        List<Gift> userGifts = botService.getUserWishListGifts(botUser.getTgAccountId());
        model.addAttribute("user", botUser);
        model.addAttribute("gifts", userGifts);
        return "wishlist";
    }

    @GetMapping("/addGift")
    public String addGift(@RequestParam(value = "giftHolderId") long id,  Model model) {
        model.addAttribute("giftHolderId", id);
        Gift gift = new Gift();
        gift.setName("2342");
        gift.setUrl("asdf");
        model.addAttribute("gift", gift);
        return "addGift";
    }

    @PostMapping("/addGift")
    public String addGift(@RequestParam(value = "giftHolderId") long id, @RequestParam("giftName") String giftName,
                             @RequestParam("giftDescription") String giftDescription,
                             @RequestParam("giftURL") String giftURL) {
        Gift gift = new Gift();
        gift.setName(giftName);
        gift.setDescription(giftDescription);
        gift.setUrl(giftURL);
        BotUser botUser = botService.findUserByTelegramId(id).get();
        botService.addGiftToUser(gift,botUser);

        return "redirect:/";
    }

    @GetMapping("/updateGift")
    public String updateGift(@RequestParam(value = "giftId") long id,  Model model) {
        model.addAttribute("giftHolderId", id);
        Gift gift = botService.findGiftById(id).get();
        model.addAttribute("gift", gift);
        return "updateGift";
    }

    @PostMapping("/updateGift")
    public String updateGift(@RequestParam(value = "giftId") long id,
                             @RequestParam("giftDescription") String giftDescription,
                             @RequestParam("giftURL") String giftURL) {
        Gift gift = botService.findGiftById(id).get();
        gift.setDescription(giftDescription);
        gift.setUrl(giftURL);
        botService.updateGift(gift);

        return "redirect:/";
    }

    @PostMapping("/deleteGift")
    public String deleteGift(@RequestParam(value = "giftId") long id) {
        Gift gift = botService.findGiftById(id).get();
        botService.deleteGift(gift.getId());

        return "redirect:/";
    }
}