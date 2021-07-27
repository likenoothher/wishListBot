CREATE TYPE status AS ENUM ('WITHOUT_STATUS', 'ADDING_GIFT_NAME', 'ADDING_GIFT_URl',  'ADDING_GIFT_DESCRIPTION',
    'SEARCHING_FRIEND', 'CONTACTING_DEVELOPER', 'SETTING_PASSWORD');

CREATE TYPE role AS ENUM ('ADMIN', 'USER');

CREATE TABLE IF NOT EXISTS wishlists
(
    id serial primary key not null
);

CREATE TABLE IF NOT EXISTS users
(
    id serial primary key not null,
    user_name varchar(255),
    first_name varchar(255),
    last_name varchar(255),
    password varchar (255),
    tgAccountId bigint,
    wishlist_id bigint,
    update_ready boolean,
    show_all_ready boolean,
    user_status varchar(127),
    user_role varchar (127),
    updated_gift_id int,
    message_id int,
    inline_message_id varchar(100),
    FOREIGN KEY (wishlist_id) REFERENCES wishlists(id)
);

CREATE TABLE IF NOT EXISTS gifts
(
    id serial primary key not null,
    gift_name varchar(255),
    gift_url varchar(512),
    occupied_by_id int,
    gift_description varchar(1024),
    photo_id varchar (1024),
    wishlist_id bigint,
    FOREIGN KEY (wishlist_id) REFERENCES wishlists(id),
    FOREIGN KEY (occupied_by_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS user_subscribers
(
    user_id int NOT NULL,
    subscriber_id int NOT NULL,
    PRIMARY KEY(user_id, subscriber_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (subscriber_id) REFERENCES users(id)
);





