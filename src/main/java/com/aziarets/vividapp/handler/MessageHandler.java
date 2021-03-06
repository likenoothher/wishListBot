package com.aziarets.vividapp.handler;

import com.aziarets.vividapp.service.BotService;
import com.aziarets.vividapp.menu.BotMenuTemplate;
import com.aziarets.vividapp.model.BotUser;
import com.aziarets.vividapp.model.BotUserStatus;
import com.aziarets.vividapp.model.Gift;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.aziarets.vividapp.menu.Icon.*;

@Component
@Transactional
public class MessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(MessageHandler.class);

    private BotService botService;
    private BotMenuTemplate menu;
    private String chatId;
    private String messageText;
    private List<BotApiMethod> messagesToSend = new ArrayList<>();
    private PasswordEncoder passwordEncoder;

    @Autowired
    public MessageHandler(BotService botService, BotMenuTemplate menu, PasswordEncoder passwordEncoder) {
        this.botService = botService;
        this.menu = menu;
        this.passwordEncoder = passwordEncoder;
    }

    public List<BotApiMethod> handleMessage(Update update, BotUser updateSender) {
        chatId = getUpdateChatId(update);
        messageText = update.getMessage().getText() == null ? "null" : update.getMessage().getText();
        messagesToSend.clear();

        if (updateSender.getBotUserStatus().equals(BotUserStatus.WITHOUT_STATUS)) {
            handleWithoutStatusRequest(updateSender);
            return messagesToSend;
        }

        if (updateSender.getBotUserStatus().equals(BotUserStatus.ADDING_GIFT_NAME)) {
            handleAddingGiftNameRequest(updateSender);
            resetUserStatus(updateSender);
            return messagesToSend;
        }

        if (updateSender.getBotUserStatus().equals(BotUserStatus.ADDING_GIFT_DESCRIPTION)) {
            handleAddingGiftDescriptionRequest(updateSender);
            resetUserStatus(updateSender);
            return messagesToSend;
        }

        if (updateSender.getBotUserStatus().equals(BotUserStatus.ADDING_GIFT_URl)) {
            handleAddingGiftURLRequest(updateSender);
            resetUserStatus(updateSender);
            return messagesToSend;
        }

        if (updateSender.getBotUserStatus().equals(BotUserStatus.SEARCHING_FRIEND)) {
            handleSearchingFriendRequest(updateSender);
            resetUserStatus(updateSender);
            return messagesToSend;
        }

        if (updateSender.getBotUserStatus().equals(BotUserStatus.CONTACTING_DEVELOPER)) {
            handleContactDeveloperRequest(updateSender);
            resetUserStatus(updateSender);
            return messagesToSend;
        }

        if (updateSender.getBotUserStatus().equals(BotUserStatus.SETTING_PASSWORD)) {
            handleSetPasswordRequest(updateSender);
            return messagesToSend;
        }

        if (updateSender.getBotUserStatus().equals(BotUserStatus.ADDING_GIFT_PHOTO) &&
            update.getMessage().hasPhoto()) {
            handleAddingPhotoRequest(update, updateSender);
            resetUserStatus(updateSender);
            return messagesToSend;
        }

        SendMessage unknownCommandMessage = menu.getMainMenuTemplate(chatId);
        unknownCommandMessage.setText("?????????????????????? ?????????????? " + DISAPPOINTED_ICON + "\n" +
            "???????????????? ?????? ?????? ???? ???????????????? ????????" + HMM_ICON);

        messagesToSend.add(unknownCommandMessage);

        return messagesToSend;
    }

    private void handleSearchingFriendRequest(BotUser updateSender) {
        Optional<BotUser> searchedUser = botService.findUserByUserName(getTelegramUserNameFromMessage(messageText));
        Optional<BotUser> userSearchedTo = botService.findUserByTelegramId(updateSender.getTgAccountId());
        logger.info("Handling searching friend request from user with id " + updateSender.getId());

        if (!searchedUser.isPresent()) {
            logger.info("Handling searching friend request(requested user wasn't found) from user with id "
                + updateSender.getId());
            messagesToSend.add(new SendMessage(chatId, "???? ???? ???????????? ?????????? ?????????????? ????????????????????????"
                + DISAPPOINTED_ICON));
            return;
        }

        if (searchedUser.isPresent() && searchedUser.get().isEnabled() == false) {
            logger.info("Handling searching friend request(requested was found, but blocked) from user with id "
                + updateSender.getId());
            messagesToSend.add(new SendMessage(chatId, "???????????? ???????????????????????? ????????????????????????"
                + DISAPPOINTED_ICON));
            return;
        }

        if (!searchedUser.equals(userSearchedTo)) {
            if (!isRequestedUserAlreadyFriend(userSearchedTo.get(), searchedUser.get())) {
                messagesToSend.add(menu.getFriendShipRequestToTemplate(searchedUser.get(), userSearchedTo.get()));
                messagesToSend.add(menu.getSendFriendshipRequestTemplate(chatId));
                logger.info("Handling searching friend request(successful) from user with id "
                    + updateSender.getId());
            } else {
                logger.info("Handling searching friend request(already friend) from user with id "
                    + updateSender.getId());
                messagesToSend.add(new SendMessage(chatId, "???? ?????? ???????????????? ???? ?????????????? ????????????????????????"
                    + UPSIDE_DOWN_FACE_ICON));
            }
        } else {
            logger.info("Handling searching friend request(attempt to subscribe to myself) from user with id "
                + updateSender.getId());
            messagesToSend.add(new SendMessage(chatId,
                "???? ???? ???????????? ?????????????????????? ?????? ???? ????????" + UPSIDE_DOWN_FACE_ICON));
        }
    }

    private void handleContactDeveloperRequest(BotUser updateSender) {
        logger.info("Handling contacting developer request from user with id " + updateSender.getId());
        messagesToSend.add(new SendMessage("988800148", "?????????????????? ???? @" + updateSender.getUserName() +
            ": " + messageText));
        messagesToSend.add(new SendMessage(chatId, CHECK_MARK_ICON + " ???????? ?????????????????? ???????????????????? ????????????????????????"));

    }

    private void handleSetPasswordRequest(BotUser updateSender) {
        logger.info("Handling set password request from user with id " + updateSender.getId());
        updateSender.setBotUserStatus(BotUserStatus.WITHOUT_STATUS);
        if (messageText.length() <= 3) {
            messagesToSend.add(new SendMessage(chatId, CROSS_MARK_ICON + " ???????????? ???? ?????????? ???????? ???????????? 4 ????????????????"));
        } else {
            updateSender.setPassword(passwordEncoder.encode(messageText));
            messagesToSend.add(new SendMessage(chatId, CHECK_MARK_ICON + " ???????????? ??????????????"));
        }
        botService.updateUser(updateSender);
    }

    private void handleAddingPhotoRequest(Update update, BotUser updateSender) { // to do : add check for photo manager actions
        logger.info("Handling adding photo request from user with id " + updateSender.getId());
        String inlineMessageId = updateSender.getCarryingInlineMessageId();
        int messageId = updateSender.getCarryingMessageId();

        Optional<Gift> gift = botService.findGiftById(updateSender.getUpdateGiftId());
        List<PhotoSize> photoSizes = update.getMessage().getPhoto();
        if (gift.isPresent()) {
            Gift updatedGift = gift.get();
            if (botService.assignPhotoURLToGift(updatedGift, photoSizes)) {
                messagesToSend.add(menu.getGiftRepresentationTemplate(updatedGift, chatId, messageId, inlineMessageId));
            } else {
                logger.warn("Exception  during adding photo to gift with id " + gift.get().getId()
                    + ". Error in bot service");
                messagesToSend.add(menu.getErrorStatusTemplate("???????????????????? ???? ????????????????, ?????????????????? ????????????",
                    chatId));
            }
        } else {
            logger.warn("Exception  during adding photo to gift with id " + gift.get().getId()
                + ". Present isn't present");
            messagesToSend.add(menu.getErrorStatusTemplate("???????????????????? ???? ????????????????, ?????????????????? ????????????",
                chatId));
        }
    }

    private void handleAddingGiftNameRequest(BotUser updateSender) {
        logger.info("Handling adding gift name request from user with id: " + updateSender.getId());
        String inlineMessageId = updateSender.getCarryingInlineMessageId();
        int messageId = updateSender.getCarryingMessageId();

        Gift gift = Gift.GiftBuilder.newGift().withName(messageText).build();

        if (botService.addGiftToUser(gift, updateSender)) {
            List<Gift> gifts = botService.findUserByTelegramId(updateSender.getTgAccountId()).get().getWishList().getGiftList();
            messagesToSend.add(menu.getMyWishListTemplate(gifts, chatId, messageId, inlineMessageId));
            addGiftAddedMessages(updateSender);
        } else {
            logger.warn("Exception  during adding gift name from user with id: " + updateSender.getId()
                + ". Result of add gift to user method false");
            messagesToSend.add(menu.getErrorStatusTemplate("?????????????? ???? ????????????????. ?????????????????? ????????????",
                chatId));
        }
    }

    private void handleAddingGiftDescriptionRequest(BotUser updateSender) {
        logger.info("Handling adding gift description request from user with id: " + updateSender.getId());
        String inlineMessageId = updateSender.getCarryingInlineMessageId();
        int messageId = updateSender.getCarryingMessageId();

        Optional<Gift> gift = botService.findGiftById(updateSender.getUpdateGiftId());

        if (gift.isPresent()) {
            Gift updatedGift = gift.get();
            updatedGift.setDescription(messageText);

            if (botService.updateGift(updatedGift)) {
                messagesToSend.add(menu.getGiftRepresentationTemplate(updatedGift, chatId, messageId, inlineMessageId));
            } else {
                logger.warn("Exception  during adding gift description from user with id: " + updateSender.getId()
                    + ". Result of update gift method false");
                messagesToSend.add(menu.getErrorStatusTemplate("???????????????? ?????????????? ???? ????????????????. ?????????????????? ????????????",
                    chatId));
            }
        } else {
            logger.warn("Exception  during adding gift description from user with id: " + updateSender.getId()
                + ". Gift with id " + updateSender.getUpdateGiftId() + " isn't present");
            messagesToSend.add(menu.getErrorStatusTemplate("???????????????? ?????????????? ???? ????????????????. ???? ???????????? ?????????? ??????????????",
                chatId));
        }
    }

    private void handleAddingGiftURLRequest(BotUser updateSender) {
        logger.info("Handling adding gift description request from user with id: " + updateSender.getId());
        String inlineMessageId = updateSender.getCarryingInlineMessageId();
        int messageId = updateSender.getCarryingMessageId();

        Optional<Gift> gift = botService.findGiftById(updateSender.getUpdateGiftId());

        if (gift.isPresent()) {
            Gift updatedGift = gift.get();
            updatedGift.setUrl(messageText);

            if (botService.updateGift(updatedGift)) {
                messagesToSend.add(menu.getGiftRepresentationTemplate(updatedGift, chatId, messageId, inlineMessageId));
            } else {
                logger.warn("Exception  during adding gift url from user with id: " + updateSender.getId()
                    + ". Result of update gift method false");
                messagesToSend.add(menu.getErrorStatusTemplate("???????????? ?????????????? ???? ????????????????. ?????????????????? ????????????",
                    chatId));
            }
        } else {
            logger.warn("Exception  during adding gift url from user with id: " + updateSender.getId()
                + ". Gift with id " + updateSender.getUpdateGiftId() + " isn't present");
            messagesToSend.add(menu.getErrorStatusTemplate("???????????? ?????????????? ???? ????????????????. ???? ???????????? ?????????? ??????????????",
                chatId));
        }
    }

    private void handleWithoutStatusRequest(BotUser updateSender) {
        if (messageText.equals("/start")) {
            logger.info("Handling /start request from user with id: " + updateSender.getId());
            messagesToSend.add(menu.getGreetingTemplate(chatId, updateSender));
            return;
        }

        if (messageText.equals("/menu")) {
            logger.info("Handling /menu request from user with id: " + updateSender.getId());
            messagesToSend.add(menu.getMainMenuTemplate(chatId));
            return;
        }

        logger.info("Handling unknown message request from user with id: " + updateSender.getId());
        SendMessage unknownCommandMessage = menu.getMainMenuTemplate(chatId);
        unknownCommandMessage.setText("???? ???????? ?????????? ?????????????? " + DISAPPOINTED_ICON + "\n" +
            "???????????????? ?????? ?????? ???? ???????????????? ????????" + HMM_ICON);

        messagesToSend.add(unknownCommandMessage);
    }


    private String getUpdateChatId(Update update) {
        if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getMessage().getChatId().toString();
        }
        if (update.hasMessage()) {
            return update.getMessage().getChatId().toString();
        }
        return "";
    }

    private boolean isRequestedUserAlreadyFriend(BotUser requestSender, BotUser requestReceiver) {
        return botService.getUserSubscribers(requestReceiver).contains(requestSender);
    }

    private void resetUserStatus(BotUser user) {
        user.setBotUserStatus(BotUserStatus.WITHOUT_STATUS);
        botService.updateUser(user);
    }

    private void addGiftAddedMessages(BotUser userAddGift) {
        List<BotUser> subscribers = botService.getUserSubscribers(userAddGift);

        for (BotUser subscriber : subscribers) {
            if (subscriber.isReadyReceiveUpdates()) {
                messagesToSend.add(new SendMessage(String.valueOf(subscriber.getTgAccountId()),
                    EXCLAMATION_ICON + "???????????????????????? @" + userAddGift.getUserName() +
                        " ?????????????? ?????????? ?????????????? ?? ???????? WishList"));
            }
        }
    }

    private String getTelegramUserNameFromMessage(String messageText) {
        if (messageText != null && !messageText.isEmpty()) {
            String firstChar = messageText.substring(0, 1);
            if (firstChar.equals("@")) {
                return messageText.substring(1, messageText.length());
            }
            return messageText;
        }
        return messageText;
    }
}
