package com.aziarets.vividapp.dao;

import com.aziarets.vividapp.model.BotUser;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.Query;
import java.util.List;

@Repository
public class BotUserDaoImpl implements BotUserDao {

    private SessionFactory factory;

    @Autowired
    public BotUserDaoImpl(SessionFactory factory) {
        this.factory = factory;
    }


    @Override
    public boolean save(BotUser botUser) {
        factory.getCurrentSession().saveOrUpdate(botUser);
        return botUser.getId() == 0 ? false : true;
    }

    @Override
    public boolean update(BotUser botUser) {
        factory.getCurrentSession().merge(botUser);
        return true;
    }

    @Override
    public BotUser getById(long id) {
        return factory.getCurrentSession().get(BotUser.class, id);
    }

    @Override
    public BotUser getByTelegramId(long telegramId) {
        Query query = factory.getCurrentSession().createQuery("select  botuser from BotUser botuser " +
            " left join fetch botuser.wishList \n" +
//            " left join fetch botuser.subscribers\n" +
//            " left join fetch botuser.subscriptions\n" +
            " where botuser.tgAccountId = :id")
            .setParameter("id", telegramId);
        BotUser user = (BotUser) query.getSingleResult();

        return  user;
    }

    @Override
    public BotUser getByUserName(String userName) {
        Query query = factory.getCurrentSession().createQuery("from BotUser where userName = :userName")
            .setParameter("userName", userName);
        try {
            BotUser user = (BotUser) query.getSingleResult();
            return user;
        }
        catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean isUserExist(long telegramId) {
        return !factory.getCurrentSession()
            .createQuery("from BotUser where tgAccountId =" + telegramId)
            .list()
            .isEmpty();
    }

    @Override
    public BotUser findGiftHolderByGiftId(long giftId) {
        Query query = factory.getCurrentSession().createQuery("select botuser from BotUser as botuser " +
            "left join botuser.wishList.giftList as gifts " +
            "where gifts.id = :giftId");
        query.setParameter("giftId", giftId);
        List<BotUser> users = query.getResultList();
        if (!users.isEmpty()) {
            return users.get(0);
        }
        return null;
    }

    @Override
    public List<BotUser> getUserSubscriptions(BotUser user) {
        Query query = factory.getCurrentSession().createQuery("select botuser from BotUser as botuser " +
            "join botuser.subscribers as subscribers " +
            "where subscribers.id = :id");
        query.setParameter("id", user.getId());
        List<BotUser> users = query.getResultList();
        return (List<BotUser>) users;
    }

    @Override
    public List<BotUser> getUserSubscribers(BotUser user) {
        Query query = factory.getCurrentSession().createQuery("select subscribers from BotUser as botuser\n" +
            "join botuser.subscribers as subscribers\n" +
            "where botuser.id = :id ");
        query.setParameter("id", user.getId());
        List<BotUser> users = query.getResultList();
        return (List<BotUser>) users;
    }
}
