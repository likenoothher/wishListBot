package com.aziarets.vividapp.dao;

import com.aziarets.vividapp.model.BotUser;
import com.aziarets.vividapp.model.Gift;
import com.aziarets.vividapp.model.WishList;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Repository
public class WishListRepoImpl implements WishListRepo{
    private SessionFactory factory;

    @Autowired
    public WishListRepoImpl(SessionFactory factory) {
        this.factory = factory;
    }

    @Override
    public long save(WishList wishList) {
        Transaction transaction = null;
        long id = 0;
        try (Session session = factory.openSession()) {
            transaction = session.beginTransaction();
            id = (Long)session.save(wishList);
            transaction.commit();
            session.close();
            return id;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
        return id;
    }

    @Override
    public boolean update(WishList wishList) {
        Transaction transaction = null;
        try (Session session = factory.openSession()) {
            transaction = session.beginTransaction();
            WishList updatedWishList = getById(wishList.getId());
            session.merge(wishList);
            transaction.commit();
            session.close();
            return true;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public WishList getById(long id) {
        Session session = factory.openSession();
        Transaction transaction = session.beginTransaction();
        WishList wishList = (WishList) session.get(WishList.class, id);
        transaction.commit();

        if (wishList != null) {
            return wishList;
        }
        session.close();
        return null;
    }
}
