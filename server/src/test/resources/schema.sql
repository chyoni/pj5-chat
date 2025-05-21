create table if not exists message
(
    message_sequence bigint        not null auto_increment primary key,
    user_id          bigint        not null comment '유저 ID',
    content          varchar(1000) not null comment '메시지 내용',
    created_at       datetime      not null comment '생성 일자',
    updated_at       datetime      not null comment '수정 일자'
);

create table if not exists user
(
    user_id                bigint       not null auto_increment primary key,
    username               varchar(30)  not null unique comment '유저명',
    password               varchar(255) not null comment '패스워드',
    connection_invite_code varchar(32)  not null unique comment '초대 코드',
    connection_count       int          not null comment '현재 연결된 커넥션 수',
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

create table if not exists channel
(
    channel_id          bigint       not null auto_increment primary key,
    title               varchar(100) not null comment '채널명',
    channel_invite_code varchar(100) not null unique comment '채널 초대 코드',
    created_at          datetime     not null comment '생성 일자',
    updated_at          datetime     not null comment '수정 일자',
    head_count          int          not null comment '참여 인원 수'
);

create table if not exists user_channel
(
    user_id           bigint   not null comment '유저 ID',
    channel_id        bigint   not null comment '채널 ID',
    last_read_msg_seq bigint   not null comment '마지막으로 읽은 메시지 번호',
    created_at        datetime not null comment '생성 일자',
    updated_at        datetime not null comment '수정 일자',
    primary key (user_id, channel_id),
    index idx_channel_id (channel_id)
);