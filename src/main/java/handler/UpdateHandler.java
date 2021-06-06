package handler;

import data.NotFoundUserNameException;
import data.Storage;
import menu.AppMenu;
import model.BotUser;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.List;


public class UpdateHandler {
    private Storage storage;
    private AppMenu menu;
    private CallbackHandler callbackHandler;
    private MessageHandler messageHandler;

    public UpdateHandler() {
        this.storage = new Storage();
        this.menu = new AppMenu();
        this.callbackHandler = new CallbackHandler(storage, menu);
        this.messageHandler = new MessageHandler(storage, menu);
    }

    public synchronized List<SendMessage> handleUpdate(Update update) {
        BotUser updateSender = null;
        try {
            updateSender = storage.identifyUser(update);
        } catch (NotFoundUserNameException e) {
            e.printStackTrace();
            return List.of(new SendMessage(extractChatId(update), "К сожалению, для пользования нашим ботом" +
                    " тебе необходимо изменить настройки приватности и открыть видимость для имени пользователя @{user_name}"));
        }
        List<SendMessage> messagesToSend = new ArrayList<>();

//        ReplyKeyboardMarkup replyKeyboard = ReplyKeyboard.ReplyKeyboardBuilder.newReplyKeyboard().withRow()
//                .button("Главное меню").endRow().build();
//        replyKeyboard.setOneTimeKeyboard(true);
//        replyKeyboard.setResizeKeyboard(true);

        if (update.hasCallbackQuery()) {
            messagesToSend.addAll(handleCallBackQuery(update, updateSender));
        }

        if (update.hasMessage()) {
            messagesToSend.addAll(handleMessage(update, updateSender));
        }

        SendMessage main_menu = new SendMessage(extractChatId(update), "");
//        main_menu.setReplyMarkup(replyKeyboard);
//        messagesToSend.add(main_menu);
        if (!messagesToSend.isEmpty()) {
            return messagesToSend;
        }

        SendMessage message = menu.showMainMenu(extractChatId(update));

        return List.of(message, main_menu);
    }

    private List<SendMessage> handleMessage(Update update, BotUser updateSender) {
        return messageHandler.handleMessage(update, updateSender);
    }

    private List<SendMessage> handleCallBackQuery(Update update, BotUser updateSender) {
        return callbackHandler.handleCallBackQuery(update, updateSender);

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


}
