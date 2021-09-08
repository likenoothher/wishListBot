package com.aziarets.vividapp.dao;

import com.aziarets.vividapp.VividApp;
import com.aziarets.vividapp.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = VividApp.class)
@TestPropertySource("classpath:application-test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class GiftDaoImplTest {

    @Autowired
    private BotUserDao botUserDao;

    @Autowired
    private WishListDao wishListDao;

    @Autowired
    private GiftDao giftDao;

    private BotUser botUser1;
    private BotUser botUser2;

    private WishList wishList1;
    private Gift gift1;
    private WishList wishList2;
    private Gift gift2;

    private WishList expectedWishList;
    private Gift expectedGift;

    @BeforeEach
    public void setUp() {
        botUser1 = BotUser.UserBuilder
            .newUser()
            .withFirstName("testFirstName1")
            .withLastName("testLastName1")
            .withUserName("testUserName1")
            .withTgAccountId(1111)
            .isAllCanSeeMyWishList(false)
            .isAllCanSeeMyWishList(true)
            .withGiftLimit(3)
            .withUserRole(BotUserRole.USER)
            .withUserStatus(BotUserStatus.WITHOUT_STATUS)
            .isEnabled(true)
            .build();

        botUser2 = BotUser.UserBuilder
            .newUser()
            .withFirstName("testFirstName2")
            .withLastName("testLastName2")
            .withUserName("testUserName2")
            .withTgAccountId(2222)
            .isAllCanSeeMyWishList(false)
            .isAllCanSeeMyWishList(true)
            .withGiftLimit(3)
            .withUserRole(BotUserRole.USER)
            .withUserStatus(BotUserStatus.WITHOUT_STATUS)
            .isEnabled(true)
            .build();

        gift1 = Gift.GiftBuilder.newGift().withName("gift1").build();
        wishList1 = WishList.WishListBuilder.newWishList().build();

        gift2 = Gift.GiftBuilder.newGift().withName("gift2").build();
        wishList2 = WishList.WishListBuilder.newWishList().build();

        expectedGift = Gift.GiftBuilder.newGift().withName("gift1").build();
        expectedGift.setId(1);

    }

    @Test
    @Transactional
    void whenSaveGift_thenGetByIdMethodReturnSavedGift(){
        long id = giftDao.save(gift1);

        assertEquals(1, id);

        Gift found = giftDao.getById(1);

        assertEquals(expectedGift, found);
    }

    @Test
    @Transactional
    public void whenSaveNullGift_thenThrowNullPointerException(){
        assertThrows(NullPointerException.class, ()-> giftDao.save(null));
    }

    @Test
    @Transactional
    void whenUpdateGift_thenGetByIdMethodReturnUpdatedGift(){
        long id = giftDao.save(gift1);

        assertEquals(1, id);

        gift1.setDescription("updated");

        giftDao.update(gift1);

        Gift found = giftDao.getById(1);

        expectedGift.setDescription("updated");

        assertEquals(expectedGift, found);
    }

    @Test
    @Transactional
    public void whenUpdateNullGift_thenThrowNullPointerException(){
        assertThrows(NullPointerException.class, ()-> giftDao.save(null));
    }

    @Test
    @Transactional
    void whenRemoveGift_thenGetByIdMethodReturnNull(){
        long id = giftDao.save(gift1);

        assertEquals(1, id);

        Gift found = giftDao.getById(1);

        assertEquals(expectedGift, found);

        giftDao.remove(1);

        found = giftDao.getById(1);

        assertNull(found);
    }

    @Test
    @Transactional
    public void whenRemoveGiftRequestWithNotExistedId_thenThrowNullPointerException(){
        assertThrows(IllegalArgumentException.class, ()-> giftDao.remove(1));
    }

    @Test
    @Transactional
    void whenGetByIdAndGiftDoesNotExist_thenReturnNull(){
        Gift found = giftDao.getById(1);

        assertEquals(null, found);
    }

    @Test
    @Transactional
    void whenGetPresentsUserGoingDonateRequest_thenReturnGiftList(){
        wishListDao.save(wishList1);
        botUser1.setWishList(wishList1);
        botUserDao.save(botUser1);

        gift1.setOccupiedBy(botUser1);
        gift2.setOccupiedBy(botUser1);

        wishList2.addGift(gift1);
        wishList2.addGift(gift2);

        botUser2.setWishList(wishList2);

        giftDao.save(gift1);
        giftDao.save(gift2);
        wishListDao.save(wishList2);

        botUserDao.save(botUser2);

        List<Gift> found = giftDao.getPresentsUserGoingDonate(1);
        List<Gift> expected = List.of(gift1, gift2);

        assertEquals(expected, found);
    }

    @Test
    @Transactional
    void whenGetAvailableToDonatePresentsRequest_thenReturnGiftList(){
        wishListDao.save(wishList1);
        botUser1.setWishList(wishList1);
        botUserDao.save(botUser1);

        gift1.setOccupiedBy(botUser1);

        wishList2.addGift(gift1);
        wishList2.addGift(gift2);

        botUser2.setWishList(wishList2);

        giftDao.save(gift1);
        giftDao.save(gift2);
        wishListDao.save(wishList2);

        botUserDao.save(botUser2);

        List<Gift> found = giftDao.getAvailableToDonatePresents(2);
        List<Gift> expected = List.of(gift2);

        assertEquals(expected, found);
    }

    @Test
    @Transactional
    void whenGetUserWishListPresentsRequest_thenReturnGiftList(){
        wishListDao.save(wishList1);
        botUser1.setWishList(wishList1);
        botUserDao.save(botUser1);

        gift1.setOccupiedBy(botUser1);

        wishList2.addGift(gift1);
        wishList2.addGift(gift2);

        botUser2.setWishList(wishList2);

        giftDao.save(gift1);
        giftDao.save(gift2);
        wishListDao.save(wishList2);

        botUserDao.save(botUser2);

        List<Gift> found = giftDao.getUserWishListPresents(2);
        List<Gift> expected = List.of(gift1, gift2);

        assertEquals(expected, found);
    }

    @Test
    @Transactional
    void whenGetPresentsDonorGoingDonateToUserRequest_thenReturnGiftList(){
        wishListDao.save(wishList1);
        botUser1.setWishList(wishList1);
        botUserDao.save(botUser1);

        gift1.setOccupiedBy(botUser1);

        wishList2.addGift(gift1);
        wishList2.addGift(gift2);

        botUser2.setWishList(wishList2);

        giftDao.save(gift1);
        giftDao.save(gift2);
        wishListDao.save(wishList2);

        botUserDao.save(botUser2);

        List<Gift> found = giftDao.getPresentsDonorGoingDonateToUser(1,2);
        List<Gift> expected = List.of(gift1);

        assertEquals(expected, found);
    }
}
