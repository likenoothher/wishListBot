package com.aziarets.vividapp.data;

import com.aziarets.vividapp.dao.*;
import com.aziarets.vividapp.model.BotUser;
import com.aziarets.vividapp.model.Gift;
import com.aziarets.vividapp.model.WishList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;


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
        BotUser user = userRepo.getByUserName(userName);
        if (user != null) {
            return  Optional.of(user);
        }
        return Optional.empty();
    }

    public Optional<BotUser> findUserByTelegramId(long telegramId) {
        BotUser user = userRepo.getByTelegramId(telegramId);
        return Optional.of(user);
    }

    public Optional<Gift> findGiftById(long id) {

        return Optional.of(giftRepo.getById(id));
    }

    public Optional<BotUser> findGiftHolderByGiftId(long id) {
        return Optional.of(userRepo.findGiftHolderByGiftId(id));
    }

    public boolean addSubscriberToSubscriptions(BotUser subscriber, BotUser subscribedTo) {
        if (subscriber != null && subscribedTo != null) {
            BotUser us = findUserByTelegramId(subscriber.getTgAccountId()).get();
            BotUser usTo = findUserByTelegramId(subscribedTo.getTgAccountId()).get();
            usTo.getSubscribers().add(us);
            return userRepo.update(usTo);
        }
        return false;
    }

    public boolean removeSubscriberFromSubscriptions(BotUser subscriber, BotUser subscribedTo) {
        if (subscriber != null && subscribedTo != null) {
            List<Gift> subscribedToGifts = getUserWishListGifts(subscribedTo.getTgAccountId());
            for(Gift gift: subscribedToGifts) {
                if(subscriber.equals(gift.getOccupiedBy())) {
                    gift.setOccupiedBy(null);
                    giftRepo.update(gift);
                }
            }
            BotUser us = findUserByTelegramId(subscriber.getTgAccountId()).get();
            BotUser usTo = findUserByTelegramId(subscribedTo.getTgAccountId()).get();
            List<BotUser> subscribers = getUserSubscribers(usTo);
            subscribers.remove(us);
            usTo.setSubscribers(subscribers);
            return userRepo.update(usTo);
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

    public List<BotUser> getUserSubscribers(BotUser user) {
        if (user != null) {
            return userRepo.getUserSubscribers(user);
        }
        return Collections.emptyList();
    }

    public List<Gift> getUserWishListGifts(long userId) {
        List<Gift> gifts = giftRepo.getUserWishListPresents(userId);
        return gifts;
    }

    public List<Gift> getAvailableToDonateGifts(long userId) {
        List<Gift> gifts = giftRepo.getAvailableToDonatePresents(userId);
        return gifts;
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

    private boolean isUserSigned(BotUser user) {
        return userRepo.isUserExist(user.getTgAccountId());
    }

}
