package com.aziarets.vividapp.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "wishlists")
public class WishList {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "wishlist_id")
    private List<Gift> giftList;

    public WishList() {
    }

    public WishList(int id) {
        this.id = id;
        this.giftList = new ArrayList<>();
    }

    public boolean addGift(Gift gift) {
        if (gift != null) {
            return giftList.add(gift);
        }
        return false;
    }

    public boolean updateGift(Gift gift) {
        Gift updatedGift = giftList.stream().filter(iteratedGift -> iteratedGift.getId() == gift.getId())
            .findAny()
            .orElse(null);
        if (updatedGift != null) {
            return (giftList.remove(updatedGift) && giftList.add(gift));
        }
        return false;
    }

    public boolean deleteGift(int id) {
        Gift giftForDelete = giftList.stream().filter(gift -> id == gift.getId())
            .findAny()
            .orElse(null);
        if (giftForDelete != null) {
            return giftList.remove(giftForDelete);
        }
        return false;
    }

    public boolean donate(int giftId, BotUser donor) {
        Gift giftForDonate = giftList.stream().filter(gift -> giftId == gift.getId())
            .findAny()
            .orElse(null);
        if (giftForDonate != null) {
            return giftForDonate.donate(donor);
        }
        return false;
    }

    public boolean refuseFromDonate(int giftId, BotUser donor) {
        Gift giftForDelete = giftList.stream().filter(gift -> giftId == gift.getId())
            .findAny()
            .orElse(null);
        if (giftForDelete != null) {
            return giftForDelete.refuseFromDonate(donor);
        }
        return false;
    }

    public boolean setFreeGiftOccupationFromDeletedSubscriber(BotUser subscriber) {
        if (subscriber != null) {
            giftList.stream().filter(iteratedGift -> subscriber.equals(iteratedGift.getOccupiedBy()))
                .forEach(giftOccupiedBySubscriber -> giftOccupiedBySubscriber.refuseFromDonate(subscriber));
            return true;
        }
        return false;
    }

    public Gift findGiftById(int id) {
        return giftList.stream().filter(gift -> id == gift.getId())
            .findAny()
            .orElse(null);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<Gift> getGiftList() {
        return Collections.unmodifiableList(giftList);
    }

    public void setGiftList(List<Gift> giftList) {
        this.giftList = giftList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WishList wishList = (WishList) o;
        return id == wishList.id &&
            Objects.equals(giftList, wishList.giftList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, giftList);
    }

//    @Override
//    public String toString() {
//        return "WishList{" +
//            "id=" + id +
//            ", giftList=" + giftList +
//            '}';
//    }

    public static final class WishListBuilder {
        private int id;
        private List<Gift> giftList;

        private WishListBuilder() {
        }

        public static WishListBuilder newWishList() {
            return new WishListBuilder();
        }

        public WishListBuilder withId(int id) {
            this.id = id;
            return this;
        }

        public WishList build() {
            return new WishList(id);
        }
    }
}
