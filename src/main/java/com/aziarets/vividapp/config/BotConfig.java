package com.aziarets.vividapp.config;

import com.aziarets.vividapp.bot.Bot;
import org.springframework.context.annotation.*;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
@ComponentScan("com.aziarets.vividapp")
public class BotConfig {
//    private AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(WebConfig.class);

//    @Bean
//    public TelegramBotsApi telegramBotsApi() throws TelegramApiException {
////        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
////        telegramBotsApi.registerBot(context.getBean(Bot.class));
//        return new TelegramBotsApi(DefaultBotSession.class);
//    }




}
