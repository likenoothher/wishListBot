package com.aziarets.vividapp.service;

import com.aziarets.vividapp.dao.*;
import com.aziarets.vividapp.util.BotUserExtractor;
import com.aziarets.vividapp.exception.NotFoundUserNameException;
import com.aziarets.vividapp.exception.UserIsBotException;
import com.aziarets.vividapp.model.BotUser;
import com.aziarets.vividapp.model.Gift;
import com.aziarets.vividapp.model.WishList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Update;


import java.util.*;

@Service
@Transactional
public class BotService {
    private BotUserExtractor botUserExtractor;
    private BotUserDao userRepo;
    private WishListDao wishListDao;
    private GiftDao giftRepo;

    @Autowired
    public BotService(BotUserExtractor botUserExtractor, BotUserDaoImpl userRepo, WishListDaoImpl wishListRepo, GiftDao giftDao) {
        this.botUserExtractor = botUserExtractor;
        this.userRepo = userRepo;
        this.wishListDao = wishListRepo;
        this.giftRepo = giftDao;
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
        WishList wishList = wishListDao.getById(botUser.getWishList().getId());
        if (wishList != null) {
            giftRepo.save(gift);
            wishList.addGift(gift); //null check
            wishListDao.update(wishList);
            return true;
        }
        return false;
    }

    public boolean updateGift(Gift gift) {
        return giftRepo.update(gift);
    }

    public boolean deleteGift(long giftId) {
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

    public List<Gift> getUserWishListGifts(long userTelegramId) {
        List<Gift> gifts = giftRepo.getUserWishListPresents(userTelegramId);
        return gifts;
    }

    public List<Gift> getAvailableToDonateGifts(long userTelegramId) {
        List<Gift> gifts = giftRepo.getAvailableToDonatePresents(userTelegramId);
        return gifts;
    }

    private boolean addUser(BotUser user) {
        WishList wishList = new WishList();
        wishListDao.save(wishList);
        user.setWishList(wishList);
        return userRepo.save(user);
    }

    public boolean updateUser(BotUser user) {
        return userRepo.update(user);
    }

    public  Map<BotUser, Gift> getUserPresentsMap(BotUser user) {
        Map<BotUser, Gift> userPresentsMap = new HashMap<>();
        List<Gift> giftsUserDonates = giftRepo.getPresentsUserGoingDonate(user.getId());
        giftsUserDonates.stream()
            .forEach(gift -> {userPresentsMap.put(findGiftHolderByGiftId(gift.getId()).get(), gift);});

        return userPresentsMap;
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