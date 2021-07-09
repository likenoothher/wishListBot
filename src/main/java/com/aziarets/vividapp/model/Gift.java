package com.aziarets.vividapp.model;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "gifts")
public class Gift {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "gift_name")
    private String name;

    @Column(name = "gift_url")
    private String url;

    @OneToOne
    @JoinColumn(name = "occupied_by_id")
    private BotUser occupiedBy;

    @Column(name = "gift_description")
    private String description;

    @Column(name = "photo_id")
    private String giftPhotoTelegramId;

    private Gift(String name, String url, BotUser occupiedBy, String description) {
        this.name = name;
        this.url = url;
        this.occupiedBy = occupiedBy;
        this.description = description;
    }

    public Gift() {
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public BotUser getOccupiedBy() {
        return occupiedBy;
    }

    public void setOccupiedBy(BotUser occupiedBy) {
        this.occupiedBy = occupiedBy;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGiftPhotoTelegramId() {
        return giftPhotoTelegramId;
    }

    public void setGiftPhotoTelegramId(String giftPhotoTelegramId) {
        this.giftPhotoTelegramId = giftPhotoTelegramId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Gift gift = (Gift) o;
        return id == gift.id && Objects.equals(name, gift.name) && Objects.equals(url, gift.url) && Objects.equals(occupiedBy, gift.occupiedBy) && Objects.equals(description, gift.description) && Objects.equals(giftPhotoTelegramId, gift.giftPhotoTelegramId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, url, occupiedBy, description, giftPhotoTelegramId);
    }

    @Override
    public String toString() {
        return "Gift{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", url='" + url + '\'' +
            ", occupiedBy=" + occupiedBy +
            ", description='" + description + '\'' +
            ", giftPhotoTelegramId='" + giftPhotoTelegramId + '\'' +
            '}';
    }

    public static final class GiftBuilder {
        private int id;
        private String name;
        private String url;
        private BotUser occupiedBy;
        private String description;

        private GiftBuilder() {
        }

        public static GiftBuilder newGift() {
            return new GiftBuilder();
        }

        public GiftBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public GiftBuilder withUrl(String url) {
            this.url = url;
            return this;
        }

        public GiftBuilder withOccupiedBy(BotUser occupiedBy) {
            this.occupiedBy = occupiedBy;
            return this;
        }

        public GiftBuilder withDescription(String description) {
            this.description = description;
            return this;
        }

        public Gift build() {
            return new Gift(name, url, occupiedBy, description);
        }
    }
}
