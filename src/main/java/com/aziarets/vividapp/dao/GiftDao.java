package com.aziarets.vividapp.dao;

import com.aziarets.vividapp.model.Gift;

import java.util.List;

public interface GiftDao {
    public long save(Gift gift);

    public boolean update(Gift gift);

    public boolean remove(long id);

    public Gift getById(long id);

    public List<Gift> getPresentsUserGoingDonate(long userId);

    public List<Gift> getAvailableToDonatePresents(long userId);

    public List<Gift> getUserWishListPresents(long userId);

    public List<Gift> getPresentsDonorGoingDonateToUser(long donorId, long donatesToId);
}
