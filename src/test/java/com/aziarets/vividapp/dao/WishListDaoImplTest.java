package com.aziarets.vividapp.dao;

import com.aziarets.vividapp.VividApp;
import com.aziarets.vividapp.model.Gift;
import com.aziarets.vividapp.model.WishList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = VividApp.class)
@TestPropertySource("classpath:application-test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class WishListDaoImplTest {

    @Autowired
    private WishListDao wishListDao;

    @Autowired
    private GiftDao giftDao;

    private WishList wishList;
    private Gift gift;

    private WishList expectedWishList;
    private Gift expectedGift;

    @BeforeEach
    @Transactional
    public void setUp() {
        gift = Gift.GiftBuilder.newGift().withName("gift").build();
        wishList = WishList.WishListBuilder.newWishList().build();

        expectedWishList = WishList.WishListBuilder.newWishList().build();
        expectedWishList.setId(1);
        expectedGift = Gift.GiftBuilder.newGift().withName("gift").build();
        expectedGift.setId(1);
        expectedWishList.addGift(expectedGift);

    }

    @Test
    @Transactional
    void whenSaveWishList_thenGetByIdMethodReturnSavedWishList() {
        giftDao.save(gift);
        wishList.addGift(gift);

        wishListDao.save(wishList);

        WishList found = wishListDao.getById(1);

        assertEquals(expectedWishList, found);
    }

    @Test
    @Transactional
    void whenUpdateWishList_thenGetByIdMethodReturnUpdatedWishList() {
        giftDao.save(gift);
        wishList.addGift(gift);

        long id = wishListDao.save(wishList);

        assertEquals(1, id);

        Gift giftForUpdate = Gift.GiftBuilder.newGift().withName("giftForUpdate").build();

        wishList.addGift(giftForUpdate);

        wishListDao.update(wishList);

        giftForUpdate.setId(2);

        expectedWishList.addGift(giftForUpdate);

        WishList found = wishListDao.getById(1);

        assertEquals(expectedWishList, found);

    }

    @Test
    @Transactional
    public void whenSaveNullWishList_thenThrowNullPointerException(){
        assertThrows(NullPointerException.class, ()-> wishListDao.update(null));
    }

    @Test
    @Transactional
    public void whenUpdateNullWishList_thenThrowNullPointerException(){
        assertThrows(NullPointerException.class, ()-> wishListDao.update(null));
    }

    @Test
    @Transactional
    public void whenGetByIdAndWishListDoesNotExist_thenReturnNull(){
        assertNull(wishListDao.getById(1));
    }

}
