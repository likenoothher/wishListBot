package com.aziarets.vividapp.dao;

import com.aziarets.vividapp.model.Gift;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.Query;
import java.util.Collections;
import java.util.List;

@Repository
public class GiftDaoImpl implements GiftDao {
    private static final Logger logger = LoggerFactory.getLogger(GiftDaoImpl.class);

    private SessionFactory factory;

    @Autowired
    public GiftDaoImpl(SessionFactory factory) {
        this.factory = factory;
    }

    @Override
    public long save(Gift gift) {
        logger.info("Saving gift with gift name " + gift.getName());
        factory.getCurrentSession().saveOrUpdate(gift);
        logger.info("Saved gift with gift name " + gift.getName() + ", gift id " + gift.getId());
        return gift.getId();
    }

    @Override
    public boolean update(Gift gift) {
        logger.info("Updating gift with gift id " + gift.getId());
        factory.getCurrentSession().merge(gift);
        return gift.getId() == 0 ? false : true;
    }

    @Override
    public boolean remove(long id) {
        logger.info("Deleting gift with gift id " + id);
        factory.getCurrentSession().remove(factory.getCurrentSession().get(Gift.class, id));
        return true;
    }

    @Override
    public Gift getById(long id) {
        logger.info("Get gift by id " + id);
        return factory.getCurrentSession().get(Gift.class, id);
    }

    @Override
    public List<Gift> getPresentsUserGoingDonate(long userId) {
        logger.info("Get presents user going donate by user id " + userId);
        Query query = factory.getCurrentSession().createQuery("from Gift where occupiedBy.id = :id");
        query.setParameter("id", userId);
        logger.info("Returning result - presents user going donate by user id " + userId);
        return query.getResultList();

    }

    @Override
    public List<Gift> getAvailableToDonatePresents(long userId) {
        logger.info("Get presents available to donate of user with  id " + userId);
        Query query = factory.getCurrentSession().createQuery("select gifts from BotUser as botuser\n" +
            "            join botuser.wishList.giftList as gifts\n" +
            "            where gifts.occupiedBy =null and botuser.id = :userId ");
        query.setParameter("userId", userId);
        logger.info("Returning result -  presents available to donate of user with id "
            + userId);
        return query.getResultList();
    }

    @Override
    public List<Gift> getUserWishListPresents(long userId) {
        logger.info("Get wishlist of user with id " + userId);
        Query query = factory.getCurrentSession().createQuery("select botuser.wishList.giftList " +
            "from BotUser botuser\n" +
            "where botuser.id = :userId")
            .setParameter("userId", userId);
        logger.info("Returning result -  wishlist of user with id " + userId);
        return query.getResultList();
    }

    @Override
    public List<Gift> getPresentsDonorGoingDonateToUser(long donorId, long donatesTo) {
        logger.info("Searching is user with id " + donorId + " donates something to user with id " + donatesTo);
        Query query = factory.getCurrentSession().createQuery("select gifts from BotUser as botuser " +
            "            left join botuser.wishList.giftList as gifts " +
            "            where gifts.occupiedBy.id = :donorId and botuser.id = :donatesTo");
        query.setParameter("donorId", donorId);
        query.setParameter("donatesTo", donatesTo);
        List<Gift> gifts = query.getResultList();
        logger.info("Returning gifts user with id " + donorId + " donates to user with id " + donatesTo);
        return gifts;
    }
}
