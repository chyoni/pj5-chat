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
    user_id                bigint       not null auto_increment primary key,
    username               varchar(30)  not null unique comment '유저명',
    password               varchar(255) not null comment '패스워드',
    connection_invite_code varchar(30)  not null unique comment '초대 코드',
    connection_count       int          not null comment '연결 횟수',
    created_at             datetime     not null comment '생성 일자',
    updated_at             datetime     not null comment '수정 일자'
);

create table if not exists user_connection
(
    partner_a_user_id bigint      not null comment '연결 유저 A',
    partner_b_user_id bigint      not null comment '연결 유저 B',
    status            varchar(30) not null comment '연결 상태',
    inviter_user_id   bigint      not null comment '초대한 유저 ID',
    created_at        datetime    not null comment '생성 일자',
    updated_at        datetime    not null comment '수정 일자',
    primary key (partner_a_user_id, partner_b_user_id)
);
create index idx_partner_b_user_id on user_connection (partner_b_user_id);
create index idx_partner_a_user_id_status on user_connection (partner_a_user_id, status);
create index idx_partner_b_user_id_status on user_connection (partner_b_user_id, status);