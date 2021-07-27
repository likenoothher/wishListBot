package com.aziarets.vividapp.menu;

import com.aziarets.vividapp.keyboardbuilder.InlineKeyboard;
import com.aziarets.vividapp.model.BotUser;
import com.aziarets.vividapp.model.Gift;
import com.aziarets.vividapp.util.PhotoTelegramUrlGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;
import java.util.Map;

import static com.aziarets.vividapp.menu.Icon.*;

@Component
public class BotMenuTemplate {

    private PhotoTelegramUrlGenerator photoTelegramUrlGenerator;

    @Autowired
    public BotMenuTemplate(PhotoTelegramUrlGenerator photoTelegramUrlGenerator) {
        this.photoTelegramUrlGenerator = photoTelegramUrlGenerator;
    }

    public SendMessage getGreetingTemplate(String chatId, BotUser updateSender) {
        SendMessage message = new SendMessage();
        message.setText("Привет, @" + updateSender.getUserName() + HI_ICON + "\nСудя по всему ты здесь в первый раз. Этот бот позволяет создавать" +
            " свой WishList" + WISH_LIST_ICON + " и делиться им с друзьями " + TWO_GUYS_ICON + " Ты можешь отмечать подарки" + I_PRESENT_ICON + " своих друзей, которые ты планируешь " +
            "им подарить, после чего они исчезнут из поля видимости остальных подписчиков\n " + POINT_RIGHT_ICON + " Таким образом" +
            " не случиться ситуации, когда вы с друзьями подарите несколько одинаковых подарков" + SCREAM_CAT_ICON + ", и что более важно," +
            " твой подарок никогда не будет пылиться на полке человека, которому ты его подарил" + ROCK_ICON + "\nЧтобы начать, кликай на кнопку \n" + RECYCLE_ICON +
            " \"Открыть главное меню\"\n Если ты нашёл баг или хочешь оставить отзыв о пользовании ботом, то в \n \"" + SETTINGS_ICON + " Настройки -> Написать разработчику\" ты можешь написать об этом " +
            "разработчику - он всё исправит" + UPSIDE_DOWN_FACE_ICON);

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

    public SendMessage getMainMenuTemplate(String chatId) {
        SendMessage message = new SendMessage();
        message.setText(RECYCLE_ICON + " Ты в главном меню\n");
        message.setReplyMarkup(createMainMenuTemplate());
        message.setChatId(chatId);
        return message;
    }

    public EditMessageText showEditedMainMenu(String chatId, int messageId, String inlineMessageId) {
        EditMessageText message = new EditMessageText();
        message.setText(RECYCLE_ICON + " Ты в главном меню\n");
        message.setReplyMarkup(createMainMenuTemplate());
        message.setChatId(chatId);
        message.setMessageId(messageId);
        message.setInlineMessageId(inlineMessageId);
        return message;
    }

    public InlineKeyboardMarkup createMainMenuTemplate() {
        return InlineKeyboard.InlineKeyboardMarkupBuilder
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
            .withRow()
            .buttonWithCallbackData("Веб-версия " + URL_ICON, "/web")
            .endRow()
            .build();
    }

    public EditMessageText getMyWishListTemplate(List<Gift> gifts, String chatId,
                                                 int messageId, String inlineMessageId) {
        EditMessageText message = new EditMessageText();
        message.setText(WISH_LIST_ICON + " Здесь ты можешь управлять своим WishList'ом\n");

        InlineKeyboardMarkup replyKeyboard = InlineKeyboard.InlineKeyboardMarkupBuilder
            .newInlineKeyboardMarkup()
            .withRow()
            .buttonWithCallbackData("Добавить подарок" + PLUS_MARK_ICON,
                "/my_wish_list/add_present")
            .endRow()
            .withCallBackButtonsFromGiftList(gifts, I_PRESENT_ICON, "/my_wish_list/edit_my_present_under/id")
            .withRow()
            .buttonWithCallbackData("« Назад",
                "/main_menu")
            .endRow()
            .build();

        message.setReplyMarkup(replyKeyboard);
        message.setChatId(chatId);
        message.setMessageId(messageId);
        message.setInlineMessageId(inlineMessageId);

        return message;
    }

    public EditMessageText getGiftRepresentationTemplate(Gift gift, String chatId,
                                                         int messageId, String inlineMessageId) {
        EditMessageText message = new EditMessageText();
        String description = gift.getDescription() == null ? "отсутствует" : gift.getDescription();
        String giftUrl = gift.getUrl() == null ? "отсутствует" : gift.getUrl().toString();
        String photoURL = gift.getGiftPhotoURL() == null ? "- изображение: отсутствует"
            : "<a href=\"" + gift.getGiftPhotoURL() + "\">&#8205;</a>";

        message.setText(MANAGING_ICON + " В этом меню ты можешь управлять подарком \"" +
            gift.getName() + "\"" +
            "\nНа данный момент :" +
            "\n- описание: " + description +
            "\n- ссылка: " + giftUrl +
            "\n" + photoURL);

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
            .buttonWithCallbackData(GIFT_IMAGE + " Редактировать изображение подарка",
                "/my_wish_list/edit_photo_of_present_under/id/" + gift.getId())
            .endRow()
            .withRow()
            .buttonWithCallbackData(MINUS_MARK_ICON + " Удалить подарок",
                "/my_wish_list/delete_my_present_under/id/" + gift.getId())
            .endRow()
            .withRow()
            .buttonWithCallbackData("« Назад",
                "/my_wish_list")
            .endRow()
            .build();

        message.setParseMode("html");
        message.setReplyMarkup(replyKeyboard);
        message.setChatId(chatId);
        message.setMessageId(messageId);
        message.setInlineMessageId(inlineMessageId);
        return message;
    }


    public SendMessage getUserDeletedPresentYouGoingToDonateTemplate(Gift deletedGift, BotUser deletedByUser) {
        SendMessage message = new SendMessage();
        message.setText(EXCLAMATION_ICON + "Пользователь " + deletedByUser.getUserName() +
            " удалил из списка желаний подарок \"" + deletedGift.getName()
            + "\", который ты хотел ему подарить " + MAN_SHRUGGING_ICON + "\n" +
            "Попробуй выбрать другой подарок");

        message.setChatId(String.valueOf(deletedGift.getOccupiedBy().getTgAccountId()));
        return message;
    }

    public EditMessageText getIPresentTemplate(Map<Gift, BotUser> gifts, String chatId, int messageId, String inlineMessageId) {
        EditMessageText message = new EditMessageText();

        if (!gifts.isEmpty()) {
            message.setText("Это подарки, которые ты планируешь подарить" + WISH_LIST_ICON +
                "\nНажми на подарок" + I_PRESENT_ICON + " из списка, для получения дополнительной информации");

            InlineKeyboardMarkup replyKeyboard = InlineKeyboard.InlineKeyboardMarkupBuilder
                .newInlineKeyboardMarkup()
                .withCallBackButtonsFromGiftMap(gifts, I_PRESENT_ICON, "/i_present/show_gift_under/id")
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
        message.setMessageId(messageId);
        message.setInlineMessageId(inlineMessageId);

        return message;
    }

    public EditMessageText getIPresentGiftInfoTemplate(Gift gift, BotUser giftHolder, String chatId,
                                                       int messageId, String inlineMessageId) {
        EditMessageText message = new EditMessageText();

        String giftName = gift.getName() == null ? "не указано" : gift.getName();
        String giftDescription = gift.getDescription() == null ? "не указано" : gift.getDescription();
        String giftUrl = gift.getUrl() == null ? "не указано" : gift.getUrl();
        String giftHolderName = giftHolder.getUserName() == null ? "не указано" : giftHolder.getUserName();
        String photoURL = gift.getGiftPhotoURL() == null ? "Изображение - отсутствует"
            : "<a href=\"" + gift.getGiftPhotoURL() + "\">&#8205;</a>";


        message.setText(DIAMOND_ICON + "Имя подарка - " + giftName + "\n" +
            "Описание - " + giftDescription + "\n" +
            "Ссылка - " + giftUrl + "\n" +
            photoURL + "\n" +
            ONE_GUY_ICON + "Для пользователя - @" + giftHolderName + "\n\n" +
            THUMB_DOWN_POINTER_ICON + "Если передумал дарить жми" + THUMB_DOWN_POINTER_ICON);


        InlineKeyboardMarkup replyKeyboard = InlineKeyboard.InlineKeyboardMarkupBuilder
            .newInlineKeyboardMarkup()
            .withRow()
            .buttonWithCallbackData("Передумал дарить " + MINUS_MARK_ICON,
                "/i_present/delete_gift_under/id/" + gift.getId())
            .endRow()
            .withRow()
            .buttonWithCallbackData("« Назад",
                "/i_present")
            .endRow()
            .build();

        message.setParseMode("html");
        message.setChatId(chatId);
        message.setMessageId(messageId);
        message.setInlineMessageId(inlineMessageId);
        message.setReplyMarkup(replyKeyboard);

        return message;
    }

    public EditMessageText getMySubscribersListTemplate(List<BotUser> subscribers, String chatId,
                                                        int messageId, String inlineMessageId) {
        EditMessageText message = new EditMessageText();

        if (!subscribers.isEmpty()) {
            message.setText(ARROW_LOWER_LEFT_ICON + "Это список пользователей, которые на тебя подписаны\n" +
                "Нажми на имя пользователя " + ONE_GUY_ICON + " для для того, чтобы увидеть более детальную информацию"
                + WISH_LIST_ICON);

            InlineKeyboardMarkup replyKeyboard = InlineKeyboard.InlineKeyboardMarkupBuilder
                .newInlineKeyboardMarkup()
                .withCallBackButtonsFromUserList(subscribers, "/my_subscribers/show/id")
                .withRow()
                .buttonWithCallbackData("« Назад",
                    "/main_menu")
                .endRow()
                .build();

            message.setReplyMarkup(replyKeyboard);
        } else {
            message.setText("Кажется на тебя пока никто не подписан " + MAN_SHRUGGING_ICON);
            InlineKeyboardMarkup replyKeyboard = InlineKeyboard.InlineKeyboardMarkupBuilder
                .newInlineKeyboardMarkup()
                .withRow()
                .buttonWithCallbackData("« Назад",
                    "/main_menu")
                .endRow()
                .build();

            message.setReplyMarkup(replyKeyboard);
        }
        message.setMessageId(messageId);
        message.setInlineMessageId(inlineMessageId);
        message.setChatId(chatId);

        return message;
    }

    public EditMessageText getSubscriberRepresentationTemplate(BotUser subscriber, String chatId,
                                                               int messageId, String inlineMessageId) {
        EditMessageText message = new EditMessageText();

        String firstName = subscriber.getFirstName() == null ? "не указано" : subscriber.getFirstName();
        String lastName = subscriber.getLastName() == null ? "не указано" : subscriber.getLastName();

        message.setText(ONE_GUY_ICON + " Информация о пользователе:\n" +
            "Имя пользователя - @" + subscriber.getUserName() + "\n"
            + "Имя - " + firstName + "\n" +
            "Фамилия - " + lastName);

        InlineKeyboardMarkup replyKeyboard = InlineKeyboard.InlineKeyboardMarkupBuilder
            .newInlineKeyboardMarkup()
            .withRow()
            .buttonWithCallbackData(MINUS_MARK_ICON + "Удалить подписчика",
                "/my_subscribers/delete_under/id/" + subscriber.getTgAccountId())
            .endRow()
            .withRow()
            .buttonWithCallbackData("« Назад",
                "/my_subscribers")
            .endRow()
            .build();

        message.setReplyMarkup(replyKeyboard);
        message.setMessageId(messageId);
        message.setInlineMessageId(inlineMessageId);
        message.setChatId(chatId);

        return message;
    }

    public SendMessage getAlertToDeletedSubscriberTemplate(BotUser deletedUser, BotUser deletedByUser) {
        SendMessage message = new SendMessage();
        message.setText(BROKEN_HEART_ICON + "Пользователь @" + deletedByUser.getUserName() +
            " удалил тебя из списка друзей");

        message.setChatId(String.valueOf(deletedUser.getTgAccountId()));
        return message;
    }


    public EditMessageText getMySubscriptionsTemplate(List<BotUser> subscriptions, String chatId,
                                                      int messageId, String inlineMessageId) {
        EditMessageText message = new EditMessageText();

        if (!subscriptions.isEmpty()) {
            message.setText("Это список пользователей на которых ты подписан.\n" +
                "Нажми на имя пользователя, чтобы увидеть его WishList" + WISH_LIST_ICON);

            InlineKeyboardMarkup replyKeyboard = InlineKeyboard.InlineKeyboardMarkupBuilder
                .newInlineKeyboardMarkup()
                .withCallBackButtonsFromUserList(subscriptions, "/my_subscriptions/show/id")
                .withRow()
                .buttonWithCallbackData("« Назад",
                    "/main_menu")
                .endRow()
                .build();

            message.setReplyMarkup(replyKeyboard);
        } else {
            message.setText("Кажется ты пока ни на кого не подписан " + MAN_SHRUGGING_ICON);
            InlineKeyboardMarkup replyKeyboard = InlineKeyboard.InlineKeyboardMarkupBuilder
                .newInlineKeyboardMarkup()
                .withRow()
                .buttonWithCallbackData("« Назад",
                    "/main_menu")
                .endRow()
                .build();

            message.setReplyMarkup(replyKeyboard);
        }
        message.setMessageId(messageId);
        message.setInlineMessageId(inlineMessageId);
        message.setChatId(chatId);

        return message;
    }

    public EditMessageText getUserWishListTemplate(BotUser wishListHolder, String chatId,
                                                   int messageId, String inlineMessageId) {
        EditMessageText message = new EditMessageText();
        List<Gift> gifts = wishListHolder.findAvailableToDonatePresents();
        if (!gifts.isEmpty()) {
            message.setText("Это список доступных подарков из WishList'а @" + wishListHolder.getUserName() + "\n" +
                "Нажми на подарок, который хочешь подарить");

            InlineKeyboardMarkup replyKeyboard = InlineKeyboard.InlineKeyboardMarkupBuilder
                .newInlineKeyboardMarkup()
                .withCallBackButtonsFromGiftList(gifts, I_PRESENT_ICON,
                    "/my_subscriptions/show_representation/gift_id")
                .withRow()
                .buttonWithCallbackData(MINUS_MARK_ICON +
                        " Отписаться от @" + wishListHolder.getUserName(),
                    "/my_subscriptions/delete_under/id/" + wishListHolder.getTgAccountId())
                .endRow()
                .withRow()
                .buttonWithCallbackData("« Назад",
                    "/my_subscriptions")
                .endRow()
                .build();

            message.setReplyMarkup(replyKeyboard);
        } else {
            message.setText("Кажется, @" + wishListHolder.getUserName() + " ничего не добавил в свой WishList," +
                " либо все его подарки уже заняты" + MAN_SHRUGGING_ICON
                + "\nНо ты можешь попросить его добавить подарок");
            InlineKeyboardMarkup replyKeyboard = InlineKeyboard.InlineKeyboardMarkupBuilder
                .newInlineKeyboardMarkup()
                .withRow()
                .buttonWithCallbackData(ANONIM_ICON +
                        " Попросить добавить подарок анонимно ",
                    "/my_subscriptions/ask_add_gift_anonymously/id/" + wishListHolder.getTgAccountId())
                .endRow()
                .withRow()
                .buttonWithCallbackData(ONE_GUY_ICON +
                        " Попросить добавить подарок от своего имени ",
                    "/my_subscriptions/ask_add_gift_explicitly/id/" + wishListHolder.getTgAccountId())
                .endRow()
                .withRow()
                .buttonWithCallbackData(MINUS_MARK_ICON +
                        " Отписаться от @" + wishListHolder.getUserName(),
                    "/my_subscriptions/delete_under/id/" + wishListHolder.getTgAccountId())
                .endRow()
                .withRow()
                .buttonWithCallbackData("« Назад",
                    "/my_subscriptions")
                .endRow()
                .build();
            message.setReplyMarkup(replyKeyboard);
        }
        message.setMessageId(messageId);
        message.setInlineMessageId(inlineMessageId);
        message.setChatId(chatId);

        return message;
    }

    public EditMessageText getGoingDonateGiftTemplate(Gift gift, BotUser giftHolder,
                                                      String chatId, int messageId, String inlineMessageId) {
        EditMessageText message = new EditMessageText();
        String description = gift.getDescription() == null ? "отсутствует" : gift.getDescription();
        String url = gift.getUrl() == null ? "отсутствует" : gift.getUrl().toString();
        String photoURL = gift.getGiftPhotoURL() == null ? "- изображеие: отсутствует"
            : "<a href=\"" + gift.getGiftPhotoURL() + "\">&#8205;</a>";

        message.setText(I_PRESENT_ICON + "Название - " + gift.getName() +
            "\n- описание: " + description +
            "\n- ссылка: " + url + "\n"
            + photoURL);

        InlineKeyboardMarkup replyKeyboard = InlineKeyboard.InlineKeyboardMarkupBuilder
            .newInlineKeyboardMarkup()
            .withRow()
            .buttonWithCallbackData(I_PRESENT_ICON + " Я - подарю!",
                "/my_subscriptions/going_donate/gift_id/" + gift.getId())
            .endRow()
            .withRow()
            .buttonWithCallbackData("« Назад",
                "/my_subscriptions/show/id/" + giftHolder.getTgAccountId())
            .endRow()
            .build();

        message.setParseMode("html");
        message.setReplyMarkup(replyKeyboard);
        message.setMessageId(messageId);
        message.setInlineMessageId(inlineMessageId);
        message.setChatId(chatId);
        return message;
    }

    public SendMessage getAnonymouslyAskAddGiftTemplate(BotUser askedUser) {
        SendMessage message = new SendMessage();
        message.setText(EXCLAMATION_ICON + " Кто-то просит добавить подарок в твой WishList");
        message.setChatId(String.valueOf(askedUser.getTgAccountId()));

        return message;
    }

    public SendMessage getExplicitAskAddGiftTemplate(BotUser askedUser) {
        SendMessage message = new SendMessage();
        message.setText(EXCLAMATION_ICON + " @" + askedUser.getUserName() + " просит добавить подарок в твой WishList");
        message.setChatId(String.valueOf(askedUser.getTgAccountId()));

        return message;
    }

    public EditMessageText getFindFriendTemplate(String chatId, int messageId, String inlineMessageId) {
        EditMessageText message = new EditMessageText();

        String photoURL =
            "<a href=\"https://res.cloudinary.com/dirdwzm1g/image/upload/v1625585016/Screenshot_4_e81c7b.jpg\">" +
                "&#8205;</a>";

        message.setText(KEYBOARD_ICON + " Напиши имя пользователя в Telegram и отправь" + photoURL);

        InlineKeyboardMarkup replyKeyboard = InlineKeyboard.InlineKeyboardMarkupBuilder
            .newInlineKeyboardMarkup()
            .withRow()
            .buttonWithCallbackData("« Назад",
                "/main_menu")
            .endRow()
            .build();

        message.setParseMode("html");
        message.setChatId(chatId);
        message.setMessageId(messageId);
        message.setInlineMessageId(inlineMessageId);
        message.setReplyMarkup(replyKeyboard);

        return message;
    }

    public SendMessage getSendFriendshipRequestTemplate(String chatId) {
        SendMessage message = new SendMessage();

        message.setText(CHECK_MARK_ICON
            + "Запрос отправлен. После принятия заявки WishList будет доступен для просмотра");
        InlineKeyboardMarkup replyKeyboard = InlineKeyboard.InlineKeyboardMarkupBuilder
            .newInlineKeyboardMarkup()
            .withRow()
            .buttonWithCallbackData("« В главное меню " + RECYCLE_ICON,
                "/main_menu")
            .endRow()
            .build();

        message.setReplyMarkup(replyKeyboard);
        message.setChatId(chatId);

        return message;
    }

    public SendMessage getFriendShipRequestToTemplate(BotUser toUserName, BotUser fromUserName) {
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
        message.setChatId(String.valueOf(toUserName.getTgAccountId()));

        return message;
    }

    public SendMessage getFriendShipAcceptedTemplate(BotUser byUserAccepted, BotUser requestedUser) {
        SendMessage message = new SendMessage();

        message.setText(CHECK_MARK_ICON + " @" + byUserAccepted.getUserName() + " принял предложение дружбы." +
            " Его WishList доступен для просмотра");

        InlineKeyboardMarkup replyKeyboard = InlineKeyboard.InlineKeyboardMarkupBuilder
            .newInlineKeyboardMarkup()
            .withRow()
            .buttonWithCallbackData("Посмотреть " + SEE_ICON,
                "/my_subscriptions/show/id/" + byUserAccepted.getTgAccountId())
            .endRow()
            .withRow()
            .buttonWithCallbackData("« В главное меню " + RECYCLE_ICON,
                "/main_menu")
            .endRow()
            .build();

        message.setReplyMarkup(replyKeyboard);
        message.setChatId(String.valueOf(requestedUser.getTgAccountId()));

        return message;
    }

    public EditMessageText getAcceptedFriendshipTemplate(BotUser requestedUser, BotUser byUserAccepted,
                                                         int messageId, String inlineMessageId,
                                                         boolean isAlreadyFriends) {
        EditMessageText message = new EditMessageText();


        message.setText(CHECK_MARK_ICON + " Запрос на дружбу от @" + requestedUser.getUserName()
            + " принят. Теперь он имеет доступ к твоему WishList'y");

        if (!isAlreadyFriends) {
            InlineKeyboardMarkup replyKeyboard = InlineKeyboard.InlineKeyboardMarkupBuilder
                .newInlineKeyboardMarkup()
                .withRow()
                .buttonWithCallbackData("Подписаться на @" + requestedUser.getUserName() + ONE_GUY_ICON,
                    "/find_friend/mutual_friendship/" + requestedUser.getTgAccountId())
                .endRow()
                .withRow()
                .buttonWithCallbackData("« В главное меню " + RECYCLE_ICON,
                    "/main_menu")
                .endRow()
                .build();

            message.setReplyMarkup(replyKeyboard);
        }
        message.setMessageId(messageId);
        message.setInlineMessageId(inlineMessageId);
        message.setChatId(String.valueOf(byUserAccepted.getTgAccountId()));
        return message;
    }

    public EditMessageText getDeniedFriendshipTemplate(BotUser requestedUser, BotUser byUserDenied,
                                                       int messageId, String inlineMessageId) {
        EditMessageText message = new EditMessageText();
        message.setMessageId(messageId);
        message.setInlineMessageId(inlineMessageId);
        message.setChatId(String.valueOf(byUserDenied.getTgAccountId()));
        message.setText(CROSS_MARK_ICON + " Запрос на дружбу от @" + requestedUser.getUserName() + " был отклонён");
        return message;
    }

    public EditMessageText getSettingsTemplate(String chatId, int messageId, String inlineMessageId) {
        EditMessageText message = new EditMessageText();
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
            .buttonWithCallbackData("Лимит подарков " + GIFT_LIMIT,
                "/settings/set_limit")
            .endRow()
            .withRow()
            .buttonWithCallbackData("Написать разработчику " + SEND_MESSAGE,
                "/settings/contact_developer")
            .endRow()
            .withRow()
            .buttonWithCallbackData("« Назад",
                "/main_menu")
            .endRow()
            .build();

        message.setReplyMarkup(replyKeyboard);
        message.setMessageId(messageId);
        message.setInlineMessageId(inlineMessageId);
        message.setChatId(chatId);

        return message;
    }

    public EditMessageText getUpdatesSettingsTemplate(String chatId, int messageId, String inlineMessageId) {
        EditMessageText message = new EditMessageText();

        message.setText(NOTIFICATION_ICON + " Хочешь получать уведомления, когда кто-то из твоих друзей " +
            "добавит/удалит подарок из своего WishList'а или попросит тебя добавить новый подарок в твой WishList?");

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
        message.setMessageId(messageId);
        message.setInlineMessageId(inlineMessageId);
        message.setChatId(chatId);

        return message;
    }

    public EditMessageText getVisibilitySettingsTemplate(String chatId, int messageId, String inlineMessageId) {
        EditMessageText message = new EditMessageText();
        message.setText(SEE_ICON + " Кто может видеть твой WishList?");

        InlineKeyboardMarkup replyKeyboard = InlineKeyboard.InlineKeyboardMarkupBuilder
            .newInlineKeyboardMarkup()
            .withRow()
            .buttonWithCallbackData("Только подписчики " + TWO_GUYS_ICON,
                "/settings/set_visibility_subscribers_only/true")
            .endRow()
            .withRow()
            .buttonWithCallbackData("Любой пользователь " + LOT_PEOPLE_ICON,
                "/settings/set_visibility_subscribers_only/false")
            .endRow()
            .withRow()
            .buttonWithCallbackData("« Назад",
                "/settings")
            .endRow()
            .build();

        message.setReplyMarkup(replyKeyboard);
        message.setMessageId(messageId);
        message.setInlineMessageId(inlineMessageId);
        message.setChatId(chatId);

        return message;
    }

    public EditMessageText getGiftLimitTemplate(String chatId, int messageId, String inlineMessageId, BotUser botUser) {
        EditMessageText message = new EditMessageText();
        message.setText(GIFT_LIMIT + " Выбери максимальное число подарков из твоего WishList'а," +
            " которое может забронировать каждый пользователь\n" +
            "На данный момент - " + botUser.getGiftLimit());

        InlineKeyboardMarkup replyKeyboard = InlineKeyboard.InlineKeyboardMarkupBuilder
            .newInlineKeyboardMarkup()
            .withRow()
            .buttonWithCallbackData("1",
                "/settings/set_limit/1")
            .buttonWithCallbackData("2",
                "/settings/set_limit/2")
            .buttonWithCallbackData("3",
                "/settings/set_limit/3")
            .buttonWithCallbackData("4",
                "/settings/set_limit/4")
            .buttonWithCallbackData("5",
                "/settings/set_limit/5")
            .endRow()
            .withRow()
            .buttonWithCallbackData("6",
                "/settings/set_limit/6")
            .buttonWithCallbackData("7",
                "/settings/set_limit/7")
            .buttonWithCallbackData("8",
                "/settings/set_limit/8")
            .buttonWithCallbackData("9",
                "/settings/set_limit/9")
            .buttonWithCallbackData("10",
                "/settings/set_limit/10")
            .endRow()
            .withRow()
            .buttonWithCallbackData("« Назад",
                "/settings")
            .endRow()
            .build();

        message.setReplyMarkup(replyKeyboard);
        message.setMessageId(messageId);
        message.setInlineMessageId(inlineMessageId);
        message.setChatId(chatId);

        return message;
    }

    public EditMessageText getWebTemplate(String chatId, int messageId, String inlineMessageId, BotUser botUser) {
        EditMessageText message = new EditMessageText();
        message.setText(URL_ICON + "Из этого меню ты можешь перейти в веб-версию бота. Установка/изменение пароля" +
            " для входа в аккаунт на сайте осуществляется в меню \n\"Установка пароля " + LOCK_ICON + "\n" +
            "Твоё имя пользователя для входа на сайт: " + botUser.getUserName());

        InlineKeyboardMarkup replyKeyboard = InlineKeyboard.InlineKeyboardMarkupBuilder
            .newInlineKeyboardMarkup()
            .withRow()
            .buttonWithUrl("Открыть веб-версию бота " + URL_ICON,
                "https://vividbot.herokuapp.com/")
            .endRow()
            .withRow()
            .buttonWithCallbackData("Установка пароля" + LOCK_ICON,
                "/web/set_password")
            .endRow()
            .withRow()
            .buttonWithCallbackData("« Назад",
                "/main_menu")
            .endRow()
            .build();

        message.setReplyMarkup(replyKeyboard);
        message.setMessageId(messageId);
        message.setInlineMessageId(inlineMessageId);
        message.setChatId(chatId);

        return message;
    }

    public SendMessage getErrorStatusTemplate(String text, String chatId) {
        if (text != null) {
            return new SendMessage(chatId, CROSS_MARK_ICON + " " + text);
        }
        return new SendMessage(chatId, CROSS_MARK_ICON + " Возникла ошибка, операция не выполнена");
    }

}
