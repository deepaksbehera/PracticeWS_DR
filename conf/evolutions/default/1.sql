# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table app_user (
  id                        bigserial not null,
  name                      varchar(255),
  created_on                timestamp not null,
  last_update               timestamp not null,
  constraint pk_app_user primary key (id))
;

create table group_channel (
  id                        bigserial not null,
  name                      varchar(255),
  is_group_general          boolean,
  created_on                timestamp not null,
  last_update               timestamp not null,
  constraint pk_group_channel primary key (id))
;

create table message (
  id                        bigserial not null,
  messgae                   varchar(255),
  send_on                   timestamp,
  send_to_id                bigint,
  send_by_id                bigint,
  is_message_personal       boolean,
  group_channel_id          bigint,
  created_on                timestamp not null,
  last_update               timestamp not null,
  constraint pk_message primary key (id))
;


create table group_channel_app_user (
  group_channel_id               bigint not null,
  app_user_id                    bigint not null,
  constraint pk_group_channel_app_user primary key (group_channel_id, app_user_id))
;
alter table message add constraint fk_message_sendTo_1 foreign key (send_to_id) references app_user (id);
create index ix_message_sendTo_1 on message (send_to_id);
alter table message add constraint fk_message_sendBy_2 foreign key (send_by_id) references app_user (id);
create index ix_message_sendBy_2 on message (send_by_id);
alter table message add constraint fk_message_groupChannel_3 foreign key (group_channel_id) references group_channel (id);
create index ix_message_groupChannel_3 on message (group_channel_id);



alter table group_channel_app_user add constraint fk_group_channel_app_user_gro_01 foreign key (group_channel_id) references group_channel (id);

alter table group_channel_app_user add constraint fk_group_channel_app_user_app_02 foreign key (app_user_id) references app_user (id);

# --- !Downs

drop table if exists app_user cascade;

drop table if exists group_channel cascade;

drop table if exists group_channel_app_user cascade;

drop table if exists message cascade;

