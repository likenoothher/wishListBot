package com.aziarets.vividapp.rest;

import com.aziarets.vividapp.exception.GiftsLimitReachedException;
import com.aziarets.vividapp.exception.IllegalOperationException;
import com.aziarets.vividapp.exceptionhandling.ApiResponse;
import com.aziarets.vividapp.model.BotUser;
import com.aziarets.vividapp.model.Gift;
import com.aziarets.vividapp.rest.dto.BotUserDTO;
import com.aziarets.vividapp.rest.dto.GiftDTO;
import com.aziarets.vividapp.service.BotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/subscriptions")
public class SubscriptionsRestController {
    private static final Logger logger = LoggerFactory.getLogger(SubscriptionsRestController.class);

    private BotService botService;

    @Autowired
    public SubscriptionsRestController(BotService botService) {
        this.botService = botService;
    }

    @GetMapping({"", "/"})
    public List<BotUserDTO> getSubscriptions(Principal principal) {
        logger.info("User " + principal.getName() + " requests subscriptions list");
        BotUser user = botService.findUserByUserName(principal.getName()).get();

        List<BotUser> subscriptions = botService.getUserSubscribers(user);
        List<BotUserDTO> subscriptionsDTO = botService.convertBotUserListToDTO(subscriptions);

        logger.info("Returning subscriptions of user with user name " + user.getId());
        return subscriptionsDTO;
    }

    @GetMapping({"/{userId}"})
    public List<GiftDTO> getSubscriptionsWishlist(@PathVariable(value = "userId") long id,
                                                  Principal principal) {
        logger.info("User " + principal.getName() + " requests wishlist of user with id " + id);
        Optional<BotUser> subscriptionUser = botService.findUserById(id);

        if (!subscriptionUser.isPresent()) {
            logger.warn("User with id {} not found. Throw forbidden message", id);
            throw new IllegalOperationException("Forbidden");
        }

        BotUser user = botService.findUserByUserName(principal.getName()).get();
        List<BotUser> subscriptions = botService.getUserSubscriptions(user);

        if (!subscriptions.contains(subscriptionUser.get())) {
            logger.warn("User {} tried to see wishlist of not his subscription. Throw forbidden message", principal.getName());
            throw new IllegalOperationException("Forbidden");
        }

        List<Gift> gifts = botService.getUserWishListGifts(subscriptionUser.get().getId());
        List<GiftDTO> giftDTO = botService.convertGiftListToDTO(gifts);

        logger.info("Returning wishlist of user with id " + id + " to user " + principal.getName());
        return giftDTO;
    }

    @PostMapping({"/donate"})
    public ResponseEntity<ApiResponse> donate(@RequestParam(value = "giftId") long id,
                                              Principal principal) {
        logger.info("User " + principal.getName() + " donates gift with id " + id);
        Optional<Gift> gift = botService.findGiftById(id);

        if (!gift.isPresent()) {
            logger.warn("Gift with id {} not found. Throw forbidden message", id);
            throw new IllegalOperationException("Forbidden");
        }

        Optional<BotUser> subscriptionUser = botService.findGiftHolderByGiftId(gift.get().getId());
        BotUser user = botService.findUserByUserName(principal.getName()).get();
        List<BotUser> subscriptions = botService.getUserSubscriptions(user);

        if (!subscriptions.contains(subscriptionUser.get())) {
            logger.warn("User {} tried to donate gift to not his subscription. Throw forbidden message", principal.getName());
            throw new IllegalOperationException("Forbidden");
        }

        boolean isAdded = false;
        try {
            isAdded = botService.donate(gift.get().getId(), user);
        } catch (GiftsLimitReachedException exception) {
            logger.warn(exception.getMessage());
            throw new GiftsLimitReachedException("Gift limit for current user is reached");
        }

        if (isAdded) {
            return new ResponseEntity<>(new ApiResponse("Gift was added to your I present list"), HttpStatus.OK);
        }
        return new ResponseEntity<>(new ApiResponse("Gift wasn't added to your I present list"),
            HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @DeleteMapping("/{userId}/delete")
    public ResponseEntity<ApiResponse> deleteSubscription(@PathVariable("userId") long id,
                                                          Principal principal) {
        logger.info("User " + principal.getName() + " deletes subscription with id " + id);
        Optional<BotUser> deletedUser = botService.findUserById(id);

        if (!deletedUser.isPresent()) {
            logger.warn("User with id {} not found. Throw forbidden message", id);
            throw new IllegalOperationException("Forbidden");
        }

        BotUser user = botService.findUserByUserName(principal.getName()).get();
        List<BotUser> subscriptions = botService.getUserSubscriptions(user);

        if (!subscriptions.contains(deletedUser.get())) {
            logger.warn("User {} tried to delete not his own subscription. Throw forbidden message", principal.getName());
            throw new IllegalOperationException("Forbidden");
        }

        boolean isRemoved = botService.removeSubscriptionFromSubscriber(deletedUser.get(), user);
        if (isRemoved) {
            return new ResponseEntity<>(new ApiResponse("Subscription was deleted"), HttpStatus.OK);
        }
        return new ResponseEntity<>(new ApiResponse("Subscription wasn't deleted"), HttpStatus.INTERNAL_SERVER_ERROR);

    }


}
