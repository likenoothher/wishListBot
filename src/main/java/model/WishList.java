package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WishList {
    private int id;
    private List<Gift> giftList;

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
            giftList.stream().filter(iteratedGift -> subscriber.equals(iteratedGift.occupiedBy()))
                    .forEach(giftOccupiedBySubscriber -> giftOccupiedBySubscriber.refuseFromDonate(subscriber));
            return true;
        }
        return false;
    }

    public List<Gift> getGiftList() {
        return Collections.unmodifiableList(giftList);
    }

    public Gift findGiftById(int id) {
        return giftList.stream().filter(gift -> id == gift.getId())
                .findAny()
                .orElse(null);
    }

    public int getId() {
        return id;
    }

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
