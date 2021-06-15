package com.aziarets.vividapp.model;

import org.telegram.telegrambots.meta.api.objects.Update;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
public class BotUser {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "tgAccountId")
    private long tgAccountId;

    @Column(name = "tgChatId")
    private long tgChatId;

    @OneToOne
    @JoinColumn(name = "wishlist_id")
    private WishList wishList;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "user_subscribers",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "subscriber_id"))
    private List<BotUser> subscribers;

    @ManyToMany(mappedBy = "subscribers")
    private List<BotUser> subscriptions;

    @Column(name = "update_ready")
    private boolean isReadyReceiveUpdates;

    @Column(name = "show_all_ready")
    private boolean isAllCanSeeMyWishList;

    @Column(name = "user_status")
    @Enumerated(EnumType.STRING)
    private BotUserStatus botUserStatus;

    @Column(name = "updated_gift_id")
    private int updateGiftId;

    @Column(name = "message_id")
    private int carryingMessageId;

    @Column(name = "inline_message_id")
    private String carryingInlineMessageId;

    public BotUser() {
    }

    public BotUser(String userName, String firstName, String lastName, long tgAccountId, long tgChatId, boolean isReadyReceiveUpdates, boolean isAllCanSeeMyWishList, BotUserStatus botUserStatus) {
        this.userName = userName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.tgAccountId = tgAccountId;
        this.tgChatId = tgChatId;
        this.isReadyReceiveUpdates = isReadyReceiveUpdates;
        this.isAllCanSeeMyWishList = isAllCanSeeMyWishList;
        this.botUserStatus = botUserStatus;
    }

    public BotUser(String userName, String firstName, String lastName, long tgAccountId, long tgChatId, WishList wishList, List<BotUser> subscribers, boolean isReadyReceiveUpdates, boolean isAllCanSeeMyWishList, BotUserStatus botUserStatus, int updateGiftId, int carryingMessageId, String carryingInlineMessageId) {
        this.userName = userName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.tgAccountId = tgAccountId;
        this.tgChatId = tgChatId;
        this.wishList = wishList;
        this.subscribers = subscribers;
        this.isReadyReceiveUpdates = isReadyReceiveUpdates;
        this.isAllCanSeeMyWishList = isAllCanSeeMyWishList;
        this.botUserStatus = botUserStatus;
        this.updateGiftId = updateGiftId;
        this.carryingMessageId = carryingMessageId;
        this.carryingInlineMessageId = carryingInlineMessageId;
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

    public boolean removeSubscriber(BotUser subscriber) {
        System.out.println("from remive sub -" + subscribers);
        System.out.println("from remive sub -" + subscriber.tString());
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
        return wishList.getGiftList().stream().filter(iteratedGift -> iteratedGift.getOccupiedBy() == null)
            .collect(Collectors.toList());
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public long getTgAccountId() {
        return tgAccountId;
    }

    public void setTgAccountId(long tgAccountId) {
        this.tgAccountId = tgAccountId;
    }

    public long getTgChatId() {
        return tgChatId;
    }

    public void setTgChatId(long tgChatId) {
        this.tgChatId = tgChatId;
    }

    public WishList getWishList() {
        return wishList;
    }

    public void setWishList(WishList wishList) {
        this.wishList = wishList;
    }

    public List<BotUser> getSubscribers() {
        return subscribers;
    }

    public void setSubscribers(List<BotUser> subscribers) {
        this.subscribers = subscribers;
    }

    public boolean isReadyReceiveUpdates() {
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

    public int getCarryingMessageId() {
        return carryingMessageId;
    }

    public void setCarryingMessageId(int carryingMessageId) {
        this.carryingMessageId = carryingMessageId;
    }

    public String getCarryingInlineMessageId() {
        return carryingInlineMessageId;
    }

    public void setCarryingInlineMessageId(String carryingInlineMessageId) {
        this.carryingInlineMessageId = carryingInlineMessageId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BotUser user = (BotUser) o;
        return id == user.id &&
            tgAccountId == user.tgAccountId &&
            tgChatId == user.tgChatId &&
            isReadyReceiveUpdates == user.isReadyReceiveUpdates &&
            isAllCanSeeMyWishList == user.isAllCanSeeMyWishList &&
            updateGiftId == user.updateGiftId &&
            carryingMessageId == user.carryingMessageId &&
            Objects.equals(userName, user.userName) &&
            Objects.equals(firstName, user.firstName) &&
            Objects.equals(lastName, user.lastName) &&
            botUserStatus == user.botUserStatus &&
            Objects.equals(carryingInlineMessageId, user.carryingInlineMessageId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userName, firstName, lastName, tgAccountId, tgChatId, isReadyReceiveUpdates, isAllCanSeeMyWishList, botUserStatus, updateGiftId, carryingMessageId, carryingInlineMessageId);
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
            ", wishList=" + wishList +
            ", isReadyReceiveUpdates=" + isReadyReceiveUpdates +
            ", isAllCanSeeMyWishList=" + isAllCanSeeMyWishList +
            ", botUserStatus=" + botUserStatus +
            ", updateGiftId=" + updateGiftId +
            ", carryingMessageId=" + carryingMessageId +
            ", carryingInlineMessageId='" + carryingInlineMessageId + '\'' +
            '}';
    }

    public String tString() {
        return "BotUser{" +
            "id=" + id +
            ", userName='" + userName + '\'' +
            ", firstName='" + firstName + '\'' +
            ", lastName='" + lastName + '\'' +
            ", tgAccountId=" + tgAccountId +
            ", tgChatId=" + tgChatId +
            ", wishList=" + wishList +
            ", subscribers=" + subscribers +
            ", isReadyReceiveUpdates=" + isReadyReceiveUpdates +
            ", isAllCanSeeMyWishList=" + isAllCanSeeMyWishList +
            ", botUserStatus=" + botUserStatus +
            ", updateGiftId=" + updateGiftId +
            ", carryingMessageId=" + carryingMessageId +
            ", carryingInlineMessageId='" + carryingInlineMessageId + '\'' +
            '}';
    }

    public static final class UserBuilder {
        private long id;
        private String userName;
        private String firstName;
        private String lastName;
        private long tgAccountId;
        private long tgChatId;
        private boolean isReadyReceiveUpdates;
        private boolean isAllCanSeeMyWishList;
        private BotUserStatus botUserStatus;

        private UserBuilder() {
        }

        public static UserBuilder newUser() {
            return new UserBuilder();
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

        public UserBuilder isReadyReceiveUpdates(boolean isReady) {
            this.isReadyReceiveUpdates = isReady;
            return this;
        }

        public UserBuilder isAllCanSeeMyWishList(boolean isAllCanSeeMyWishList) {
            this.isAllCanSeeMyWishList = isAllCanSeeMyWishList;
            return this;
        }

        public UserBuilder withUserStatus(BotUserStatus botUserStatus) {
            this.botUserStatus = botUserStatus;
            return this;
        }


        public BotUser build() {
            return new BotUser(userName, firstName, lastName, tgAccountId, tgChatId, isReadyReceiveUpdates, isAllCanSeeMyWishList, botUserStatus);
        }


    }
}