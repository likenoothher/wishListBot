package com.aziarets.vividapp.rest;

import com.aziarets.vividapp.exception.IllegalOperationException;
import com.aziarets.vividapp.exception.NotFoundUserIdException;
import com.aziarets.vividapp.exception.NotFoundUserNameException;
import com.aziarets.vividapp.model.BotUser;
import com.aziarets.vividapp.service.BotService;
import com.aziarets.vividapp.util.NotificationSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping(value = "api/admin/")
public class AdminRestController {
    private static final Logger logger = LoggerFactory.getLogger(AdminRestController.class);

    private BotService botService;
    private NotificationSender notificationSender;

    @Autowired
    public AdminRestController(BotService botService, NotificationSender notificationSender) {
        this.botService = botService;
        this.notificationSender = notificationSender;
    }

    @GetMapping("/users/{id}")
    public BotUser searchUserById(@PathVariable(value = "id") long id) {
        logger.info("Admin request for searching user with id " + id);
        Optional<BotUser> user = botService.findUserById(id);
        if (!user.isPresent()) {
            logger.warn("User with id " + id + " not found");
            throw new NotFoundUserIdException("User with id " + id + " not found");
        }
        logger.info("Returning user with id " + id);
        return user.get();
    }

    @GetMapping("/user_name/{userName}")
    public BotUser searchUserByUserName(@PathVariable(value = "userName") String userName) {
        logger.info("Admin request for searching user with user name " + userName);
        Optional<BotUser> user = botService.findUserByUserName(userName);
        if (!user.isPresent()) {
            logger.warn("User with user name " + userName + " not found");
            throw new NotFoundUserNameException("User with user name " + userName + " not found");
        }
        logger.info("Returning user with user name " + userName);
        return user.get();
    }

    @PostMapping("/send_message")
    public ResponseEntity<String> sendMessage(@RequestParam(value = "id") long id,
                                              @RequestParam(value = "message") String message) {
        logger.info("Admin request for sending message to user with id " + id);
        Optional<BotUser> user = botService.findUserById(id);
        if (!user.isPresent()) {
            logger.warn("User with id " + id + " not found");
            throw new NotFoundUserIdException("User with id " + id + " not found");
        }
        logger.info("Admin sends message to user with user with id " + id);
        notificationSender.sendMessage(user.get(), message);
        return new ResponseEntity<>("Message was sent to user with id " + id, HttpStatus.OK);
    }

    @GetMapping("/block/{id}")
    public ResponseEntity<String> block(@PathVariable(value = "id") long id) {
        logger.info("Admin request for blocking user with id " + id);
        Optional<BotUser> user = botService.findUserById(id);
        if (!user.isPresent()) {
            logger.warn("User with id " + id + " not found");
            throw new NotFoundUserIdException("User with id " + id + " not found");
        }
        if (!user.get().isEnabled()) {
            logger.warn("User with id " + id + " is already blocked");
            throw new IllegalOperationException("User with id " + id + " is already blocked");
        }

        BotUser blockedUser = user.get();
        blockedUser.setEnabled(false);
        boolean isBlocked = botService.updateUser(blockedUser);
        if (isBlocked) {
            return new ResponseEntity<>("User with id " + id + " was blocked", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("User with id " + id + " wasn't blocked. Some error occurred",
                HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/unblock/{id}")
    public ResponseEntity<String> unblock(@PathVariable(value = "id") long id) {
        logger.info("Admin request for blocking user with id " + id);
        Optional<BotUser> user = botService.findUserById(id);
        if (!user.isPresent()) {
            logger.warn("User with id " + id + " not found");
            throw new NotFoundUserIdException("User with id " + id + " not found");
        }
        if (user.get().isEnabled()) {
            logger.warn("User with id " + id + " is already unblocked");
            throw new IllegalOperationException("User with id " + id + " is already unblocked");
        }

        BotUser blockedUser = user.get();
        blockedUser.setEnabled(true);
        boolean isBlocked = botService.updateUser(blockedUser);
        if (isBlocked) {
            return new ResponseEntity<>("User with id " + id + " was unblocked", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("User with id " + id + " wasn't unblocked. Some error occurred",
                HttpStatus.BAD_REQUEST);
        }
    }
}
