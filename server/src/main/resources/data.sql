CREATE TABLE IF NOT EXISTS users
(
    user_id   BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    user_name VARCHAR(255)                            NOT NULL,
    email     VARCHAR(512)                            NOT NULL,
    CONSTRAINT pk_user PRIMARY KEY (user_id),
    CONSTRAINT UQ_USER_EMAIL UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS item_requests
(
    item_request_id  BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    description VARCHAR(255),
    created     TIMESTAMP WITHOUT TIME ZONE             NOT NULL,
    author_id     BIGINT                                  NOT NULL,
    CONSTRAINT pk_request PRIMARY KEY (item_request_id),
    CONSTRAINT fk_request_author FOREIGN KEY (author_id) REFERENCES users (user_id)
);

CREATE TABLE IF NOT EXISTS items
(
    item_id     BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    available   BOOLEAN                                 NOT NULL,
    description VARCHAR(255),
    item_name   VARCHAR(255)                            NOT NULL,
    request_id  BIGINT,
    owner_id    BIGINT                                  NOT NULL,
    CONSTRAINT pk_item PRIMARY KEY (item_id),
    CONSTRAINT fk_item_owner FOREIGN KEY (owner_id) REFERENCES users (user_id),
    CONSTRAINT fk_item_request FOREIGN KEY (request_id) REFERENCES item_requests (item_request_id)
);

CREATE TABLE IF NOT EXISTS bookings
(
    booking_id    BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    booking_end   TIMESTAMP WITHOUT TIME ZONE,
    booking_start TIMESTAMP WITHOUT TIME ZONE,
    status        VARCHAR(255),
    booker_id     BIGINT                                  NOT NULL,
    item_id       BIGINT                                  NOT NULL,
    CONSTRAINT pk_booking PRIMARY KEY (booking_id),
    CONSTRAINT fk_booking_booker FOREIGN KEY (booker_id) REFERENCES users (user_id),
    CONSTRAINT fk_booking_item FOREIGN KEY (item_id) REFERENCES items (item_id)
);

CREATE TABLE IF NOT EXISTS comments
(
    comment_id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    created    TIMESTAMP WITHOUT TIME ZONE,
    text       VARCHAR(255),
    item_id    BIGINT                                  NOT NULL,
    author_id  BIGINT                                  NOT NULL,
    CONSTRAINT pk_comment PRIMARY KEY (comment_id),
    CONSTRAINT fk_comment_item FOREIGN KEY (item_id) REFERENCES items (item_id),
    CONSTRAINT fk_comment_user FOREIGN KEY (author_id) REFERENCES users (user_id)
);