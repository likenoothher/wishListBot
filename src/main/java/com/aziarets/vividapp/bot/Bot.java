package com.aziarets.vividapp.bot;

import com.aziarets.vividapp.controller.SubscribersController;
import com.aziarets.vividapp.handler.UpdateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.List;

@Component
@PropertySource(value= {"classpath:application.properties"})
public class Bot extends TelegramLongPollingBot {
    private static final Logger logger = LoggerFactory.getLogger(Bot.class);

    private String botUserName;
    private String botToken;
    private UpdateHandler handler;

    @Autowired
    public Bot(@Value("${bot.user_name}") String botUserName, @Value("${bot.token}") String botToken,
               UpdateHandler handler) {
        this.handler = handler;
        this.botToken = botToken;
        this.botUserName = botUserName;
        TelegramBotsApi telegramBotsApi = null;
        try {
            telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        } catch (TelegramApiException e) {
            logger.warn("Exception during bot's initialization: " + e.getLocalizedMessage());
        }
        try {
            telegramBotsApi.registerBot(this);
        } catch (TelegramApiException e) {
            logger.warn("Exception during bot's registration: " + e.getLocalizedMessage());
        }
    }

    public String getBotUsername() {
        return botUserName;
    }

    public String getBotToken() {
        return botToken;
    }

    public void onUpdateReceived(Update update) {
        List<BotApiMethod> messages = handler.handleUpdate(update);
        for (BotApiMethod message : messages) {
            try {
                execute(message);
            } catch (TelegramApiException e) {
                logger.warn("Exception during sending messages: " + e.getLocalizedMessage());
            }

        }
    }
}
