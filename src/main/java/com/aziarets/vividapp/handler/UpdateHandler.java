package com.aziarets.vividapp.handler;

import com.aziarets.vividapp.data.NotFoundUserNameException;
import com.aziarets.vividapp.data.Storage;
import com.aziarets.vividapp.data.UserIsBotException;
import com.aziarets.vividapp.menu.AppMenu;
import com.aziarets.vividapp.menu.Icon;
import com.aziarets.vividapp.model.BotUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.List;

@Component
public class UpdateHandler {
    private Storage storage;
    private AppMenu menu;
    private CallbackHandler callbackHandler;
    private MessageHandler messageHandler;

    @Autowired
    public UpdateHandler(Storage storage, AppMenu menu, CallbackHandler callbackHandler, MessageHandler messageHandler) {
        this.storage = storage;
        this.menu = menu;
        this.callbackHandler = callbackHandler;
        this.messageHandler = messageHandler;
    }

    public synchronized List<BotApiMethod> handleUpdate(Update update) {
        String chatId = extractChatId(update);
        BotUser updateSender = null;
        try {
            updateSender = storage.identifyUser(update);
        } catch (NotFoundUserNameException e) {
            e.printStackTrace();
            return List.of(new SendMessage(chatId, "К сожалению, для пользования нашим ботом" +
                " тебе необходимо изменить настройки приватности и открыть видимость для имени пользователя @{user_name}"));
        } catch (UserIsBotException e) {
            e.printStackTrace();
            return List.of(new SendMessage(chatId, "Ботам здесь не рады"));
        }
        List<BotApiMethod> messagesToSend = new ArrayList<>();

        if (update.hasCallbackQuery() && updateSender != null) {
            messagesToSend.addAll(handleCallBackQuery(update, updateSender));
        }

        if (update.hasMessage() && updateSender != null) {
            messagesToSend.addAll(handleMessage(update, updateSender));
        }

        if (!messagesToSend.isEmpty()) {
            return messagesToSend;
        }

        SendMessage message = menu.showMainMenu(chatId);
        message.setText(Icon.DISAPPOINTED_ICON + " Не знаю такой команды, попробуй ещё раз из главного меню");

        return List.of(message);
    }

    private List<BotApiMethod> handleMessage(Update update, BotUser updateSender) {
        return messageHandler.handleMessage(update, updateSender);
    }

    private List<BotApiMethod> handleCallBackQuery(Update update, BotUser updateSender) {
        return callbackHandler.handleCallBackQuery(update, updateSender);

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

}
