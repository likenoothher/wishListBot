package com.aziarets.vividapp.dao;

import com.aziarets.vividapp.model.BotUser;
import com.aziarets.vividapp.model.Gift;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.Query;
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
        Transaction transaction = null;
        long id = 0;
        try (Session session = factory.openSession()) {
            transaction = session.beginTransaction();
            id = (Long)session.save(gift);
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
    public boolean update(Gift gift) {
        Transaction transaction = null;
        try (Session session = factory.openSession()) {
            transaction = session.beginTransaction();
            session.merge(gift);
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
    public boolean remove(long id) {
        Transaction transaction = null;
        try (Session session = factory.openSession()) {
            transaction = session.beginTransaction();
            Gift deletedGift = getById(id);
            session.remove(deletedGift);
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
    public Gift getById(long id) {
        Gift gift = null;
        Transaction transaction = null;
        try (Session session = factory.openSession()) {
            transaction = session.beginTransaction();
            gift = (Gift) session.get(Gift.class, id);
            transaction.commit();
            session.close();
            return gift;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
        return gift;
    }

    @Override
    public List<Gift> getPresentsUserGoingDonate(long userId) {
        Session session = factory.openSession();
        Query query = session.createQuery("from Gift where occupiedBy.id = :id");
        query.setParameter("id", userId);
        List<Gift> gifts = query.getResultList();
        session.close();

        return gifts;
    }

    @Override
    public List<Gift> findAvailableToDonatePresents(long userTelegramId) {
        Session session = factory.openSession();
        Query query = session.createQuery("from Gift where occupiedBy.id = null ");
        List<Gift> gifts = query.getResultList();
        session.close();

        return gifts;
    }
}
