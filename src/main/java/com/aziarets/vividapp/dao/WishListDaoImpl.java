package com.aziarets.vividapp.dao;

import com.aziarets.vividapp.model.WishList;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.Query;


@Repository
public class WishListDaoImpl implements WishListDao {
    private SessionFactory factory;

    @Autowired
    public WishListDaoImpl(SessionFactory factory) {
        this.factory = factory;
    }

    @Override
    public long save(WishList wishList) {
        factory.getCurrentSession().saveOrUpdate(wishList);
        return wishList.getId();
    }

    @Override
    public boolean update(WishList wishList) {
        factory.getCurrentSession().saveOrUpdate(wishList);
        return true;
    }

    @Override
    public WishList getById(long id) {
        Query query = factory.getCurrentSession().createQuery("select wishlist from WishList wishlist " +
            " left join fetch wishlist.giftList \n" +
            " where wishlist.id = :id")
            .setParameter("id", id);
        WishList wishList = (WishList) query.getSingleResult();

        return  wishList;
//        return factory.getCurrentSession().get(WishList.class, id);
    }
}
