package com.aziarets.vividapp.dao;

import com.aziarets.vividapp.model.BotUser;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.Query;
import java.util.List;
import java.util.Optional;

@Repository
public class BotUserRepoImpl implements BotUserRepo {

    private SessionFactory factory;

    @Autowired
    public BotUserRepoImpl(SessionFactory factory) {
        this.factory = factory;
    }

    @Override
    public boolean save(BotUser botUser) {
        Transaction transaction = null;
        long id = 0;
        try (Session session = factory.openSession()) {
            transaction = session.beginTransaction();
            id = (Long)session.save(botUser);
            transaction.commit();
            session.close();
            if(id != 0) {
                return true;
            }
            return false;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean update(BotUser botUser) {
        Transaction transaction = null;
        try (Session session = factory.openSession()) {
            transaction = session.beginTransaction();
            session.merge(botUser);
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
    public BotUser getById(long id) {
        BotUser user = null;
        Transaction transaction = null;
        try (Session session = factory.openSession()) {
            transaction = session.beginTransaction();
            user = (BotUser) session.get(BotUser.class, id);
            transaction.commit();
            session.close();
            return user;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
        return user;
    }

    @Override
    public BotUser getByTelegramId(long telegramId) {
        Session session = factory.openSession();
        Query query = session.createQuery("from BotUser where tgAccountId = :id");
        query.setParameter("id", telegramId);
        List<BotUser> users = query.getResultList();
        if (!users.isEmpty()) {
            return users.get(0);
        }
        session.close();
        return null;
    }

    @Override
    public BotUser getByUserName(String userName) {
        Session session = factory.openSession();
        Query query = session.createQuery("from BotUser where userName = :userName");
        query.setParameter("userName", userName);
        List<BotUser> users = query.getResultList();
        if (!users.isEmpty()) {
            return users.get(0);
        }
        session.close();
        return null;
    }

    @Override
    public boolean isUserExist(long telegramId) { // переделать
        Session session = factory.openSession();
        Transaction transaction = session.beginTransaction();
        List<BotUser> users = session.createQuery("from BotUser where tgAccountId =" + telegramId).getResultList();
        transaction.commit();
        session.close();
        if (!users.isEmpty()) {
            return true;
        }
        return false;

    }

    @Override
    public BotUser findGiftHolderByGiftId(long giftId) {
        Session session = factory.openSession();
        Query query = session.createQuery("select botuser from BotUser as botuser " +
            "left join botuser.wishList.giftList as gifts " +
            "where gifts.id = :giftId");
        query.setParameter("giftId", giftId);
        List<BotUser> users = query.getResultList();
        if (!users.isEmpty()) {
            return users.get(0);
        }
        session.close();
        return null;
    }

    @Override
    public List<BotUser> getUserSubscriptions(BotUser user) {
        Session session = factory.openSession();
        Query query = session.createQuery("select botuser from BotUser as botuser " +
            "join botuser.subscribers as subscribers " +
            "where subscribers.id = :id");
        query.setParameter("id", user.getId());
        List<BotUser> users = query.getResultList();
        session.close();
        return users;
    }
}
