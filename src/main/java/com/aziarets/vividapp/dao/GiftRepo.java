package com.aziarets.vividapp.dao;

import com.aziarets.vividapp.model.BotUser;
import com.aziarets.vividapp.model.Gift;

import java.util.List;
import java.util.Map;

public interface GiftRepo {
    public long save(Gift gift);
    public boolean update(Gift gift);
    public boolean remove(long id);
    public Gift getById(long id);
    public List<Gift> getPresentsUserGoingDonate(long userTelegramId);
    public List<Gift> getAvailableToDonatePresents(long userTelegramId);
    public List<Gift> getUserWishListPresents(long userId);
}
