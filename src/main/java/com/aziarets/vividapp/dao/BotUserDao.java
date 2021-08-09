package com.aziarets.vividapp.dao;

import com.aziarets.vividapp.model.BotUser;

import java.util.List;

public interface BotUserDao {
    public long save(BotUser botUser);

    public boolean update(BotUser botUser);

    public BotUser getById(long id);

    public BotUser getByTelegramId(long telegramId);

    public BotUser getByUserName(String userName);

    public boolean isUserExist(long telegramId);

    public BotUser findGiftHolderByGiftId(long giftId);

    public List<BotUser> getUserSubscriptions(long id);

    public List<BotUser> getUserSubscribers(long id);

    public boolean isUserSubscribedTo(long subscribedToId, long subscriberId);

    public boolean isUserEnabled(long userTelegramId);

}
