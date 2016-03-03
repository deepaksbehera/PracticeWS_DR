# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table app_user (
  id                        bigserial not null,
  name                      varchar(255),
  user_name                 varchar(255),
  password                  varchar(255),
  photo                     bytea,
  created_on                timestamp not null,
  last_update               timestamp not null,
  constraint uq_app_user_user_name unique (user_name),
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

create table messages (
  id                        bigserial not null,
  messgae                   TEXT,
  send_on                   timestamp,
  send_to_id                bigint,
  send_by_id                bigint,
  is_message_personal       boolean,
  is_seen                   boolean,
  is_deleted                boolean,
  group_channel_id          bigint,
  created_on                timestamp not null,
  last_update               timestamp not null,
  constraint pk_messages primary key (id))
;


create table app_user_group_channel (
  group_channel_id               bigint not null,
  app_user_id                    bigint not null,
  constraint pk_app_user_group_channel primary key (group_channel_id, app_user_id))
;

create table app_user_admin (
  group_channel_id               bigint not null,
  app_user_id                    bigint not null,
  constraint pk_app_user_admin primary key (group_channel_id, app_user_id))
;
alter table messages add constraint fk_messages_sendTo_1 foreign key (send_to_id) references app_user (id);
create index ix_messages_sendTo_1 on messages (send_to_id);
alter table messages add constraint fk_messages_sendBy_2 foreign key (send_by_id) references app_user (id);
create index ix_messages_sendBy_2 on messages (send_by_id);
alter table messages add constraint fk_messages_groupChannel_3 foreign key (group_channel_id) references group_channel (id);
create index ix_messages_groupChannel_3 on messages (group_channel_id);



alter table app_user_group_channel add constraint fk_app_user_group_channel_gro_01 foreign key (group_channel_id) references group_channel (id);

alter table app_user_group_channel add constraint fk_app_user_group_channel_app_02 foreign key (app_user_id) references app_user (id);

alter table app_user_admin add constraint fk_app_user_admin_group_chann_01 foreign key (group_channel_id) references group_channel (id);

alter table app_user_admin add constraint fk_app_user_admin_app_user_02 foreign key (app_user_id) references app_user (id);

# --- !Downs

drop table if exists app_user cascade;

drop table if exists group_channel cascade;

drop table if exists app_user_group_channel cascade;

drop table if exists app_user_admin cascade;

drop table if exists messages cascade;

