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
        return id == wishList.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "WishList{" +
            "id=" + id +
            ", giftList=" + giftList +
            '}';
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
