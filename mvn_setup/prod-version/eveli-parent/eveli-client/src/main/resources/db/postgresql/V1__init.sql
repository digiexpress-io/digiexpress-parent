---
-- #%L
-- eveli-client
-- %%
-- Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
-- %%
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
-- 
--      http://www.apache.org/licenses/LICENSE-2.0
-- 
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
-- #L%
---
create sequence REVINFO_SEQ start with 1 increment by 50;

create table asset_release (
    id bigserial not null,
    content_tag varchar(255),
    created timestamp(6) with time zone not null,
    created_by varchar(255),
    description varchar(255),
    name varchar(255) not null unique,
    workflow_tag varchar(255),
    wrench_tag varchar(255),
    primary key (id)
);

create table comment (
    id bigserial not null,
    comment_text TEXT not null,
    created timestamp(6) with time zone not null,
    external boolean,
    source varchar(255) check (source in ('FRONTDESK','PORTAL')),
    user_name varchar(255),
    reply_to_id bigint,
    task_id bigint,
    primary key (id)
);

create table process (
    id bigserial not null,
    created timestamp(6) not null,
    input_context_id varchar(255),
    input_parent_context_id varchar(255),
    questionnaire_id varchar(255),
    status varchar(255) check (status in ('CREATED','ANSWERING','ANSWERED','IN_PROGRESS','WAITING','COMPLETED','REJECTED')),
    task_id varchar(255),
    updated timestamp(6) not null,
    user_id varchar(255),
    workflow_name varchar(255) not null,
    primary key (id)
);

create table REVINFO (
    REV integer not null,
    REVTSTMP bigint,
    primary key (REV)
);

create table task (
    id bigserial not null,
    assigned_user varchar(255),
    assigned_user_email varchar(255),
    client_identificator varchar(255),
    completed timestamp(6) with time zone,
    created timestamp(6) with time zone not null,
    description TEXT,
    due_date date,
    priority smallint not null check (priority between 0 and 2),
    status smallint not null check (status between 0 and 4),
    subject varchar(255) not null,
    task_ref varchar(255) not null unique,
    updated timestamp(6) with time zone not null,
    updater_id varchar(255),
    version integer not null,
    primary key (id)
);

create table task_access (
    user_id varchar(255) not null,
    updated timestamp(6) with time zone not null,
    task_id bigint not null,
    primary key (task_id, user_id)
);

create table task_access_aud (
    task_id bigint not null,
    user_id varchar(255) not null,
    rev integer not null,
    revtype smallint,
    updated timestamp(6) with time zone,
    primary key (rev, task_id, user_id)
);

create table task_aud (
    id bigint not null,
    rev integer not null,
    revtype smallint,
    assigned_user varchar(255),
    assigned_user_email varchar(255),
    client_identificator varchar(255),
    completed timestamp(6) with time zone,
    created timestamp(6) with time zone,
    description TEXT,
    due_date date,
    priority smallint check (priority between 0 and 2),
    status smallint check (status between 0 and 4),
    subject varchar(255),
    task_ref varchar(255),
    updated timestamp(6) with time zone,
    updater_id varchar(255),
    version integer,
    primary key (id, rev)
);

create table task_keywords (
    task_id bigint not null,
    key_words varchar(255)
);

create table task_link (
    id bigserial not null,
    link_address varchar(255),
    link_key varchar(255),
    task_id bigint,
    primary key (id)
);

create table task_payload (
    id bigserial not null,
    downloadable boolean,
    payload oid,
    payload_name varchar(255),
    payload_type varchar(255),
    visible boolean,
    task_id bigint,
    primary key (id)
);

create table task_roles (
    task_id bigint not null,
    assigned_roles varchar(255)
);

create table task_roles_aud (
    rev integer not null,
    task_id bigint not null,
    assigned_roles varchar(255) not null,
    revtype smallint,
    primary key (task_id, assigned_roles, rev)
);

create table workflow (
    id bigserial not null,
    end_date date,
    flow_name varchar(255),
    form_id varchar(255),
    form_name varchar(255),
    form_tag varchar(255),
    name varchar(255) not null unique,
    start_date date,
    updated timestamp(6) with time zone not null,
    primary key (id)
);

create table workflow_release (
    id bigserial not null,
    created timestamp(6) with time zone not null,
    created_by varchar(255),
    description varchar(255),
    name varchar(255) not null unique,
    primary key (id)
);

create table workflow_release_entry (
    id bigserial not null,
    end_date date,
    flow_name varchar(255),
    form_name varchar(255),
    form_tag varchar(255),
    name varchar(255) not null,
    start_date date,
    updated timestamp(6) with time zone not null,
    workflow_release_id bigint,
    primary key (id)
);

alter table if exists comment 
   add constraint fk_comment_reply_to_id_to_comment 
   foreign key (reply_to_id) 
   references comment;

alter table if exists comment 
   add constraint fk_comment_task_id_to_task 
   foreign key (task_id) 
   references task;

alter table if exists task_access 
   add constraint fk_task_access_to_task 
   foreign key (task_id) 
   references task;

alter table if exists task_access_aud 
   add constraint FKonspt59vvbdht4y4ljb80s5fa 
   foreign key (rev) 
   references REVINFO;

alter table if exists task_aud 
   add constraint FK1ujiqh8xqch6cgmjxu5rcn6vk 
   foreign key (rev) 
   references REVINFO;

alter table if exists task_keywords 
   add constraint fk_task_keywords_task_id_to_task 
   foreign key (task_id) 
   references task;

alter table if exists task_link 
   add constraint fk_link_task_id_to_task 
   foreign key (task_id) 
   references task;

alter table if exists task_payload 
   add constraint fk_payload_to_task 
   foreign key (task_id) 
   references task;

alter table if exists task_roles 
   add constraint fk_task_roles_task_id_to_task 
   foreign key (task_id) 
   references task;

alter table if exists task_roles_aud 
   add constraint FKflhycr209fau6hnw50ro7mdmf 
   foreign key (rev) 
   references REVINFO;

alter table if exists workflow_release_entry 
   add constraint fk_wf_release_entry_to_wf_release 
   foreign key (workflow_release_id) 
   references workflow_release;
