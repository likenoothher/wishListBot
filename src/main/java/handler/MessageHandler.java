package handler;

import data.Storage;
import menu.AppMenu;
import model.BotUser;
import model.BotUserStatus;
import model.Gift;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
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

    public List<BotApiMethod> handleMessage(Update update, BotUser updateSender) {
        List<BotApiMethod> messagesToSend = new ArrayList<>();
        String chatId = extractChatId(update);

        SendMessage unknownCommandMessage = menu.showMainMenu(chatId);
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

        if (searchedUser.isPresent()) {
            if (!isRequestedUserAlreadyFriend(searchedUser.get(), searchedUser.get())) {
                messagesToSend.add(menu.showFriendShipRequestTo(searchedUser.get(), userSearchedTo.get())); // message to request receiver
                messagesToSend.add(menu.showSendRequestMenu(chatId)); // message to request sender
            } else {
                messagesToSend.add(new SendMessage(chatId, "Ты уже подписан на данного пользователя"
                    + UPSIDE_DOWN_FACE_ICON));
            }
        } else {
            messagesToSend.add(new SendMessage(chatId,
                "Мы не нашли пользователя с таким именем " + DISAPPOINTED_ICON + " Но ты можешь его пригласить из главного меню!"));
        }

        BotUser updatedUser = userSearchedTo.get();
        updatedUser.setBotUserStatus(BotUserStatus.WITHOUT_STATUS);
        storage.updateUser(updatedUser);

        return messagesToSend;

    }

    private List<BotApiMethod> handleAddingGiftDescriptionRequest(Update update, BotUser updateSender) {
        List<BotApiMethod> messagesToSend = new ArrayList<>();
        String chatId = extractChatId(update);
        String messageText = update.getMessage().getText();

        Optional<Gift> gift = storage.findGiftById(updateSender.getUpdateGiftId());
        if (gift.isPresent()) {
            Gift updatedGift = gift.get();
            updatedGift.setDescription(messageText);
            BotUser giftHolder = storage.findUserByTelegramId(updateSender.getTgAccountId()).get();
            boolean isDescriptionAdded = storage.updateGiftOfUser(updatedGift, giftHolder);
            giftHolder.setBotUserStatus(BotUserStatus.WITHOUT_STATUS);
            storage.updateUser(giftHolder);
            if (isDescriptionAdded) {
                messagesToSend.add(menu.showOkStatusMenu("Описание подарка \"" + updatedGift.getName() + "\" изменено", chatId));
            } else {
                messagesToSend.add(menu.showErrorStatusMenu("Описание подарка не изменено. Произошла ошибка", chatId));
            }
        } else {
            messagesToSend.add(menu.showErrorStatusMenu("Описание подарка не изменено. Не смогли найти подарок", chatId));
        }
        return messagesToSend;
    }

    private List<BotApiMethod> handleAddingGiftNameRequest(Update update, BotUser updateSender) {
        List<BotApiMethod> messagesToSend = new ArrayList<>();
        String chatId = extractChatId(update);
        String messageText = update.getMessage().getText();

        Gift gift = Gift.GiftBuilder.newGift().withName(messageText).build();

        boolean isAdded = storage.addGiftToUser(gift, updateSender);
        updateSender.setBotUserStatus(BotUserStatus.WITHOUT_STATUS);
        storage.updateUser(updateSender);

        if (isAdded) {
            messagesToSend.add(menu.showCustomSingleInlineButton("Подарок добавлен в твой WishList. Добавить " +
                    "описание и ссылку на подарок, можно в  " + WISH_LIST_ICON + " \"Мой WishList \"->"
                    + MANAGING_ICON + " Редактирование подарков\"", "Редактирование подарков " + MANAGING_ICON,
                "/my_wish_list/manage_list", chatId));
//            messagesToSend.add(menu.showOkStatusMenu("Подарок добавлен в твой WishList. Добавить " +
//                    "описание и ссылку на подарок, можно в  " + WISH_LIST_ICON + " \"Мой WishList \"->"
//                    + MANAGING_ICON + " Редактирование подарков\"",
//                chatId));

            List<BotUser> subscribers = storage.findUserByTelegramId(updateSender.getTgAccountId()).get().getSubscribers();
            for (BotUser subscriber : subscribers) {
                if (subscriber.isReadyReceiveUpdated()) {
                    messagesToSend.add(new SendMessage(String.valueOf(subscriber.getTgChatId()),
                        EXCLAMATION_ICON + "Пользователь @" + updateSender.getUserName() +
                            " добавил новый подарок в свой WishList"));
                }
            }
        }

        return messagesToSend;
    }

    private List<BotApiMethod> handleAddingGiftURLRequest(Update update, BotUser updateSender) {
        List<BotApiMethod> messagesToSend = new ArrayList<>();
        String chatId = extractChatId(update);
        String messageText = update.getMessage().getText();

        Optional<Gift> gift = storage.findGiftById(updateSender.getUpdateGiftId());
        if (gift.isPresent()) {
            Gift updatedGift = gift.get();
            updatedGift.setUrl(messageText);
            BotUser giftHolder = storage.findUserByTelegramId(updateSender.getTgAccountId()).get();
            boolean isDescriptionAdded = storage.updateGiftOfUser(updatedGift, giftHolder);
            giftHolder.setBotUserStatus(BotUserStatus.WITHOUT_STATUS);
            storage.updateUser(giftHolder);
            if (isDescriptionAdded) {
                messagesToSend.add(menu.showOkStatusMenu("Ссылка подарка \"" + updatedGift.getName() + "\" изменена", chatId));
            } else {
                messagesToSend.add(menu.showErrorStatusMenu("Ссылка подарка не изменена. Произошла ошибка", chatId));
            }
        } else {
            messagesToSend.add(menu.showErrorStatusMenu("Ссылка подарка не изменена. Не смогли найти подарок", chatId));
        }
        return messagesToSend;
    }

    private List<BotApiMethod> handleWithoutStatusRequest(Update update, BotUser updateSender) {
        List<BotApiMethod> messagesToSend = new ArrayList<>();
        String chatId = extractChatId(update);
        String messageText = update.getMessage().getText();
        String messagePrefix = messageText.substring(0, 1);

        if (messageText.equals("/start")) {
            messagesToSend.add(menu.showGreetingMenu(chatId));
            //return messages to send?
        }

        if (messageText.equals("Главное меню")) {
            messagesToSend.add(menu.showMainMenu(chatId));
            //return messages to send?
        }

//        if (messagePrefix.equals("@")) {
//            if (messageText.substring(1).equals(updateSender.getUserName())) {
//                messagesToSend.addAll(List.of(new SendMessage(chatId,
//                    "Извини, я не могу добавить тебя в друзья к самому себе" + MAN_SHRUGGING_ICON)));
//            } else {
//                messagesToSend.addAll(handleAddSubscriberPrefixMessage(messageText, updateSender, chatId));
//            }
//        }
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
