create table if not exists message
(
    message_sequence bigint        not null auto_increment primary key,
    username         varchar(30)   not null comment '유저명',
    content          varchar(1000) not null comment '메시지 내용',
    created_at       datetime      not null comment '생성 일자',
    updated_at       datetime      not null comment '수정 일자'
)