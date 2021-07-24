package com.aziarets.vividapp.dao;

import com.aziarets.vividapp.handler.CallbackHandler;
import com.aziarets.vividapp.model.BotUser;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.List;

@Repository
public class BotUserDaoImpl implements BotUserDao {
    private static final Logger logger = LoggerFactory.getLogger(BotUserDaoImpl.class);

    private SessionFactory factory;

    @Autowired
    public BotUserDaoImpl(SessionFactory factory) {
        this.factory = factory;
    }


    @Override
    public boolean save(BotUser botUser) {
        logger.info("Saving user with user name " + botUser.getUserName());
        factory.getCurrentSession().saveOrUpdate(botUser);
        return botUser.getId() == 0 ? false : true;
    }

    @Override
    public boolean update(BotUser botUser) {
        logger.info("Updating user with user name " + botUser.getUserName());
        factory.getCurrentSession().merge(botUser);
        return true;
    }

    @Override
    public BotUser getById(long id) {
        logger.info("Get user by id: " + id);
        return factory.getCurrentSession().get(BotUser.class, id);
    }

    @Override
    public BotUser getByTelegramId(long telegramId) {
        logger.info("Get user by telegram id: " + telegramId);
        Query query = factory.getCurrentSession().createQuery("select  botuser from BotUser botuser " +
            " left join fetch botuser.wishList \n" +
//            " left join fetch botuser.subscribers\n" +
//            " left join fetch botuser.subscriptions\n" +
            " where botuser.tgAccountId = :id")
            .setParameter("id", telegramId);

        BotUser user = (BotUser) query.getSingleResult();
        logger.info("Returning result - user with telegram id: " + telegramId);
        return user;
    }

    @Override
    public BotUser getByUserName(String userName) {
        logger.info("Get user by user name : " + userName);
        Query query = factory.getCurrentSession().createQuery("from BotUser where userName = :userName")
            .setParameter("userName", userName);
        try {
            BotUser user = (BotUser) query.getSingleResult();
            logger.info("Returning result - user by user name: " + userName);
            return user;
        } catch (NoResultException e) {
            logger.warn("Exception during searching user with user name " + userName + ":" + e.getLocalizedMessage());
            return null;
        }

    }

    @Override
    public boolean isUserExist(long telegramId) {
        logger.info("Searching existence user by telegram id : " + telegramId);
        return !factory.getCurrentSession()
            .createQuery("from BotUser where tgAccountId =" + telegramId)
            .list()
            .isEmpty();
    }

    @Override
    public BotUser findGiftHolderByGiftId(long giftId) {
        logger.info("Searching gift holder by gift id : " + giftId);
        Query query = factory.getCurrentSession().createQuery("select botuser from BotUser as botuser " +
            "left join botuser.wishList.giftList as gifts " +
            "where gifts.id = :giftId");
        query.setParameter("giftId", giftId);
        List<BotUser> users = query.getResultList();
        if (!users.isEmpty()) {
            logger.info("Returning gift holder by gift id: " + giftId + ", found user - "
                + users.get(0).getUserName());
            return users.get(0);
        }
        logger.info("Returning null gift holder by gift id: " + giftId);
        return null;
    }

    @Override
    public List<BotUser> getUserSubscriptions(long id) {
        logger.info("Searching subscriptions by bot user id : " + id);
        Query query = factory.getCurrentSession().createQuery("select botuser from BotUser as botuser " +
            "join botuser.subscribers as subscribers " +
            "where subscribers.id = :id");
        query.setParameter("id", id);
        List<BotUser> users = query.getResultList();
        logger.info("Returning result of searching subscriptions by bot user id : " + id);
        return (List<BotUser>) users;
    }

    @Override
    public List<BotUser> getUserSubscribers(long id) {
        logger.info("Searching subscribers by bot user id : " + id);
        Query query = factory.getCurrentSession().createQuery("select subscribers from BotUser as botuser\n" +
            "join botuser.subscribers as subscribers\n" +
            "where botuser.id = :id ");
        query.setParameter("id", id);
        List<BotUser> users = query.getResultList();
        logger.info("BotUserDao returning result of searching subscribers by bot user id : " + id);
        return (List<BotUser>) users;
    }

    @Override
    public boolean isUserSubscribedTo(long subscribedToId, long subscriberId) {
        logger.info("Searching is user with id " + subscriberId + " subscribed to user with id : " + subscribedToId);
        Query query = factory.getCurrentSession().createQuery("select subscribers from BotUser as botuser\n" +
            "join botuser.subscribers as subscribers\n" +
            "where botuser.id = :subscribedToId and subscribers.id = :subscriberId");
        query.setParameter("subscribedToId", subscribedToId);
        query.setParameter("subscriberId", subscriberId);
        List<BotUser> users = query.getResultList();
        boolean isSubscribed = !users.isEmpty();
        logger.info("Returning result of searching is user with id "
            + subscriberId + " subscribed to user with id : " + subscribedToId + ". Result - " + isSubscribed);
        return isSubscribed;
    }

    @Override
    public boolean isUserEnabled(long userTelegramId) {
        logger.info("Searching is user account with id " + userTelegramId + " enabled");
        Query query = factory.getCurrentSession().createQuery("select botuser from BotUser as botuser\n" +
            "where botuser.tgAccountId = :userTelegramId and botuser.enabled = true");
        query.setParameter("userTelegramId", userTelegramId);
        boolean isEnabled = !query.getResultList().isEmpty();
        logger.info("Searching is user account with id " + userTelegramId + " enabled. Result - " + isEnabled);
        return isEnabled;
    }


}
