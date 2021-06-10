package menu;

import builder.InlineKeyboard;
import model.BotUser;
import model.Gift;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;
import java.util.Map;

import static menu.Icon.*;

public class AppMenu {

    public SendMessage showGreetingMenu(String chatId, BotUser updateSender) {
        SendMessage message = new SendMessage();
        message.setText("Привет, @" + updateSender.getUserName() + HI_ICON + "\nСудя по всему ты здесь в первый раз. Этот бот позволяет создавать" +
            " свой WishList" + WISH_LIST_ICON + " и делиться им с друзьями " + TWO_GUYS_ICON + " Ты можешь отмечать подарки" + I_PRESENT_ICON + " своих друзей, которые ты планируешь " +
            "им подарить, после чего они исчезнут из поля видимости остальных подписчиков\n " + POINT_RIGHT_ICON + " Таким образом" +
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
            .build();
    }

    public EditMessageText showMyWishListMenu(List<Gift> gifts, String chatId, int messageId, String inlineMessageId) {
        EditMessageText message = new EditMessageText();
        message.setText(WISH_LIST_ICON + " Здесь ты можешь управлять своим WishList'ом\n");

        InlineKeyboardMarkup replyKeyboard =  InlineKeyboard.InlineKeyboardMarkupBuilder
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

    public EditMessageText createGiftRepresentation(Gift gift, String chatId) {
        String giftName = gift.getName() == null ? "не указано" : gift.getName();
        String giftDescription = gift.getDescription() == null ? "не указано" : gift.getDescription();
        String giftUrl = gift.getUrl() == null ? "не указано" : gift.getUrl();
        EditMessageText itemMessage = new EditMessageText();
        itemMessage.setChatId(chatId);
        itemMessage.setText(DIAMOND_ICON + "Имя подарка - " + giftName + "\n" +
            "Описание - " + giftDescription + "\n" +
            "Ссылка - " + giftUrl + "\n\n" +
            THUMB_DOWN_POINTER_ICON + "Для редактирования/удаления жми" + THUMB_DOWN_POINTER_ICON);

        InlineKeyboardMarkup replyKeyboard = InlineKeyboard.InlineKeyboardMarkupBuilder
            .newInlineKeyboardMarkup()
            .withRow()
            .buttonWithCallbackData("Редактировать " + MANAGING_ICON,
                "/my_wish_list/edit_my_present_under/id/" + gift.getId())
            .buttonWithCallbackData("Удалить " + MINUS_MARK_ICON,
                "/my_wish_list/delete_my_present_under/id/" + gift.getId())
            .endRow()
            .build();

        itemMessage.setReplyMarkup(replyKeyboard);
        return itemMessage;
    }

    public EditMessageText createEditGiftRepresentationTemplate(Gift gift, String chatId, int messageId, String inlineMessageId) {
        EditMessageText editedText = new EditMessageText();

        String giftName = gift.getName() == null ? "не указано" : gift.getName();
        String giftDescription = gift.getDescription() == null ? "не указано" : gift.getDescription();
        String giftUrl = gift.getUrl() == null ? "не указано" : gift.getUrl();

        editedText.setText(DIAMOND_ICON + "Имя подарка - " + giftName + "\n" +
            "Описание - " + giftDescription + "\n" +
            "Ссылка - " + giftUrl + "\n\n" +
            THUMB_DOWN_POINTER_ICON + "Для редактирования/удаления жми" + THUMB_DOWN_POINTER_ICON);
        editedText.setChatId(chatId);
        editedText.setMessageId(messageId);
        editedText.setInlineMessageId(inlineMessageId);

        InlineKeyboardMarkup replyKeyboard = InlineKeyboard.InlineKeyboardMarkupBuilder
            .newInlineKeyboardMarkup()
            .withRow()
            .buttonWithCallbackData("Редактировать " + MANAGING_ICON,
                "/my_wish_list/edit_my_present_under/id/" + gift.getId())
            .buttonWithCallbackData("Удалить " + MINUS_MARK_ICON,
                "/my_wish_list/delete_my_present_under/id/" + gift.getId())
            .endRow()
            .build();

        editedText.setReplyMarkup(replyKeyboard);

        return editedText;

    }

    public EditMessageText createEditGiftTextTemplate(Gift gift, String chatId, int messageId, String inlineMessageId) {

        EditMessageText editedText = new EditMessageText();
        editedText.setChatId(chatId);
        editedText.setMessageId(messageId);
        editedText.setInlineMessageId(inlineMessageId);
        editedText.setText(CHECK_MARK_ICON + " Подарок \"" + gift.getName() + "\" был изменён");
        return editedText;
    }

    public EditMessageText createDeleteGiftTextTemplate(Gift gift, String chatId, int messageId, String inlineMessageId) {
        EditMessageText editedText = new EditMessageText();
        editedText.setChatId(chatId);
        editedText.setMessageId(messageId);
        editedText.setInlineMessageId(inlineMessageId);
        editedText.setText(CROSS_MARK_ICON + " Подарок \"" + gift.getName() + "\" был удалён");

        return editedText;
    }

    public EditMessageText showGiftRepresentationMenu(Gift gift, String chatId, int messageId, String inlineMessageId) {
        EditMessageText message = new EditMessageText();
        String description = gift.getDescription() == null ? "отсутствует" : gift.getDescription();
        String url = gift.getUrl() == null ? "отсутствует" : gift.getUrl().toString();
        message.setText(MANAGING_ICON + " В этом меню ты можешь управлять подарком \"" +
            gift.getName() + "\"" +
            "\nНа данный момент момент:" +
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
            .buttonWithCallbackData(MINUS_MARK_ICON + " Удалить подарок",
                "/my_wish_list/delete_my_present_under/id/" + gift.getId())
            .endRow()
            .withRow()
            .buttonWithCallbackData("« Назад",
                "/my_wish_list")
            .endRow()
            .build();

        message.setReplyMarkup(replyKeyboard);
        message.setChatId(chatId);
        message.setMessageId(messageId);
        message.setInlineMessageId(inlineMessageId);
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

    public EditMessageText showIPresentMenu(Map<BotUser, List<Gift>> gifts, String chatId, int messageId, String inlineMessageId) {
        EditMessageText message = new EditMessageText();
        message.setChatId(chatId);
        message.setMessageId(messageId);
        message.setInlineMessageId(inlineMessageId);

        if (!gifts.isEmpty()) {
            message.setText("Это подарки, которые ты планируешь подарить" + WISH_LIST_ICON +
                "\nНажми на подарок" + I_PRESENT_ICON + " из списка, для получения дополнительной информации");

            InlineKeyboardMarkup replyKeyboard = InlineKeyboard.InlineKeyboardMarkupBuilder
                .newInlineKeyboardMarkup()
                .withCallBackButtonsFromGiftList(gifts, I_PRESENT_ICON, "/i_present/show_gift_under/id") // убрал / после айди
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

    public EditMessageText createGiftInfoIPresentMenu(Gift gift, BotUser giftHolder, String chatId, int messageId, String inlineMessageId) {
        EditMessageText editedText = new EditMessageText();

        String giftName = gift.getName() == null ? "не указано" : gift.getName();
        String giftDescription = gift.getDescription() == null ? "не указано" : gift.getDescription();
        String giftUrl = gift.getUrl() == null ? "не указано" : gift.getUrl();
        String giftHolderName = giftHolder.getUserName() == null ? "не указано" : giftHolder.getUserName();

        editedText.setText(DIAMOND_ICON + "Имя подарка - " + giftName + "\n" +
            "Описание - " + giftDescription + "\n" +
            "Ссылка - " + giftUrl + "\n" +
            ONE_GUY_ICON +"Для пользователя - @" + giftHolderName + "\n\n" +
            THUMB_DOWN_POINTER_ICON + "Если передумал дарить жми" + THUMB_DOWN_POINTER_ICON);
        editedText.setChatId(chatId);
        editedText.setMessageId(messageId);
        editedText.setInlineMessageId(inlineMessageId);

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

        editedText.setReplyMarkup(replyKeyboard);

        return editedText;
    }

    public EditMessageText showMySubscribersListMenu(List<BotUser> subscribers, String chatId, int messageId, String inlineMessageId) {
        EditMessageText message = new EditMessageText();
        if (!subscribers.isEmpty()) {
            message.setText(ARROW_LOWER_LEFT_ICON + "Это список пользователей, которые на тебя подписаны\n" +
                "Нажми на имя пользователя "+ONE_GUY_ICON+" для для того, чтобы увидеть более детальную информацию" + WISH_LIST_ICON );

            InlineKeyboardMarkup replyKeyboard = InlineKeyboard.InlineKeyboardMarkupBuilder
                .newInlineKeyboardMarkup()
                .withCallBackButtonsFromUserList(subscribers, ONE_GUY_ICON, "/my_subscribers/show/id")
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

    public EditMessageText showSubscriberRepresentationMenu(BotUser subscriber, String chatId, int messageId, String inlineMessageId) {
        StringBuilder textMessage = new StringBuilder();
        String firstName = subscriber.getFirstName() == null ? "не указано" : subscriber.getFirstName();
        String lastName = subscriber.getLastName() == null ? "не указано" : subscriber.getLastName();

        textMessage.append(ONE_GUY_ICON + " Информация о пользователе:\n");
        textMessage
            .append("Имя пользователя - @" + subscriber.getUserName())
            .append("\n")
            .append("Имя - " + firstName)
            .append("\n")
            .append("Фамилия - " + lastName);

        EditMessageText message = new EditMessageText();
        message.setText(textMessage.toString());

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

    public SendMessage showNotificationToDeletedSubscriberMenu(BotUser deletedUser, BotUser deletedByUser) {
        SendMessage message = new SendMessage();
        message.setText(BROKEN_HEART_ICON + "Пользователь @" + deletedByUser.getUserName() +
            " удалил тебя из списка друзей");

        message.setChatId(String.valueOf(deletedUser.getTgChatId()));
        return message;
    }


    public EditMessageText showMySubscriptionsMenu(List<BotUser> subscriptions, String chatId, int messageId, String inlineMessageId) {
        EditMessageText message = new EditMessageText();
        if (!subscriptions.isEmpty()) {
            message.setText("Это список пользователей на которых ты подписан.\n" +
                "Нажми на имя пользователя, для дополнительной информации" + WISH_LIST_ICON);

            InlineKeyboardMarkup replyKeyboard = InlineKeyboard.InlineKeyboardMarkupBuilder
                .newInlineKeyboardMarkup()
                .withCallBackButtonsFromUserList(subscriptions, ONE_GUY_ICON, "/my_subscriptions/show/id")
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

    public EditMessageText showSubscriptionRepresentationMenu(BotUser subscription, String chatId, int messageId, String inlineMessageId) {
        StringBuilder textMessage = new StringBuilder();
        String firstName = subscription.getFirstName() == null ? "не указано" : subscription.getFirstName();
        String lastName = subscription.getLastName() == null ? "не указано" : subscription.getLastName();

        textMessage.append(ONE_GUY_ICON + " Информация о пользователе:\n");
        textMessage
            .append("Имя пользователя - @" + subscription.getUserName())
            .append("\n")
            .append("Имя - " + firstName)
            .append("Фамилия - " + lastName);

        EditMessageText message = new EditMessageText();
        message.setText(textMessage.toString());

        InlineKeyboardMarkup replyKeyboard = InlineKeyboard.InlineKeyboardMarkupBuilder
            .newInlineKeyboardMarkup()
            .withRow()
            .buttonWithCallbackData(WISH_LIST_ICON + " Посмотреть WishList",
                "/my_subscriptions/show/id" + subscription.getTgAccountId())
            .endRow()
            .withRow()
            .buttonWithCallbackData(MINUS_MARK_ICON + " Отписаться",
                "/my_subscriptions/delete_under/id/" + subscription.getTgAccountId())
            .endRow()
            .withRow()
            .buttonWithCallbackData("« Назад",
                "/my_subscriptions")
            .endRow()
            .build();

        message.setReplyMarkup(replyKeyboard);
        message.setMessageId(messageId);
        message.setInlineMessageId(inlineMessageId);
        message.setChatId(chatId);
        return message;
    }

    public EditMessageText showUserWishListMenu(BotUser wishListHolder, String chatId, int messageId, String inlineMessageId) {
        EditMessageText message = new EditMessageText();
        List<Gift> gifts = wishListHolder.findAvailableToDonatePresents();
        if (!gifts.isEmpty()) {
            message.setText("Это список доступных подарков из WishList'а @" + wishListHolder.getUserName() + "\n" +
                "Нажми на подарок, который хочешь подарить");

            InlineKeyboardMarkup replyKeyboard = InlineKeyboard.InlineKeyboardMarkupBuilder
                .newInlineKeyboardMarkup()
                .withCallBackButtonsFromGiftList(gifts, I_PRESENT_ICON, "/my_subscriptions/show_representation/gift_id")
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
                " либо все его подарки уже заняты" + MAN_SHRUGGING_ICON + "\nНо ты можешь попросить его добавить подарок");
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

    public EditMessageText showGoingDonateGiftRepresentationMenu(Gift gift,BotUser giftHolder, String chatId, int messageId, String inlineMessageId) {
        EditMessageText message = new EditMessageText();
        String description = gift.getDescription() == null ? "отсутствует" : gift.getDescription();
        String url = gift.getUrl() == null ? "отсутствует" : gift.getUrl().toString();
        message.setText(I_PRESENT_ICON + "Название - " + gift.getName() +
            "\n- описание: " + description +
            "\n- ссылка: " + url);

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

        message.setReplyMarkup(replyKeyboard);
        message.setMessageId(messageId);
        message.setInlineMessageId(inlineMessageId);
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

    public SendMessage showSendRequestMenu(String chatId) {
        SendMessage message = new SendMessage();
        message.setText(CHECK_MARK_ICON
            + "Запрос отправлен. После принятия заявки WishList будет доступен для просмотра");
        InlineKeyboardMarkup replyKeyboard = InlineKeyboard.InlineKeyboardMarkupBuilder
            .newInlineKeyboardMarkup()
            .withRow()
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

    public EditMessageText showSettingsMenu(String chatId, int messageId, String inlineMessageId) {
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
//            .withRow()
//            .buttonWithCallbackData("Помощь " + SOS_ICON,
//                "/settings/help")
//            .endRow()
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

    public EditMessageText showIsReadyReceiveUpdateMenu(String chatId, int messageId, String inlineMessageId) {
        EditMessageText message = new EditMessageText();
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
        message.setMessageId(messageId);
        message.setInlineMessageId(inlineMessageId);
        message.setChatId(chatId);
        return message;
    }

    public EditMessageText showSetVisibilityWishListMenu(String chatId, int messageId, String inlineMessageId) {
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

    public SendMessage showOkStatusMenu(String text, String chatId) {
        if (text != null) {
            return new SendMessage(chatId, CHECK_MARK_ICON + " " + text);
        }
        return new SendMessage(chatId, CHECK_MARK_ICON + " Успешно!");
    }

    public EditMessageText showAcceptingFriendshipStatusOkMenu(String userName, String chatId, int messageId, String inlineMessageId) {
        EditMessageText message = new EditMessageText();
        message.setMessageId(messageId);
        message.setInlineMessageId(inlineMessageId);
        message.setChatId(chatId);
        message.setText(CHECK_MARK_ICON + " Запрос на дружбу от @" + userName + " принят. Теперь он имеет доступ к твоему WishList'y");
        return message;
    }

    public EditMessageText showAcceptingFriendshipStatusDeniedMenu(String userName, String chatId, int messageId, String inlineMessageId) {
        EditMessageText message = new EditMessageText();
        message.setMessageId(messageId);
        message.setInlineMessageId(inlineMessageId);
        message.setChatId(chatId);
        message.setText(CROSS_MARK_ICON + " Запрос на дружбу от @" + userName + " был отклонён");
        return message;
    }

    public SendMessage showErrorStatusMenu(String text, String chatId) {
        if (text != null) {
            return new SendMessage(chatId, CROSS_MARK_ICON + " " + text);
        }
        return new SendMessage(chatId, CROSS_MARK_ICON + " Возникла ошибка, операция не выполнена");
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
