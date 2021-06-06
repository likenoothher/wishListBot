package menu;

import builder.InlineKeyboard;
import model.BotUser;
import model.Gift;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;

import static menu.Icon.*;

public class AppMenu {

    public SendMessage showMainMenu(String chatId) {
        SendMessage message = new SendMessage();
        message.setText("Ты в главном меню\n");

        InlineKeyboardMarkup replyKeyboard = InlineKeyboard.InlineKeyboardMarkupBuilder
                .newInlineKeyboardMarkup()
                .withRow()
                .buttonWithCallbackData("Мой WishList " + WISH_LIST_ICON, "/my_wish_list")
                .buttonWithCallbackData("Я дарю " + I_PRESENT_ICON, "/i_present")
                .endRow()
                .withRow()
                .buttonWithCallbackData("Мои подписчики" + ARROW_LOWER_LEFT_ICON, "/my_subscribers")
                .buttonWithCallbackData("Мои подписки" + ARROW_UPPER_RIGHT_ICON, "/my_subscriptions")
                .endRow()
                .withRow()
                .buttonWithCallbackData("Найти друга " + FIND_FRIEND_ICON, "/find_friend")
                .buttonWithCallbackData("Настройки " + SETTINGS_ICON, "/settings")
                .endRow()
                .build();

        message.setReplyMarkup(replyKeyboard);

        message.setChatId(chatId);
        return message;
    }

    public SendMessage showMyWishListMenu(String chatId) {
        SendMessage message = new SendMessage();
        message.setText("Ты сейчас в меню из которого можешь управлять своим WishList'ом");

        InlineKeyboardMarkup replyKeyboard = InlineKeyboard.InlineKeyboardMarkupBuilder
                .newInlineKeyboardMarkup()
                .withRow()
                .buttonWithCallbackData("Добавить подарок для себя" + PLUS_MARK_ICON,
                        "/add_present_for_me")
                .endRow()
                .withRow()
                .buttonWithCallbackData("Удалить подарок из WishList" + MINUS_MARK_ICON,
                        "/delete_one_of_my_present")
                .endRow()
                .build();

        message.setReplyMarkup(replyKeyboard);

        message.setChatId(chatId);
        return message;
    }

    public SendMessage showAddPresentForMeAddPresentNameMenu(String chatId) {
        SendMessage message = new SendMessage();
        message.setText("Введи название подарка в формате:\n" +
                "${название подарка}\n" +
                "(например, $iPhone 125)");

        message.setChatId(chatId);
        return message;
    }

    public SendMessage showDeletePresentFromWishListMenu(List<Gift> gifts, String chatId) {
        SendMessage message = new SendMessage();

        if (!gifts.isEmpty()) {
            message.setText("Нажми на подарок из списка, который ты хочешь удалить");

            InlineKeyboardMarkup replyKeyboard = InlineKeyboard.InlineKeyboardMarkupBuilder
                    .newInlineKeyboardMarkup()
                    .withCallBackButtonsFromList(gifts, "/delete_my_present_under/id")
                    .build();

            message.setReplyMarkup(replyKeyboard);
        } else {
            message.setText("Чтобы что-то удалить сначала нужно что-то добавить" + UPSIDE_DOWN_FACE_ICON);
        }

        message.setChatId(chatId);
        return message;
    }

    public SendMessage showUserDeletedPresentYouGoingToDonateMenu(Gift deletedGift, BotUser deletedByUser) {
        SendMessage message = new SendMessage();
        message.setText(MINUS_MARK_ICON + "Пользователь " + deletedByUser.getUserName() +
                " удалил подарок \"" + deletedGift.getName() + "\", который ты хотел ему подарить " + MAN_SHRUGGING_ICON + "\n" +
                "Попробуй выбрать для подарка что-нибудь ещё");

        message.setChatId(String.valueOf(deletedGift.occupiedBy().getTgChatId()));
        return message;
    }

    public SendMessage showDeleteItemFromIPresentMenu(List<Gift> gifts, String chatId) {
        SendMessage message = new SendMessage();

        if (!gifts.isEmpty()) {
            message.setText("Это подарки, которые ты планируешь подарить." + WISH_LIST_ICON +
                    "Нажми на подарок из списка, который ты передумал дарить");

            InlineKeyboardMarkup replyKeyboard = InlineKeyboard.InlineKeyboardMarkupBuilder
                    .newInlineKeyboardMarkup()
                    .withCallBackButtonsFromList(gifts, "/delete_i_present_under_number/id")
                    .build();

            message.setReplyMarkup(replyKeyboard);
        } else {
            message.setText("Это список подарков, который ты мог бы кому-то подарить, но, " +
                    "к сожалению, здесь пока ничего нет " + MAN_SHRUGGING_ICON);
        }

        message.setChatId(chatId);
        return message;
    }

    public SendMessage showMySubscribersMenu(String chatId) {
        SendMessage message = new SendMessage();
        message.setText("Ты сейчас в меню из которого можешь управлять своими подписчиками");

        InlineKeyboardMarkup replyKeyboard = InlineKeyboard.InlineKeyboardMarkupBuilder
                .newInlineKeyboardMarkup()
                .withRow()
                .buttonWithCallbackData(SEE_ICON + "Посмотреть подписчиков", "/show_my_subscribers")
                .endRow()
                .withRow()
                .buttonWithCallbackData(MINUS_MARK_ICON + "Удалить подписчиков", "/delete_subscribers")
                .endRow()
                .build();

        message.setReplyMarkup(replyKeyboard);

        message.setChatId(chatId);
        return message;
    }

    public SendMessage showMySubscribersListMenu(List<BotUser> subscribers, String chatId) {
        StringBuilder textMessage = new StringBuilder();
        if (!subscribers.isEmpty()) {
            textMessage.append(TWO_GUYS_ICON + "Список твоих друзей:\n");
            for (int i = 0; i < subscribers.size(); i++) {
                BotUser user = subscribers.get(i);
                textMessage.append(i + 1)
                        .append(".")
                        .append("User name - " + user.getUserName())
                        .append("\n")
                        .append("Имя - " + user.getFirstName());  // Добавить для кого дарит
            }
        } else {
            textMessage.append(" Кажется на тебя пока никто не подписан" + MAN_SHRUGGING_ICON);
        }
        SendMessage message = new SendMessage();
        message.setText(textMessage.toString());

        message.setChatId(chatId);
        return message;
    }


    public SendMessage showDeleteUserFromMySubscribersMenu(List<BotUser> subscribers, String chatId) {
        SendMessage message = new SendMessage();

        if (!subscribers.isEmpty()) {
            message.setText(WISH_LIST_ICON + "Это список твоих подписчиков.\n" +
                    "Нажми на того кто больше тебе не друг и он больше не сможет получить доступ к твоему WishList'у");

            InlineKeyboardMarkup replyKeyboard = InlineKeyboard.InlineKeyboardMarkupBuilder
                    .newInlineKeyboardMarkup()
                    .withCallBackButtonsFromUserList(subscribers, "/delete_my_subscriber_with/id")
                    .build();

            message.setReplyMarkup(replyKeyboard);
        } else {
            message.setText("Если хочешь кого-то удалить, то сначала было бы неплохо кого-то добавить"
                    + UPSIDE_DOWN_FACE_ICON);
        }

        message.setChatId(chatId);
        return message;
    }

    public SendMessage showNotificationToDeletedSubscriberMenu(BotUser deletedUser, BotUser deletedByUser) {
        SendMessage message = new SendMessage();
        message.setText(BROKEN_HEART_ICON + "Пользователь " + deletedByUser.getUserName() +
                " удалил тебя из списка друзей");

        message.setChatId(String.valueOf(deletedUser.getTgChatId()));
        return message;
    }


    public SendMessage showMySubscriptionsMenu(String chatId) {
        SendMessage message = new SendMessage();
        message.setText("Ты сейчас в меню из которого можешь управлять своими подписками");

        InlineKeyboardMarkup replyKeyboard = InlineKeyboard.InlineKeyboardMarkupBuilder
                .newInlineKeyboardMarkup()
                .withRow()
                .buttonWithCallbackData(SEE_ICON + "Посмотреть подписки", "/show_my_subscriptions")
                .endRow()
                .withRow()
                .buttonWithCallbackData(MINUS_MARK_ICON + "Отписаться", "/delete_subscriptions")
                .endRow()
                .build();

        message.setReplyMarkup(replyKeyboard);

        message.setChatId(chatId);
        return message;
    }

    public SendMessage showMySubscriptionsListMenu(List<BotUser> subscriptions, String chatId) {
        SendMessage message = new SendMessage();
        if (!subscriptions.isEmpty()) {
            message.setText("Это список людей на которых ты подписан.\n" +
                    "Нажми на имя юзера, чтобы увидеть его WishList" + WISH_LIST_ICON);

            InlineKeyboardMarkup replyKeyboard = InlineKeyboard.InlineKeyboardMarkupBuilder
                    .newInlineKeyboardMarkup()
                    .withCallBackButtonsFromUserList(subscriptions, "/see_wish_list_of_user/id")
                    .build();

            message.setReplyMarkup(replyKeyboard);
        } else {
            message.setText("Кажется ты пока ни накого не подписан" + MAN_SHRUGGING_ICON);
        }

        message.setChatId(chatId);
        return message;
    }

    public SendMessage showUserWishListMenu(BotUser wishListHolder, String chatId) { // поменять бот юзера на лист как и везде
        SendMessage message = new SendMessage();
        if (wishListHolder != null && !wishListHolder.getWishList().getGiftList().isEmpty()) {
            List<Gift> gifts = wishListHolder.getWishList().getGiftList();
            message.setText("Это список доступных подарков из WishList'а @" + wishListHolder.getUserName() + "\n" +
                    "Нажми на подарок, который хочешь подарить");

            InlineKeyboardMarkup replyKeyboard = InlineKeyboard.InlineKeyboardMarkupBuilder
                    .newInlineKeyboardMarkup()
                    .withCallBackButtonsFromGiftsList(gifts, "/i_gonna_present/id")
                    .build();

            message.setReplyMarkup(replyKeyboard);
        } else {
            message.setText("Упс... Кажется, @" + wishListHolder.getUserName() + " ничего не добавил в свой WishList," +
                    "либо все его подарки уже заняты" + MAN_SHRUGGING_ICON);
            InlineKeyboardMarkup replyKeyboard = InlineKeyboard.InlineKeyboardMarkupBuilder
                    .newInlineKeyboardMarkup()
                    .withRow()
                    .buttonWithCallbackData(ANONIM_ICON +
                            " Попросить добавить подарок анонимно ", "ask-add-gift-anonym/id/" + wishListHolder.getTgAccountId())
                    .endRow()
                    .withRow()
                    .buttonWithCallbackData(ONE_GUY_ICON +
                            " Попросить добавить подарок открыто ", "ask-add-gift-explicitly/id/" + wishListHolder.getTgAccountId())
                    .endRow()
                    .build();
            message.setReplyMarkup(replyKeyboard);
        }

        message.setChatId(chatId);
        return message;
    }

    public SendMessage showAnonimAskAddGiftMenu(String chatId) {
        SendMessage message = new SendMessage();
        message.setText(EXCLAMATION_ICON + "Привет" + HI_ICON+ " Кто-то просит добавить какой-нибудь подарок в твой WishList");

        message.setChatId(chatId);
        return message;
    }

    public SendMessage showExplicitAskAddGiftMenu( BotUser askedUser) {
        SendMessage message = new SendMessage();
        message.setText(EXCLAMATION_ICON + "Привет" + HI_ICON + "@" + askedUser.getUserName()+" просит добавить подарок в твой WishList");

        message.setChatId(String.valueOf(askedUser.getTgChatId()));
        return message;
    }


    public SendMessage showDeleteUserFromSubscriptionsMenu(List<BotUser> subscriptions, String chatId) {
        SendMessage message = new SendMessage();
        if (!subscriptions.isEmpty()) {
            message.setText("Это список людей на которых ты подписан.\n" +
                    "Нажми на имя юзера, чтобы удалить его из своих подписок" + WISH_LIST_ICON);

            InlineKeyboardMarkup replyKeyboard = InlineKeyboard.InlineKeyboardMarkupBuilder
                    .newInlineKeyboardMarkup()
                    .withCallBackButtonsFromUserList(subscriptions, "/delete_my_subscription_with/id")
                    .build();

            message.setReplyMarkup(replyKeyboard);
        } else {
            message.setText("Кажется ты пока ни накого не подписан" + MAN_SHRUGGING_ICON);
        }

        message.setChatId(chatId);
        return message;
    }


    public SendMessage showFindFriendMenu(String chatId) {
        SendMessage message = new SendMessage();
        message.setText("Ты сейчас в меню в котором можешь найти своих друзей\n"
                + "Введи @user_name своего друга по которому его можно найти в Telegram" +
                " в формате @{user_name} и, если он зарегестрирован в боте, мы отправим " +
                "ему запрос на добавления тебя в друзья");

        message.setChatId(chatId);
        return message;
    }

    public SendMessage showSendRequestMenu(String chatId) {
        SendMessage message = new SendMessage();
        message.setText(CHECK_MARK_ICON
                + "Запрос отправлен. После принятия заявки WishList будет доступен для просмотра");

        message.setChatId(chatId);
        return message;
    }

    public SendMessage showFriendShipRequestTo(BotUser toUserName, BotUser fromUserName) {
        SendMessage message = new SendMessage();
        message.setText(PAPERCLIPS_ICON + "️ЗАПРОС НА ДРУЖБУ! " + PAPERCLIPS_ICON + " \n" +
                HI_ICON + "Привет, " + toUserName.getUserName()
                + "! Пользователь " + fromUserName.getUserName() + " хочет добавить тебя в друзья");

        InlineKeyboardMarkup replyKeyboard = InlineKeyboard.InlineKeyboardMarkupBuilder
                .newInlineKeyboardMarkup()
                .withRow()
                .buttonWithCallbackData(CHECK_MARK_ICON + "Принять",
                        "/accept_friendship/" + fromUserName.getTgAccountId()
                                + "/" + toUserName.getTgAccountId())
                .endRow()
                .withRow()
                .buttonWithCallbackData(CROSS_MARK_ICON + "Отклонить",
                        "/deny_friendship/" + fromUserName.getTgAccountId()
                                + "/" + toUserName.getTgAccountId())
                .endRow()
                .build();


        message.setReplyMarkup(replyKeyboard);
        message.setChatId(String.valueOf(toUserName.getTgChatId()));

        return message;
    }

    public SendMessage showAcceptedFriendshipMenu(BotUser acceptedUser, BotUser byUserAccepted) {
        SendMessage message = new SendMessage();
        message.setText(CHECK_MARK_ICON + "Запрос на дружбу был принят пользователем " + byUserAccepted.getUserName() +
                ". Его WishList доступен для просмотра");

        message.setChatId(String.valueOf(acceptedUser.getTgChatId()));
        return message;
    }


    public SendMessage showDeniedFriendshipMenu(BotUser byUserDenied, BotUser userDenied) {
        SendMessage message = new SendMessage();
        message.setText(CROSS_MARK_ICON + "Запрос на дружбу был отклонён пользователем " + byUserDenied.getUserName());

        message.setChatId(String.valueOf(userDenied.getTgChatId()));
        return message;
    }

    public SendMessage showSettingsMenu(String chatId) {
        SendMessage message = new SendMessage();
        message.setText(SETTINGS_ICON + "Настройки");

        InlineKeyboardMarkup replyKeyboard = InlineKeyboard.InlineKeyboardMarkupBuilder
                .newInlineKeyboardMarkup()
                .withRow()
                .buttonWithCallbackData("Настройка уведомлений" + NOTIFICATION_ICON,
                        "/set_is_ready_receive_update")
                .endRow()
                .withRow()
                .buttonWithCallbackData("Видимость моего WishList'a" + SEE_ICON,
                        "/set_visibility")
                .endRow()
                .build();

        message.setReplyMarkup(replyKeyboard);

        message.setChatId(chatId);
        return message;
    }

    public SendMessage showIsReadyReceiveUpdateMenu(String chatId) {
        SendMessage message = new SendMessage();
        message.setText(NOTIFICATION_ICON + " Хочешь получать уведомления, когда кто-то из твоих друзей " +
                "добавит новый подарок в свой WishList?");

        InlineKeyboardMarkup replyKeyboard = InlineKeyboard.InlineKeyboardMarkupBuilder
                .newInlineKeyboardMarkup()
                .withRow()
                .buttonWithCallbackData("Хочу " + THUMB_UP_ICON,
                        "/set_is_ready_receive_update_true")
                .endRow()
                .withRow()
                .buttonWithCallbackData("Не хочу " + THUMB_DOWN_ICON,
                        "/set_is_ready_receive_update_false")
                .endRow()
                .build();

        message.setReplyMarkup(replyKeyboard);

        message.setChatId(chatId);
        return message;
    }

    public SendMessage showSetVisibilityWishListMenu(String chatId) {
        SendMessage message = new SendMessage();
        message.setText(SEE_ICON + " Кто может видеть твой WishList?");

        InlineKeyboardMarkup replyKeyboard = InlineKeyboard.InlineKeyboardMarkupBuilder
                .newInlineKeyboardMarkup()
                .withRow()
                .buttonWithCallbackData("Только подписчики " + TWO_GUYS_ICON,
                        "/set_visibility_subscribers_all_false")
                .endRow()
                .withRow()
                .buttonWithCallbackData("Любой пользователь " + LOT_PEOPLE_ICON,
                        "/set_visibility_subscribers_all_true")
                .endRow()
                .build();

        message.setReplyMarkup(replyKeyboard);

        message.setChatId(chatId);
        return message;
    }

    public SendMessage showStatusMenu(boolean status, String chatId) {
        SendMessage message = showMainMenu(chatId);
        if (status) {
            message.setText(CHECK_MARK_ICON + "Успешно!");
        } else {
            message.setText(CROSS_MARK_ICON + "Возникла ошибка");
        }

        return message;
    }

    public SendMessage showStatusMenu(boolean status, String chatId, String statusOk) {
        SendMessage message = showMainMenu(chatId);
        if (status) {
            message.setText(CHECK_MARK_ICON + statusOk);
        } else {
            message.setText(CROSS_MARK_ICON + "Возникла ошибка");
        }

        return message;
    }

    public SendMessage showStatusMenu(boolean status, String chatId, String statusOk, String statusError) {
        SendMessage message = showMainMenu(chatId);
        if (status) {
            message.setText(CHECK_MARK_ICON + statusOk);
        } else {
            message.setText(CROSS_MARK_ICON + statusError);
        }

        return message;
    }


}
