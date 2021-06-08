package handler;

import data.Storage;
import menu.AppMenu;
import model.BotUser;
import model.BotUserStatus;
import model.Gift;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
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


    public List<BotApiMethod> handleCallBackQuery(Update update, BotUser updateSender) {
        List<BotApiMethod> messagesToSend = new ArrayList<>();
        updateSender.setBotUserStatus(BotUserStatus.WITHOUT_STATUS);
        storage.updateUser(updateSender);

        String callbackData = extractCallbackData(update.getCallbackQuery());

        if (callbackData.startsWith("/my_wish_list")) {
            messagesToSend.addAll(handleMyWishListRequests(update, updateSender));
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

        if (callbackData.startsWith("/main_menu")) {
            messagesToSend.add(menu.showMainMenu(extractChatId(update)));
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

        String callbackData = extractCallbackData(update.getCallbackQuery());
        String chatId = extractChatId(update);

        if (callbackData.equals("/my_wish_list")) {
            messagesToSend.add(menu.showMyWishListMenu(chatId));
        }

        if (callbackData.equals("/my_wish_list/add_present")) {
            Optional<BotUser> user = storage.findUserByTelegramId(updateSender.getTgAccountId());

            if (user.isPresent()) {
                BotUser updatedUser = user.get();
                updatedUser.setBotUserStatus(BotUserStatus.ADDING_GIFT_NAME);
                storage.updateUser(updatedUser);
                messagesToSend.add(new SendMessage(chatId, KEYBOARD_ICON + " Напиши имя подарка и отправь"));
            } else {
                messagesToSend.add(menu.showErrorStatusMenu("Почему то не смогли найти пользователя в нашей базе данных, " +
                    "скорей всего из-за кривых рук человека писавшего этот текст (-_-)", chatId));
            }
        }

        if (callbackData.equals("/my_wish_list/manage_list")) {
            List<Gift> gifts = storage.findUserByTelegramId(updateSender.getTgAccountId()).get()
                .getWishList().getGiftList();
            messagesToSend.add(menu.showManagingPresentsInWishListMenu(gifts, chatId));
        }

        if (callbackData.contains("/my_wish_list/edit_my_present_under/id/")) {
            int updatedGiftId = extractLastAfterSlashId(callbackData);

            Optional<Gift> updatedGift = storage.findGiftById(updatedGiftId);
            if (updatedGift.isPresent()) {
                messagesToSend.add(menu.showGiftRepresentationMenu(updatedGift.get(), chatId));
            } else {
                messagesToSend.add(menu.showErrorStatusMenu("Почему то не смогли найти подарок в нашей базе данных, " +
                    "скорей всего из-за кривых рук человека писавшего этот текст (-_-)", chatId));
            }
        }

        if (callbackData.contains("my_wish_list/edit_description_of_present_under/id/")) {

            int updatedGiftId = extractLastAfterSlashId(callbackData);
            System.out.println(updatedGiftId);
            Optional<BotUser> user = storage.findUserByTelegramId(updateSender.getTgAccountId());
            System.out.println(user.isPresent());
            if (user.isPresent()) {
                BotUser updatedUser = user.get();
                updatedUser.setBotUserStatus(BotUserStatus.ADDING_GIFT_DESCRIPTION);
                updatedUser.setUpdateGiftId(updatedGiftId);
                System.out.println(storage.updateUser(updatedUser));

                messagesToSend.add(new SendMessage(chatId, KEYBOARD_ICON + " Напиши детальное описание подарка и отправь"));
            } else {
                messagesToSend.add(menu.showErrorStatusMenu("Почему то не смогли найти подарок в нашей базе данных, " +
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
                storage.updateUser(updatedUser);
                messagesToSend.add(new SendMessage(chatId, KEYBOARD_ICON + " Вставь ссылку на подарок и отправь"));
            } else {
                messagesToSend.add(menu.showErrorStatusMenu("Почему то не смогли найти подарок в нашей базе данных, " +
                    "скорей всего из-за кривых рук человека писавшего этот текст (-_-)", chatId));
            }
        }

        if (callbackData.equals("/my_wish_list/delete_present")) {
            List<Gift> gifts = updateSender.getWishList().getGiftList(); // гифт лист и виш лист одно и тоже. отрефакторить
            messagesToSend.add(menu.showDeletePresentFromWishListMenu(gifts, chatId));
        }

        if (callbackData.contains("/my_wish_list/delete_my_present_under/id/")) {
            int giftIdForDelete = extractLastAfterSlashId(callbackData);
            Optional<Gift> deletedGift = storage.findGiftById(giftIdForDelete);

            if (storage.deleteGiftOfUser(giftIdForDelete, updateSender)) {
                messagesToSend.add(menu.showOkStatusMenu("Подарок был удалён", chatId));
                if (deletedGift.get().occupiedBy() != null) {
                    messagesToSend.add(menu.showUserDeletedPresentYouGoingToDonateMenu(deletedGift.get(), updateSender));
                }
            } else {
                messagesToSend.add(new SendMessage(chatId, "Почему то не смогли найти подарок в нашей базе данных, " +
                    "скорей всего из-за кривых рук человека писавшего этот текст (-_-)"));
            }
        }
        return messagesToSend;
    }

    private List<BotApiMethod> handleIPresentRequests(Update update, BotUser updateSender) {
        List<BotApiMethod> messagesToSend = new ArrayList<>();

        String callbackData = extractCallbackData(update.getCallbackQuery());
        String chatId = extractChatId(update);

        if (callbackData.equals("/i_present")) {
            List<Gift> iPresentList = storage.getUserPresentsList(updateSender);
            messagesToSend.add(menu.showDeleteItemFromIPresentMenu(iPresentList, chatId));
        }

        if (callbackData.contains("i_present/delete_gift_under/id/")) {
            int refusedGiftId = extractLastAfterSlashId(callbackData);

            if (storage.refuseFromDonate(refusedGiftId, updateSender)) {
                messagesToSend.add(menu.showStatusMenu(true, chatId));
            } else {
                messagesToSend.add(menu.showStatusMenu(false, chatId));
            }
        }
        return messagesToSend;
    }

    private List<BotApiMethod> handleMySubscribersRequests(Update update, BotUser updateSender) {
        List<BotApiMethod> messagesToSend = new ArrayList<>();

        String callbackData = extractCallbackData(update.getCallbackQuery());
        String chatId = extractChatId(update);

        if (callbackData.equals("/my_subscribers")) {
            messagesToSend.add(menu.showMySubscribersMenu(chatId));
        }

        if (callbackData.equals("/my_subscribers/show")) {
//            List<BotUser> userSubscribers = storage
//                .findUserByTelegramId(updateSender.getTgAccountId()).get().getSubscribers();
            List<BotUser> userSubscribers = updateSender.getSubscribers();
            messagesToSend.add(menu.showMySubscribersListMenu(userSubscribers, chatId));
        }

        if (callbackData.equals("/my_subscribers/delete")) {
//            List<BotUser> userSubscribers = storage
//                .findUserByTelegramId(updateSender.getTgAccountId()).get().getSubscribers();
            List<BotUser> userSubscribers = updateSender.getSubscribers();
            messagesToSend.add(menu.showDeleteUserFromMySubscribersMenu(userSubscribers, chatId));
        }

        if (callbackData.contains("/my_subscribers/delete_under/id")) {
            long deletedUserId = extractLastAfterSlashId(callbackData);
            long byUserDeletedId = updateSender.getTgAccountId();

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
        return messagesToSend;
    }

    private List<BotApiMethod> handleMySubscriptionsRequests(Update update, BotUser updateSender) {
        List<BotApiMethod> messagesToSend = new ArrayList<>();

        String callbackData = extractCallbackData(update.getCallbackQuery());
        String chatId = extractChatId(update);

        if (callbackData.equals("/my_subscriptions")) {
            messagesToSend.add(menu.showMySubscriptionsMenu(chatId));
        }


        if (callbackData.equals("/my_subscriptions/show")) {
            List<BotUser> userSubscriptions = storage.getUserSubscriptions(updateSender);
            messagesToSend.add(menu.showMySubscriptionsListMenu(userSubscriptions, chatId));
        }

        if (callbackData.contains("/my_subscriptions/show/id")) {
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

        if (callbackData.contains("/my_subscriptions/going_donate/gift_id/")) {
            int giftId = extractLastAfterSlashId(callbackData);
            messagesToSend.add(menu.showStatusMenu(storage.donate(giftId, updateSender), chatId,
                " Подарок добавлен в секцию \"Я - дарю\" " + I_PRESENT_ICON,
                " Подарок не добавлен. Произошла ошибка"));
        }

        if (callbackData.contains("/my_subscriptions/ask_add_gift_anonymously/id/")) {
            int userRequestedToId = extractLastAfterSlashId(callbackData);
            Optional<BotUser> userRequestedTo = storage.findUserByTelegramId(userRequestedToId);

            if (userRequestedTo.isPresent()) {
                messagesToSend.add(menu.showAnonymouslyAskAddGiftMenu(userRequestedTo.get()));
                messagesToSend.add(menu.showOkStatusMenu("Запрос отправлен", chatId));
            } else {
                messagesToSend.add(menu.showErrorStatusMenu("Запрос не отправлен. Не смогли найти пользователя"
                    , chatId));
            }
        }

        if (callbackData.contains("/my_subscriptions/ask_add_gift_explicitly/id/")) {
            int userRequestedId = extractLastAfterSlashId(callbackData);
            Optional<BotUser> userRequested = storage.findUserByTelegramId(userRequestedId);
            if (userRequested.isPresent()) {
                messagesToSend.add(menu.showExplicitAskAddGiftMenu(userRequested.get()));
                messagesToSend.add(menu.showOkStatusMenu("Запрос отправлен", chatId));
            } else {
                messagesToSend.add(menu.showErrorStatusMenu("Запрос не отправлен. Не смогли найти пользователя"
                    , chatId));
            }

        }

        if (callbackData.equals("/my_subscriptions/delete")) {
            messagesToSend.add(menu.showDeleteUserFromSubscriptionsMenu(storage.getUserSubscriptions(updateSender), chatId));
        }

        if (callbackData.contains("/my_subscriptions/delete_under/id")) {
            long deletedUserId = extractLastAfterSlashId(callbackData);
            long byUserDeletedId = updateSender.getTgAccountId();

            BotUser deletedUser = storage.findUserByTelegramId(deletedUserId).orElse(null);
            BotUser byUserDeleted = storage.findUserByTelegramId(byUserDeletedId).orElse(null);

            boolean isUnsubscribed = storage.removeSubscriptionFromSubscriber(deletedUser, byUserDeleted);

            if (isUnsubscribed) {
                messagesToSend.add(menu.showOkStatusMenu("Ты был успешно отписан от пользователя @"
                    + deletedUser.getUserName(), chatId));
            } else {
                messagesToSend.add(menu.showErrorStatusMenu("Возникла какая-то ошибка, не смогли отписать " +
                    "тебя от пользователя " + deletedUser.getUserName(), chatId));
            }
        }

        return messagesToSend;
    }

    private List<BotApiMethod> handleFindFriendRequests(Update update, BotUser updateSender) {
        List<BotApiMethod> messagesToSend = new ArrayList<>();

        String callbackData = extractCallbackData(update.getCallbackQuery());
        String chatId = extractChatId(update);

        if (callbackData.equals("/find_friend")) {
            Optional<BotUser> user = storage.findUserByTelegramId(updateSender.getTgAccountId());

            if (user.isPresent()) {
                BotUser updatedUser = user.get();
                updatedUser.setBotUserStatus(BotUserStatus.SEARCHING_FRIEND);
                storage.updateUser(updatedUser);
                messagesToSend.add(new SendMessage(chatId, KEYBOARD_ICON + " Напиши имя пользователя в Telegram"));
            } else {
                messagesToSend.add(menu.showErrorStatusMenu("Почему то не смогли найти пользователя в нашей базе данных, " +
                    "скорей всего из-за кривых рук человека писавшего этот текст (-_-)", chatId));
            }
        }

        if (callbackData.contains("/find_friend/accept_friendship/")) {
            long requestedUserId = extractSecondLastAfterSlashId(callbackData);
            long byUserAcceptedId = extractLastAfterSlashId(callbackData);

            BotUser requestedUser = storage.findUserByTelegramId(requestedUserId).orElse(null);
            BotUser byUserAccepted = storage.findUserByTelegramId(byUserAcceptedId).orElse(null);

            boolean isSubscribed = storage.addSubscriberToSubscriptions(requestedUser, byUserAccepted);

            if (isSubscribed) {
                messagesToSend.add(menu.showFriendShipAcceptedMenu(byUserAccepted, String.valueOf(requestedUser.getTgChatId())));
                messagesToSend.add(menu.showOkStatusMenu(" Запрос на дружбу от @" + requestedUser.getUserName() + " принят." +
                    " Теперь он имеет доступ к твоему WishList'y", String.valueOf(byUserAccepted.getTgChatId())));
            } else {
                messagesToSend.add(menu.showErrorStatusMenu("Произошла ошибка, пользователь не был добавлен",
                    String.valueOf(byUserAccepted.getTgChatId())));
            }
        }

        if (callbackData.contains("/find_friend/deny_friendship/")) {
            long requestedUserId = extractSecondLastAfterSlashId(callbackData);
            long byUserDeniedId = extractLastAfterSlashId(callbackData);

            BotUser requestedUser = storage.findUserByTelegramId(requestedUserId).orElse(null);
            BotUser byUserDenied = storage.findUserByTelegramId(byUserDeniedId).orElse(null);

            if (!byUserDenied.getSubscribers().contains(requestedUser)) {
                messagesToSend.add(menu.showErrorStatusMenu(byUserDenied.getUserName() + " отклонил предложение дружбы",
                    String.valueOf(requestedUser.getTgChatId())));
            } else {
                messagesToSend.add(new SendMessage(String.valueOf(byUserDenied.getTgChatId()),
                    "Пользователь уже был добавлен" + MAN_SHRUGGING_ICON + "\n" +
                        " Удалить его можно через меню\n\"Мои подписчики\"" + ARROW_LOWER_LEFT_ICON));
            }
        }

        return messagesToSend;
    }

    private List<BotApiMethod> handleSettingsRequests(Update update, BotUser updateSender) {
        List<BotApiMethod> messagesToSend = new ArrayList<>();

        String callbackData = extractCallbackData(update.getCallbackQuery());
        String chatId = extractChatId(update);
        if (callbackData.equals("/settings")) {
            messagesToSend.add(menu.showSettingsMenu(chatId));
        }
        if (callbackData.equals("/settings/help")) {
            messagesToSend.add(new SendMessage(chatId, "Серьёзно планировала здесь что то увидеть?\uD83D\uDE06"));
        }

        if (callbackData.equals("/settings/set_is_ready_receive_update")) {
            messagesToSend.add(menu.showIsReadyReceiveUpdateMenu(chatId));
        }

        if (callbackData.equals("/settings/set_is_ready_receive_update/true")) {
            Optional<BotUser> currentUser = storage.findUserByTelegramId(updateSender.getTgAccountId());

            if (currentUser.isPresent()) {
                BotUser updatedUser = currentUser.get();
                updatedUser.setReadyReceiveUpdates(true);
                if (storage.updateUser(updatedUser)) {
                    messagesToSend.add(menu.showOkStatusMenu("Уведомления об обновлениях друзей включены", chatId));
                } else {
                    messagesToSend.add(menu.showErrorStatusMenu("Произошла ошибка.Настройки не сохранены", chatId));
                }
            } else {
                messagesToSend.add(menu.showErrorStatusMenu("Настройки не сохранены. Не найден пользователь", chatId));
            }
        }

        if (callbackData.equals("/settings/set_is_ready_receive_update/false")) {
            Optional<BotUser> currentUser = storage.findUserByTelegramId(updateSender.getTgAccountId());

            if (currentUser.isPresent()) {
                BotUser updatedUser = currentUser.get();
                updatedUser.setReadyReceiveUpdates(false);
                if (storage.updateUser(updatedUser)) {
                    messagesToSend.add(menu.showOkStatusMenu("Уведомления об обновлениях друзей отключены", chatId));
                } else {
                    messagesToSend.add(menu.showErrorStatusMenu("Произошла ошибка.Настройки не сохранены", chatId));
                }
            } else {
                messagesToSend.add(menu.showErrorStatusMenu("Настройки не сохранены. Не найден пользователь", chatId));
            }
        }

        if (callbackData.equals("/settings/set_visibility")) {
            messagesToSend.add(menu.showSetVisibilityWishListMenu(chatId));
        }

        if (callbackData.equals("/settings/set_visibility_subscribers_only/false")) {
            Optional<BotUser> currentUser = storage.findUserByTelegramId(updateSender.getTgAccountId());
            if (currentUser.isPresent()) {
                BotUser updatedUser = currentUser.get();
                updatedUser.setAllCanSeeMyWishList(false);
                if (storage.updateUser(updatedUser)) {
                    messagesToSend.add(menu.showOkStatusMenu("Видимость твоего WishList'а - только подписчики", chatId));
                } else {
                    messagesToSend.add(menu.showErrorStatusMenu("Произошла ошибка.Настройки не сохранены", chatId));
                }
            } else {
                messagesToSend.add(menu.showErrorStatusMenu("Настройки не сохранены. Не найден пользователь", chatId));
            }
        }

        if (callbackData.equals("/settings/set_visibility_subscribers_only/true")) {
            Optional<BotUser> currentUser = storage.findUserByTelegramId(updateSender.getTgAccountId());
            if (currentUser.isPresent()) {
                BotUser updatedUser = currentUser.get();
                updatedUser.setAllCanSeeMyWishList(true);
                if (storage.updateUser(updatedUser)) {
                    messagesToSend.add(menu.showOkStatusMenu("Видимость твоего WishList'а - все пользователи", chatId));
                } else {
                    messagesToSend.add(menu.showErrorStatusMenu("Произошла ошибка.Настройки не сохранены", chatId));
                }
            } else {
                messagesToSend.add(menu.showErrorStatusMenu("Настройки не сохранены. Не найден пользователь", chatId));
            }
        }

        return messagesToSend;
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
