create table if not exists message
(
    message_sequence bigint        not null auto_increment primary key,
    username         varchar(30)   not null comment '유저명',
    content          varchar(1000) not null comment '메시지 내용',
    created_at       datetime      not null comment '생성 일자',
    updated_at       datetime      not null comment '수정 일자'
);

create table if not exists user
(
    user_id    bigint       not null auto_increment primary key,
    username   varchar(30)  not null unique comment '유저명',
    password   varchar(255) not null comment '패스워드',
    created_at datetime     not null comment '생성 일자',
    updated_at datetime     not null comment '수정 일자'
)