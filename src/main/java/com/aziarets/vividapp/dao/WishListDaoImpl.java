package com.aziarets.vividapp.dao;

import com.aziarets.vividapp.model.Gift;
import com.aziarets.vividapp.model.WishList;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.Query;


@Repository
public class WishListDaoImpl implements WishListDao {
    private static final Logger logger = LoggerFactory.getLogger(WishListDaoImpl.class);

    private SessionFactory factory;

    @Autowired
    public WishListDaoImpl(SessionFactory factory) {
        this.factory = factory;
    }

    @Override
    public long save(WishList wishList) {
        logger.info("Save wishlist");
        factory.getCurrentSession().saveOrUpdate(wishList);
        logger.info("Saved wishlist with id: " + wishList.getId());
        return wishList.getId();
    }

    @Override
    public boolean update(WishList wishList) {
        logger.info("Update wishlist with id: " + wishList.getId());
        factory.getCurrentSession().saveOrUpdate(wishList);
        return true;
    }

    @Override
    public WishList getById(long id) {
        logger.info("Get wishlist by id: " + id);
        return factory.getCurrentSession().get(WishList.class, id);
    }
}
