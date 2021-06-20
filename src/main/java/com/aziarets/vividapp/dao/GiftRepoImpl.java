package com.aziarets.vividapp.dao;

import com.aziarets.vividapp.model.BotUser;
import com.aziarets.vividapp.model.Gift;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.Query;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Repository
public class GiftRepoImpl implements GiftRepo{

    private SessionFactory factory;

    @Autowired
    public GiftRepoImpl(SessionFactory factory) {
        this.factory = factory;
    }

    @Override
    public long save(Gift gift) {
        factory.getCurrentSession().saveOrUpdate(gift);
        return gift.getId();
    }

    @Override
    public boolean update(Gift gift) {
        factory.getCurrentSession().saveOrUpdate(gift);
        return gift.getId() == 0 ? false : true;
    }

    @Override
    public boolean remove(long id) {
        factory.getCurrentSession().remove(factory.getCurrentSession().get(Gift.class, id));
        return true; // переделать
    }

    @Override
    public Gift getById(long id) {
        return factory.getCurrentSession().get(Gift.class, id);
    }

    @Override
    public List<Gift> getPresentsUserGoingDonate(long userId) {
        Query query = factory.getCurrentSession().createQuery("from Gift where occupiedBy.id = :id");
        query.setParameter("id", userId);
        return query.getResultList();

    }

    @Override
    public List<Gift> getAvailableToDonatePresents(long userTelegramId) {
        Query query = factory.getCurrentSession().createQuery("from Gift where occupiedBy.id = null ");
        return query.getResultList();
    }

    @Override
    public List<Gift> getUserWishListPresents(long tgAccountId) {
        try {
            Query query = factory.getCurrentSession().createQuery("select botuser.wishList.giftList from BotUser botuser\n" +
                "where botuser.tgAccountId = :tgAccountId")
                .setParameter("tgAccountId", tgAccountId);

            List<Gift> gifts = query.getResultList();
            return gifts;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }
}
