package bot;

import handler.UpdateHandler;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

public class Bot extends TelegramLongPollingBot {
    private UpdateHandler handler = new UpdateHandler();

    public String getBotUsername() {
        return "Vivid";
    }

    public String getBotToken() {
        return "1899375504:AAE-M6_miu3OytFn9pt_otNdniFK82Pb7Kg";
    }

    public void onUpdateReceived(Update update) {

        List<SendMessage> messages = handler.handleUpdate(update);
        EditMessageText editMessageText = new EditMessageText();
        for (SendMessage message : messages) {
            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }

        }
    }
}
