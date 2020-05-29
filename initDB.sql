CREATE TABLE service_users (
    id BIGINT not null AUTO_INCREMENT,
    userName VARCHAR(40) not null,
    userPassword VARCHAR(255) not null,
    userRole VARCHAR(10) not null,

    PRIMARY KEY (id)
);

CREATE TABLE commissions (
    id BIGINT not null AUTO_INCREMENT,
    commissionPt DECIMAL(5,2) not null,
    currencyFrom VARCHAR(3) not null,
    currencyTo VARCHAR(3) not null,

    PRIMARY KEY (id)
);

CREATE TABLE exchangerates (
    id BIGINT not null AUTO_INCREMENT,
    currencyFrom VARCHAR(3) not null,
    rate DECIMAL(38,5) not null,
    currencyTo VARCHAR(3) not null,

    PRIMARY KEY (id)
);