package com.aziarets.vividapp.data;

import com.aziarets.vividapp.dao.*;
import com.aziarets.vividapp.model.BotUser;
import com.aziarets.vividapp.model.Gift;
import com.aziarets.vividapp.model.WishList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class Storage {
    private BotUserExtractor botUserExtractor;
    private BotUserRepo userRepo;
    private WishListRepo wishListRepo;
    private GiftRepo giftRepo;

    @Autowired
    public Storage(BotUserExtractor botUserExtractor, BotUserRepoImpl userRepo, WishListRepoImpl wishListRepo, GiftRepo giftRepo) {
        this.botUserExtractor = botUserExtractor;
        this.userRepo = userRepo;
        this.wishListRepo = wishListRepo;
        this.giftRepo = giftRepo;
    }

    public synchronized BotUser identifyUser(Update update) throws NotFoundUserNameException, UserIsBotException {
        BotUser currentUpdateUser = botUserExtractor.identifyUser(update);
        if (isUserSigned(currentUpdateUser)) {
            return userRepo.getByTelegramId(currentUpdateUser.getTgAccountId());
        } else {
            addUser(currentUpdateUser);
            return currentUpdateUser;
        }
    }

    public boolean addGiftToUser(Gift gift, BotUser botUser) {
        WishList wishList = wishListRepo.getById(botUser.getWishList().getId());
        if (wishList != null) {
            giftRepo.save(gift);
            wishList.addGift(gift); //null check
            wishListRepo.update(wishList);
            return true;
        }
        return false;
    }

    public boolean updateGiftOfUser(Gift gift, BotUser botUser) {
        return giftRepo.update(gift);
    }

    public boolean deleteGiftOfUser(int giftId, BotUser botUser) {
        return giftRepo.remove(giftId);
    }

    public Optional<BotUser> findUserByUserName(String userName) {
        return Optional.of(userRepo.getByUserName(userName));
    }

    public Optional<BotUser> findUserByTelegramId(long id) {
        return Optional.of(userRepo.getByTelegramId(id));
    }

    public Optional<Gift> findGiftById(long id) {
        return Optional.of(giftRepo.getById(id));
    }

    public Optional<BotUser> findGiftHolderByGiftId(long id) {
        return Optional.of(userRepo.findGiftHolderByGiftId(id));
    }

    public boolean addSubscriberToSubscriptions(BotUser subscriber, BotUser subscribedTo) {
        if (subscriber != null && subscribedTo != null) {
            subscribedTo.addSubscriber(subscriber);
            return userRepo.update(subscribedTo);
        }
        return false;
    }

    public boolean removeSubscriberFromSubscriptions(BotUser subscriber, BotUser subscribedTo) {
        if (subscriber != null && subscribedTo != null) {
            List<Gift> subscribedToGifts = subscribedTo.getWishList().getGiftList();
            for(Gift gift: subscribedToGifts) {
                if(gift.getOccupiedBy().equals(subscriber)) {
                    gift.setOccupiedBy(null);
                    giftRepo.update(gift);
                }
            }
            subscribedTo.removeSubscriber(subscriber);
            return userRepo.update(subscribedTo);
        }
        return false;
    }

    public boolean removeSubscriptionFromSubscriber(BotUser subscriber, BotUser subscribedTo) { //evict this method
        return removeSubscriberFromSubscriptions(subscribedTo, subscriber);
    }

    public List<BotUser> getUserSubscriptions(BotUser user) {
        if (user != null) {
            return userRepo.getUserSubscriptions(user);
        }
        return Collections.emptyList();
    }

    private boolean addUser(BotUser user) {
        WishList wishList = new WishList();
        wishListRepo.save(wishList);
        user.setWishList(wishList);
        return userRepo.save(user);
    }

    public boolean updateUser(BotUser user) {
        return userRepo.update(user);
    }

    public List<Gift> getUserPresentsMap(BotUser user) {
        return giftRepo.getPresentsUserGoingDonate(user.getId());
    }

    public boolean donate(long giftId, BotUser donor) {
        Gift gift = giftRepo.getById(giftId);
        if (gift != null && gift.getOccupiedBy() == null && donor !=null) {
            gift.setOccupiedBy(donor);
            return giftRepo.update(gift);
        }
        return false;
    }

    public boolean refuseFromDonate(int giftId, BotUser donor) { // почему то донор и occupied by не equals - разобраться
        Gift gift = giftRepo.getById(giftId);
        if (gift != null && donor !=null && gift.getOccupiedBy().getId() == (donor.getId())) {
            gift.setOccupiedBy(null);
            return giftRepo.update(gift);
        }
        return false;
    }

    public List<Gift> findAvailableToDonatePresents(BotUser donateTo) {
        return giftRepo.findAvailableToDonatePresents(donateTo.getTgAccountId());
    }

    private boolean isUserSigned(BotUser user) {
        return userRepo.isUserExist(user.getTgAccountId());
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
}
