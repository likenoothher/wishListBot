package com.aziarets.vividapp.util;

import com.aziarets.vividapp.exception.NotFoundUserNameException;
import com.aziarets.vividapp.exception.UserIsBotException;
import com.aziarets.vividapp.handler.UpdateType;
import com.aziarets.vividapp.model.BotUser;
import com.aziarets.vividapp.model.BotUserStatus;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

@Component
public class BotUserExtractor {

    public synchronized BotUser identifyUser(Update update) throws NotFoundUserNameException, UserIsBotException {
        return createUserFromUpdateInfo(update);
    }

    private BotUser createUserFromUpdateInfo(Update update) throws NotFoundUserNameException, UserIsBotException {
        User gotFrom = extractUserInfoFromUpdate(update);
        if (gotFrom.getUserName() == null) {
            throw new NotFoundUserNameException("Имя пользователя null");
        }
        if (gotFrom.getIsBot() == true) {
            throw new UserIsBotException("Бот пытается получить доступ к сервису");
        }
        return BotUser.UserBuilder.newUser()
            .withTgAccountId(gotFrom.getId())
            .withFirstName(gotFrom.getFirstName())
            .withLastName(gotFrom.getLastName())
            .withUserName(gotFrom.getUserName())
            .withUserStatus(BotUserStatus.WITHOUT_STATUS)
            .isReadyReceiveUpdates(true)
            .isAllCanSeeMyWishList(false)
            .build();
    }

    private User extractUserInfoFromUpdate(Update update) { // описаны не все типы ответа! доделать
        UpdateType updateType = getUpdateType(update);
        if (updateType.equals(UpdateType.CALLBACK)) {
            return update.getCallbackQuery().getFrom();
        }
        if (updateType.equals(UpdateType.MESSAGE)) {
            return update.getMessage().getFrom();
        }
        if (updateType.equals(UpdateType.EDITED_MESSAGE)) {
            return update.getEditedMessage().getFrom();
        }
        return update.getInlineQuery().getFrom();
    }

    private long extractChatIdFromUpdate(Update update) { // описаны не все типы ответа! доделать
        UpdateType updateType = getUpdateType(update);
        if (updateType.equals(UpdateType.CALLBACK)) {
            return update.getCallbackQuery().getMessage().getChatId();
        }
        if (updateType.equals(UpdateType.MESSAGE)) {
            return update.getMessage().getChatId();
        }
        return update.getEditedMessage().getChatId();
    }

    private UpdateType getUpdateType(Update update) {
        if (update.hasMessage()) return UpdateType.MESSAGE;
        if (update.hasCallbackQuery()) return UpdateType.CALLBACK;
        if (update.hasEditedMessage()) return UpdateType.EDITED_MESSAGE;
        return UpdateType.INLINE_QUERY;
    }
}
