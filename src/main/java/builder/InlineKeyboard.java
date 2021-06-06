package builder;

import menu.Icon;
import model.BotUser;
import model.Gift;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

import static menu.Icon.*;

public class InlineKeyboard {
    private final List<InlineKeyboardButton> row;
    private final List<List<InlineKeyboardButton>> keyboard;

    private InlineKeyboard(List<InlineKeyboardButton> row, List<List<InlineKeyboardButton>> keyboard) {
        this.row = row;
        this.keyboard = keyboard;
    }

    public List<InlineKeyboardButton> getRow() {
        return row;
    }

    public List<List<InlineKeyboardButton>> getKeyboard() {
        return keyboard;
    }


    public static final class InlineKeyboardMarkupBuilder {
        private List<InlineKeyboardButton> row;
        private List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        private InlineKeyboardMarkupBuilder() {

        }

        public static InlineKeyboardMarkupBuilder newInlineKeyboardMarkup() {
            return new InlineKeyboardMarkupBuilder();
        }

        public InlineKeyboardMarkupBuilder withRow() {
            this.row = new ArrayList<>();
            return this;
        }

        public InlineKeyboardMarkupBuilder buttonWithUrl(String text, String url) {
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
            inlineKeyboardButton.setText(text);
            inlineKeyboardButton.setUrl(url);
            row.add(inlineKeyboardButton);
            return this;
        }

        public InlineKeyboardMarkupBuilder buttonWithCallbackData(String text, String callbackData) {
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
            inlineKeyboardButton.setText(text);
            inlineKeyboardButton.setCallbackData(callbackData);
            row.add(inlineKeyboardButton);
            return this;
        }

        public InlineKeyboardMarkupBuilder endRow() {
            this.keyboard.add(this.row);
            this.row = null;
            return this;
        }

        public InlineKeyboardMarkupBuilder withCallBackButtonsFromList(List<Gift> list, String callBackPrefix) {
            for (int i = 0; i < list.size(); i++) {
                Gift gift = list.get(i);
                this.row = new ArrayList<>();
                InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
                inlineKeyboardButton.setText((i + 1) + "." + gift.getName() + MINUS_MARK_ICON);
                inlineKeyboardButton.setCallbackData(callBackPrefix + "/" + gift.getId());
                row.add(inlineKeyboardButton);
                this.keyboard.add(this.row);
                this.row = null;
            }
            return this;
        }

        public InlineKeyboardMarkupBuilder withCallBackButtonsFromGiftsList(List<Gift> list, String callBackPrefix) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).occupiedBy() == null) {
                    Gift gift = list.get(i);
                    this.row = new ArrayList<>();
                    InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
                    inlineKeyboardButton.setText((i + 1) + "." + gift.getName() + " - Я дарю! " + Icon.I_PRESENT_ICON);
                    inlineKeyboardButton.setCallbackData(callBackPrefix + "/" + gift.getId());
                    row.add(inlineKeyboardButton);
                    this.keyboard.add(this.row);
                    this.row = null;
                }
            }
            return this;
        }

        public InlineKeyboardMarkupBuilder withCallBackButtonsFromUserListWithCrossMark(List<BotUser> list, String callBackPrefix) {
            for (int i = 0; i < list.size(); i++) {
                BotUser user = list.get(i);
                this.row = new ArrayList<>();
                InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
                inlineKeyboardButton.setText(CROSS_MARK_ICON + (i + 1) + ". " + user.getUserName());
                inlineKeyboardButton.setCallbackData(callBackPrefix + "/" + user.getTgAccountId());
                row.add(inlineKeyboardButton);
                this.keyboard.add(this.row);
                this.row = null;
            }
            return this;
        }

        public InlineKeyboardMarkupBuilder withCallBackButtonsFromUserList(List<BotUser> list, String callBackPrefix) {
            for (int i = 0; i < list.size(); i++) {
                BotUser user = list.get(i);
                this.row = new ArrayList<>();
                InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
                inlineKeyboardButton.setText(ONE_GUY_ICON + (i + 1) + ". " + user.getUserName());
                inlineKeyboardButton.setCallbackData(callBackPrefix + "/" + user.getTgAccountId());
                row.add(inlineKeyboardButton);
                this.keyboard.add(this.row);
                this.row = null;
            }
            return this;
        }

        public InlineKeyboardMarkup build() {
            InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup(keyboard);
            return inlineKeyboard;
        }
    }
}

