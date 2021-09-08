package com.aziarets.vividapp.handler;

import com.aziarets.vividapp.exception.GiftsLimitReachedException;
import com.aziarets.vividapp.menu.BotMenuTemplate;
import com.aziarets.vividapp.model.BotUser;
import com.aziarets.vividapp.model.BotUserStatus;
import com.aziarets.vividapp.model.Gift;
import com.aziarets.vividapp.service.BotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.aziarets.vividapp.menu.Icon.*;

@Component
public class CallbackHandler {
    private static final Logger logger = LoggerFactory.getLogger(CallbackHandler.class);

    private BotService botService;
    private BotMenuTemplate menu;
    private String chatId;
    private String callbackData;
    private String inlineMessageId;
    private int messageId;
    private List<BotApiMethod> messagesToSend = new ArrayList<>();

    @Autowired
    public CallbackHandler(BotService botService, BotMenuTemplate menu) {
        this.botService = botService;
        this.menu = menu;
    }

    public List<BotApiMethod> handleCallBackQuery(Update update, BotUser updateSender) {
        chatId = getUpdateChatId(update);
        messagesToSend.clear();
        callbackData = extractCallbackData(update.getCallbackQuery());
        inlineMessageId = update.getCallbackQuery().getInlineMessageId();
        messageId = update.getCallbackQuery().getMessage().getMessageId();

        resetBotUserStatus(updateSender);

        if (callbackData.startsWith("/my_wish_list")) {
            handleMyWishListRequests(update, updateSender);
            return messagesToSend;
        }

        if (callbackData.startsWith("/i_present")) {
            handleIPresentRequests(update, updateSender);
            return messagesToSend;
        }

        if (callbackData.startsWith("/my_subscribers")) {
            handleMySubscribersRequests(update, updateSender);
            return messagesToSend;
        }

        if (callbackData.startsWith("/my_subscriptions")) {
            handleMySubscriptionsRequests(update, updateSender);
            return messagesToSend;
        }

        if (callbackData.startsWith("/find_friend")) {
            handleFindFriendRequests(update, updateSender);
            return messagesToSend;
        }

        if (callbackData.startsWith("/settings")) {
            handleSettingsRequests(update, updateSender);
            return messagesToSend;
        }

        if (callbackData.startsWith("/web")) {
            handleWebRequests(update, updateSender);
            return messagesToSend;
        }

        if (callbackData.equals("/main_menu")) {
            messagesToSend.add(callbackAnswer(update));
            messagesToSend.add(menu.showEditedMainMenu(chatId, messageId, inlineMessageId));
            return messagesToSend;
        }

        logger.info("Handling unknown call back request from user with id " + updateSender.getId());
        SendMessage unknownCommandMessage = new SendMessage(chatId, "Не знаю такой команды"
            + DISAPPOINTED_ICON);
        unknownCommandMessage.setChatId(chatId);
        messagesToSend.add(unknownCommandMessage);

        return messagesToSend;
    }

    private void handleMyWishListRequests(Update update, BotUser updateSender) {
        if (callbackData.equals("/my_wish_list")) {
            logger.info("Handling my wish list request from user with id " + updateSender.getId());
            List<Gift> gifts = botService.getUserWishListGifts(updateSender.getId());
            messagesToSend.add(callbackAnswer(update));
            messagesToSend.add(menu.getMyWishListTemplate(gifts, chatId, messageId, inlineMessageId));
            return;
        }

        if (callbackData.equals("/my_wish_list/add_present")) {
            logger.info("Handling add present request from user with id " + updateSender.getId());
            updateBotUserCarriedParameters(updateSender, BotUserStatus.ADDING_GIFT_NAME);
            messagesToSend.add(callbackAnswer(update));
            messagesToSend.add(new SendMessage(chatId, KEYBOARD_ICON + " Напиши имя подарка и отправь"));
            return;
        }

        if (callbackData.contains("/my_wish_list/edit_my_present_under/id/")) {
            int updatedGiftId = extractLastAfterSlashId(callbackData);
            logger.info("Handling edit present request from user with id " + updateSender.getId()
                + " edited gift id " + updatedGiftId);
            Optional<Gift> updatedGift = botService.findGiftById(updatedGiftId);

            if (updatedGift.isPresent()) {
                messagesToSend.add(callbackAnswer(update));
                messagesToSend.add(menu.getGiftRepresentationTemplate(updatedGift.get(), chatId, messageId, inlineMessageId));
            } else {
                logger.warn("Exception  during handling delete present request from user with id " + updateSender.getId()
                    + " updated gift with id " + updatedGiftId + " wasn't found");
                messagesToSend.add(callbackAnswer(update, "Подарок не удалён, т.к. не был найден"));
            }
            return;
        }

        if (callbackData.contains("my_wish_list/edit_description_of_present_under/id/")) {
            int updatedGiftId = extractLastAfterSlashId(callbackData);
            logger.info("Handling edit present(edit description) request from user with id " + updateSender.getId()
                + " edited gift id " + updatedGiftId);
            updateBotUserCarriedParameters(updateSender, BotUserStatus.ADDING_GIFT_DESCRIPTION, updatedGiftId);
            messagesToSend.add(callbackAnswer(update));
            messagesToSend.add(new SendMessage(chatId, KEYBOARD_ICON + " Напиши детальное описание подарка и отправь"));
            return;
        }

        if (callbackData.contains("my_wish_list/edit_url_of_present_under/id/")) {
            int updatedGiftId = extractLastAfterSlashId(callbackData);
            logger.info("Handling edit present(edit url) request from user with id " + updateSender.getId()
                + " edited gift id " + updatedGiftId);
            updateBotUserCarriedParameters(updateSender, BotUserStatus.ADDING_GIFT_URl, updatedGiftId);
            messagesToSend.add(callbackAnswer(update));
            messagesToSend.add(new SendMessage(chatId, KEYBOARD_ICON + " Вставь ссылку на подарок и отправь"));

            return;
        }

        if (callbackData.contains("my_wish_list/edit_photo_of_present_under/id/")) {
            int updatedGiftId = extractLastAfterSlashId(callbackData);
            logger.info("Handling edit present(edit photo) request from user with id " + updateSender.getId()
                + " edited gift id " + updatedGiftId);
            updateBotUserCarriedParameters(updateSender, BotUserStatus.ADDING_GIFT_PHOTO, updatedGiftId);
            messagesToSend.add(callbackAnswer(update));
            messagesToSend.add(new SendMessage(chatId, KEYBOARD_ICON + " Прикрепи изображение подарка и отправь"));

            return;
        }

        if (callbackData.contains("/my_wish_list/delete_my_present_under/id/")) {
            int giftIdForDelete = extractLastAfterSlashId(callbackData);
            logger.info("Handling delete present request from user with id " + updateSender.getId()
                + " deleted gift id " + giftIdForDelete);
            Gift deletedGift = botService.findGiftById(giftIdForDelete).orElse(null);

            if (botService.deleteGift(giftIdForDelete) && deletedGift != null) {
                List<Gift> gifts = botService.getUserWishListGifts(updateSender.getId());
                messagesToSend.add(callbackAnswer(update, CHECK_MARK_ICON + "Подарок " + deletedGift.getName()
                    + " был удалён"));
                messagesToSend.add(menu.getMyWishListTemplate(gifts, chatId, messageId, inlineMessageId));

                if (deletedGift.getOccupiedBy() != null && deletedGift.getOccupiedBy().isReadyReceiveUpdates()) {
                    messagesToSend.add(menu.getUserDeletedPresentYouGoingToDonateTemplate(deletedGift, updateSender));
                }
            } else {
                logger.warn("Exception  during handling delete present request from user with id " + updateSender.getId()
                    + " deleted gift with id " + giftIdForDelete + " wasn't found");
                messagesToSend.add(callbackAnswer(update, "Подарок не удалён, т.к. не был найден"));
            }
        }
        return;
    }

    private void handleIPresentRequests(Update update, BotUser updateSender) {
        if (callbackData.equals("/i_present")) {
            logger.info("Handling I present request from user with id " + updateSender.getId());
            Map<Gift, BotUser> iPresentList = botService.getUserPresentsMap(updateSender);
            messagesToSend.add(callbackAnswer(update));
            messagesToSend.add(menu.getIPresentTemplate(iPresentList, chatId, messageId, inlineMessageId));
            return;
        }

        if (callbackData.contains("/i_present/show_gift_under/id")) {
            int requestedGiftId = extractLastAfterSlashId(callbackData);
            logger.info("Handling I present request(show gift) from user with id " + updateSender.getId()
                + "showing present id " + requestedGiftId);
            Gift requestedGift = botService.findGiftById(requestedGiftId).get();
            BotUser giftHolder = botService.findGiftHolderByGiftId(requestedGiftId).get();
            messagesToSend.add(callbackAnswer(update));
            messagesToSend.add(menu.getIPresentGiftInfoTemplate(requestedGift, giftHolder, chatId, messageId, inlineMessageId));
            return;
        }

        if (callbackData.contains("/i_present/delete_gift_under/id/")) {
            int refusedGiftId = extractLastAfterSlashId(callbackData);
            logger.info("Handling I present request(refuse from donate) from user with id " + updateSender.getId()
                + "refused present id " + refusedGiftId);
            if (botService.refuseFromDonate(refusedGiftId, updateSender)) {
                Map<Gift, BotUser> iPresentList = botService.getUserPresentsMap(updateSender);
                messagesToSend.add(callbackAnswer(update, CHECK_MARK_ICON + " Подарок был удалён"));
                messagesToSend.add(menu.getIPresentTemplate(iPresentList, chatId, messageId, inlineMessageId));
            } else {
                logger.warn("Exception  during handling I present request(refuse from donate) from user with id " +
                    +updateSender.getId() + ", false result refuse from donate method");
                Map<Gift, BotUser> iPresentList = botService.getUserPresentsMap(updateSender);
                messagesToSend.add(callbackAnswer(update, CROSS_MARK_ICON + " Подарок не был удалён"));
                messagesToSend.add(menu.getIPresentTemplate(iPresentList, chatId, messageId, inlineMessageId));
            }
        }
        return;
    }

    private void handleMySubscribersRequests(Update update, BotUser updateSender) {
        if (callbackData.equals("/my_subscribers")) {
            logger.info("Handling my subscribers request from user with id " + updateSender.getId());
            List<BotUser> userSubscribers = botService.getUserSubscribers(updateSender);
            messagesToSend.add(callbackAnswer(update));
            messagesToSend.add(menu.getMySubscribersListTemplate(userSubscribers, chatId, messageId, inlineMessageId));
            return;
        }

        if (callbackData.contains("/my_subscribers/show/id")) {
            int subscriberId = extractLastAfterSlashId(callbackData);
            logger.info("Handling my subscribers request(show subscriber) from user with id " + updateSender.getId()
                + "showing user id " + subscriberId);
            BotUser subscriber = botService.findUserByTelegramId(subscriberId).get();
            messagesToSend.add(callbackAnswer(update));
            messagesToSend.add(menu.getSubscriberRepresentationTemplate(subscriber, chatId, messageId, inlineMessageId));
            return;
        }

        if (callbackData.contains("/my_subscribers/delete_under/id")) {
            long deletedUserId = extractLastAfterSlashId(callbackData);
            long byUserDeletedId = updateSender.getTgAccountId();
            logger.info("Handling my subscribers request(delete subscriber) from user with id " + updateSender.getId()
                + "deleting user id " + deletedUserId);

            BotUser deletedUser = botService.findUserByTelegramId(deletedUserId).orElse(null);
            BotUser byUserDeleted = botService.findUserByTelegramId(byUserDeletedId).orElse(null);

            if (botService.removeSubscriberFromSubscriptions(deletedUser, byUserDeleted)) {
                List<BotUser> userSubscribers = botService.getUserSubscribers(updateSender);
                messagesToSend.add(callbackAnswer(update, CHECK_MARK_ICON
                    + " Пользователь был удалён из списка твоих подписчиков"));
                messagesToSend.add(menu.getMySubscribersListTemplate(userSubscribers, chatId, messageId, inlineMessageId));
                if (deletedUser.isReadyReceiveUpdates()) {
                    messagesToSend.add(menu.getAlertToDeletedSubscriberTemplate(deletedUser, byUserDeleted));
                }
            } else {
                logger.warn("Exception  during handling my subscribers request(delete subscriber) from user with id " +
                    +updateSender.getId() + ", false result during remove subscriber method");
                List<BotUser> userSubscribers = botService.getUserSubscribers(updateSender);
                messagesToSend.add(callbackAnswer(update, CROSS_MARK_ICON
                    + " Пользователь не был удалён из списка твоих подписчиков"));
                messagesToSend.add(menu.getMySubscribersListTemplate(userSubscribers, chatId, messageId, inlineMessageId));
            }
        }
        return;
    }

    private void handleMySubscriptionsRequests(Update update, BotUser updateSender) {
        if (callbackData.equals("/my_subscriptions")) {
            logger.info("Handling my subscriptions request from user with id " + updateSender.getId());
            List<BotUser> userSubscriptions = botService.getUserSubscriptions(updateSender);
            messagesToSend.add(callbackAnswer(update));
            messagesToSend.add(menu.getMySubscriptionsTemplate(userSubscriptions, chatId, messageId, inlineMessageId));
            return;
        }

        if (callbackData.contains("/my_subscriptions/show_representation/gift_id")) {
            int giftId = extractLastAfterSlashId(callbackData);
            logger.info("Handling my subscriptions request(show gift representation) from user with id " + updateSender.getId()
                + "showing gift id " + giftId);
            Gift gift = botService.findGiftById(giftId).get();
            BotUser giftHolder = botService.findGiftHolderByGiftId(giftId).get();
            messagesToSend.add(callbackAnswer(update));
            messagesToSend.add(menu.getGoingDonateGiftTemplate(gift, giftHolder, chatId, messageId, inlineMessageId));
            return;
        }

        if (callbackData.contains("/my_subscriptions/show/id")) {
            int wishListHolderId = extractLastAfterSlashId(callbackData);
            logger.info("Handling my subscriptions request(show subscription) from user with id " + updateSender.getId()
                + "showing user id " + wishListHolderId);
            Optional<BotUser> wishListHolder = botService.findUserByTelegramId(wishListHolderId);
            List<BotUser> userSubscriptions = botService.getUserSubscriptions(updateSender);
            List<Gift> gifts = botService.getAvailableToDonateGifts(wishListHolder.get().getId());

            if (wishListHolder.isPresent()) {
                wishListHolder.get().getWishList().setGiftList(gifts);
                messagesToSend.add(callbackAnswer(update));
                messagesToSend.add(menu.getUserWishListTemplate(wishListHolder.get(), chatId, messageId, inlineMessageId));
            } else {
                logger.warn("Exception  during handling my subscriptions request(show subscription) from user with id " +
                    +updateSender.getId() + ", user with id " + wishListHolderId + " isn't present");
                messagesToSend.add(callbackAnswer(update, CROSS_MARK_ICON + " Возникла какая то ошибка, не смогли найти " +
                    "WishList пользователя"));
                messagesToSend.add(menu.getMySubscriptionsTemplate(userSubscriptions, chatId, messageId, inlineMessageId));
            }
            return;
        }

        if (callbackData.contains("/my_subscriptions/going_donate/gift_id/")) {
            int giftId = extractLastAfterSlashId(callbackData);
            logger.info("Handling my subscriptions request(going donate) from user with id " + updateSender.getId()
                + "going donate gift id " + giftId);
            BotUser wishListHolder;
            boolean isDonated = false;
            try {
                isDonated = botService.donate(giftId, updateSender);
            } catch (GiftsLimitReachedException e) {
                wishListHolder = botService.findGiftHolderByGiftId(giftId).get();
                messagesToSend.add(callbackAnswer(update, CROSS_MARK_ICON
                    + " Исчерпан лимит подарков для данного пользователя"));
                messagesToSend.add(menu.getUserWishListTemplate(wishListHolder, chatId, messageId, inlineMessageId));
                return;
            }
            if (isDonated) {
                wishListHolder = botService.findGiftHolderByGiftId(giftId).get();
                messagesToSend.add(callbackAnswer(update, CHECK_MARK_ICON
                    + " Подарок добавлен в твою секцию \"Я дарю \"" + I_PRESENT_ICON));
                messagesToSend.add(menu.getUserWishListTemplate(wishListHolder, chatId, messageId, inlineMessageId));
            } else {
                logger.warn("Exception  during handling my subscriptions request(going donate) from user with id " +
                    +updateSender.getId() + ", false result during donate method");
                wishListHolder = botService.findGiftHolderByGiftId(giftId).get();
                messagesToSend.add(callbackAnswer(update, CROSS_MARK_ICON + " Подарок не добавлен. Произошла ошибка"));
                messagesToSend.add(menu.getUserWishListTemplate(wishListHolder, chatId, messageId, inlineMessageId));
            }
            return;
        }

        if (callbackData.contains("/my_subscriptions/ask_add_gift_anonymously/id/")) {
            int userRequestedToId = extractLastAfterSlashId(callbackData);
            logger.info("Handling my subscriptions request(ask add gift anonymously) from user with id " + updateSender.getId()
                + "requested user id " + userRequestedToId);
            Optional<BotUser> userRequestedTo = botService.findUserByTelegramId(userRequestedToId);
            List<BotUser> userSubscriptions = botService.getUserSubscriptions(updateSender);
            List<BotUser> userSubscribers = botService.getUserSubscribers(userRequestedTo.get());

            if (userRequestedTo.isPresent() && userSubscribers.contains(updateSender)
                && userRequestedTo.get().isReadyReceiveUpdates()) {
                messagesToSend.add(menu.getAnonymouslyAskAddGiftTemplate(userRequestedTo.get()));
                messagesToSend.add(callbackAnswer(update, CHECK_MARK_ICON + " Запрос отправлен"));
                messagesToSend.add(menu.getMySubscriptionsTemplate(userSubscriptions, chatId, messageId, inlineMessageId));
            } else {
                logger.warn("Exception  during handling my subscriptions request(ask add gift anonymously) from user with id " +
                    +updateSender.getId() + ", user requested to add gift isn't present or user with id"
                    + updateSender.getId() + " is not in the subscribers list of user with id "
                    + userRequestedTo.get().getId());
                messagesToSend.add(callbackAnswer(update, CROSS_MARK_ICON
                    + " Запрос не отправлен. Возможно ты был удалён из списка друзей или пользователь отключил " +
                    "доставку уведомлений"));
                messagesToSend.add(menu.getMySubscriptionsTemplate(userSubscriptions, chatId, messageId, inlineMessageId));
            }
            return;
        }

        if (callbackData.contains("/my_subscriptions/ask_add_gift_explicitly/id/")) {
            int userRequestedToId = extractLastAfterSlashId(callbackData);
            logger.info("Handling my subscriptions request(ask add gift explicitly) from user with id " + updateSender.getId()
                + "requested user id " + userRequestedToId);
            Optional<BotUser> userRequestedTo = botService.findUserByTelegramId(userRequestedToId);
            List<BotUser> userSubscriptions = botService.getUserSubscriptions(updateSender);
            List<BotUser> userSubscribers = botService.getUserSubscribers(userRequestedTo.get());

            if (userRequestedTo.isPresent() && userSubscribers.contains(updateSender)
                && userRequestedTo.get().isReadyReceiveUpdates()) {
                messagesToSend.add(menu.getExplicitAskAddGiftTemplate(userRequestedTo.get()));
                messagesToSend.add(callbackAnswer(update, CHECK_MARK_ICON + " Запрос отправлен"));
                messagesToSend.add(menu.getMySubscriptionsTemplate(userSubscriptions, chatId, messageId, inlineMessageId));
            } else {
                logger.warn("Exception  during handling my subscriptions request(ask add gift explicitly) from user with id " +
                    +updateSender.getId() + ", user requested to add gift isn't present or user with id"
                    + updateSender.getId() + " is not in the subscribers list of user with id "
                    + userRequestedTo.get().getId());
                messagesToSend.add(callbackAnswer(update, CROSS_MARK_ICON
                    + " Запрос не отправлен. Возможно ты был удалён из списка друзей или пользователь отключил " +
                    "доставку уведомлений"));
                messagesToSend.add(menu.getMySubscriptionsTemplate(userSubscriptions, chatId, messageId, inlineMessageId));
            }
            return;
        }

        if (callbackData.contains("/my_subscriptions/delete_under/id")) {
            long deletedUserId = extractLastAfterSlashId(callbackData);
            long byUserDeletedId = updateSender.getTgAccountId();
            logger.info("Handling my subscriptions request(delete subscriptions) from user with id " + updateSender.getId()
                + "deleted user id " + deletedUserId);

            BotUser deletedUser = botService.findUserByTelegramId(deletedUserId).orElse(null);
            BotUser byUserDeleted = botService.findUserByTelegramId(byUserDeletedId).orElse(null);

            if (botService.removeSubscriptionFromSubscriber(deletedUser, byUserDeleted)) {
                List<BotUser> userSubscriptions = botService.getUserSubscriptions(updateSender);
                messagesToSend.add(callbackAnswer(update, CHECK_MARK_ICON + " Ты был успешно отписан от пользователя @"
                    + deletedUser.getUserName()));
                messagesToSend.add(menu.getMySubscriptionsTemplate(userSubscriptions, chatId, messageId, inlineMessageId));
            } else {
                logger.warn("Exception  during handling my subscriptions request(delete subscriptions) from user with id " +
                    +updateSender.getId() + ", couldn't remove subscription with id " + deletedUserId
                    + " from user with id " + byUserDeletedId);
                messagesToSend.add(callbackAnswer(update, CROSS_MARK_ICON + " Возникла ошибка, не смогли отписать @"
                    + deletedUser.getUserName() + " от тебя"));
            }
        }
        return;
    }

    private void handleFindFriendRequests(Update update, BotUser updateSender) {
        if (callbackData.equals("/find_friend")) {
            logger.info("Handling find friend request from user with id " + updateSender.getId());
            updateSender.setBotUserStatus(BotUserStatus.SEARCHING_FRIEND);
            botService.updateUser(updateSender);
            messagesToSend.add(callbackAnswer(update));
            messagesToSend.add(menu.getFindFriendTemplate(chatId, messageId, inlineMessageId));
            return;
        }

        if (callbackData.contains("/find_friend/mutual_friendship")) {
            long requestedToId = extractLastAfterSlashId(callbackData);
            BotUser requestedUser = botService.findUserByTelegramId(requestedToId).orElse(null);
            BotUser byUserRequested = botService.findUserByTelegramId(updateSender.getTgAccountId()).orElse(null);
            logger.info("Handling mutual friendship request from user with id " + updateSender.getId());

            messagesToSend.add(callbackAnswer(update));
            if (requestedUser != null && byUserRequested != null) {
                messagesToSend.add(menu.getFriendShipRequestToTemplate(requestedUser, byUserRequested));
                messagesToSend.add(menu.getSendFriendshipRequestTemplate(chatId));
            } else {
                logger.warn("Exception during handling mutual friendship request from user with id " +
                    +updateSender.getId() + ", requestedUser or/and byUserRequested is null");
                messagesToSend.add(menu.getErrorStatusTemplate("Произошла ошибка, запрос не был отправлен",
                    String.valueOf(byUserRequested.getTgAccountId())));
            }
            return;
        }

        if (callbackData.contains("/find_friend/accept_friendship/")) {
            long requestedUserId = extractSecondLastAfterSlashId(callbackData);
            long byUserAcceptedId = extractLastAfterSlashId(callbackData);
            logger.info("Handling find friend request(accept friendship) from user with id " + updateSender.getId()
                + "accepted user id " + requestedUserId);

            BotUser requestedUser = botService.findUserByTelegramId(requestedUserId).orElse(null);
            BotUser byUserAccepted = botService.findUserByTelegramId(byUserAcceptedId).orElse(null);

            messagesToSend.add(callbackAnswer(update));
            if (botService.addSubscriberToSubscriptions(requestedUser, byUserAccepted)) {
                messagesToSend.add(menu.getFriendShipAcceptedTemplate(byUserAccepted, requestedUser));
                boolean isUpdateSenderInSubscriptions = botService.isUserSubscribedTo(requestedUser.getId(), byUserAccepted.getId());
                messagesToSend.add(menu.getAcceptedFriendshipTemplate(requestedUser,
                    byUserAccepted, messageId, inlineMessageId, isUpdateSenderInSubscriptions));
            } else {
                logger.warn("Exception during handling find friend request(accept friendship) from user with id " +
                    +updateSender.getId() + ", requestedUser or/and byUserDenied is null");
                messagesToSend.add(menu.getErrorStatusTemplate("Произошла ошибка, пользователь не был добавлен",
                    String.valueOf(byUserAccepted.getTgAccountId())));
            }
            return;
        }

        if (callbackData.contains("/find_friend/deny_friendship/")) {
            long requestedUserId = extractSecondLastAfterSlashId(callbackData);
            long byUserDeniedId = extractLastAfterSlashId(callbackData);
            logger.info("Handling find friend request(denied friendship) from user with id " + updateSender.getId()
                + "denied user id " + requestedUserId);


            BotUser requestedUser = botService.findUserByTelegramId(requestedUserId).orElse(null);
            BotUser byUserDenied = botService.findUserByTelegramId(byUserDeniedId).orElse(null);

            messagesToSend.add(callbackAnswer(update));
            if (requestedUser != null && byUserDenied != null) {

                messagesToSend.add(menu.getErrorStatusTemplate(byUserDenied.getUserName() + " отклонил предложение дружбы",
                    String.valueOf(requestedUser.getTgAccountId())));
                messagesToSend.add(menu.getDeniedFriendshipTemplate(requestedUser, byUserDenied, messageId, inlineMessageId));
            } else {
                logger.warn("Exception  during handling find friend request(denied friendship) from user with id " +
                    +updateSender.getId() + ", requestedUser or/and byUserDenied is null");
                messagesToSend.add(menu.getErrorStatusTemplate("Произошла ошибка, пользователь не был добавлен",
                    String.valueOf(byUserDenied.getTgAccountId())));
            }
        }
        return;
    }

    private void handleSettingsRequests(Update update, BotUser updateSender) {
        if (callbackData.equals("/settings")) {
            logger.info("Handling settings request from user with id " + updateSender.getId());

            messagesToSend.add(callbackAnswer(update));
            messagesToSend.add(menu.getSettingsTemplate(chatId, messageId, inlineMessageId));
            return;
        }

        if (callbackData.equals("/settings/contact_developer")) {
            logger.info("Handling settings request(contact developer) from user with id " + updateSender.getId());

            updateSender.setBotUserStatus(BotUserStatus.CONTACTING_DEVELOPER);
            botService.updateUser(updateSender);
            messagesToSend.add(callbackAnswer(update));
            messagesToSend.add(new SendMessage(chatId, "Напиши своё обращение к разработчику и отправь"));
            return;
        }

        if (callbackData.equals("/settings/set_is_ready_receive_update")) {
            logger.info("Handling settings request(is ready to receive updates) from user with id "
                + updateSender.getId());
            messagesToSend.add(callbackAnswer(update));
            messagesToSend.add(menu.getUpdatesSettingsTemplate(chatId, messageId, inlineMessageId));
            return;
        }

        if (callbackData.equals("/settings/set_is_ready_receive_update/true")) {
            logger.info("Handling settings request(is ready to receive updates) from user with id "
                + updateSender.getId() + ", value - true");
            updateSender.setReadyReceiveUpdates(true);
            if (botService.updateUser(updateSender)) {
                messagesToSend.add(callbackAnswer(update, CHECK_MARK_ICON
                    + " Уведомления об обновлениях друзей включены"));
                messagesToSend.add(menu.getSettingsTemplate(chatId, messageId, inlineMessageId));
            } else {
                logger.warn("Exception  during handling settings request(is ready to receive updates) from user with id " +
                    +updateSender.getId() + ", value - true");
                messagesToSend.add(callbackAnswer(update, CROSS_MARK_ICON
                    + " Произошла ошибка. Настройки не сохранены"));
                messagesToSend.add(menu.getSettingsTemplate(chatId, messageId, inlineMessageId));
            }
            return;
        }

        if (callbackData.equals("/settings/set_is_ready_receive_update/false")) {
            logger.info("Handling settings request(is ready to receive updates) from user with id "
                + updateSender.getId() + ", value - false");
            updateSender.setReadyReceiveUpdates(false);
            if (botService.updateUser(updateSender)) {
                messagesToSend.add(callbackAnswer(update, CHECK_MARK_ICON
                    + " Уведомления об обновлениях друзей отключены"));
                messagesToSend.add(menu.getSettingsTemplate(chatId, messageId, inlineMessageId));
            } else {
                logger.warn("Exception  during handling settings request(is ready to receive updates) from user with id " +
                    +updateSender.getId() + ", value - false");
                messagesToSend.add(callbackAnswer(update, CROSS_MARK_ICON
                    + " Произошла ошибка. Настройки не сохранены"));
                messagesToSend.add(menu.getSettingsTemplate(chatId, messageId, inlineMessageId));
            }
            return;
        }

        if (callbackData.equals("/settings/set_visibility")) {
            logger.info("Handling settings request(set visibility) from user with id "
                + updateSender.getId());
            messagesToSend.add(callbackAnswer(update));
            messagesToSend.add(menu.getVisibilitySettingsTemplate(chatId, messageId, inlineMessageId));
            return;
        }

        if (callbackData.equals("/settings/set_visibility_subscribers_only/true")) {
            logger.info("Handling settings request(set visibility) from user with id "
                + updateSender.getId() + ", value - true");
            updateSender.setAllCanSeeMyWishList(false);
            if (botService.updateUser(updateSender)) {
                messagesToSend.add(callbackAnswer(update, CHECK_MARK_ICON
                    + " Видимость твоего WishList'а - только подписчики"));
                messagesToSend.add(menu.getSettingsTemplate(chatId, messageId, inlineMessageId));
            } else {
                logger.warn("Exception  during handling settings request(set visibility) from user with id " +
                    +updateSender.getId() + ", value - true");
                messagesToSend.add(callbackAnswer(update, CROSS_MARK_ICON
                    + " Произошла ошибка. Настройки не сохранены"));
                messagesToSend.add(menu.getSettingsTemplate(chatId, messageId, inlineMessageId));
            }
            return;
        }

        if (callbackData.equals("/settings/set_visibility_subscribers_only/false")) {
            logger.info("Handling settings request(set visibility) from user with id "
                + updateSender.getId() + ", value - false");
            updateSender.setAllCanSeeMyWishList(true);
            if (botService.updateUser(updateSender)) {
                messagesToSend.add(callbackAnswer(update, CHECK_MARK_ICON
                    + " Видимость твоего WishList'а - все пользователи"));
                messagesToSend.add(menu.getSettingsTemplate(chatId, messageId, inlineMessageId));
            } else {
                logger.warn("Exception  during handling settings request(set visibility) from user with id " +
                    +updateSender.getId() + ", value - false");
                messagesToSend.add(callbackAnswer(update, CROSS_MARK_ICON
                    + " Произошла ошибка.Настройки не сохранены"));
                messagesToSend.add(menu.getSettingsTemplate(chatId, messageId, inlineMessageId));
            }
        }

        if (callbackData.equals("/settings/set_limit")) {
            logger.info("Handling settings request(set gift limit) from user with id "
                + updateSender.getId());
            messagesToSend.add(callbackAnswer(update));
            messagesToSend.add(menu.getGiftLimitTemplate(chatId, messageId, inlineMessageId, updateSender));
            return;
        }

        if (callbackData.contains("/settings/set_limit")) {
            int giftLimit = extractLastAfterSlashId(callbackData);
            logger.info("Handling settings request(set gift limit) from user with id "
                + updateSender.getId() + ", value - " + giftLimit);
            updateSender.setGiftLimit(giftLimit);
            if (botService.updateUser(updateSender)) {
                messagesToSend.add(callbackAnswer(update, CHECK_MARK_ICON
                    + " Лимит подарков - " + giftLimit));
                messagesToSend.add(menu.getSettingsTemplate(chatId, messageId, inlineMessageId));
            } else {
                logger.warn("Exception  during handling settings request(set gift limit) from user with id " +
                    +updateSender.getId() + ", value - " + giftLimit);
                messagesToSend.add(callbackAnswer(update, CROSS_MARK_ICON
                    + " Произошла ошибка.Настройки не сохранены"));
                messagesToSend.add(menu.getSettingsTemplate(chatId, messageId, inlineMessageId));
            }
        }
        return;
    }

    private void handleWebRequests(Update update, BotUser updateSender) {
        if (callbackData.equals("/web")) {
            logger.info("Handling web request from user with id " + updateSender.getId());
            messagesToSend.add(callbackAnswer(update));
            messagesToSend.add(menu.getWebTemplate(chatId, messageId, inlineMessageId, updateSender));
            return;
        }

        if (callbackData.equals("/web/set_password")) {
            logger.info("Handling web request(set password) from user with id " + updateSender.getId());
            updateBotUserCarriedParameters(updateSender, BotUserStatus.SETTING_PASSWORD);
            messagesToSend.add(callbackAnswer(update));
            messagesToSend.add(new SendMessage(chatId, KEYBOARD_ICON + " Введи пароль(не короче 4 символов) и отправь"));
            return;
        }
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

    private String extractCallbackData(CallbackQuery callbackQuery) {
        return callbackQuery.getData();
    }

    private Long extractSecondLastAfterSlashId(String callbackQueryData) {
        String[] array = callbackQueryData.split("/");
        return Long.parseLong(array[array.length - 2]);
    }

    private Integer extractLastAfterSlashId(String callbackQueryData) {
        String[] array = callbackQueryData.split("/");
        return Integer.parseInt(array[array.length - 1]);
    }

    private AnswerCallbackQuery callbackAnswer(Update update, String message) {
        AnswerCallbackQuery answer = new AnswerCallbackQuery();
        String callbackId = update.getCallbackQuery().getId();
        answer.setCallbackQueryId(callbackId);
        answer.setText(message);
        answer.setShowAlert(false);
        return answer;
    }

    private AnswerCallbackQuery callbackAnswer(Update update) {
        AnswerCallbackQuery answer = new AnswerCallbackQuery();
        String callbackId = update.getCallbackQuery().getId();
        answer.setCallbackQueryId(callbackId);
        answer.setShowAlert(false);
        return answer;
    }

    private void updateBotUserCarriedParameters(BotUser user, BotUserStatus botUserStatus) {
        user.setBotUserStatus(botUserStatus);
        user.setCarryingMessageId(messageId);
        user.setCarryingInlineMessageId(inlineMessageId);
        botService.updateUser(user);
    }

    private void updateBotUserCarriedParameters(BotUser user, BotUserStatus botUserStatus, int updatedGiftId) {
        user.setUpdateGiftId(updatedGiftId);
        user.setBotUserStatus(botUserStatus);
        user.setCarryingMessageId(messageId);
        user.setCarryingInlineMessageId(inlineMessageId);
        botService.updateUser(user);
    }

    private void resetBotUserStatus(BotUser user) {
        if (user.getBotUserStatus() != BotUserStatus.WITHOUT_STATUS) {
            user.setBotUserStatus(BotUserStatus.WITHOUT_STATUS);
            botService.updateUser(user);
        }
    }
}
