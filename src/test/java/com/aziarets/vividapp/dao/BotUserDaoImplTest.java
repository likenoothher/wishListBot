package com.aziarets.vividapp.dao;

import com.aziarets.vividapp.VividApp;
import com.aziarets.vividapp.exception.IllegalOperationException;
import com.aziarets.vividapp.model.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.event.annotation.BeforeTestMethod;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = VividApp.class)
@TestPropertySource("classpath:application-test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BotUserDaoImplTest {

    @Autowired
    private BotUserDao botUserDao;

    @Autowired
    private WishListDao wishListDao;

    @Autowired
    private GiftDao giftDao;

    private BotUser botUser;
    private BotUser expected;

    @BeforeEach
    public void setUp() {
        botUser = BotUser.UserBuilder
            .newUser()
            .withFirstName("testFirstName")
            .withLastName("testLastName")
            .withUserName("testUserName")
            .withTgAccountId(1111)
            .isAllCanSeeMyWishList(false)
            .isAllCanSeeMyWishList(true)
            .withGiftLimit(3)
            .withUserRole(BotUserRole.USER)
            .withUserStatus(BotUserStatus.WITHOUT_STATUS)
            .isEnabled(true)
            .build();

        expected = BotUser.UserBuilder
            .newUser()
            .withFirstName("testFirstName")
            .withLastName("testLastName")
            .withUserName("testUserName")
            .withTgAccountId(1111)
            .isAllCanSeeMyWishList(false)
            .isAllCanSeeMyWishList(true)
            .withGiftLimit(3)
            .withUserRole(BotUserRole.USER)
            .withUserStatus(BotUserStatus.WITHOUT_STATUS)
            .isEnabled(true)
            .build();
        expected.setId(1);
    }

    @Test
    @Transactional
    public void whenSaveUser_thenGetByMethodsReturnSavedUser(){
        long id = botUserDao.save(botUser);

        assertEquals(1, id);

        BotUser found = botUserDao.getById(1);

        assertEquals(expected, found);

        found = botUserDao.getByUserName("testUserName");

        assertEquals(expected, found);

        int existedTelegramId = 1111;
        found = botUserDao.getByTelegramId(existedTelegramId);

        assertEquals(expected, found);
    }

    @Test
    @Transactional
    public void whenSaveNullUser_thenThrowNullPointerException(){
        assertThrows(NullPointerException.class, ()-> botUserDao.save(null));
    }

    @Test
    @Transactional
    public void whenUpdateUser_thenUserUpdated(){
        botUserDao.save(botUser);

        botUser.setFirstName("updated");

        botUserDao.update(botUser);

        BotUser found = botUserDao.getById(1);

        expected.setFirstName("updated");

        assertEquals(expected, found);
    }

    @Test
    @Transactional
    public void whenUpdateNullUser_thenThrowNullPointerException(){
        assertThrows(NullPointerException.class, ()-> botUserDao.update(null));
    }

    @Test
    @Transactional
    public void whenGetByMethodWithNotExistedParameter_thenReturnNull(){
        botUserDao.save(botUser);

        int notExistedUserId = 2;
        BotUser found = botUserDao.getById(notExistedUserId);

        assertEquals(null, found);

        found = botUserDao.getByUserName("notExistedName");

        assertEquals(null, found);

        int notExistedTelegramId = 123;
        found = botUserDao.getByTelegramId(notExistedTelegramId);

        assertEquals(null, found);
    }

    @Test
    @Transactional
    public void whenGetByNullUserName_thenReturnNull(){
        assertNull(botUserDao.getByUserName(null));
    }

    @Test
    @Transactional
    public void whenUserExistRequestAndUserExist_thenReturnTrue(){
        botUserDao.save(botUser);

        assertTrue(botUserDao.isUserExist(1111));
    }

    @Test
    @Transactional
    public void whenUserExistRequestAndUserIsNotExist_thenReturnFalse(){
        botUserDao.save(botUser);

        int notExistedTelegramId = 123;
        assertFalse(botUserDao.isUserExist(notExistedTelegramId));
    }

    @Test
    @Transactional
    public void whenFindGiftHolderRequestAndGiftExist_thenReturnUser(){
        Gift gift = Gift.GiftBuilder.newGift().withName("gift").build();
        WishList wishList = WishList.WishListBuilder.newWishList().build();

        botUser.setWishList(wishList);
        botUserDao.save(botUser);
        giftDao.save(gift);
        wishList.addGift(gift);

        wishListDao.update(wishList);

        BotUser giftHolder = botUserDao.findGiftHolderByGiftId(1);

        assertEquals(expected, giftHolder);
    }

    @Test
    @Transactional
    public void whenFindGiftHolderRequestAndGiftIsNotExist_thenReturnNull(){
        Gift gift = Gift.GiftBuilder.newGift().withName("gift").build();
        WishList wishList = WishList.WishListBuilder.newWishList().build();

        botUser.setWishList(wishList);
        botUserDao.save(botUser);
        giftDao.save(gift);
        wishList.addGift(gift);

        wishListDao.update(wishList);

        int notExistedGiftId = 123;
        BotUser giftHolder = botUserDao.findGiftHolderByGiftId(notExistedGiftId);

        assertEquals(null, giftHolder);
    }

    @Test
    @Transactional
    public void whenFindSubscriptionRequestAndSubscriptionExist_thenReturnListOfSubscriptions(){
       BotUser subscription = BotUser.UserBuilder
           .newUser()
           .withFirstName("testFirstName1")
           .withLastName("testLastName1")
           .withUserName("testUserName1")
           .withTgAccountId(2222)
           .isAllCanSeeMyWishList(false)
           .isAllCanSeeMyWishList(true)
           .withGiftLimit(3)
           .withUserRole(BotUserRole.USER)
           .withUserStatus(BotUserStatus.WITHOUT_STATUS)
           .isEnabled(true)
           .build();

       botUserDao.save(subscription);

       botUser.setSubscriptions(Set.of(subscription));

       botUserDao.save(botUser);

       List<BotUser> expected = botUserDao.getUserSubscriptions(2);

       subscription.setId(1);

       assertEquals(expected, List.of(subscription));

    }

    @Test
    @Transactional
    public void whenFindSubscriberRequestAndSubscriptionExist_thenReturnListOfSubscribers(){
        BotUser subscriber = BotUser.UserBuilder
            .newUser()
            .withFirstName("testFirstName1")
            .withLastName("testLastName1")
            .withUserName("testUserName1")
            .withTgAccountId(2222)
            .isAllCanSeeMyWishList(false)
            .isAllCanSeeMyWishList(true)
            .withGiftLimit(3)
            .withUserRole(BotUserRole.USER)
            .withUserStatus(BotUserStatus.WITHOUT_STATUS)
            .isEnabled(true)
            .build();

        botUserDao.save(subscriber);

        botUser.setSubscribers(List.of(subscriber));

        botUserDao.save(botUser);

        List<BotUser> expected = botUserDao.getUserSubscribers(2);

        subscriber.setId(1);

        assertEquals(expected, List.of(subscriber));

    }

    @Test
    @Transactional
    public void whenFindIsUserSubscribedToRequestAndUserIsSubscribed_thenReturnTrue(){
        BotUser subscriber = BotUser.UserBuilder
            .newUser()
            .withFirstName("testFirstName1")
            .withLastName("testLastName1")
            .withUserName("testUserName1")
            .withTgAccountId(2222)
            .isAllCanSeeMyWishList(false)
            .isAllCanSeeMyWishList(true)
            .withGiftLimit(3)
            .withUserRole(BotUserRole.USER)
            .withUserStatus(BotUserStatus.WITHOUT_STATUS)
            .isEnabled(true)
            .build();

        botUserDao.save(subscriber);

        botUser.setSubscribers(List.of(subscriber));

        botUserDao.save(botUser);

        assertTrue(botUserDao.isUserSubscribedTo(2,1));

    }

    @Test
    @Transactional
    public void whenFindIsUserSubscribedToRequestAndUserIsNotSubscribed_thenReturnFalse(){
        BotUser subscriber = BotUser.UserBuilder
            .newUser()
            .withFirstName("testFirstName1")
            .withLastName("testLastName1")
            .withUserName("testUserName1")
            .withTgAccountId(2222)
            .isAllCanSeeMyWishList(false)
            .isAllCanSeeMyWishList(true)
            .withGiftLimit(3)
            .withUserRole(BotUserRole.USER)
            .withUserStatus(BotUserStatus.WITHOUT_STATUS)
            .isEnabled(true)
            .build();

        botUserDao.save(subscriber);

        botUserDao.save(botUser);

        assertFalse(botUserDao.isUserSubscribedTo(1,2));

    }

    @Test
    @Transactional
    public void whenIsUserEnabledRequestAndUserIsEnabled_thenReturnTrue(){

        botUserDao.save(botUser);

        assertTrue(botUserDao.isUserEnabled(1111));

    }

    @Test
    @Transactional
    public void whenIsUserEnabledRequestAndUserIsNotEnabled_thenReturnFalse(){
        botUser.setEnabled(false);

        botUserDao.save(botUser);

        assertFalse(botUserDao.isUserEnabled(1111));

    }

    @Test
    @Transactional
    public void whenIsUserEnabledRequestAndUserTgIdDoesNotExist_thenReturnFalse(){
        assertFalse(botUserDao.isUserEnabled(1234));

    }

}
