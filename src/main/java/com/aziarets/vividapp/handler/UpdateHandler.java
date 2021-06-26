package com.aziarets.vividapp.handler;

import com.aziarets.vividapp.exception.NotFoundUserNameException;
import com.aziarets.vividapp.service.BotService;
import com.aziarets.vividapp.exception.UserIsBotException;
import com.aziarets.vividapp.menu.BotMenuTemplate;
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
    private BotService botService;
    private BotMenuTemplate menu;
    private CallbackHandler callbackHandler;
    private MessageHandler messageHandler;
    private String chatId;
    private List<BotApiMethod> messagesToSend = new ArrayList<>();

    @Autowired
    public UpdateHandler(BotService botService, BotMenuTemplate menu, CallbackHandler callbackHandler, MessageHandler messageHandler) {
        this.botService = botService;
        this.menu = menu;
        this.callbackHandler = callbackHandler;
        this.messageHandler = messageHandler;
    }

    public synchronized List<BotApiMethod> handleUpdate(Update update) {
        chatId = getUpdateChatId(update);
        messagesToSend.clear();

        BotUser updateSender = null;
        try {
            updateSender = botService.identifyUser(update);
        } catch (NotFoundUserNameException e) {
            e.printStackTrace();
            return List.of(new SendMessage(chatId, "К сожалению, для пользования нашим ботом" +
                " тебе необходимо изменить настройки приватности и открыть видимость имени пользователя @{user_name}"));
        } catch (UserIsBotException e) {
            e.printStackTrace();
            return List.of(new SendMessage(chatId, "Ботам здесь не рады"));
        }

        if (update.hasCallbackQuery() && updateSender != null) {
            messagesToSend.addAll(handleCallBackQuery(update, updateSender));
            return messagesToSend;
        }

        if (update.hasMessage() && updateSender != null) {
            messagesToSend.addAll(handleMessage(update, updateSender));
            return messagesToSend;
        }

        SendMessage unknownCommandMessage = menu.getMainMenuTemplate(chatId);
        unknownCommandMessage.setText(Icon.DISAPPOINTED_ICON
            + " Не знаю такой команды, попробуй ещё раз из главного меню");
        messagesToSend.add(unknownCommandMessage);

        return messagesToSend;
    }

    private List<BotApiMethod> handleMessage(Update update, BotUser updateSender) {
        return messageHandler.handleMessage(update, updateSender);
    }

    private List<BotApiMethod> handleCallBackQuery(Update update, BotUser updateSender) {
        return callbackHandler.handleCallBackQuery(update, updateSender);
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

}
