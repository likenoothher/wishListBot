package builder;

import model.BotUser;
import model.Gift;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static menu.Icon.ONE_GUY_ICON;

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

        public InlineKeyboardMarkupBuilder button(InlineKeyboardButton button) {
            row.add(button);
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

        public InlineKeyboardMarkupBuilder withCallBackButtonsFromGiftList(List<Gift> list, String icon, String callBackPrefix) {
            for (int i = 0; i < list.size(); i++) {
                Gift gift = list.get(i);
                this.row = new ArrayList<>();
                InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
                inlineKeyboardButton.setText(icon + " " + gift.getName() + " ");
                inlineKeyboardButton.setCallbackData(callBackPrefix + "/" + gift.getId());
                row.add(inlineKeyboardButton);
                this.keyboard.add(this.row);
                this.row = null;
            }
            return this;
        }

        public InlineKeyboardMarkupBuilder withCallBackButtonsFromGiftList(Map<BotUser, List<Gift>> map, String icon, String callBackPrefix) {
            for (Map.Entry<BotUser, List<Gift>> entry : map.entrySet()) {
                String userName = entry.getKey().getUserName();
                List<Gift> gifts = entry.getValue();
                for (Gift gift : gifts) {
                    this.row = new ArrayList<>();
                    InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
                    inlineKeyboardButton.setText(icon + " " + gift.getName() + " для пользователя @" + userName);
                    inlineKeyboardButton.setCallbackData(callBackPrefix + "/" + gift.getId());
                    row.add(inlineKeyboardButton);
                    this.keyboard.add(this.row);
                    this.row = null;
                }
            }
            return this;
        }

        public InlineKeyboardMarkupBuilder withCallBackButtonsFromUserList(List<BotUser> list, String icon, String callBackPrefix) {
            for (int i = 0; i < list.size(); i++) {
                BotUser user = list.get(i);
                this.row = new ArrayList<>();
                InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
                inlineKeyboardButton.setText(ONE_GUY_ICON + " @" + user.getUserName());
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

