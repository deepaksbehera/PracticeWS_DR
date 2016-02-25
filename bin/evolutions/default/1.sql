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

create table group (
  id                        bigserial not null,
  name                      varchar(255),
  created_on                timestamp not null,
  last_update               timestamp not null,
  constraint pk_group primary key (id))
;

create table messages (
  id                        bigserial not null,
  messgae                   varchar(255),
  send_to_id                bigint,
  send_by_id                bigint,
  message_type              varchar(10),
  group_id                  bigint,
  created_on                timestamp not null,
  last_update               timestamp not null,
  constraint ck_messages_message_type check (message_type in ('GROUP','INDIVIDUAL')),
  constraint pk_messages primary key (id))
;


create table group_app_user (
  group_id                       bigint not null,
  app_user_id                    bigint not null,
  constraint pk_group_app_user primary key (group_id, app_user_id))
;
alter table messages add constraint fk_messages_sendTo_1 foreign key (send_to_id) references app_user (id);
create index ix_messages_sendTo_1 on messages (send_to_id);
alter table messages add constraint fk_messages_sendBy_2 foreign key (send_by_id) references app_user (id);
create index ix_messages_sendBy_2 on messages (send_by_id);
alter table messages add constraint fk_messages_group_3 foreign key (group_id) references group (id);
create index ix_messages_group_3 on messages (group_id);



alter table group_app_user add constraint fk_group_app_user_group_01 foreign key (group_id) references group (id);

alter table group_app_user add constraint fk_group_app_user_app_user_02 foreign key (app_user_id) references app_user (id);

# --- !Downs

drop table if exists app_user cascade;

drop table if exists group cascade;

drop table if exists group_app_user cascade;

drop table if exists messages cascade;

