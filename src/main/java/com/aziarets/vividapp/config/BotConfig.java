package com.aziarets.vividapp.config;

import com.aziarets.vividapp.bot.Bot;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
@ComponentScan("com.aziarets.vividapp")
public class BotConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ObjectMapper objectMapper(){
        return new ObjectMapper();
    }

//    private AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(WebConfig.class);

//    @Bean
//    public TelegramBotsApi telegramBotsApi() throws TelegramApiException {
////        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
////        telegramBotsApi.registerBot(context.getBean(Bot.class));
//        return new TelegramBotsApi(DefaultBotSession.class);
//    }




}
