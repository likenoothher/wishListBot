package com.aziarets.vividapp.dao;

import com.aziarets.vividapp.model.BotUser;
import com.aziarets.vividapp.model.Gift;

import java.util.List;
import java.util.Optional;

public interface BotUserRepo {
    public boolean save(BotUser botUser);
    public boolean update(BotUser botUser);
    public BotUser getById(long id);
    public BotUser getByTelegramId(long telegramId);
    public BotUser getByUserName(String userName);
    public boolean isUserExist(long telegramId);
    public BotUser findGiftHolderByGiftId(long giftId);
    public List<BotUser> getUserSubscriptions(BotUser user);

}