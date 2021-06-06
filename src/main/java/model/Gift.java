package model;

import java.net.URL;

public class Gift {
    private static int currentId = 1;

    private int id;
    private String name;
    private URL url;
    private BotUser occupiedBy;
    private int price;
    private String description;

    private Gift(int id, String name, URL url, BotUser occupiedBy, int price, String description) {
        this.id = currentId;
        currentId++;
        this.name = name;
        this.url = url;
        this.occupiedBy = occupiedBy;
        this.price = price;
        this.description = description;
    }

    public boolean donate(BotUser donor) {
        if (donor != null && occupiedBy == null) {
            occupiedBy = donor;
            return true;
        }
        return false;
    }

    public boolean refuseFromDonate(BotUser donor) {
        if (donor != null && occupiedBy.equals(donor)) {
            occupiedBy = null;
            return true;
        }
        return false;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public URL getUrl() {
        return url;
    }

    public BotUser occupiedBy() {
        return occupiedBy;
    }

    public int getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "Gift{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", url=" + url +
                ", occupiedBy=" + occupiedBy +
                ", price=" + price +
                ", description='" + description + '\'' +
                '}';
    }

    public static final class GiftBuilder {
        private int id;
        private String name;
        private URL url;
        private BotUser occupiedBy;
        private int price;
        private String description;

        private GiftBuilder() {
        }

        public static GiftBuilder newGift() {
            return new GiftBuilder();
        }

        public GiftBuilder withId(int id) {
            this.id = currentId;
            currentId++;
            return this;
        }

        public GiftBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public GiftBuilder withUrl(URL url) {
            this.url = url;
            return this;
        }

        public GiftBuilder withIsOccupied(BotUser occupiedBy) {
            this.occupiedBy = occupiedBy;
            return this;
        }

        public GiftBuilder withPrice(int price) {
            this.price = price;
            return this;
        }

        public GiftBuilder withDescription(String description) {
            this.description = description;
            return this;
        }

        public Gift build() {
            return new Gift(id, name, url, occupiedBy, price, description);
        }
    }
}
