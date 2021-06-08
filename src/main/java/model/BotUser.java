package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class BotUser {
    private static int currentId = 1;
    private long id;
    private String userName;
    private String firstName;
    private String lastName;
    private long tgAccountId;
    private long tgChatId;
    private String phoneNumber;
    private WishList wishList;
    private List<BotUser> subscribers;
    private boolean isReadyReceiveUpdates;
    private boolean isAllCanSeeMyWishList;
    private BotUserStatus botUserStatus;
    private int updateGiftId;

    private BotUser(long id, String userName, String firstName, String lastName, long tgAccountId,
                    long thChatId, String phoneNumber) {
        this.id = currentId;
        currentId++;
        this.userName = userName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.tgAccountId = tgAccountId;
        this.tgChatId = thChatId;
        this.phoneNumber = phoneNumber;
        this.wishList = WishList.WishListBuilder.newWishList().build();
        this.subscribers = new ArrayList<>();
        this.isReadyReceiveUpdates = true;
        this.isAllCanSeeMyWishList = false;
        this.botUserStatus = BotUserStatus.WITHOUT_STATUS;
        this.updateGiftId = -1;

    }

    public boolean addGift(Gift gift) {
        if (gift != null) {
            return wishList.addGift(gift);
        }
        return false;
    }

    public boolean updateGift(Gift gift) {
        if (gift != null) {
            return wishList.updateGift(gift);
        }
        return false;
    }


    public boolean deleteGift(int id) {
        return wishList.deleteGift(id);
    }

    public boolean donate(int giftId, BotUser donor) {
        if (donor != null) {
            return wishList.donate(giftId, donor);
        }
        return false;
    }

    public boolean refuseFromDonate(int giftId, BotUser donor) {
        if (donor != null) {
            return wishList.refuseFromDonate(giftId, donor);
        }
        return false;
    }

    public Gift findGiftById(int id) {
        return wishList.findGiftById(id);
    }


    public boolean addSubscriber(BotUser subscriber) {
        if (!subscribers.contains(subscriber)) {
            return subscribers.add(subscriber);
        }
        return false;
    }

    public boolean deleteSubscriber(BotUser subscriber) {
        if (subscribers.contains(subscriber)) {
            return (subscribers.remove(subscriber) && setFreeGiftOccupationFromDeletedSubscriber(subscriber));
        }
        return false;
    }

    private boolean setFreeGiftOccupationFromDeletedSubscriber(BotUser subscriber) {
        if (subscriber != null) {
            return this.wishList.setFreeGiftOccupationFromDeletedSubscriber(subscriber);
        }
        return false;
    }

    public List<Gift> findAvailableToDonatePresents() {
        return wishList.getGiftList().stream().filter(iteratedGift -> iteratedGift.occupiedBy() == null)
            .collect(Collectors.toList());
    }

    public long getId() {
        return id;
    }

    public String getUserName() {
        return userName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public long getTgAccountId() {
        return tgAccountId;
    }

    public long getTgChatId() {
        return tgChatId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public WishList getWishList() {
        return wishList;
    }

    public List<BotUser> getSubscribers() {
        return Collections.unmodifiableList(subscribers);
    }

    public boolean isReadyReceiveUpdated() {
        return isReadyReceiveUpdates;
    }

    public void setReadyReceiveUpdates(boolean readyReceiveUpdates) {
        isReadyReceiveUpdates = readyReceiveUpdates;
    }

    public boolean isAllCanSeeMyWishList() {
        return isAllCanSeeMyWishList;
    }

    public void setAllCanSeeMyWishList(boolean allCanSeeMyWishList) {
        isAllCanSeeMyWishList = allCanSeeMyWishList;
    }

    public BotUserStatus getBotUserStatus() {
        return botUserStatus;
    }

    public void setBotUserStatus(BotUserStatus botUserStatus) {
        this.botUserStatus = botUserStatus;
    }

    public int getUpdateGiftId() {
        return updateGiftId;
    }

    public void setUpdateGiftId(int updateGiftId) {
        this.updateGiftId = updateGiftId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BotUser botUser = (BotUser) o;
        return id == botUser.id &&
            tgAccountId == botUser.tgAccountId &&
            tgChatId == botUser.tgChatId &&
            isReadyReceiveUpdates == botUser.isReadyReceiveUpdates &&
            Objects.equals(userName, botUser.userName) &&
            Objects.equals(firstName, botUser.firstName) &&
            Objects.equals(lastName, botUser.lastName) &&
            Objects.equals(phoneNumber, botUser.phoneNumber) &&
            Objects.equals(wishList, botUser.wishList) &&
            Objects.equals(subscribers, botUser.subscribers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userName, firstName, lastName, tgAccountId, tgChatId, phoneNumber,
            wishList, subscribers, isReadyReceiveUpdates);
    }

    @Override
    public String toString() {
        return "BotUser{" +
            "id=" + id +
            ", userName='" + userName + '\'' +
            ", firstName='" + firstName + '\'' +
            ", lastName='" + lastName + '\'' +
            ", tgAccountId=" + tgAccountId +
            ", tgChatId=" + tgChatId +
            ", phoneNumber='" + phoneNumber + '\'' +
            ", wishList=" + wishList +
            ", subscribers=" + subscribers +
            ", isReadyReceiveUpdates=" + isReadyReceiveUpdates +
            '}';
    }

    public static final class UserBuilder {
        private long id;
        private String userName;
        private String firstName;
        private String lastName;
        private long tgAccountId;
        private long tgChatId;
        private String phoneNumber;

        private UserBuilder() {
        }

        public static UserBuilder newUser() {
            return new UserBuilder();
        }

        public UserBuilder withId(long id) {
            this.id = id;
            return this;
        }

        public UserBuilder withUserName(String userName) {
            this.userName = userName;
            return this;
        }

        public UserBuilder withFirstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public UserBuilder withLastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public UserBuilder withTgAccountId(long tgAccountId) {
            this.tgAccountId = tgAccountId;
            return this;
        }

        public UserBuilder withTgChatId(long tgChatId) {
            this.tgChatId = tgChatId;
            return this;
        }

        public UserBuilder withPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }


        public BotUser build() {
            return new BotUser(id, userName, firstName, lastName, tgAccountId, tgChatId, phoneNumber);
        }
    }
}
