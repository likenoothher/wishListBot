package com.aziarets.vividapp.handler;

import com.aziarets.vividapp.data.Storage;
import com.aziarets.vividapp.menu.BotMenuTemplate;
import com.aziarets.vividapp.model.BotUser;
import com.aziarets.vividapp.model.BotUserStatus;
import com.aziarets.vividapp.model.Gift;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.aziarets.vividapp.menu.Icon.*;

@Component
@Transactional
public class MessageHandler {
    private Storage storage;
    private BotMenuTemplate menu;

    @Autowired
    public MessageHandler(Storage storage, BotMenuTemplate menu) {
        this.storage = storage;
        this.menu = menu;
    }

    public List<BotApiMethod> handleMessage(Update update, BotUser updateSender) {
        List<BotApiMethod> messagesToSend = new ArrayList<>();
        String chatId = extractChatId(update);

        SendMessage unknownCommandMessage = menu.getMainMenuTemplate(chatId);
        unknownCommandMessage.setText("Не знаю такой команды " + DISAPPOINTED_ICON + "\n" +
            "Попробуй ещё раз из главного меню" + HMM_ICON);

        if (updateSender.getBotUserStatus().equals(BotUserStatus.WITHOUT_STATUS)) {
            messagesToSend.addAll(handleWithoutStatusRequest(update, updateSender));
        }

        if (updateSender.getBotUserStatus().equals(BotUserStatus.ADDING_GIFT_NAME)) {
            messagesToSend.addAll(handleAddingGiftNameRequest(update, updateSender));
        }

        if (updateSender.getBotUserStatus().equals(BotUserStatus.ADDING_GIFT_DESCRIPTION)) {
            messagesToSend.addAll(handleAddingGiftDescriptionRequest(update, updateSender));
        }

        if (updateSender.getBotUserStatus().equals(BotUserStatus.ADDING_GIFT_URl)) {
            messagesToSend.addAll(handleAddingGiftURLRequest(update, updateSender));
        }

        if (updateSender.getBotUserStatus().equals(BotUserStatus.SEARCHING_FRIEND)) {
            messagesToSend.addAll(handleSearchingFriendRequest(update, updateSender));
        }

        if (updateSender.getBotUserStatus().equals(BotUserStatus.CONTACTING_DEVELOPER)) {
            messagesToSend.addAll(handleContactDeveloperRequest(update, updateSender));
        }

        if (!messagesToSend.isEmpty()) {
            return messagesToSend;
        }
        messagesToSend.add(unknownCommandMessage);
        return messagesToSend;
    }

    private List<BotApiMethod> handleSearchingFriendRequest(Update update, BotUser updateSender) {
        List<BotApiMethod> messagesToSend = new ArrayList<>();
        String chatId = extractChatId(update);
        String messageText = update.getMessage().getText();

        Optional<BotUser> searchedUser = storage.findUserByUserName(messageText);
        Optional<BotUser> userSearchedTo = storage.findUserByTelegramId(updateSender.getTgAccountId());

        if(!searchedUser.isPresent()) {
            messagesToSend.add(new SendMessage(chatId, "Мы не смогли найти данного пользователя"
                + DISAPPOINTED_ICON));
            return messagesToSend;
        }

        if (!searchedUser.equals(userSearchedTo)) {
            if (!isRequestedUserAlreadyFriend(userSearchedTo.get(), searchedUser.get())) {
                messagesToSend.add(menu.getFriendShipRequestToTemplate(searchedUser.get(), userSearchedTo.get())); // message to request receiver
                messagesToSend.add(menu.getSendFriendshipRequestTemplate(chatId)); // message to request sender
            } else {
                messagesToSend.add(new SendMessage(chatId, "Ты уже подписан на данного пользователя"
                    + UPSIDE_DOWN_FACE_ICON));
            }
        } else {
            messagesToSend.add(new SendMessage(chatId,
                "Ты не можешь подписаться сам на себя" + UPSIDE_DOWN_FACE_ICON));
        }
        BotUser updatedUser = userSearchedTo.get();
        updatedUser.setBotUserStatus(BotUserStatus.WITHOUT_STATUS);
        storage.updateUser(updatedUser);
        return messagesToSend;

    }

    private List<BotApiMethod> handleContactDeveloperRequest(Update update, BotUser updateSender) {
        List<BotApiMethod> messagesToSend = new ArrayList<>();
        String chatId = extractChatId(update);
        String messageText = update.getMessage().getText();

        messagesToSend.add(new SendMessage("988800148", "Сообщение от @" + updateSender.getUserName() +
            ":" + messageText));
        messagesToSend.add(new SendMessage(chatId, CHECK_MARK_ICON + " Твоё сообщение отправлено разработчику"));

        Optional<BotUser> userSearchedTo = storage.findUserByTelegramId(updateSender.getTgAccountId());
        BotUser updatedUser = userSearchedTo.get();
        updatedUser.setBotUserStatus(BotUserStatus.WITHOUT_STATUS);
        storage.updateUser(updatedUser);
        return messagesToSend;

    }

    private List<BotApiMethod> handleAddingGiftNameRequest(Update update, BotUser updateSender) {
        List<BotApiMethod> messagesToSend = new ArrayList<>();
        String chatId = extractChatId(update);
        String messageText = update.getMessage().getText();
        String inlineMessageId = updateSender.getCarryingInlineMessageId();
        int messageId = updateSender.getCarryingMessageId();

        Gift gift = Gift.GiftBuilder.newGift().withName(messageText).build();

        boolean isAdded = storage.addGiftToUser(gift, updateSender);
        updateSender.setBotUserStatus(BotUserStatus.WITHOUT_STATUS);
        storage.updateUser(updateSender);
        if (isAdded) {
            List<Gift> gifts = storage.findUserByTelegramId(updateSender.getTgAccountId()).get().getWishList().getGiftList();
            messagesToSend.add(menu.getMyWishListTemplate(gifts, chatId, messageId, inlineMessageId));
            List<BotUser> subscribers = storage.getUserSubscribers(updateSender);
            for (BotUser subscriber : subscribers) {
                if (subscriber.isReadyReceiveUpdates()) {
                    messagesToSend.add(new SendMessage(String.valueOf(subscriber.getTgAccountId()),
                        EXCLAMATION_ICON + "Пользователь @" + updateSender.getUserName() +
                            " добавил новый подарок в свой WishList"));
                }
            }
        }

        return messagesToSend;
    }

    private List<BotApiMethod> handleAddingGiftDescriptionRequest(Update update, BotUser updateSender) {
        List<BotApiMethod> messagesToSend = new ArrayList<>();
        String chatId = extractChatId(update);
        String messageText = update.getMessage().getText();
        String inlineMessageId = updateSender.getCarryingInlineMessageId();
        int messageId = updateSender.getCarryingMessageId();

        Optional<Gift> gift = storage.findGiftById(updateSender.getUpdateGiftId());
        if (gift.isPresent()) {
            Gift updatedGift = gift.get();
            updatedGift.setDescription(messageText);
            BotUser giftHolder = storage.findUserByTelegramId(updateSender.getTgAccountId()).get();
            boolean isDescriptionAdded = storage.updateGiftOfUser(updatedGift, giftHolder);
            giftHolder.setBotUserStatus(BotUserStatus.WITHOUT_STATUS);
            storage.updateUser(giftHolder);
            if (isDescriptionAdded) {
                messagesToSend.add(menu.getGiftRepresentationTemplate(updatedGift, chatId, messageId, inlineMessageId));
            } else {
                messagesToSend.add(menu.getErrorStatusTemplate("Описание подарка не изменено. Произошла ошибка", chatId));
            }
        } else {
            messagesToSend.add(menu.getErrorStatusTemplate("Описание подарка не изменено. Не смогли найти подарок", chatId));
        }
        return messagesToSend;
    }


    private List<BotApiMethod> handleAddingGiftURLRequest(Update update, BotUser updateSender) {
        List<BotApiMethod> messagesToSend = new ArrayList<>();
        String chatId = extractChatId(update);
        String messageText = update.getMessage().getText();
        String inlineMessageId = updateSender.getCarryingInlineMessageId();
        int messageId = updateSender.getCarryingMessageId();

        Optional<Gift> gift = storage.findGiftById(updateSender.getUpdateGiftId());
        if (gift.isPresent()) {
            Gift updatedGift = gift.get();
            updatedGift.setUrl(messageText);
            BotUser giftHolder = storage.findUserByTelegramId(updateSender.getTgAccountId()).get();
            boolean isDescriptionAdded = storage.updateGiftOfUser(updatedGift, giftHolder);
            giftHolder.setBotUserStatus(BotUserStatus.WITHOUT_STATUS);
            storage.updateUser(giftHolder);
            if (isDescriptionAdded) {
                messagesToSend.add(menu.getGiftRepresentationTemplate(updatedGift, chatId, messageId, inlineMessageId));
            } else {
                messagesToSend.add(menu.getErrorStatusTemplate("Ссылка подарка не изменена. Произошла ошибка", chatId));
            }
        } else {
            messagesToSend.add(menu.getErrorStatusTemplate("Ссылка подарка не изменена. Не смогли найти подарок", chatId));
        }
        return messagesToSend;
    }

    private List<BotApiMethod> handleWithoutStatusRequest(Update update, BotUser updateSender) {
        List<BotApiMethod> messagesToSend = new ArrayList<>();
        String chatId = extractChatId(update);
        String messageText = update.getMessage().getText();
        String messagePrefix = messageText.substring(0, 1);

        if (messageText.equals("/start")) {
            messagesToSend.add(menu.getGreetingTemplate(chatId, updateSender));
            //return messages to send?
        }

        if (messageText.equals("Главное меню")) {
            messagesToSend.add(menu.getMainMenuTemplate(chatId));
            //return messages to send?
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
        return storage.getUserSubscribers(requestReceiver).contains(requestSender);
    }
}
