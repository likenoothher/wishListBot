package com.aziarets.vividapp.bot;

import com.aziarets.vividapp.handler.UpdateHandler;
import org.springframework.beans.factory.annotation.Autowired;
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
public class Bot extends TelegramLongPollingBot {

    private UpdateHandler handler;

    @Autowired
    public Bot(UpdateHandler handler) {
        this.handler = handler;
        TelegramBotsApi telegramBotsApi = null;
        try {
            telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        try {
            telegramBotsApi.registerBot(this);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public String getBotUsername() {
        return "Vivid";
    }

    public String getBotToken() {
        return "1899375504:AAE-M6_miu3OytFn9pt_otNdniFK82Pb7Kg";
    }

    public void onUpdateReceived(Update update) {
        List<BotApiMethod> messages = handler.handleUpdate(update);
        EditMessageText editMessageText = new EditMessageText();
        for (BotApiMethod message : messages) {
            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }

        }
    }
}
