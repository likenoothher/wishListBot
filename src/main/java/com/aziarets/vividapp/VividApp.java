//package com.aziarets.vividapp;
//
//import com.aziarets.vividapp.bot.Bot;
//import com.aziarets.vividapp.config.BotConfig;
//import com.aziarets.vividapp.config.WebConfig;
//import org.springframework.context.annotation.AnnotationConfigApplicationContext;
//import org.telegram.telegrambots.meta.TelegramBotsApi;
//import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
//
//
//public class VividApp {
//    public static void main(String[] args) {
//        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(BotConfig.class, WebConfig.class);
//
//        TelegramBotsApi telegramBotsApi = context.getBean(TelegramBotsApi.class);
//        Bot bot = context.getBean(Bot.class);
//        try {
//            telegramBotsApi.registerBot(bot);
//        } catch (TelegramApiException e) {
//            e.printStackTrace();
//        }
//    }
//}
