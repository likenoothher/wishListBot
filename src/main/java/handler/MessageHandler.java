package handler;

import data.Storage;
import menu.AppMenu;
import model.BotUser;
import model.Gift;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static menu.Icon.*;

public class MessageHandler {
    private Storage storage;
    private AppMenu menu;

    public MessageHandler(Storage storage, AppMenu menu) {
        this.storage = storage;
        this.menu = menu;
    }

    public List<SendMessage> handleMessage(Update update, BotUser updateSender) {
        List<SendMessage> messagesToSend = new ArrayList<>();
        String chatId = extractChatId(update);

        SendMessage unknownCommandMessage = menu.showMainMenu(chatId);
        unknownCommandMessage.setText("Не знаю такой команды " + DISAPPOINTED_ICON + "\n" +
                "Попробуй ещё раз из главного меню" + HMM_ICON);


        String messageText = update.getMessage().getText();
        String messagePrefix = messageText.substring(0, 1);

        if (messageText.equals("Главное меню") || messageText.equals("/start")) {
            messagesToSend.add(menu.showMainMenu(chatId));
            //return messages to send?
        }

        if (messagePrefix.equals("$")) {
            messagesToSend.addAll(handleAddPresentPrefixMessage(messageText, updateSender, chatId));
        }

        if (messagePrefix.equals("@")) {
            if (messageText.substring(1).equals(updateSender.getUserName())) {
                messagesToSend.addAll(List.of(new SendMessage(chatId,
                        "Извини, я не могу добавить тебя в друзья к самому себе" + UPSIDE_DOWN_FACE_ICON)));
            } else {
                messagesToSend.addAll(handleAddSubscriberPrefixMessage(messageText, updateSender, chatId));
            }
        }

        if (!messagesToSend.isEmpty()) {
            return messagesToSend;
        }
        messagesToSend.add(unknownCommandMessage);
        return messagesToSend;
    }

    private List<SendMessage> handleAddPresentPrefixMessage(String messageText, BotUser requestSender, String chatId) {
        List<SendMessage> messagesToSend = new ArrayList<>();
        Gift gift = Gift.GiftBuilder.newGift().withName(messageText.substring(1)).build();

        boolean isAdded = storage.addGiftToUser(gift, requestSender);

        if (isAdded) {
            messagesToSend.add(menu.showStatusMenu(isAdded, chatId,
                    "Подарок добавлен в твой WishList"));
            List<BotUser> subscribers = storage.findUserByTelegramId(requestSender.getTgAccountId()).get().getSubscribers();
            for (BotUser subscriber : subscribers) {
                if (subscriber.isReadyReceiveUpdated()) {
                    messagesToSend.add(new SendMessage(String.valueOf(subscriber.getTgChatId()),
                            EXCLAMATION_ICON + "Пользователь @" + requestSender.getUserName() +
                                    " добавил новый подарок в свой WishList"));
                }
            }
        }

        return messagesToSend;
    }

    private List<SendMessage> handleAddSubscriberPrefixMessage(String messageText, BotUser requestSender, String chatId) {
        Optional<BotUser> requestReceiver = storage.findUserByUserName(messageText.substring(1));
        List<SendMessage> messagesToSend = new ArrayList<>();

        if (requestReceiver.isPresent()) {
            if (!isRequestedUserAlreadyFriend(requestSender, requestReceiver.get())) {
                messagesToSend.add(menu.showFriendShipRequestTo(requestReceiver.get(), requestSender)); // message to request receiver
                messagesToSend.add(menu.showSendRequestMenu(chatId)); // message to request sender
            } else {
                messagesToSend.add(new SendMessage(chatId, "Ты уже подписан на данного пользователя"
                        + UPSIDE_DOWN_FACE_ICON));
            }
        } else {
            messagesToSend.add(new SendMessage(chatId,
                    "Мы не нашли пользователя с таким именем " + DISAPPOINTED_ICON + " Но ты можешь его пригласить из главного меню!"));
        }
        return messagesToSend;
    }

    private String extractChatId(Update update) {
        if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getMessage().getChatId().toString();
        }
        if (update.hasMessage()) {
            return update.getMessage().getChatId().toString();
        }
        return "";
    }

    private boolean isRequestedUserAlreadyFriend(BotUser requestSender, BotUser requestReceiver) {
        return requestReceiver.getSubscribers().contains(requestSender);
    }
}
