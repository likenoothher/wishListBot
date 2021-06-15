package com.aziarets.vividapp.config;

import com.aziarets.vividapp.bot.Bot;
import com.aziarets.vividapp.data.BotUserExtractor;
import com.aziarets.vividapp.data.Storage;
import com.aziarets.vividapp.handler.CallbackHandler;
import com.aziarets.vividapp.handler.MessageHandler;
import com.aziarets.vividapp.handler.UpdateHandler;
import com.aziarets.vividapp.menu.AppMenu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import javax.sql.DataSource;
import java.util.Objects;
import java.util.Properties;

@Configuration
@ComponentScan("com.aziarets.vividapp")
public class AppConfig {

    @Bean
    public TelegramBotsApi telegramBotsApi() throws TelegramApiException {
        return new TelegramBotsApi(DefaultBotSession.class);
    }
//
//    @Bean
//    public Bot bot(){
//        return new Bot(updateHandler());
//    }
//
//    @Bean
//    public UpdateHandler updateHandler(){
//        return new UpdateHandler(storage(), appMenu(), callbackHandler(), messageHandler());
//    }
//
//    @Bean
//    public AppMenu appMenu(){
//        return new AppMenu();
//    }
//
//    @Bean
//    public Storage storage(){
//        return new Storage(botUserExtractor());
//    }
//
//    @Bean
//    public BotUserExtractor botUserExtractor(){
//        return new BotUserExtractor();
//    }
//
//    @Bean
//    public CallbackHandler callbackHandler(){
//        return new CallbackHandler(storage(), appMenu());
//    }
//
//    @Bean
//    public MessageHandler messageHandler(){
//        return new MessageHandler(storage(), appMenu());
//    }


}
