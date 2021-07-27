package com.aziarets.vividapp;

import com.aziarets.vividapp.bot.Bot;
import com.aziarets.vividapp.config.BotConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


@SpringBootApplication
public class VividApp {
    public static void main(String[] args) {
        SpringApplication.run(VividApp.class, args);
    }
}
