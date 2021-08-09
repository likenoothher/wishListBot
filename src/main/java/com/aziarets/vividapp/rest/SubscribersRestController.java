package com.aziarets.vividapp.rest;

import com.aziarets.vividapp.exception.IllegalOperationException;
import com.aziarets.vividapp.exceptionhandling.ApiResponse;
import com.aziarets.vividapp.model.BotUser;
import com.aziarets.vividapp.rest.dto.BotUserDTO;
import com.aziarets.vividapp.service.BotService;
import com.aziarets.vividapp.util.NotificationSender;
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
@RequestMapping("api/subscribers")
public class SubscribersRestController {
    private static final Logger logger = LoggerFactory.getLogger(SubscribersRestController.class);

    private BotService botService;
    private NotificationSender notificationSender;


    @Autowired
    public SubscribersRestController(BotService botService, NotificationSender notificationSender) {
        this.botService = botService;
        this.notificationSender = notificationSender;
    }

    @GetMapping({"", "/"})
    public List<BotUserDTO> getSubscribers(Principal principal) {
        logger.info("User " + principal.getName() + " requests subscribers list");
        BotUser user = botService.findUserByUserName(principal.getName()).get();

        List<BotUser> subscribers = botService.getUserSubscribers(user);
        List<BotUserDTO> subscribersDTO = botService.convertBotUserListToDTO(subscribers);

        logger.info("Returning subscribers of user with user name " + user.getId());
        return subscribersDTO;
    }

    @DeleteMapping("/{userId}/delete")
    public ResponseEntity<ApiResponse> deleteSubscriber(@PathVariable("userId") long id,
                                                        Principal principal) {
        logger.info("User " + principal.getName() + " deletes subscriber with id " + id);
        Optional<BotUser> deletedUser = botService.findUserById(id);

        if (!deletedUser.isPresent()) {
            logger.warn("User with id {} not found. Throw forbidden message", id);
            throw new IllegalOperationException("Forbidden");
        }

        BotUser user = botService.findUserByUserName(principal.getName()).get();
        List<BotUser> subscribers = botService.getUserSubscribers(user);

        if (!subscribers.contains(deletedUser.get())) {
            logger.warn("User {} tried to delete not his own subscriber. Throw forbidden message", principal.getName());
            throw new IllegalOperationException("Forbidden");
        }

        boolean isRemoved = botService.removeSubscriberFromSubscriptions(deletedUser.get(), user);
        if (isRemoved) {
            notificationSender.sendSubscriberDeletedNotification(deletedUser.get(), user);
            return new ResponseEntity<>(new ApiResponse("Subscriber was deleted"), HttpStatus.OK);
        }
        return new ResponseEntity<>(new ApiResponse("Subscriber wasn't deleted"), HttpStatus.INTERNAL_SERVER_ERROR);

    }
}
