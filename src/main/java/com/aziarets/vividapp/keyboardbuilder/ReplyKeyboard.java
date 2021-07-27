package com.aziarets.vividapp.keyboardbuilder;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

public class ReplyKeyboard {
    private final KeyboardRow row;
    private final List<KeyboardRow> keyboard;

    public ReplyKeyboard(KeyboardRow row, List<KeyboardRow> keyboard) {
        this.row = row;
        this.keyboard = keyboard;
    }

    public KeyboardRow getRow() {
        return row;
    }

    public List<KeyboardRow> getKeyboard() {
        return keyboard;
    }

    public static final class ReplyKeyboardBuilder {
        private KeyboardRow row;
        private List<KeyboardRow> keyboard = new ArrayList<>();

        private ReplyKeyboardBuilder() {
        }

        public static ReplyKeyboardBuilder newReplyKeyboard() {
            return new ReplyKeyboardBuilder();
        }

        public ReplyKeyboardBuilder withRow() {
            this.row = new KeyboardRow();
            return this;
        }

        public ReplyKeyboardBuilder button(String text) {
            KeyboardButton keyboardButton = new KeyboardButton();
            keyboardButton.setText(text);
            row.add(keyboardButton);
            return this;
        }

        public ReplyKeyboardBuilder endRow() {
            this.keyboard.add(this.row);
            this.row = null;
            return this;
        }

        public ReplyKeyboardMarkup build() {
            ReplyKeyboardMarkup replyKeyboard = new ReplyKeyboardMarkup(keyboard);
            replyKeyboard.setOneTimeKeyboard(true);
            replyKeyboard.setResizeKeyboard(true);

            return replyKeyboard;
        }
    }
}
