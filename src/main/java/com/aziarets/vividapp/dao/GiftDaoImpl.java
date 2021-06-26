package com.aziarets.vividapp.dao;

import com.aziarets.vividapp.model.Gift;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.Query;
import java.util.Collections;
import java.util.List;

@Repository
public class GiftDaoImpl implements GiftDao {

    private SessionFactory factory;

    @Autowired
    public GiftDaoImpl(SessionFactory factory) {
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
    public List<Gift> getUserWishListPresents(long userTelegramId) {
        try {
            Query query = factory.getCurrentSession().createQuery("select botuser.wishList.giftList from BotUser botuser\n" +
                "where botuser.tgAccountId = :userTelegramId")
                .setParameter("userTelegramId", userTelegramId);

            List<Gift> gifts = query.getResultList();
            return gifts;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }
}
