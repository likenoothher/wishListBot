package handler;

import data.Storage;
import menu.AppMenu;
import model.BotUser;
import model.Gift;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static menu.Icon.*;

public class CallbackHandler {
    private Storage storage;
    private AppMenu menu;

    public CallbackHandler(Storage storage, AppMenu menu) {
        this.storage = storage;
        this.menu = menu;
    }

    public List<SendMessage> handleCallBackQuery(Update update, BotUser updateSender) {
        List<SendMessage> messagesToSend = new ArrayList<>();

        String callbackData = extractCallbackData(update.getCallbackQuery());
        String chatId = extractChatId(update);

        SendMessage unknownCommandMessage = new SendMessage(chatId, "Не знаю такой команды"
                + DISAPPOINTED_ICON);

        if (callbackData.equals("/my_wish_list")) {
            messagesToSend.add(menu.showMyWishListMenu(chatId));
        }

        if (callbackData.equals("/add_present_for_me")) {
            messagesToSend.add(menu.showAddPresentForMeAddPresentNameMenu(chatId));
        }

        if (callbackData.equals("/delete_one_of_my_present")) {
            List<Gift> gifts = updateSender.getWishList().getGiftList(); // гифт лист и виш лист одно и тоже. отрефакторить
            messagesToSend.add(menu.showDeletePresentFromWishListMenu(gifts, chatId));
        }

        if (callbackData.contains("/delete_my_present_under/id/")) {
            int giftIdForDelete = extractLastAfterSlashId(callbackData);
            Optional<Gift> deletedGift = storage.findGiftById(giftIdForDelete);

            boolean isDeleted = storage.deleteGiftOfUser(giftIdForDelete, updateSender);
            System.out.println(isDeleted);
            if (isDeleted) {
                messagesToSend.add(menu.showStatusMenu(isDeleted, chatId));
                if (deletedGift.get().occupiedBy() != null) {
                    messagesToSend.add(menu.showUserDeletedPresentYouGoingToDonateMenu(deletedGift.get(), updateSender));
                }
            } else {
                messagesToSend.add(new SendMessage(chatId, "Почему то не смогли найти подарок в нашей базе данных, " +
                        "скорей всего из-за кривых рук человека писавшего этот текст (-_-)"));
            }
        }


        if (callbackData.equals("/i_present")) {
            List<Gift> iPresentList = storage.getUserPresentsList(updateSender);
            messagesToSend.add(menu.showDeleteItemFromIPresentMenu(iPresentList, chatId));
        }

        if (callbackData.contains("/delete_i_present_under_number/id/")) {
            int refusedGiftId = extractLastAfterSlashId(callbackData);

            messagesToSend.add(menu.showStatusMenu(storage.refuseFromDonate(refusedGiftId, updateSender), chatId));
        }


        if (callbackData.equals("/my_subscribers")) {
            messagesToSend.add(menu.showMySubscribersMenu(chatId));
        }

        if (callbackData.equals("/show_my_subscribers")) {
            List<BotUser> userSubscribers = storage
                    .findUserByTelegramId(updateSender.getTgAccountId()).get().getSubscribers();
            messagesToSend.add(menu.showMySubscribersListMenu(userSubscribers, chatId));
        }

        if (callbackData.equals("/delete_subscribers")) {
            List<BotUser> userSubscribers = storage
                    .findUserByTelegramId(updateSender.getTgAccountId()).get().getSubscribers();
            messagesToSend.add(menu.showDeleteUserFromMySubscribersMenu(userSubscribers, chatId));
        }

        if (callbackData.contains("/delete_my_subscriber_with/id")) {
            long deletedUserId = extractLastAfterSlashId(callbackData);

            long byUserDeletedId = update.getCallbackQuery().getFrom().getId();

            BotUser deletedUser = storage.findUserByTelegramId(deletedUserId).orElse(null);
            BotUser byUserDeleted = storage.findUserByTelegramId(byUserDeletedId).orElse(null);

            boolean isUnsubscribed = storage.removeSubscriberFromSubscriptions(deletedUser, byUserDeleted);

            if (isUnsubscribed) {
                messagesToSend.add(menu.showStatusMenu(isUnsubscribed, chatId));
                messagesToSend.add(menu.showNotificationToDeletedSubscriberMenu(deletedUser, byUserDeleted));
            } else {
                messagesToSend.add(new SendMessage(chatId, "Возникла какая то ошибка, не смогли отписать " +
                        "пользователя " + deletedUser.getUserName() + " от тебя"));
            }
        }


        if (callbackData.equals("/my_subscriptions")) {
            messagesToSend.add(menu.showMySubscriptionsMenu(chatId));
        }

        if (callbackData.equals("/show_my_subscriptions")) {
            List<BotUser> userSubscriptions = storage.getUserSubscriptions(updateSender);
            messagesToSend.add(menu.showMySubscriptionsListMenu(userSubscriptions, chatId));
        }

        if (callbackData.contains("/see_wish_list_of_user/id")) {
            int wishListHolderId = extractLastAfterSlashId(callbackData);
            Optional<BotUser> wishListHolder = storage.findUserByTelegramId(wishListHolderId);
            if (wishListHolder.isPresent()) {
                messagesToSend.add(menu.showUserWishListMenu(wishListHolder.get(), chatId));
            } else {
                messagesToSend.add(new SendMessage(chatId, "Возникла какая то ошибка, не смогли найти " +
                        "WishList пользователя. Напиши " +
                        "создателю и скажи, всё что ты о нём думаешь. Думаю, он всё исправит :/"));
            }
        }

        if (callbackData.contains("/i_gonna_present/id")) {
            int giftId = extractLastAfterSlashId(callbackData);
            messagesToSend.add(menu.showStatusMenu(storage.donate(giftId, updateSender), chatId,
                    " Подарок добавлен в твою секцию \"Я - дарю\"",
                    " Подарок не добавлен. Произошла ошибка"));
        }

        if (callbackData.contains("ask-add-gift-anonym")) {
            int userRequestedId = extractLastAfterSlashId(callbackData);
            Optional<BotUser> userRequested = storage.findUserByTelegramId(userRequestedId);
            if (userRequested.isPresent()) {
                messagesToSend.add(menu.showAnonimAskAddGiftMenu(String.valueOf(userRequested.get().getTgChatId())));
                messagesToSend.add(menu.showStatusMenu(true, chatId, "Запрос отправлен"));
            } else {
                messagesToSend.add(menu.showStatusMenu(false, chatId));
            }

        }

        if (callbackData.contains("ask-add-gift-explicitly/id/")) {
            int userRequestedId = extractLastAfterSlashId(callbackData);
            Optional<BotUser> userRequested = storage.findUserByTelegramId(userRequestedId);
            if (userRequested.isPresent()) {
                messagesToSend.add(menu.showExplicitAskAddGiftMenu(userRequested.get()));
                messagesToSend.add(menu.showStatusMenu(true, chatId, "Запрос отправлен"));
            } else {
                messagesToSend.add(menu.showStatusMenu(false, chatId));
            }

        }

        if (callbackData.equals("/delete_subscriptions")) {
            messagesToSend.add(menu.showDeleteUserFromSubscriptionsMenu(storage.getUserSubscriptions(updateSender), chatId));
        }

        if (callbackData.contains("/delete_my_subscription_with/id")) {
            long deletedUserId = extractLastAfterSlashId(callbackData);

            long byUserDeletedId = update.getCallbackQuery().getFrom().getId();

            BotUser deletedUser = storage.findUserByTelegramId(deletedUserId).orElse(null);
            BotUser byUserDeleted = storage.findUserByTelegramId(byUserDeletedId).orElse(null);

            boolean isUnsubscribed = storage.removeSubscriptionFromSubscriber(deletedUser, byUserDeleted);

            if (isUnsubscribed) {
                messagesToSend.add(menu.showStatusMenu(isUnsubscribed, chatId));
            } else {
                messagesToSend.add(new SendMessage(chatId, "Возникла какая то ошибка, не смогли отписать " +
                        "тебя от пользователя " + deletedUser.getUserName()));
            }
        }


        if (callbackData.equals("/find_friend")) {
            messagesToSend.add(menu.showFindFriendMenu(chatId));
        }

        if (callbackData.contains("/accept_friendship/")) {
            long acceptedUserId = extractSecondLastAfterSlashId(callbackData);
            long byUserAcceptedId = extractLastAfterSlashId(callbackData);

            BotUser acceptedUser = storage.findUserByTelegramId(acceptedUserId).orElse(null);
            BotUser byUserAccepted = storage.findUserByTelegramId(byUserAcceptedId).orElse(null);

            boolean isSubscribed = storage.addSubscriberToSubscriptions(acceptedUser, byUserAccepted);

            if (isSubscribed) {
                messagesToSend.add(menu.showAcceptedFriendshipMenu(acceptedUser, byUserAccepted));
            }
        }

        if (callbackData.contains("/deny_friendship/")) {
            long deniedUserId = extractSecondLastAfterSlashId(callbackData);
            long byUserDeniedId = extractLastAfterSlashId(callbackData);

            BotUser userDenied = storage.findUserByTelegramId(deniedUserId).orElse(null);
            BotUser byUserDenied = storage.findUserByTelegramId(byUserDeniedId).orElse(null);

            if (!byUserDenied.getSubscribers().contains(userDenied)) {

                messagesToSend.add(menu.showDeniedFriendshipMenu(byUserDenied, userDenied));
            } else {
                messagesToSend.add(new SendMessage(String.valueOf(byUserDenied.getTgChatId()),
                        "Пользователь уже добавлен" + MAN_SHRUGGING_ICON + "\n" +
                                " Удалить его можно через меню \"Мои подписчики\""));
            }

        }

        if (callbackData.equals("/settings")) {
            messagesToSend.add(menu.showSettingsMenu(chatId));
        }

        if (callbackData.equals("/set_is_ready_receive_update")) {
            messagesToSend.add(menu.showIsReadyReceiveUpdateMenu(chatId));
        }

        if (callbackData.equals("/set_is_ready_receive_update_true")) {
            Optional<BotUser> currentUser = storage.findUserByTelegramId(updateSender.getTgAccountId());
            if (currentUser.isPresent()) {
                BotUser updatedUser = currentUser.get();
                updatedUser.setReadyReceiveUpdates(true);
                messagesToSend.add(menu.showStatusMenu(storage.updateUser(updatedUser), chatId,
                        "Уведомление об обновлениях друзей включено"));
            } else {
                messagesToSend.add(new SendMessage(chatId, "Настройки не сохранены. Не найден пользователь"
                        + CROSS_MARK_ICON));
            }
        }

        if (callbackData.equals("/set_is_ready_receive_update_false")) {
            Optional<BotUser> currentUser = storage.findUserByTelegramId(updateSender.getTgAccountId());
            if (currentUser.isPresent()) {
                BotUser updatedUser = currentUser.get();
                updatedUser.setReadyReceiveUpdates(false);
                messagesToSend.add(menu.showStatusMenu(storage.updateUser(updatedUser), chatId,
                        "Уведомление об обновлениях друзей выключено"));
            } else {
                messagesToSend.add(new SendMessage(chatId, "Настройки не сохранены. Не найден пользователь"
                        + CROSS_MARK_ICON));
            }
        }

        if (callbackData.equals("/set_visibility")) {
            messagesToSend.add(menu.showSetVisibilityWishListMenu(chatId));
        }

        if (callbackData.equals("/set_visibility_subscribers_all_false")) {
            Optional<BotUser> currentUser = storage.findUserByTelegramId(updateSender.getTgAccountId());
            if (currentUser.isPresent()) {
                BotUser updatedUser = currentUser.get();
                updatedUser.setAllCanSeeMyWishList(false);
                messagesToSend.add(menu.showStatusMenu(storage.updateUser(updatedUser), chatId,
                        "Видимость твоего WishList'а - только подписчики" ));
            } else {
                messagesToSend.add(new SendMessage(chatId, "Настройки не сохранены. Не найден пользователь"
                        + CROSS_MARK_ICON));
            }
        }

        if (callbackData.equals("/set_visibility_subscribers_all_true")) {
            Optional<BotUser> currentUser = storage.findUserByTelegramId(updateSender.getTgAccountId());
            if (currentUser.isPresent()) {
                BotUser updatedUser = currentUser.get();
                updatedUser.setAllCanSeeMyWishList(true);
                messagesToSend.add(menu.showStatusMenu(storage.updateUser(updatedUser), chatId,
                        "Видимость твоего WishList'а - все пользователи",
                        "Произошла ошибка. Настройки не сохранены" + CROSS_MARK_ICON));
            } else {
                messagesToSend.add(new SendMessage(chatId, "Настройки не сохранены. Не найден пользователь"
                        + CROSS_MARK_ICON));
            }
        }

        if (!messagesToSend.isEmpty()) {
            return messagesToSend;
        }

        unknownCommandMessage.setChatId(chatId);
        return List.of(unknownCommandMessage);
    }

    private String extractChatId(Update update) {
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
}
