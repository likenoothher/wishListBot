package com.aziarets.vividapp.dao;

import com.aziarets.vividapp.model.WishList;

public interface WishListDao {
    public long save(WishList wishList);

    public boolean update(WishList wishList);

    public WishList getById(long id);
}
