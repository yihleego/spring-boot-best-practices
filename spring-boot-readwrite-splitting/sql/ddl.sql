create database if not exists test;
use test;

create table user
(
    id           bigint primary key auto_increment not null,
    username     varchar(20)                       not null,
    password     varchar(40)                       not null,
    created_time datetime                          not null,
    updated_time datetime                          null,
    constraint uk_user_username unique (username)
);
