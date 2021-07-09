package com.aziarets.vividapp.util;

import com.aziarets.vividapp.exception.NotFoundUserNameException;
import com.aziarets.vividapp.exception.UserIsBotException;
import com.aziarets.vividapp.handler.UpdateType;
import com.aziarets.vividapp.model.BotUser;
import com.aziarets.vividapp.model.BotUserRole;
import com.aziarets.vividapp.model.BotUserStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

@Component
public class BotUserExtractor {

    private PasswordGenerator passwordGenerator;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public BotUserExtractor(PasswordGenerator passwordGenerator, PasswordEncoder passwordEncoder) {
        this.passwordGenerator = passwordGenerator;
        this.passwordEncoder = passwordEncoder;
    }

    public synchronized BotUser identifyUser(Update update) throws NotFoundUserNameException, UserIsBotException {
        return createUserFromUpdateInfo(update);
    }

    public long getUpdateSenderId(Update update) {
        User gotFrom = extractUserInfoFromUpdate(update);
        return gotFrom.getId();
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
            .withPassword(passwordEncoder.encode(passwordGenerator.getRandomPassword()))
            .withUserStatus(BotUserStatus.WITHOUT_STATUS)
            .withUserRole(BotUserRole.ROLE_USER)
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
