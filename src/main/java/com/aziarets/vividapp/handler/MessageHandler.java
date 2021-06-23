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
    private String chatId;
    private String messageText;
    private List<BotApiMethod> messagesToSend = new ArrayList<>();

    @Autowired
    public MessageHandler(Storage storage, BotMenuTemplate menu) {
        this.storage = storage;
        this.menu = menu;
    }

    public List<BotApiMethod> handleMessage(Update update, BotUser updateSender) {
        chatId = getUpdateChatId(update);
        messageText = update.getMessage().getText();
        messagesToSend.clear();

        if (updateSender.getBotUserStatus().equals(BotUserStatus.WITHOUT_STATUS)) {
            handleWithoutStatusRequest(updateSender);
            return messagesToSend;
        }

        if (updateSender.getBotUserStatus().equals(BotUserStatus.ADDING_GIFT_NAME)) {
            handleAddingGiftNameRequest(updateSender);
            resetUserStatus(updateSender);
            return messagesToSend;
        }

        if (updateSender.getBotUserStatus().equals(BotUserStatus.ADDING_GIFT_DESCRIPTION)) {
            handleAddingGiftDescriptionRequest(updateSender);
            resetUserStatus(updateSender);
            return messagesToSend;
        }

        if (updateSender.getBotUserStatus().equals(BotUserStatus.ADDING_GIFT_URl)) {
            handleAddingGiftURLRequest(updateSender);
            resetUserStatus(updateSender);
            return messagesToSend;
        }

        if (updateSender.getBotUserStatus().equals(BotUserStatus.SEARCHING_FRIEND)) {
            handleSearchingFriendRequest(updateSender);
            resetUserStatus(updateSender);
            return messagesToSend;
        }

        if (updateSender.getBotUserStatus().equals(BotUserStatus.CONTACTING_DEVELOPER)) {
            handleContactDeveloperRequest(updateSender);
            resetUserStatus(updateSender);
            return messagesToSend;
        }

        SendMessage unknownCommandMessage = menu.getMainMenuTemplate(chatId);
        unknownCommandMessage.setText("Не знаю такой команды " + DISAPPOINTED_ICON + "\n" +
            "Попробуй ещё раз из главного меню" + HMM_ICON);

        messagesToSend.add(unknownCommandMessage);

        return messagesToSend;
    }

    private void handleSearchingFriendRequest(BotUser updateSender) {
        Optional<BotUser> searchedUser = storage.findUserByUserName(messageText);
        Optional<BotUser> userSearchedTo = storage.findUserByTelegramId(updateSender.getTgAccountId());

        if(!searchedUser.isPresent()) {
            messagesToSend.add(new SendMessage(chatId, "Мы не смогли найти данного пользователя"
                + DISAPPOINTED_ICON));
            return;
        }

        if (!searchedUser.equals(userSearchedTo)) {
            if (!isRequestedUserAlreadyFriend(userSearchedTo.get(), searchedUser.get())) {
                messagesToSend.add(menu.getFriendShipRequestToTemplate(searchedUser.get(), userSearchedTo.get()));
                messagesToSend.add(menu.getSendFriendshipRequestTemplate(chatId));
            } else {
                messagesToSend.add(new SendMessage(chatId, "Ты уже подписан на данного пользователя"
                    + UPSIDE_DOWN_FACE_ICON));
            }
        } else {
            messagesToSend.add(new SendMessage(chatId,
                "Ты не можешь подписаться сам на себя" + UPSIDE_DOWN_FACE_ICON));
        }
    }

    private void handleContactDeveloperRequest(BotUser updateSender) {
        messagesToSend.add(new SendMessage("988800148", "Сообщение от @" + updateSender.getUserName() +
            ": " + messageText));
        messagesToSend.add(new SendMessage(chatId, CHECK_MARK_ICON + " Твоё сообщение отправлено разработчику"));

    }

    private void handleAddingGiftNameRequest(BotUser updateSender) {
        String inlineMessageId = updateSender.getCarryingInlineMessageId();
        int messageId = updateSender.getCarryingMessageId();

        Gift gift = Gift.GiftBuilder.newGift().withName(messageText).build();

        if (storage.addGiftToUser(gift, updateSender)) {
            List<Gift> gifts = storage.findUserByTelegramId(updateSender.getTgAccountId()).get().getWishList().getGiftList();
            messagesToSend.add(menu.getMyWishListTemplate(gifts, chatId, messageId, inlineMessageId));
            addGiftAddedMessages(updateSender);
        }
    }

    private void handleAddingGiftDescriptionRequest(BotUser updateSender) {
        String inlineMessageId = updateSender.getCarryingInlineMessageId();
        int messageId = updateSender.getCarryingMessageId();

        Optional<Gift> gift = storage.findGiftById(updateSender.getUpdateGiftId());

        if (gift.isPresent()) {
            Gift updatedGift = gift.get();
            updatedGift.setDescription(messageText);

            if (storage.updateGiftOfUser(updatedGift, updateSender)) {
                messagesToSend.add(menu.getGiftRepresentationTemplate(updatedGift, chatId, messageId, inlineMessageId));
            } else {
                messagesToSend.add(menu.getErrorStatusTemplate("Описание подарка не изменено. Произошла ошибка",
                    chatId));
            }
        } else {
            messagesToSend.add(menu.getErrorStatusTemplate("Описание подарка не изменено. Не смогли найти подарок",
                chatId));
        }
    }


    private void handleAddingGiftURLRequest(BotUser updateSender) {
        String inlineMessageId = updateSender.getCarryingInlineMessageId();
        int messageId = updateSender.getCarryingMessageId();

        Optional<Gift> gift = storage.findGiftById(updateSender.getUpdateGiftId());

        if (gift.isPresent()) {
            Gift updatedGift = gift.get();
            updatedGift.setUrl(messageText);

            if (storage.updateGiftOfUser(updatedGift, updateSender)) {
                messagesToSend.add(menu.getGiftRepresentationTemplate(updatedGift, chatId, messageId, inlineMessageId));
            } else {
                messagesToSend.add(menu.getErrorStatusTemplate("Ссылка подарка не изменена. Произошла ошибка",
                    chatId));
            }
        } else {
            messagesToSend.add(menu.getErrorStatusTemplate("Ссылка подарка не изменена. Не смогли найти подарок",
                chatId));
        }
    }

    private void handleWithoutStatusRequest(BotUser updateSender) {
        if (messageText.equals("/start")) {
            messagesToSend.add(menu.getGreetingTemplate(chatId, updateSender));
            return;
        }

        if (messageText.equals("Главное меню")) {
            messagesToSend.add(menu.getMainMenuTemplate(chatId));
            return;
        }

        SendMessage unknownCommandMessage = menu.getMainMenuTemplate(chatId);
        unknownCommandMessage.setText("Не знаю такой команды " + DISAPPOINTED_ICON + "\n" +
            "Попробуй ещё раз из главного меню" + HMM_ICON);

        messagesToSend.add(unknownCommandMessage);
    }


    private String getUpdateChatId(Update update) {
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

    private void resetUserStatus(BotUser user) {
        user.setBotUserStatus(BotUserStatus.WITHOUT_STATUS);
        storage.updateUser(user);
    }

    private void addGiftAddedMessages(BotUser userAddGift) {
        List<BotUser> subscribers = storage.getUserSubscribers(userAddGift);

        for (BotUser subscriber : subscribers) {
            if (subscriber.isReadyReceiveUpdates()) {
                messagesToSend.add(new SendMessage(String.valueOf(subscriber.getTgAccountId()),
                    EXCLAMATION_ICON + "Пользователь @" + userAddGift.getUserName() +
                        " добавил новый подарок в свой WishList"));
            }
        }
    }
}
