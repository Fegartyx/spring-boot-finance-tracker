Create DATABASE finance_tracker;
Create type transaction_type as enum ('income', 'expense');
CREATE TABLE users
(
    id           uuid PRIMARY KEY,
    username     VARCHAR(255) NOT NULL UNIQUE,
    password     VARCHAR(255) NOT NULL,
    email        VARCHAR(255) NOT NULL UNIQUE,
    token        VARCHAR(255) UNIQUE,
    token_expiry BIGINT
);

CREATE TABLE wallet
(
    id      uuid primary key,
    name    VARCHAR(255) NOT NULL,
    balance decimal      NOT NULL,
    user_id uuid         NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

create table categories
(
    id   SERIAL primary key,
    name varchar(255)     NOT NULL,
    type transaction_type NOT NULL
);

create table transactions
(
    id          uuid primary key,
    amount      decimal   NOT NULL,
    date        timestamp NOT NULL,
    description text,
    category_id integer   not null,
    wallet_id   uuid      NOT NULL,
    user_id     uuid      NOT NULL,
    FOREIGN KEY (wallet_id) REFERENCES wallet (id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE SET NULL,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);