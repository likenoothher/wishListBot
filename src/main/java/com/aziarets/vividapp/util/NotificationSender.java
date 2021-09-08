package com.aziarets.vividapp.util;

import com.aziarets.vividapp.bot.Bot;
import com.aziarets.vividapp.menu.BotMenuTemplate;
import com.aziarets.vividapp.model.BotUser;
import com.aziarets.vividapp.model.Gift;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;

import static com.aziarets.vividapp.menu.Icon.EXCLAMATION_ICON;

@Component
public class NotificationSender {
    private Bot bot;
    private BotMenuTemplate menu;

    @Autowired
    public NotificationSender(Bot bot, BotMenuTemplate menu) {
        this.bot = bot;
        this.menu = menu;
    }

    private void sendNotification(BotApiMethod notification) {
        bot.sendNotification(notification);
    }

    public void sendUserAddGiftNotification(BotUser botUser) {
        List<BotUser> subscribers = botUser.getSubscribers();
        for (BotUser subscriber : subscribers) {
            if (subscriber.isReadyReceiveUpdates()) {
                sendNotification(new SendMessage(String.valueOf(subscriber.getTgAccountId()),
                    EXCLAMATION_ICON + "Пользователь @" + botUser.getUserName() +
                        " добавил новый подарок в свой WishList"));
            }
        }
    }

    public void sendUserDeletedGiftYouDonateNotification(Gift gift, BotUser giftHolder) {
        if (gift.getOccupiedBy().isReadyReceiveUpdates()) {
            SendMessage notification = menu.getUserDeletedPresentYouGoingToDonateTemplate(gift, giftHolder);
            sendNotification(notification);
        }
    }

    public void sendSubscriberDeletedNotification(BotUser deletedUser, BotUser deletedByUser) {
        if (deletedUser.isReadyReceiveUpdates()) {
            SendMessage notification = menu.getAlertToDeletedSubscriberTemplate(deletedUser, deletedByUser);
            sendNotification(notification);
        }
    }

    public void sendMessage(BotUser to, String message) {
        sendNotification(new SendMessage(String.valueOf(to.getTgAccountId()), message));
    }

    public void sendMessageToDeveloper(BotUser from, String message) {
        sendNotification(new SendMessage(String.valueOf("988800148"), "Сообщение от @" + from.getUserName()
            + ": " + message));

    }
}
