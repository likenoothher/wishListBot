package com.aziarets.vividapp.handler;

import com.aziarets.vividapp.exception.NotFoundUserNameException;
import com.aziarets.vividapp.exception.UserIsDisabled;
import com.aziarets.vividapp.service.BotService;
import com.aziarets.vividapp.exception.UserIsBotException;
import com.aziarets.vividapp.menu.BotMenuTemplate;
import com.aziarets.vividapp.menu.Icon;
import com.aziarets.vividapp.model.BotUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.List;

@Component
public class UpdateHandler {
    private static final Logger logger = LoggerFactory.getLogger(UpdateHandler.class);

    private BotService botService;
    private BotMenuTemplate menu;
    private CallbackHandler callbackHandler;
    private MessageHandler messageHandler;
    private String chatId;
    private List<BotApiMethod> messagesToSend = new ArrayList<>();

    @Autowired
    public UpdateHandler(BotService botService, BotMenuTemplate menu, CallbackHandler callbackHandler,
                         MessageHandler messageHandler) {
        this.botService = botService;
        this.menu = menu;
        this.callbackHandler = callbackHandler;
        this.messageHandler = messageHandler;
    }

    public synchronized List<BotApiMethod> handleUpdate(Update update) {
        chatId = getUpdateChatId(update);
        messagesToSend.clear();
        logger.info("Handling update with id: " + update.getUpdateId());

        BotUser updateSender = null;
        try {
            logger.info("Identifying user from update with id: " + update.getUpdateId());
            updateSender = botService.identifyUser(update);
            logger.info("User identified from update with id: " + update.getUpdateId());
        } catch (NotFoundUserNameException e) {
            logger.warn("Not found user name during identifying update with id: " + update.getUpdateId());
            return List.of(new SendMessage(chatId, "К сожалению, для пользования нашим ботом" +
                " тебе необходимо изменить настройки приватности и открыть видимость имени пользователя @{user_name}"));
        } catch (UserIsBotException e) {
            logger.warn("User from update with id: " + update.getUpdateId() + " is bot. Access rejected");
            return List.of(new SendMessage(chatId, "Ботам здесь не рады"));
        } catch (UserIsDisabled e) {
            logger.warn("User from update with id: " + update.getUpdateId() + " is disabled. Access rejected");
            return List.of(new SendMessage(chatId, "Твой аккаунт заблокирован"));
        }

        if (update.hasCallbackQuery() && updateSender != null) {
            messagesToSend.addAll(handleCallBackQuery(update, updateSender));
            return messagesToSend;
        }

        if (update.hasMessage() && updateSender != null) {
            messagesToSend.addAll(handleMessage(update, updateSender));
            return messagesToSend;
        }

        logger.info("Handling unknown request from user with id: " + updateSender.getId());
        SendMessage unknownCommandMessage = menu.getMainMenuTemplate(chatId);
        unknownCommandMessage.setText(Icon.DISAPPOINTED_ICON
            + " Не знаю такой команды, попробуй ещё раз из главного меню");
        messagesToSend.add(unknownCommandMessage);

        return messagesToSend;
    }

    private List<BotApiMethod> handleMessage(Update update, BotUser updateSender) {
        logger.info("Handling message request from update with id: " + update.getUpdateId());
        return messageHandler.handleMessage(update, updateSender);
    }

    private List<BotApiMethod> handleCallBackQuery(Update update, BotUser updateSender) {
        logger.info("Handling call back query request from update with id: " + update.getUpdateId());
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
