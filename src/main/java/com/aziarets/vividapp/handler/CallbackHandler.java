package com.aziarets.vividapp.handler;

import com.aziarets.vividapp.data.Storage;
import com.aziarets.vividapp.menu.BotMenuTemplate;
import com.aziarets.vividapp.model.BotUser;
import com.aziarets.vividapp.model.BotUserStatus;
import com.aziarets.vividapp.model.Gift;
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
    private Storage storage;
    private BotMenuTemplate menu;
    private String chatId;
    private String callbackData;
    private List<BotApiMethod> messagesToSend = new ArrayList<>();

    @Autowired
    public CallbackHandler(Storage storage, BotMenuTemplate menu) {
        this.storage = storage;
        this.menu = menu;
    }

    public List<BotApiMethod> handleCallBackQuery(Update update, BotUser updateSender) {
        chatId = getUpdateChatId(update);
        messagesToSend.clear();
        callbackData = extractCallbackData(update.getCallbackQuery());

        resetBotUserStatus(updateSender);

        if (callbackData.startsWith("/my_wish_list")) {
            messagesToSend.addAll(handleMyWishListRequests(update, updateSender));
            return
        }

        if (callbackData.startsWith("/i_present")) {
            messagesToSend.addAll(handleIPresentRequests(update, updateSender));
        }

        if (callbackData.startsWith("/my_subscribers")) {
            messagesToSend.addAll(handleMySubscribersRequests(update, updateSender));
        }

        if (callbackData.startsWith("/my_subscriptions")) {
            messagesToSend.addAll(handleMySubscriptionsRequests(update, updateSender));
        }

        if (callbackData.startsWith("/find_friend")) {
            messagesToSend.addAll(handleFindFriendRequests(update, updateSender));
        }

        if (callbackData.startsWith("/settings")) {
            messagesToSend.addAll(handleSettingsRequests(update, updateSender));
        }

        if (callbackData.equals("/main_menu")) {
            int messageId = update.getCallbackQuery().getMessage().getMessageId();
            String inlineMessageId = update.getCallbackQuery().getInlineMessageId();
            messagesToSend.add(callbackAnswer(update));
            messagesToSend.add(menu.showEditedMainMenu(extractChatId(update), messageId, inlineMessageId));
        }

        if (!messagesToSend.isEmpty()) {
            return messagesToSend;
        }
        String chatId = extractChatId(update);
        SendMessage unknownCommandMessage = new SendMessage(chatId, "Не знаю такой команды"
            + DISAPPOINTED_ICON);
        unknownCommandMessage.setChatId(chatId);

        return List.of(unknownCommandMessage);
    }

    private List<BotApiMethod> handleMyWishListRequests(Update update, BotUser updateSender) {
        List<BotApiMethod> messagesToSend = new ArrayList<>();
        String inlineMessageId = update.getCallbackQuery().getInlineMessageId();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();

        String callbackData = extractCallbackData(update.getCallbackQuery());
        String chatId = extractChatId(update);

        if (callbackData.equals("/my_wish_list")) {
            List<Gift> gifts = storage.getUserWishListGifts(updateSender.getTgAccountId());
            messagesToSend.add(callbackAnswer(update));
            messagesToSend.add(menu.getMyWishListTemplate(gifts, chatId, messageId, inlineMessageId));
        }

        if (callbackData.equals("/my_wish_list/add_present")) {
            Optional<BotUser> user = storage.findUserByTelegramId(updateSender.getTgAccountId());

            if (user.isPresent()) {
                BotUser updatedUser = user.get();
                updatedUser.setBotUserStatus(BotUserStatus.ADDING_GIFT_NAME);
                updatedUser.setCarryingMessageId(update.getCallbackQuery().getMessage().getMessageId());
                updatedUser.setCarryingInlineMessageId(update.getCallbackQuery().getInlineMessageId());
                storage.updateUser(updatedUser);
                messagesToSend.add(callbackAnswer(update));
                messagesToSend.add(new SendMessage(chatId, KEYBOARD_ICON + " Напиши имя подарка и отправь"));
            } else {
                messagesToSend.add(callbackAnswer(update, "Почему то не смогли найти пользователя в нашей базе данных, " +
                    "скорей всего из-за кривых рук человека писавшего этот текст (-_-)"));
            }
        }

        if (callbackData.contains("/my_wish_list/edit_my_present_under/id/")) {
            int updatedGiftId = extractLastAfterSlashId(callbackData);

            Optional<Gift> updatedGift = storage.findGiftById(updatedGiftId);
            if (updatedGift.isPresent()) {
//                storage.updateUser(updateSender);
                messagesToSend.add(callbackAnswer(update));
                messagesToSend.add(menu.getGiftRepresentationTemplate(updatedGift.get(), chatId, messageId, inlineMessageId));
            } else {
                messagesToSend.add(callbackAnswer(update, "Почему то не смогли найти пользователя в нашей базе данных, " +
                    "скорей всего из-за кривых рук человека писавшего этот текст (-_-)"));
            }
        }

        if (callbackData.contains("my_wish_list/edit_description_of_present_under/id/")) {
            int updatedGiftId = extractLastAfterSlashId(callbackData);
            Optional<BotUser> user = storage.findUserByTelegramId(updateSender.getTgAccountId());

            if (user.isPresent()) {
                BotUser updatedUser = user.get();
                updatedUser.setBotUserStatus(BotUserStatus.ADDING_GIFT_DESCRIPTION);
                updatedUser.setUpdateGiftId(updatedGiftId);
                updatedUser.setCarryingMessageId(update.getCallbackQuery().getMessage().getMessageId());
                updatedUser.setCarryingInlineMessageId(update.getCallbackQuery().getInlineMessageId());
                storage.updateUser(updatedUser);
                messagesToSend.add(callbackAnswer(update));
                messagesToSend.add(new SendMessage(chatId, KEYBOARD_ICON + " Напиши детальное описание подарка и отправь"));
            } else {
                messagesToSend.add(menu.getErrorStatusTemplate("Почему то не смогли найти подарок в нашей базе данных, " +
                    "скорей всего из-за кривых рук человека писавшего этот текст (-_-)", chatId));
            }
        }

        if (callbackData.contains("my_wish_list/edit_url_of_present_under/id/")) {
            int updatedGiftId = extractLastAfterSlashId(callbackData);
            Optional<BotUser> user = storage.findUserByTelegramId(updateSender.getTgAccountId());
            if (user.isPresent()) {
                BotUser updatedUser = user.get();
                updatedUser.setBotUserStatus(BotUserStatus.ADDING_GIFT_URl);
                updatedUser.setUpdateGiftId(updatedGiftId);
                updatedUser.setCarryingMessageId(update.getCallbackQuery().getMessage().getMessageId());
                updatedUser.setCarryingInlineMessageId(update.getCallbackQuery().getInlineMessageId());
                storage.updateUser(updatedUser);
                messagesToSend.add(callbackAnswer(update));
                messagesToSend.add(new SendMessage(chatId, KEYBOARD_ICON + " Вставь ссылку на подарок и отправь"));
            } else {
                messagesToSend.add(callbackAnswer(update, "Почему то не смогли найти пользователя в нашей базе данных, " +
                    "скорей всего из-за кривых рук человека писавшего этот текст (-_-)"));
            }
        }

        if (callbackData.contains("/my_wish_list/delete_my_present_under/id/")) {
            int giftIdForDelete = extractLastAfterSlashId(callbackData);
            Optional<Gift> deletedGift = storage.findGiftById(giftIdForDelete);

            if (storage.deleteGiftOfUser(giftIdForDelete, updateSender)) {
                List<Gift> gifts = storage.getUserWishListGifts(updateSender.getTgAccountId());
                messagesToSend.add(callbackAnswer(update, CHECK_MARK_ICON + "Подарок "+ deletedGift.get().getName() + " был удалён"));
                messagesToSend.add(menu.getMyWishListTemplate(gifts, chatId, messageId, inlineMessageId));

                if (deletedGift.get().getOccupiedBy() != null) {
                    messagesToSend.add(menu.getUserDeletedPresentYouGoingToDonateTemplate(deletedGift.get(), updateSender)); // wrong receiver of message
                }
            } else {
                messagesToSend.add(callbackAnswer(update, "Почему то не смогли найти пользователя в нашей базе данных, " +
                    "скорей всего из-за кривых рук человека писавшего этот текст (-_-)"));
            }
        }
        return messagesToSend;
    }

    private List<BotApiMethod> handleIPresentRequests(Update update, BotUser updateSender) {
        List<BotApiMethod> messagesToSend = new ArrayList<>();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();
        String inlineMessageId = update.getCallbackQuery().getInlineMessageId();

        String callbackData = extractCallbackData(update.getCallbackQuery());
        String chatId = extractChatId(update);

        if (callbackData.equals("/i_present")) {

            Map<BotUser, Gift> iPresentList = storage.getUserPresentsMap(updateSender);
            messagesToSend.add(callbackAnswer(update));
            messagesToSend.add(menu.getIPresentTemplate(iPresentList, chatId, messageId, inlineMessageId));
        }

        if (callbackData.contains("/i_present/show_gift_under/id")) {
            int requestedGiftId = extractLastAfterSlashId(callbackData);
            Gift requestedGift = storage.findGiftById(requestedGiftId).get();
            BotUser giftHolder = storage.findGiftHolderByGiftId(requestedGiftId).get();
            messagesToSend.add(callbackAnswer(update));
            messagesToSend.add(menu.getIPresentGiftInfoTemplate(requestedGift, giftHolder, chatId, messageId, inlineMessageId));
        }

        if (callbackData.contains("/i_present/delete_gift_under/id/")) {
            int refusedGiftId = extractLastAfterSlashId(callbackData);
            if (storage.refuseFromDonate(refusedGiftId, updateSender)) {
                Map<BotUser, Gift> iPresentList  = storage.getUserPresentsMap(updateSender);
                messagesToSend.add(callbackAnswer(update, CHECK_MARK_ICON + " Подарок был удалён"));
                messagesToSend.add(menu.getIPresentTemplate(iPresentList, chatId, messageId, inlineMessageId)); // добавить всплывающее окно
            } else {
                Map<BotUser, Gift> iPresentList  = storage.getUserPresentsMap(updateSender);
                messagesToSend.add(callbackAnswer(update, CROSS_MARK_ICON + " Подарок не был удалён"));
                messagesToSend.add(menu.getIPresentTemplate(iPresentList, chatId, messageId, inlineMessageId));
            }
        }
        return messagesToSend;
    }

    private List<BotApiMethod> handleMySubscribersRequests(Update update, BotUser updateSender) {
        List<BotApiMethod> messagesToSend = new ArrayList<>();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();
        String inlineMessageId = update.getCallbackQuery().getInlineMessageId();

        String callbackData = extractCallbackData(update.getCallbackQuery());
        String chatId = extractChatId(update);

        if (callbackData.equals("/my_subscribers")) {
            List<BotUser> userSubscribers = storage.getUserSubscribers(updateSender);
            messagesToSend.add(callbackAnswer(update));
            messagesToSend.add(menu.getMySubscribersListTemplate(userSubscribers, chatId, messageId, inlineMessageId));
        }


        if (callbackData.contains("/my_subscribers/show/id")) {
            int subscriberId = extractLastAfterSlashId(callbackData);
            BotUser subscriber = storage.findUserByTelegramId(subscriberId).get();
            messagesToSend.add(callbackAnswer(update));
            messagesToSend.add(menu.getSubscriberRepresentationTemplate(subscriber, chatId, messageId, inlineMessageId));
        }

        if (callbackData.contains("/my_subscribers/delete_under/id")) {
            long deletedUserId = extractLastAfterSlashId(callbackData);
            long byUserDeletedId = updateSender.getTgAccountId();

            BotUser deletedUser = storage.findUserByTelegramId(deletedUserId).orElse(null);
            BotUser byUserDeleted = storage.findUserByTelegramId(byUserDeletedId).orElse(null);

            boolean isUnsubscribed = storage.removeSubscriberFromSubscriptions(deletedUser, byUserDeleted);
            List<BotUser> userSubscribers = storage.getUserSubscribers(updateSender);
            if (isUnsubscribed) {
                messagesToSend.add(callbackAnswer(update, CHECK_MARK_ICON + " Пользователь был удалён из списка твоих подписчиков"));
                messagesToSend.add(menu.getMySubscribersListTemplate(userSubscribers, chatId, messageId, inlineMessageId));
                messagesToSend.add(menu.getAlertToDeletedSubscriberTemplate(deletedUser, byUserDeleted));
            } else {
                messagesToSend.add(callbackAnswer(update, CROSS_MARK_ICON + " Пользователь не был удалён из списка твоих подписчиков"));
                messagesToSend.add(menu.getMySubscribersListTemplate(userSubscribers, chatId, messageId, inlineMessageId));
            }
        }
        return messagesToSend;
    }

    private List<BotApiMethod> handleMySubscriptionsRequests(Update update, BotUser updateSender) {
        List<BotApiMethod> messagesToSend = new ArrayList<>();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();
        String inlineMessageId = update.getCallbackQuery().getInlineMessageId();

        String callbackData = extractCallbackData(update.getCallbackQuery());
        String chatId = extractChatId(update);

        if (callbackData.equals("/my_subscriptions")) {
            List<BotUser> userSubscriptions = storage.getUserSubscriptions(updateSender);
            messagesToSend.add(callbackAnswer(update));
            messagesToSend.add(menu.getMySubscriptionsTemplate(userSubscriptions, chatId, messageId, inlineMessageId));
        }

        if (callbackData.contains("/my_subscriptions/show_representation/gift_id")) {
            int giftId = extractLastAfterSlashId(callbackData);
            Gift gift  = storage.findGiftById(giftId).get();
            BotUser giftHolder = storage.findGiftHolderByGiftId(giftId).get();
            messagesToSend.add(callbackAnswer(update));
            messagesToSend.add(menu.getGoingDonateGiftTemplate(gift,giftHolder,  chatId, messageId, inlineMessageId));
        }

        if (callbackData.contains("/my_subscriptions/show/id")) {
            int wishListHolderId = extractLastAfterSlashId(callbackData);
            Optional<BotUser> wishListHolder = storage.findUserByTelegramId(wishListHolderId);
            List<BotUser> userSubscriptions = storage.getUserSubscriptions(updateSender);
            List<Gift> gifts = storage.getAvailableToDonateGifts(wishListHolderId);

            if (wishListHolder.isPresent()) {
                wishListHolder.get().getWishList().setGiftList(gifts);
                messagesToSend.add(callbackAnswer(update));
                messagesToSend.add(menu.getUserWishListTemplate(wishListHolder.get(), chatId, messageId, inlineMessageId));
            } else {
                messagesToSend.add(callbackAnswer(update, CROSS_MARK_ICON + " Возникла какая то ошибка, не смогли найти " +
                    "WishList пользователя"));
                messagesToSend.add(menu.getMySubscriptionsTemplate(userSubscriptions, chatId, messageId, inlineMessageId));
            }
        }

        if (callbackData.contains("/my_subscriptions/going_donate/gift_id/")) {
            int giftId = extractLastAfterSlashId(callbackData);
            Optional<BotUser> wishListHolder;

            if (storage.donate(giftId, updateSender)) {
                wishListHolder = storage.findGiftHolderByGiftId(giftId);
                List<Gift> gifts = storage.getAvailableToDonateGifts(wishListHolder.get().getId());
                wishListHolder.get().getWishList().setGiftList(gifts);
                messagesToSend.add(callbackAnswer(update, CHECK_MARK_ICON + " Подарок добавлен в твою секцию \"Я дарю \"" +I_PRESENT_ICON));
                messagesToSend.add(menu.getUserWishListTemplate(wishListHolder.get(), chatId, messageId, inlineMessageId));
            } else {
                wishListHolder = storage.findGiftHolderByGiftId(giftId);
                messagesToSend.add(callbackAnswer(update, CROSS_MARK_ICON + " Подарок не добавлен. Произошла ошибка"));
                messagesToSend.add(menu.getUserWishListTemplate(wishListHolder.get(), chatId, messageId, inlineMessageId));
            }
        }

        if (callbackData.contains("/my_subscriptions/ask_add_gift_anonymously/id/")) {
            int userRequestedToId = extractLastAfterSlashId(callbackData);
            Optional<BotUser> userRequestedTo = storage.findUserByTelegramId(userRequestedToId);
            List<BotUser> userSubscriptions = storage.getUserSubscriptions(updateSender);
            List<BotUser> userSubscribers = storage.getUserSubscribers(userRequestedTo.get());

            if (userRequestedTo.isPresent() && userSubscribers.contains(updateSender)) {
                messagesToSend.add(menu.getAnonymouslyAskAddGiftTemplate(userRequestedTo.get()));
                messagesToSend.add(callbackAnswer(update, CHECK_MARK_ICON + " Запрос отправлен"));
                messagesToSend.add(menu.getMySubscriptionsTemplate(userSubscriptions, chatId, messageId, inlineMessageId));
            } else {
                messagesToSend.add(callbackAnswer(update, CROSS_MARK_ICON + " Запрос не отправлен. Возможно ты был удалён из списка друзей"));
                messagesToSend.add(menu.getMySubscriptionsTemplate(userSubscriptions, chatId, messageId, inlineMessageId));
            }
        }

        if (callbackData.contains("/my_subscriptions/ask_add_gift_explicitly/id/")) {
            int userRequestedToId = extractLastAfterSlashId(callbackData);
            Optional<BotUser> userRequestedTo = storage.findUserByTelegramId(userRequestedToId);
            List<BotUser> userSubscriptions = storage.getUserSubscriptions(updateSender);
            List<BotUser> userSubscribers = storage.getUserSubscribers(userRequestedTo.get());

            if (userRequestedTo.isPresent() && userSubscribers.contains(updateSender)) {
                messagesToSend.add(menu.getExplicitAskAddGiftTemplate(userRequestedTo.get()));
                messagesToSend.add(callbackAnswer(update, CHECK_MARK_ICON + " Запрос отправлен"));
                messagesToSend.add(menu.getMySubscriptionsTemplate(userSubscriptions, chatId, messageId, inlineMessageId));
            } else {
                messagesToSend.add(callbackAnswer(update, CROSS_MARK_ICON + " Запрос не отправлен. Не смогли найти пользователя"));
                messagesToSend.add(menu.getMySubscriptionsTemplate(userSubscriptions, chatId, messageId, inlineMessageId));
            }
        }

        if (callbackData.contains("/my_subscriptions/delete_under/id")) {
            long deletedUserId = extractLastAfterSlashId(callbackData);
            long byUserDeletedId = updateSender.getTgAccountId();

            BotUser deletedUser = storage.findUserByTelegramId(deletedUserId).orElse(null);
            BotUser byUserDeleted = storage.findUserByTelegramId(byUserDeletedId).orElse(null);

            boolean isUnsubscribed = storage.removeSubscriptionFromSubscriber(deletedUser, byUserDeleted);
            List<BotUser> userSubscriptions = storage.getUserSubscriptions(updateSender);
            if (isUnsubscribed) {
                messagesToSend.add(callbackAnswer(update, CHECK_MARK_ICON + " Ты был успешно отписан от пользователя @"
                  + deletedUser.getUserName()));
                messagesToSend.add(menu.getMySubscriptionsTemplate(userSubscriptions, chatId, messageId, inlineMessageId));
            } else {
                messagesToSend.add(callbackAnswer(update, CROSS_MARK_ICON + " Возникла ошибка, не смогли отписать @"
                    + deletedUser.getUserName() + " от тебя"));
            }
        }

        return messagesToSend;
    }

    private List<BotApiMethod> handleFindFriendRequests(Update update, BotUser updateSender) {
        List<BotApiMethod> messagesToSend = new ArrayList<>();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();
        String inlineMessageId = update.getCallbackQuery().getInlineMessageId();

        String callbackData = extractCallbackData(update.getCallbackQuery());
        String chatId = extractChatId(update);

        if (callbackData.equals("/find_friend")) {
            Optional<BotUser> user = storage.findUserByTelegramId(updateSender.getTgAccountId());

            if (user.isPresent()) {
                BotUser updatedUser = user.get();
                updatedUser.setBotUserStatus(BotUserStatus.SEARCHING_FRIEND);
                storage.updateUser(updatedUser);
                messagesToSend.add(callbackAnswer(update));
                messagesToSend.add(new SendMessage(chatId, KEYBOARD_ICON + " Напиши имя пользователя в Telegram"));
            }
        }

        if (callbackData.contains("/find_friend/accept_friendship/")) {
            long requestedUserId = extractSecondLastAfterSlashId(callbackData);
            long byUserAcceptedId = extractLastAfterSlashId(callbackData);

            BotUser requestedUser = storage.findUserByTelegramId(requestedUserId).orElse(null);
            BotUser byUserAccepted = storage.findUserByTelegramId(byUserAcceptedId).orElse(null);

            boolean isSubscribed = storage.addSubscriberToSubscriptions(requestedUser, byUserAccepted);

            messagesToSend.add(callbackAnswer(update));
            if (isSubscribed) {
                messagesToSend.add(menu.getFriendShipAcceptedTemplate(byUserAccepted, String.valueOf(requestedUser.getTgAccountId())));
                messagesToSend.add(menu.getAcceptedFriendshipTemplate(requestedUser.getUserName(),
                    String.valueOf(byUserAccepted.getTgAccountId()), messageId, inlineMessageId));
            } else {
                messagesToSend.add(menu.getErrorStatusTemplate("Произошла ошибка, пользователь не был добавлен",
                    String.valueOf(byUserAccepted.getTgAccountId())));
            }
        }

        if (callbackData.contains("/find_friend/deny_friendship/")) {
            long requestedUserId = extractSecondLastAfterSlashId(callbackData);
            long byUserDeniedId = extractLastAfterSlashId(callbackData);

            BotUser requestedUser = storage.findUserByTelegramId(requestedUserId).orElse(null);
            BotUser byUserDenied = storage.findUserByTelegramId(byUserDeniedId).orElse(null);

            messagesToSend.add(callbackAnswer(update));
            messagesToSend.add(menu.getErrorStatusTemplate(byUserDenied.getUserName() + " отклонил предложение дружбы",
                String.valueOf(requestedUser.getTgAccountId())));
            messagesToSend.add(menu.getDeniedFriendshipTemplate(requestedUser.getUserName(),
                String.valueOf(byUserDenied.getTgAccountId()), messageId, inlineMessageId));
        }

        return messagesToSend;
    }

    private List<BotApiMethod> handleSettingsRequests(Update update, BotUser updateSender) {
        List<BotApiMethod> messagesToSend = new ArrayList<>();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();
        String inlineMessageId = update.getCallbackQuery().getInlineMessageId();

        String callbackData = extractCallbackData(update.getCallbackQuery());
        String chatId = extractChatId(update);
        if (callbackData.equals("/settings")) {
            messagesToSend.add(callbackAnswer(update));
            messagesToSend.add(menu.getSettingsTemplate(chatId, messageId, inlineMessageId));
        }
//        if (callbackData.equals("/settings/help")) {          ADD SOS MENU!!!!!!!!!!!!
//            messagesToSend.add(callbackAnswer(update));
//            messagesToSend.add(new SendMessage(chatId, "Серьёзно планировала здесь что то увидеть?\uD83D\uDE06"));
//        }

        if (callbackData.equals("/settings/contact_developer")) {
            updateSender.setBotUserStatus(BotUserStatus.CONTACTING_DEVELOPER);
            storage.updateUser(updateSender);
            messagesToSend.add(callbackAnswer(update));
            messagesToSend.add(new SendMessage(chatId, "Напиши своё обращение к разработчику и отправь"));
        }


        if (callbackData.equals("/settings/set_is_ready_receive_update")) {
            messagesToSend.add(callbackAnswer(update));
            messagesToSend.add(menu.getUpdatesSettingsTemplate(chatId, messageId, inlineMessageId));
        }

        if (callbackData.equals("/settings/set_is_ready_receive_update/true")) {
            Optional<BotUser> currentUser = storage.findUserByTelegramId(updateSender.getTgAccountId());

            if (currentUser.isPresent()) {
                BotUser updatedUser = currentUser.get();
                updatedUser.setReadyReceiveUpdates(true);
                if (storage.updateUser(updatedUser)) {
                    messagesToSend.add(callbackAnswer(update, CHECK_MARK_ICON + " Уведомления об обновлениях друзей включены"));
                    messagesToSend.add(menu.getSettingsTemplate(chatId, messageId, inlineMessageId));
                } else {
                    messagesToSend.add(callbackAnswer(update, CROSS_MARK_ICON + " Произошла ошибка.Настройки не сохранены"));
                    messagesToSend.add(menu.getSettingsTemplate(chatId, messageId, inlineMessageId));
                }
            } else {
                messagesToSend.add(callbackAnswer(update, CROSS_MARK_ICON + " Настройки не сохранены. Не найден пользователь"));
                messagesToSend.add(menu.getSettingsTemplate(chatId, messageId, inlineMessageId));
            }
        }

        if (callbackData.equals("/settings/set_is_ready_receive_update/false")) {
            Optional<BotUser> currentUser = storage.findUserByTelegramId(updateSender.getTgAccountId());

            if (currentUser.isPresent()) {
                BotUser updatedUser = currentUser.get();
                updatedUser.setReadyReceiveUpdates(false);
                if (storage.updateUser(updatedUser)) {
                    messagesToSend.add(callbackAnswer(update, CHECK_MARK_ICON + " Уведомления об обновлениях друзей отключены"));
                    messagesToSend.add(menu.getSettingsTemplate(chatId, messageId, inlineMessageId));
                } else {
                    messagesToSend.add(callbackAnswer(update, CROSS_MARK_ICON + " Произошла ошибка.Настройки не сохранены"));
                    messagesToSend.add(menu.getSettingsTemplate(chatId, messageId, inlineMessageId));
                }
            } else {
                messagesToSend.add(callbackAnswer(update, CROSS_MARK_ICON + " Настройки не сохранены. Не найден пользователь"));
                messagesToSend.add(menu.getSettingsTemplate(chatId, messageId, inlineMessageId));
            }
        }

        if (callbackData.equals("/settings/set_visibility")) {
            messagesToSend.add(callbackAnswer(update));
            messagesToSend.add(menu.getVisibilitySettingsTemplate(chatId, messageId, inlineMessageId));
        }

        if (callbackData.equals("/settings/set_visibility_subscribers_only/true")) {
            Optional<BotUser> currentUser = storage.findUserByTelegramId(updateSender.getTgAccountId());
            if (currentUser.isPresent()) {
                BotUser updatedUser = currentUser.get();
                updatedUser.setAllCanSeeMyWishList(false);
                if (storage.updateUser(updatedUser)) {
                    messagesToSend.add(callbackAnswer(update, CHECK_MARK_ICON + " Видимость твоего WishList'а - только подписчики"));
                    messagesToSend.add(menu.getSettingsTemplate(chatId, messageId, inlineMessageId));
                } else {
                    messagesToSend.add(callbackAnswer(update, CROSS_MARK_ICON + " Произошла ошибка.Настройки не сохранены"));
                    messagesToSend.add(menu.getSettingsTemplate(chatId, messageId, inlineMessageId));
                }
            } else {
                messagesToSend.add(callbackAnswer(update, CROSS_MARK_ICON + " Настройки не сохранены. Не найден пользователь"));
                messagesToSend.add(menu.getSettingsTemplate(chatId, messageId, inlineMessageId));
            }
        }

        if (callbackData.equals("/settings/set_visibility_subscribers_only/false")) {
            Optional<BotUser> currentUser = storage.findUserByTelegramId(updateSender.getTgAccountId());
            if (currentUser.isPresent()) {
                BotUser updatedUser = currentUser.get();
                updatedUser.setAllCanSeeMyWishList(true);
                if (storage.updateUser(updatedUser)) {
                    messagesToSend.add(callbackAnswer(update, CHECK_MARK_ICON + " Видимость твоего WishList'а - все пользователи"));
                    messagesToSend.add(menu.getSettingsTemplate(chatId, messageId, inlineMessageId));
                } else {
                    messagesToSend.add(callbackAnswer(update, CROSS_MARK_ICON + " Произошла ошибка.Настройки не сохранены"));
                    messagesToSend.add(menu.getSettingsTemplate(chatId, messageId, inlineMessageId));
                }
            } else {
                messagesToSend.add(callbackAnswer(update, CROSS_MARK_ICON + " Настройки не сохранены. Не найден пользователь"));
                messagesToSend.add(menu.getSettingsTemplate(chatId, messageId, inlineMessageId));
            }
        }

        return messagesToSend;
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

    private AnswerCallbackQuery callbackAnswer(Update update, String message){
        AnswerCallbackQuery answer = new AnswerCallbackQuery();
        String callbackId = update.getCallbackQuery().getId();
        answer.setCallbackQueryId(callbackId);
        answer.setText(message);
        answer.setShowAlert(false);
        return answer;
    }

    private AnswerCallbackQuery callbackAnswer(Update update){
        AnswerCallbackQuery answer = new AnswerCallbackQuery();
        String callbackId = update.getCallbackQuery().getId();
        answer.setCallbackQueryId(callbackId);
        answer.setShowAlert(false);
        return answer;
    }

    private void resetBotUserStatus(BotUser user) {
        if(user.getBotUserStatus() != BotUserStatus.WITHOUT_STATUS) {
            user.setBotUserStatus(BotUserStatus.WITHOUT_STATUS);
            storage.updateUser(user);
        }
    }
}
