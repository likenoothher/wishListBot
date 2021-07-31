package com.aziarets.vividapp.service;

import com.aziarets.vividapp.dao.*;
import com.aziarets.vividapp.exception.GiftsLimitReachedException;
import com.aziarets.vividapp.exception.UserIsDisabled;
import com.aziarets.vividapp.rest.dto.BotUserDTO;
import com.aziarets.vividapp.rest.dto.GiftDTO;
import com.aziarets.vividapp.rest.facade.BotUserFacade;
import com.aziarets.vividapp.rest.facade.GiftFacade;
import com.aziarets.vividapp.util.BotUserExtractor;
import com.aziarets.vividapp.exception.NotFoundUserNameException;
import com.aziarets.vividapp.exception.UserIsBotException;
import com.aziarets.vividapp.model.BotUser;
import com.aziarets.vividapp.model.Gift;
import com.aziarets.vividapp.model.WishList;
import com.aziarets.vividapp.util.PhotoManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;


import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class BotService {
    private static final Logger logger = LoggerFactory.getLogger(BotService.class);

    private BotUserExtractor botUserExtractor;
    private BotUserDao userDao;
    private WishListDao wishListDao;
    private GiftDao giftDao;
    private PhotoManager photoManager;
    private BotUserFacade botUserFacade;
    private GiftFacade giftFacade;

    @Autowired
    public BotService(BotUserExtractor botUserExtractor, BotUserDao userDao, WishListDao wishListDao,
                      GiftDao giftDao, PhotoManager photoManager, BotUserFacade botUserFacade, GiftFacade giftFacade) {
        this.botUserExtractor = botUserExtractor;
        this.userDao = userDao;
        this.wishListDao = wishListDao;
        this.giftDao = giftDao;
        this.photoManager = photoManager;
        this.botUserFacade = botUserFacade;
        this.giftFacade = giftFacade;
    }

    public BotUser identifyUser(Update update)
        throws NotFoundUserNameException, UserIsBotException, UserIsDisabled {
        logger.info("Identify user from update id:" + update.getUpdateId());
        Long updateSenderId = botUserExtractor.getUpdateSenderId(update);
        if (isUserSigned(updateSenderId)) {
            if (!isUserEnabled(updateSenderId)) {
                throw new UserIsDisabled("Пользователь с id" + updateSenderId + " заблокирован");
            }
            logger.info("Returning familiar user from update id:" + update.getUpdateId() + ", user id: "
                + updateSenderId);
            return userDao.getByTelegramId(updateSenderId);
        } else {
            logger.info("Handle new user from update id:" + update.getUpdateId() + ", user id: "
                + updateSenderId);
            BotUser currentUpdateUser = botUserExtractor.identifyUser(update);
            addUser(currentUpdateUser);
            return currentUpdateUser;
        }
    }

    public boolean addGiftToUser(Gift gift, BotUser botUser) {
        logger.info("Add gift with id " + gift.getId() + " to user with id:" + botUser.getId());
        WishList wishList = wishListDao.getById(botUser.getWishList().getId());
        if (wishList != null) {
            giftDao.save(gift);
            wishList.addGift(gift);
            wishListDao.update(wishList);
            logger.info("Gift with id " + gift.getId() + " added to user with id:" + botUser.getId());
            return true;
        }
        logger.info("Gift with id " + gift.getId() + " wasn't added to user with id:" + botUser.getId());
        return false;
    }

    public boolean updateGift(Gift gift) {
        logger.info("Update gift with id: " + gift.getId());
        return giftDao.update(gift);
    }

    public boolean deleteGift(long giftId) {
        logger.info("Deleting gift with id: " + giftId);
        Gift gift = giftDao.getById(giftId);
        photoManager.deletePhoto(gift);
        return giftDao.remove(giftId);
    }

    public boolean assignPhotoURLToGift(Gift gift, List<PhotoSize> photoSizes) {
        logger.info("Assigning photo url to gift with id: " + gift.getId());
        if (gift.getGiftPhotoURL() != null && gift.getGiftPhotoCloudinaryId() != null) {
            logger.info("Deleting existed photo of gift with id " + gift.getId());
            photoManager.deletePhoto(gift);
        }
        return photoManager.assignGiftPhotoParameters(gift, photoSizes);
    }

    public boolean assignPhotoURLToGift(Gift gift, MultipartFile file) {
        logger.info("Assigning photo url to gift with id: " + gift.getId());
        if (gift.getGiftPhotoURL() != null && gift.getGiftPhotoCloudinaryId() != null) {
            logger.info("Deleting existed photo of gift with id " + gift.getId());
            photoManager.deletePhoto(gift);
        }
        return photoManager.assignGiftPhotoParameters(gift, file);
    }

    public boolean deleteGiftPhoto(Gift gift) {
        logger.info("Deleting photo of gift with id " + gift.getId());
        if (gift.getGiftPhotoURL() != null && gift.getGiftPhotoCloudinaryId() != null) {
            logger.info("Deleting existed photo of gift with id " + gift.getId());
            photoManager.deletePhoto(gift);
            return true;
        }
        return false;
    }

    public Optional<BotUser> findUserById(long id) {
        logger.info("Searching user with id: " + id);
        BotUser user = userDao.getById(id);
        if (user != null) {
            logger.info("Returning user with id: " + id);
            return Optional.of(user);
        }
        logger.warn("User with id " + id + " not found. Returning Optional.empty()");
        return Optional.empty();
    }

    public Optional<BotUser> findUserByUserName(String userName) {
        logger.info("Searching user with user name: " + userName);
        BotUser user = userDao.getByUserName(userName);
        if (user != null) {
            logger.info("Returning user with user name: " + userName);
            return Optional.of(user);
        }
        logger.warn("User with user name " + userName + " not found. Returning Optional.empty()");
        return Optional.empty();
    }

    public Optional<BotUser> findUserByTelegramId(long telegramId) {
        logger.info("Searching user with telegram id: " + telegramId);
        BotUser user = userDao.getByTelegramId(telegramId);
        if (user != null) {
            logger.info("Returning user with telegram id: " + telegramId);
            return Optional.of(user);
        }
        logger.warn("User with telegram id " + telegramId + " not found. Returning Optional.empty()");
        return Optional.empty();
    }

    public Optional<Gift> findGiftById(long id) {
        logger.info("Searching gift with id: " + id);
        Gift gift = giftDao.getById(id);
        if (gift != null) {
            logger.info("Returning gift with id: " + id);
            return Optional.of(gift);
        }
        logger.warn("Gift with id " + id + " not found. Returning Optional.empty()");
        return Optional.empty();
    }

    public Optional<BotUser> findGiftHolderByGiftId(long giftId) {
        logger.info("Searching gift holder by gift with id: " + giftId);
        BotUser user = userDao.findGiftHolderByGiftId(giftId);
        if (user != null) {
            logger.info("Returning gift holder by gift with id: " + giftId);
            return Optional.of(user);
        }
        logger.warn("Gift holder by gift with id " + giftId + " not found. Returning Optional.empty()");
        return Optional.empty();
    }

    public boolean addSubscriberToSubscriptions(BotUser subscriber, BotUser subscribedTo) {
        logger.info("Add subscriber with id: " + subscriber.getId() + " to user with id: "
            + subscribedTo.getId());
        if (subscriber != null && subscribedTo != null
            && !isUserSubscribedTo(subscribedTo.getId(), subscriber.getId())) {

            List<BotUser> subscribers = getUserSubscribers(subscribedTo);
            subscribers.add(subscriber);
            subscribedTo.setSubscribers(subscribers);
            return userDao.update(subscribedTo);
        }
        logger.warn("Couldn't add subscriber with id: " + subscriber.getId() + " to user with id:" +
            subscribedTo.getId() + " . Subscriber or/and subscribed to is null");
        return false;
    }

    public boolean removeSubscriberFromSubscriptions(BotUser subscriber, BotUser subscribedTo) {
        logger.info("Deleting subscriber with id: " + subscriber.getId() + " to user with id: "
            + subscribedTo.getId());
        if (subscriber != null && subscribedTo != null) {
            List<Gift> subscribedToGifts = getUserWishListGifts(subscribedTo.getId());
            for (Gift gift : subscribedToGifts) {
                clearGiftOccupiedFrom(subscriber, gift);
            }

            List<BotUser> subscribers = getUserSubscribers(subscribedTo);
            subscribers.remove(subscriber);
            subscribedTo.setSubscribers(subscribers);
            return userDao.update(subscribedTo);
        }
        logger.warn("Couldn't remove subscriber with id: " + subscriber.getId() + " to user with id:" +
            +subscribedTo.getId() + " . Subscriber or/and subscribed to is null");
        return false;
    }

    public boolean removeSubscriptionFromSubscriber(BotUser subscriber, BotUser subscribedTo) {
        return removeSubscriberFromSubscriptions(subscribedTo, subscriber);
    }

    public List<BotUser> getUserSubscriptions(BotUser user) {
        logger.info("Get subscriptions of user with id: " + user.getId());
        if (user != null) {
            return userDao.getUserSubscriptions(user.getId());
        }
        logger.warn("Couldn't get subscriptions of user with id: " + user.getId() + ". User is null");
        return Collections.emptyList();
    }

    public List<BotUser> getUserSubscribers(BotUser user) {
        logger.info("Get subscribers of user with id: " + user.getId());
        if (user != null) {
            return userDao.getUserSubscribers(user.getId());
        }
        logger.warn("Couldn't get subscribers of user with id: " + user.getId() + ". User is null");
        return Collections.emptyList();
    }

    public List<Gift> getUserWishListGifts(long userId) {
        logger.info("Get wishlist's gifts of user with id: " + userId);
        return giftDao.getUserWishListPresents(userId);
    }

    public List<Gift> getAvailableToDonateGifts(long userId) {
        logger.info("Get available to donate gifts of user with id: " + userId);
        return giftDao.getAvailableToDonatePresents(userId);
    }

    private boolean addUser(BotUser user) {
        logger.info("Add user with user name: " + user.getUserName());
        WishList wishList = new WishList();
        wishListDao.save(wishList);
        user.setWishList(wishList);
        return userDao.save(user);
    }

    public boolean updateUser(BotUser user) {
        logger.info("Update user with user name: " + user.getUserName());
        return userDao.update(user);
    }

    public Map<Gift, BotUser> getUserPresentsMap(BotUser user) {
        logger.info("Get user's present map of user with id: " + user.getId());
        Map<Gift, BotUser> userPresentsMap = new HashMap<>();
        List<Gift> giftsUserDonates = giftDao.getPresentsUserGoingDonate(user.getId());
        giftsUserDonates.stream()
            .forEach(gift -> {
                userPresentsMap.put(gift, findGiftHolderByGiftId(gift.getId()).get());
            });
        logger.info("return user's present map with id: " + user.getId());
        return userPresentsMap;
    }

    public boolean donate(long giftId, BotUser donor) throws GiftsLimitReachedException {
        logger.info("Handle donate request from user with id: " + donor.getId() + " for gift with id: "
            + giftId);
        BotUser giftHolder = userDao.findGiftHolderByGiftId(giftId);
        if (isDonorReachedGiftLimit(donor, giftHolder)) {
            throw new GiftsLimitReachedException("Пользователь c id " + donor.getId() + " пытается превысить лимит" +
                " подарков, уставновленный пользователем с id " + giftHolder.getId());
        }
        Gift gift = giftDao.getById(giftId);
        if (gift != null && gift.getOccupiedBy() == null && donor != null) {
            gift.setOccupiedBy(donor);
            return giftDao.update(gift);
        }
        logger.info("Couldn't handle donate request from user with id: " + donor.getId() + " for gift with id: "
            + giftId + " Gift or/and donor is null, or gift was already occupied");
        return false;
    }

    public boolean isDonorReachedGiftLimit(BotUser donor, BotUser donatedTo) {
        int giftsLimit = donatedTo.getGiftLimit();
        List<Gift> gifts = giftDao.getPresentsDonorGoingDonateToUser(donor.getId(), donatedTo.getId());
        if (gifts.size() < giftsLimit) {
            return false;
        }
        return true;
    }

    public boolean refuseFromDonate(long giftId, BotUser donor) {
        logger.info("Handle refuse from donate request from user with id: " + donor.getId()
            + " for gift with id: " + giftId);
        Gift gift = giftDao.getById(giftId);
        if (gift != null && donor != null && gift.getOccupiedBy().equals(donor)) {
            gift.setOccupiedBy(null);
            return giftDao.update(gift);
        }
        logger.info("Couldn't handle refuse from donate request from user with id: "
            + donor.getId() + " for gift with id: "
            + giftId + " Gift or/and donor is null, or gift wasn't occupied by user with id: " + donor.getId());
        return false;
    }

    public boolean isUserSubscribedTo(long subscribedToId, long subscriberId) {
        return userDao.isUserSubscribedTo(subscribedToId, subscriberId);
    }

    public boolean isUserEnabled(long userTelegramId) {
        return userDao.isUserEnabled(userTelegramId);
    }

    public Map<GiftDTO, BotUserDTO> convertIPresentMapToDTO(Map<Gift, BotUser> userGifts) {
        logger.info("Starting converting I present map to DTO");
        Map<GiftDTO, BotUserDTO> userGiftsDTO = new HashMap<>();
        userGifts.entrySet()
            .stream()
            .forEach(entry -> {
                GiftDTO giftDTO = giftFacade.giftToGiftDTO(entry.getKey());
                BotUserDTO userDTO = botUserFacade.botUserToBotUserDTO(entry.getValue());
                userGiftsDTO.put(giftDTO, userDTO);
            });
        logger.info("Returning converted I present map");
        return userGiftsDTO;
    }

    public List<GiftDTO> convertGiftListToDTO(List<Gift> gifts) {
        logger.info("Starting converting gift list to DTO");
        return gifts
            .stream()
            .map(gift -> {
                return giftFacade.giftToGiftDTO(gift);
            })
            .collect(Collectors.toList());
    }

    public List<BotUserDTO> convertBotUserListToDTO(List<BotUser> users) {
        logger.info("Starting converting bot user list to DTO");
        return users
            .stream()
            .map(user -> {
                return botUserFacade.botUserToBotUserDTO(user);
            })
            .collect(Collectors.toList());
    }

    private void clearGiftOccupiedFrom(BotUser subscriber, Gift gift) {
        if (subscriber.equals(gift.getOccupiedBy())) {
            gift.setOccupiedBy(null);
            giftDao.update(gift);
        }
    }

    private boolean isUserSigned(long userTgAccountId) {
        return userDao.isUserExist(userTgAccountId);
    }

}
