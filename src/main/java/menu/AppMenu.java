package menu;

import builder.InlineKeyboard;
import model.BotUser;
import model.Gift;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;

import static menu.Icon.*;

public class AppMenu {

    public SendMessage showGreetingMenu(String chatId) {
        SendMessage message = new SendMessage();
        message.setText("Привет" + HI_ICON + "\nСудя по всему ты здесь в первый раз. Этот бот позволяет создавать" +
            " свой WishList" + WISH_LIST_ICON + " и делиться им с друзьями " + TWO_GUYS_ICON + " Ты можешь отмечать подарки" + I_PRESENT_ICON + " своих друзей, которые ты планируешь " +
            "им подарить, после чего они исчезнут из поля видимости остальных подписчиков\n " + POINT_RIGHT_ICON + " Таким образом," +
            " не случиться ситуации, когда вы с друзьями подарите несколько одинаковых подарков" + SCREAM_CAT_ICON + ", и что более важно," +
            " твой подарок никогда не будет пылиться на полке человека, которому ты его подарил" + ROCK_ICON + "\nЧтобы начать, кликай на кнопку \n" + RECYCLE_ICON +
            " \"Открыть главное меню\"\n Более подробно в разделе \n \"" + SETTINGS_ICON + " Настройки -> Помощь\"");

        InlineKeyboardMarkup replyKeyboard = InlineKeyboard.InlineKeyboardMarkupBuilder
            .newInlineKeyboardMarkup()
            .withRow()
            .buttonWithCallbackData(RECYCLE_ICON + " Открыть главное меню ", "/main_menu")
            .endRow()

            .build();

        message.setReplyMarkup(replyKeyboard);

        message.setChatId(chatId);
        return message;
    }

    public SendMessage showMainMenu(String chatId) {
        SendMessage message = new SendMessage();
        message.setText(RECYCLE_ICON + " Ты в главном меню\n");

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
            .buttonWithCallbackData("Добавить подарок" + PLUS_MARK_ICON,
                "/my_wish_list/add_present")
            .endRow()
            .withRow()
            .buttonWithCallbackData("Редактирование подарков " + MANAGING_ICON,
                "/my_wish_list/manage_list")
            .endRow()
            .withRow()
            .buttonWithCallbackData("Удалить подарок " + MINUS_MARK_ICON,
                "/my_wish_list/delete_present")
            .endRow()
            .withRow()
            .buttonWithCallbackData("« Назад",
                "/main_menu")
            .endRow()
            .build();

        message.setReplyMarkup(replyKeyboard);

        message.setChatId(chatId);
        return message;
    }

    public SendMessage showManagingPresentsInWishListMenu(List<Gift> gifts, String chatId) {
        SendMessage message = new SendMessage();

        if (!gifts.isEmpty()) {
            message.setText(MANAGING_ICON + "Нажми на подарок из списка, который хочешь изменить");

            InlineKeyboardMarkup replyKeyboard = InlineKeyboard.InlineKeyboardMarkupBuilder
                .newInlineKeyboardMarkup()
                .withCallBackButtonsFromList(gifts, "/my_wish_list/edit_my_present_under/id/")
                .withRow()
                .buttonWithCallbackData("« Назад",
                    "/my_wish_list")
                .endRow()
                .build();

            message.setReplyMarkup(replyKeyboard);
        } else {
            message.setText("Чтобы что-то изменять сначала нужно что-то добавить " + UPSIDE_DOWN_FACE_ICON);
        }

        message.setChatId(chatId);
        return message;
    }

    public SendMessage showGiftRepresentationMenu(Gift gift, String chatId) {
        SendMessage message = new SendMessage();
        String description = gift.getDescription() == null ? "отсутствует" : gift.getDescription();
        String url = gift.getUrl() == null ? "отсутствует" : gift.getUrl().toString();
        message.setText(MANAGING_ICON + " В этом меню ты можешь отредактировать описание и ссылку на подарок \"" +
            gift.getName() + "\"" +
            "\nНа текущий момент:" +
            "\n- описание: " + description +
            "\n- ссылка: " + url);

        InlineKeyboardMarkup replyKeyboard = InlineKeyboard.InlineKeyboardMarkupBuilder
            .newInlineKeyboardMarkup()
            .withRow()
            .buttonWithCallbackData(DESCRIPTION_ICON + " Редактировать описание подарка",
                "/my_wish_list/edit_description_of_present_under/id/" + gift.getId())
            .endRow()
            .withRow()
            .buttonWithCallbackData(URL_ICON + " Редактировать ссылку на подарок",
                "/my_wish_list/edit_url_of_present_under/id/" + gift.getId())
            .endRow()
            .withRow()
            .buttonWithCallbackData("« Назад",
                "/my_wish_list/manage_list")
            .endRow()
            .build();

        message.setReplyMarkup(replyKeyboard);
        message.setChatId(chatId);
        return message;
    }

    public SendMessage showDeletePresentFromWishListMenu(List<Gift> gifts, String chatId) {
        SendMessage message = new SendMessage();

        if (!gifts.isEmpty()) {
            message.setText("Нажми на подарок из списка, который ты хочешь удалить");

            InlineKeyboardMarkup replyKeyboard = InlineKeyboard.InlineKeyboardMarkupBuilder
                .newInlineKeyboardMarkup()
                .withCallBackButtonsFromList(gifts, "/my_wish_list/delete_my_present_under/id/")
                .withRow()
                .buttonWithCallbackData("« Назад",
                    "/my_wish_list")
                .endRow()
                .build();

            message.setReplyMarkup(replyKeyboard);
        } else {
            message.setText("Чтобы что-то удалить сначала нужно что-то добавить " + UPSIDE_DOWN_FACE_ICON);
        }

        message.setChatId(chatId);
        return message;
    }

    public SendMessage showUserDeletedPresentYouGoingToDonateMenu(Gift deletedGift, BotUser deletedByUser) {
        SendMessage message = new SendMessage();
        message.setText(EXCLAMATION_ICON + "Пользователь " + deletedByUser.getUserName() +
            " удалил из списка желаний подарок \"" + deletedGift.getName()
            + "\", который ты хотел ему подарить " + MAN_SHRUGGING_ICON + "\n" +
            "Попробуй выбрать другой подарок");

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
                .withCallBackButtonsFromList(gifts, "i_present/delete_gift_under/id/")
                .withRow()
                .buttonWithCallbackData("« Назад",
                    "/main_menu")
                .endRow()
                .build();

            message.setReplyMarkup(replyKeyboard);
        } else {
            message.setText("Это список подарков, который ты мог бы кому-то подарить, но, " +
                "к сожалению, здесь пока ничего нет " + MAN_SHRUGGING_ICON);
            InlineKeyboardMarkup replyKeyboard = InlineKeyboard.InlineKeyboardMarkupBuilder
                .newInlineKeyboardMarkup()
                .withRow()
                .buttonWithCallbackData("« Назад",
                    "/main_menu")
                .endRow()
                .build();

            message.setReplyMarkup(replyKeyboard);
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
            .buttonWithCallbackData(SEE_ICON + "Посмотреть подписчиков", "/my_subscribers/show")
            .endRow()
            .withRow()
            .buttonWithCallbackData(MINUS_MARK_ICON + "Удалить подписчиков", "/my_subscribers/delete")
            .endRow()
            .withRow()
            .buttonWithCallbackData("« Назад",
                "/main_menu")
            .endRow()
            .build();

        message.setReplyMarkup(replyKeyboard);

        message.setChatId(chatId);
        return message;
    }

    public SendMessage showMySubscribersListMenu(List<BotUser> subscribers, String chatId) {
        StringBuilder textMessage = new StringBuilder();
        if (!subscribers.isEmpty()) {
            textMessage.append(TWO_GUYS_ICON + " Список твоих подписчиков:\n");
            for (int i = 0; i < subscribers.size(); i++) {
                BotUser user = subscribers.get(i);
                String firstName = user.getFirstName() == null ? "не указано" : user.getFirstName();
                textMessage.append(i + 1)
                    .append(".")
                    .append("User name - " + user.getUserName())
                    .append("\n")
                    .append("Имя - " + firstName);  // Добавить для кого дарит
            }
        } else {
            textMessage.append(" Кажется на тебя пока никто не подписан" + MAN_SHRUGGING_ICON);
        }
        SendMessage message = new SendMessage();
        message.setText(textMessage.toString());

        InlineKeyboardMarkup replyKeyboard = InlineKeyboard.InlineKeyboardMarkupBuilder
            .newInlineKeyboardMarkup()
            .withRow()
            .buttonWithCallbackData("« Назад",
                "/my_subscribers")
            .endRow()
            .build();

        message.setReplyMarkup(replyKeyboard);

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
                .withCallBackButtonsFromUserList(subscribers, "/my_subscribers/delete_under/id")
                .withRow()
                .buttonWithCallbackData("« Назад",
                    "/my_subscribers")
                .endRow()
                .build();

            message.setReplyMarkup(replyKeyboard);
        } else {
            message.setText("Если хочешь кого-то удалить, то сначала было бы неплохо кого-то добавить "
                + UPSIDE_DOWN_FACE_ICON);
        }

        message.setChatId(chatId);
        return message;
    }

    public SendMessage showNotificationToDeletedSubscriberMenu(BotUser deletedUser, BotUser deletedByUser) {
        SendMessage message = new SendMessage();
        message.setText(BROKEN_HEART_ICON + "Пользователь @" + deletedByUser.getUserName() +
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
            .buttonWithCallbackData(SEE_ICON + "Посмотреть подписки", "/my_subscriptions/show")
            .endRow()
            .withRow()
            .buttonWithCallbackData(MINUS_MARK_ICON + "Отписаться", "/my_subscriptions/delete")
            .endRow()
            .withRow()
            .buttonWithCallbackData("« Назад",
                "/main_menu")
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
                .withCallBackButtonsFromUserList(subscriptions, "/my_subscriptions/show/id")
                .withRow()
                .buttonWithCallbackData("« Назад",
                    "/my_subscriptions/show")
                .endRow()
                .build();

            message.setReplyMarkup(replyKeyboard);
        } else {
            message.setText("Кажется ты пока ни на кого не подписан" + MAN_SHRUGGING_ICON);
            InlineKeyboardMarkup replyKeyboard = InlineKeyboard.InlineKeyboardMarkupBuilder
                .newInlineKeyboardMarkup()
                .withRow()
                .buttonWithCallbackData("« Назад",
                    "/my_subscriptions")
                .endRow()
                .build();

            message.setReplyMarkup(replyKeyboard);
        }

        message.setChatId(chatId);
        return message;
    }

    public SendMessage showUserWishListMenu(BotUser wishListHolder, String chatId) {
        SendMessage message = new SendMessage();
        List<Gift> gifts = wishListHolder.findAvailableToDonatePresents();
        if (!gifts.isEmpty()) {
            message.setText("Это список доступных подарков из WishList'а @" + wishListHolder.getUserName() + "\n" +
                "Нажми на подарок, который хочешь подарить");

            InlineKeyboardMarkup replyKeyboard = InlineKeyboard.InlineKeyboardMarkupBuilder
                .newInlineKeyboardMarkup()
                .withCallBackButtonsFromGiftsList(gifts, "/my_subscriptions/going_donate/gift_id")
                .build();

            message.setReplyMarkup(replyKeyboard);
        } else {
            message.setText("Упс... Кажется, @" + wishListHolder.getUserName() + " ничего не добавил в свой WishList," +
                " либо все его подарки уже заняты" + MAN_SHRUGGING_ICON);
            InlineKeyboardMarkup replyKeyboard = InlineKeyboard.InlineKeyboardMarkupBuilder
                .newInlineKeyboardMarkup()
                .withRow()
                .buttonWithCallbackData(ANONIM_ICON +
                        " Попросить добавить подарок анонимно ",
                    "/my_subscriptions/ask_add_gift_anonymously/id/" + wishListHolder.getTgAccountId())
                .endRow()
                .withRow()
                .buttonWithCallbackData(ONE_GUY_ICON +
                        " Попросить добавить подарок открыто ",
                    "/my_subscriptions/ask_add_gift_explicitly/id/" + wishListHolder.getTgAccountId())
                .endRow()
                .withRow()
                .buttonWithCallbackData("« Назад",
                    "/my_subscriptions/show")
                .endRow()
                .build();
            message.setReplyMarkup(replyKeyboard);
        }


        message.setChatId(chatId);
        return message;
    }

    public SendMessage showAnonymouslyAskAddGiftMenu(BotUser askedUser) {
        SendMessage message = new SendMessage();
        message.setText(EXCLAMATION_ICON + " Кто-то просит добавить подарок в твой WishList");

        message.setChatId(String.valueOf(askedUser.getTgChatId()));
        return message;
    }

    public SendMessage showExplicitAskAddGiftMenu(BotUser askedUser) {
        SendMessage message = new SendMessage();
        message.setText(EXCLAMATION_ICON + " @" + askedUser.getUserName() + " просит добавить подарок в твой WishList");

        message.setChatId(String.valueOf(askedUser.getTgChatId()));
        return message;
    }


    public SendMessage showDeleteUserFromSubscriptionsMenu(List<BotUser> subscriptions, String chatId) {
        SendMessage message = new SendMessage();
        if (!subscriptions.isEmpty()) {
            message.setText("Это список людей на которых ты подписан.\n" +
                "Нажми на имя пользователя, чтобы удалить его из своих подписок" + WISH_LIST_ICON);

            InlineKeyboardMarkup replyKeyboard = InlineKeyboard.InlineKeyboardMarkupBuilder
                .newInlineKeyboardMarkup()
                .withCallBackButtonsFromUserList(subscriptions, "/my_subscriptions/delete_under/id")
                .withRow()
                .buttonWithCallbackData("« Назад",
                    "/my_subscriptions")
                .endRow()
                .build();

            message.setReplyMarkup(replyKeyboard);
        } else {
            message.setText("Чтобы кого-то удалить нужно сначала кого-то добавить" + UPSIDE_DOWN_FACE_ICON);
            InlineKeyboardMarkup replyKeyboard = InlineKeyboard.InlineKeyboardMarkupBuilder
                .newInlineKeyboardMarkup()
                .withRow()
                .buttonWithCallbackData("« Назад",
                    "/my_subscriptions")
                .endRow()
                .build();

            message.setReplyMarkup(replyKeyboard);
        }

        message.setChatId(chatId);
        return message;
    }

//
//    public SendMessage showFindFriendMenu(String chatId) {
//        SendMessage message = new SendMessage();
//        message.setText("Ты сейчас в меню в котором можешь найти своих друзей\n"
//                + "Введи @user_name своего друга по которому его можно найти в Telegram" +
//                " в формате @{user_name} и, если он зарегестрирован в боте, мы отправим " +
//                "ему запрос на добавления тебя в друзья");
//
//        message.setChatId(chatId);
//        return message;
//    }

    public SendMessage showSendRequestMenu(String chatId) {
        SendMessage message = new SendMessage();
        message.setText(CHECK_MARK_ICON
            + "Запрос отправлен. После принятия заявки WishList будет доступен для просмотра");
        InlineKeyboardMarkup replyKeyboard = InlineKeyboard.InlineKeyboardMarkupBuilder
            .newInlineKeyboardMarkup()
            .buttonWithCallbackData("« " + RECYCLE_ICON + " В главное меню",
                "/main_menu")
            .endRow()
            .build();

        message.setReplyMarkup(replyKeyboard);

        message.setChatId(chatId);
        return message;
    }

    public SendMessage showFriendShipRequestTo(BotUser toUserName, BotUser fromUserName) {
        SendMessage message = new SendMessage();
        message.setText(PAPERCLIPS_ICON + "️ЗАПРОС НА ДРУЖБУ! " + PAPERCLIPS_ICON + " \n" +
            HI_ICON + "Привет, " + toUserName.getUserName()
            + "! Это @" + fromUserName.getUserName() + ", добавь меня в друзья!");

        InlineKeyboardMarkup replyKeyboard = InlineKeyboard.InlineKeyboardMarkupBuilder
            .newInlineKeyboardMarkup()
            .withRow()
            .buttonWithCallbackData(CHECK_MARK_ICON + "Принять",
                "/find_friend/accept_friendship/" + fromUserName.getTgAccountId()
                    + "/" + toUserName.getTgAccountId())
            .endRow()
            .withRow()
            .buttonWithCallbackData(CROSS_MARK_ICON + "Отклонить",
                "/find_friend/deny_friendship/" + fromUserName.getTgAccountId()
                    + "/" + toUserName.getTgAccountId())
            .endRow()
            .build();


        message.setReplyMarkup(replyKeyboard);
        message.setChatId(String.valueOf(toUserName.getTgChatId()));

        return message;
    }

    public SendMessage showFriendShipAcceptedMenu(BotUser byUserAccepted, String chatId) {
        SendMessage message = new SendMessage();
        message.setText(CHECK_MARK_ICON + " @" + byUserAccepted.getUserName() + " принял предложение дружбы" +
            ". Его WishList доступен для просмотра");

        InlineKeyboardMarkup replyKeyboard = InlineKeyboard.InlineKeyboardMarkupBuilder
            .newInlineKeyboardMarkup()
            .withRow()
            .buttonWithCallbackData("Посмотреть " + SEE_ICON,
                "/my_subscriptions/show/id/" + byUserAccepted.getTgAccountId())
            .endRow()
            .build();

        message.setReplyMarkup(replyKeyboard);

        message.setChatId(chatId);
        return message;
    }

    public SendMessage showSettingsMenu(String chatId) {
        SendMessage message = new SendMessage();
        message.setText(SETTINGS_ICON + "Ты в меню настроек");

        InlineKeyboardMarkup replyKeyboard = InlineKeyboard.InlineKeyboardMarkupBuilder
            .newInlineKeyboardMarkup()
            .withRow()
            .buttonWithCallbackData("Настройка уведомлений " + NOTIFICATION_ICON,
                "/settings/set_is_ready_receive_update")
            .endRow()
            .withRow()
            .buttonWithCallbackData("Видимость моего WishList'a" + SEE_ICON,
                "/settings/set_visibility")
            .endRow()
            .withRow()
            .buttonWithCallbackData("Помощь " + SOS_ICON,
                "/settings/help")
            .endRow()
            .withRow()
            .buttonWithCallbackData("« Назад",
                "/main_menu")
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
                "/settings/set_is_ready_receive_update/true")
            .endRow()
            .withRow()
            .buttonWithCallbackData("Не хочу " + THUMB_DOWN_ICON,
                "/settings/set_is_ready_receive_update/false")
            .endRow()
            .withRow()
            .buttonWithCallbackData("« Назад",
                "/settings")
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
                "/settings/set_visibility_subscribers_only/false")
            .endRow()
            .withRow()
            .buttonWithCallbackData("Любой пользователь " + LOT_PEOPLE_ICON,
                "/set_visibility_subscribers_all_true")
            .endRow()
            .withRow()
            .buttonWithCallbackData("« Назад",
                "/settings")
            .endRow()
            .build();

        message.setReplyMarkup(replyKeyboard);

        message.setChatId(chatId);
        return message;
    }

    public SendMessage showOkStatusMenu(String text, String chatId) {
        if (text != null) {
            return new SendMessage(chatId, CHECK_MARK_ICON + " " + text);
        }
        return new SendMessage(chatId, CHECK_MARK_ICON + " Успешно!");
    }

    public SendMessage showErrorStatusMenu(String text, String chatId) {
        if (text != null) {
            return new SendMessage(chatId, CROSS_MARK_ICON + " " + text);
        }
        return new SendMessage(chatId, CROSS_MARK_ICON + " Возникла ошибка, операция не выполнена");
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

    public SendMessage showCustomSingleInlineButton(String text, String buttonText, String callbackData, String chatId) {
        SendMessage message = new SendMessage();
        message.setText(text);

        InlineKeyboardMarkup replyKeyboard = InlineKeyboard.InlineKeyboardMarkupBuilder
            .newInlineKeyboardMarkup()
            .withRow()
            .buttonWithCallbackData(buttonText, callbackData)
            .endRow()
            .build();

        message.setReplyMarkup(replyKeyboard);

        message.setChatId(chatId);
        return message;
    }


}
