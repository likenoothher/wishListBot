package data;

import model.BotUser;
import model.Gift;
import model.WishList;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.*;
import java.util.stream.Collectors;

public class Storage {
    private BotUserExtractor botUserExtractor = new BotUserExtractor();
    private List<BotUser> users = new ArrayList<>();


    public synchronized BotUser identifyUser(Update update) throws NotFoundUserNameException, UserIsBotException {
        BotUser currentUpdateUser = botUserExtractor.identifyUser(update);

        if (isUserSigned(currentUpdateUser)) {
            return users.stream().filter(iteratedUser ->
                currentUpdateUser.getTgAccountId() == iteratedUser.getTgAccountId())
                .findAny()
                .orElse(currentUpdateUser);
        } else {
            addUser(currentUpdateUser);
            return currentUpdateUser;
        }
    }

    public boolean addGiftToUser(Gift gift, BotUser botUser) {
        if (gift != null && botUser != null) {
            int index = users.indexOf(botUser);
            if (index == -1) {
                return false;
            }
            return users.get(index).addGift(gift);
        }
        return false;
    }

    public boolean updateGiftOfUser(Gift gift, BotUser botUser) {
        if (gift != null && botUser != null) {
            int index = users.indexOf(botUser);
            if (index == -1) {
                return false;
            }
            return users.get(index).updateGift(gift);
        }
        return false;
    }

    public boolean deleteGiftOfUser(int id, BotUser botUser) {
        if (botUser != null) {
            int index = users.indexOf(botUser);
            if (index == -1) {
                return false;
            }
            return users.get(index).deleteGift(id);
        }
        return false;
    }

    public Optional<BotUser> findUserByUserName(String userName) {
        return users.stream().filter(iteratedUser -> userName.equals(iteratedUser.getUserName()))
            .findAny();
    }

    public Optional<BotUser> findUserByTelegramId(long id) {
        return users.stream().filter(iteratedUser -> id == iteratedUser.getTgAccountId())
            .findAny();
    }

    public Optional<Gift> findGiftById(int id) {
        BotUser giftHolder = users.stream().filter(iteratedBotUser -> iteratedBotUser.findGiftById(id) != null)
            .findAny()
            .orElse(null);
        if (giftHolder != null) {
            return Optional.of(giftHolder.findGiftById(id));
        }
        return Optional.empty();
    }

    public boolean addSubscriberToSubscriptions(BotUser subscriber, BotUser subscribedTo) {
        if (subscriber != null && subscribedTo != null) {
            int indexOfSubscribedTo = users.indexOf(subscribedTo);
            if (indexOfSubscribedTo != -1) {
                return users.get(indexOfSubscribedTo).addSubscriber(subscriber);
            }
        }
        return false;
    }

    public boolean removeSubscriberFromSubscriptions(BotUser subscriber, BotUser subscribedTo) {
        if (subscriber != null && subscribedTo != null) {
            int indexOfSubscribedTo = users.indexOf(subscribedTo);
            if (indexOfSubscribedTo != -1) {
                System.out.println("was here storage true");
                return users.get(indexOfSubscribedTo).deleteSubscriber(subscriber);
            }
        }
        System.out.println("was here storage false");
        return false;
    }

    public boolean removeSubscriptionFromSubscriber(BotUser subscriber, BotUser subscribedTo) {
        return removeSubscriberFromSubscriptions(subscribedTo, subscriber);
    }

    public List<BotUser> getUserSubscriptions(BotUser user) {
        if (user != null) {
            return users.stream()
                .filter(iteratedUser -> iteratedUser.getSubscribers().contains(user))
                .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private boolean addUser(BotUser user) {
        if (!users.contains(user)) {
            return users.add(user);
        }
        return false;
    }

    public boolean updateUser(BotUser user) {
        BotUser updatedUser = users.stream().filter(iteratedUser -> user.getId() == iteratedUser.getId())
            .findAny()
            .orElse(null);
        if (updatedUser != null) {
            return (users.remove(updatedUser) && users.add(user));
        }
        return false;
    }

    public List<Gift> getUserPresentsList(BotUser user) {
        if (user != null) {
            return users.stream()
                .map(BotUser::getWishList)
                .map(WishList::getGiftList)
                .flatMap(Collection::stream)
                .filter(gift -> user.equals(gift.occupiedBy()))
                .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public boolean donate(int giftId, BotUser donor) {
        BotUser giftHolder = users.stream()
            .filter(iteratedUser -> iteratedUser.findGiftById(giftId) != null)
            .findAny()
            .orElse(null);
        if (giftHolder != null) {
            return giftHolder.donate(giftId, donor);
        }
        return false;
    }

    public boolean refuseFromDonate(int giftId, BotUser donor) {
        BotUser giftHolder = users.stream()
            .filter(iteratedUser -> iteratedUser.findGiftById(giftId) != null)
            .findAny()
            .orElse(null);
        if (giftHolder != null) {
            return giftHolder.refuseFromDonate(giftId, donor);
        }
        return false;
    }

    public List<Gift> findAvailableToDonatePresents(BotUser donateTo) {
        if (users.contains(donateTo)) {
            int userIndex = users.indexOf(donateTo);
            return users.get(userIndex).findAvailableToDonatePresents();
        }
        return Collections.emptyList();
    }

    private boolean isUserSigned(BotUser user) {
        return users.stream().anyMatch(iteratedUser -> user.getTgAccountId() == iteratedUser.getTgAccountId());
    }

    private BotUser createUserFromUpdateInfo(Update update) {
        User gotFrom = extractUserInfoFromUpdate(update);
        return BotUser.UserBuilder.newUser()
            .withTgAccountId(gotFrom.getId())
            .withTgChatId(extractChatIdFromUpdate(update))
            .withFirstName(gotFrom.getFirstName())
            .withLastName(gotFrom.getLastName())
            .withUserName(gotFrom.getUserName())
            .build();
    }

    private User extractUserInfoFromUpdate(Update update) { // описаны не все типы ответа! доделать
        UpdateType updateType = getUpdateType(update);
        if (updateType.equals(UpdateType.CALLBACK)) {
            return update.getCallbackQuery().getFrom();
        }
        if (updateType.equals(UpdateType.MESSAGE)) {
            return update.getMessage().getFrom();
        }
        if (updateType.equals(UpdateType.EDITED_MESSAGE)) {
            return update.getEditedMessage().getFrom();
        }
        return update.getInlineQuery().getFrom();
    }

    private long extractChatIdFromUpdate(Update update) { // описаны не все типы ответа! доделать
        UpdateType updateType = getUpdateType(update);
        if (updateType.equals(UpdateType.CALLBACK)) {
            return update.getCallbackQuery().getMessage().getChatId();
        }
        if (updateType.equals(UpdateType.MESSAGE)) {
            return update.getMessage().getChatId();
        }
        return update.getEditedMessage().getChatId();
    }

    private UpdateType getUpdateType(Update update) {
        if (update.hasMessage()) return UpdateType.MESSAGE;
        if (update.hasCallbackQuery()) return UpdateType.CALLBACK;
        if (update.hasEditedMessage()) return UpdateType.EDITED_MESSAGE;
        return UpdateType.INLINE_QUERY;
    }

    public List<BotUser> getUsers() {
        return Collections.unmodifiableList(users);
    }
}
