package com.aziarets.vividapp.controller;

import com.aziarets.vividapp.exception.AlreadyDonatesException;
import com.aziarets.vividapp.model.BotUser;
import com.aziarets.vividapp.model.Gift;
import com.aziarets.vividapp.service.BotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(SubscribersController.class);

    private BotService botService;

    @Autowired
    public SubscriptionsController(BotService botService) {
        this.botService = botService;
    }

    @GetMapping({"", "/"})
    public String showSubscriptionsList(Model model, Principal principal) {
        BotUser botUser = botService.findUserByUserName(principal.getName()).get();
        List<BotUser> subscriptions = botService.getUserSubscriptions(botUser);
        model.addAttribute("user", botUser);
        model.addAttribute("subscriptions", subscriptions);
        logger.info("Returning subscriptions page for " + principal.getName());
        return "subscriptions";
    }

    @GetMapping("/{userId}")
    public String showSubscriptionWishList(@PathVariable Long userId, Model model, Principal principal) {
        BotUser currentUser = botService.findUserByUserName(principal.getName()).get();
        if (botService.isUserSubscribedTo(userId, currentUser.getId())) {
            BotUser botUser = botService.findUserById(userId).get();
            List<Gift> availableToDonateGifts = botService.getAvailableToDonateGifts(botUser.getTgAccountId());
            model.addAttribute("user", botUser);
            model.addAttribute("availableGifts", availableToDonateGifts);
            if(botService.isDonorAlreadyGoingDonateToUser(currentUser.getId(), userId)) {
                model.addAttribute("showIPresentButton", false);
            }
            else {
                model.addAttribute("showIPresentButton", true);
            }
            logger.info("Returning wish list of user with id "+userId +" for " + principal.getName());
            return "subscriptionWishList";
        }
        logger.info("Illegal request for wish list of user with id " +userId +" from user " + principal.getName()
        + ". Not in subscription list");
        return "forbidden";
    }

    @PostMapping("/donate")
    public String goingDonatePresent(@RequestParam(value = "giftId") Long giftId, Principal principal) {
        logger.info("Handling donate gift request, gift id " + giftId +
            ", request from user  " + principal.getName());
        BotUser botUser = botService.findUserByUserName(principal.getName()).get();
        BotUser giftHolder = botService.findGiftHolderByGiftId(giftId).get();
        boolean isAdded = false;
        try {
            isAdded = botService.donate(giftId, botUser);
        } catch (AlreadyDonatesException e) {
            logger.warn("User with id " + botUser.getId() + " tried to add one more present for donating to " +
                "user with id " + giftHolder.getId() + " despite he already donates gift to him");
            return "forbidden";
        }
        logger.info("Donating gift request for gift with id " + giftId +
            ", from user  " + principal.getName() +", result - " + isAdded);
        return "redirect:/subscriptions/" + giftHolder.getId();
    }

    @PostMapping("/unsubscribe")
    public String unsubscribe(@RequestParam(value = "userId") Long userId,
                              @RequestParam(value = "unsubscribedFrom") Long unsubscribedFrom,
                              Principal principal) {
        logger.info("Handling unsubscribe from user with id "+unsubscribedFrom+ " request from user "
            + principal.getName());
        BotUser byUserDeleted = botService.findUserById(userId).get();
        BotUser deletedUser = botService.findUserByTelegramId(unsubscribedFrom).get();
        boolean isUnsubscribed = botService.removeSubscriptionFromSubscriber(deletedUser, byUserDeleted);
        logger.info("Unsubscribe from user with id "+unsubscribedFrom+ " request from user "
            + principal.getName() + " result - " + isUnsubscribed);
        return "redirect:/subscriptions/";
    }
}
