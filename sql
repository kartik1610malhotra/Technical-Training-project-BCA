
create database technical;

use technical

CREATE TABLE user (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50),
    username VARCHAR(50) UNIQUE,
    account_number INT UNIQUE,
    password VARCHAR(50),
    balance DECIMAL(10, 2)
);

insert into user(name,username,account_number,password,balance) 
values("kartik","1hr",123456,"123456",5000);
