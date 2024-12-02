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

    create table feedback_approval (
        id bigserial not null,
        category_id bigint not null,
        created_on_date timestamp(6) with time zone not null,
        reply_id bigint,
        source_id varchar(255) not null,
        star_rating integer not null,
        updated_on_date timestamp(6) with time zone not null,
        primary key (id),
        unique (category_id, reply_id, source_id)
    );

    create table feedback_category (
        id bigserial not null,
        created_by_user_id varchar(255) not null,
        created_on_date timestamp(6) with time zone not null,
        label varchar(255) not null,
        origin varchar(255) not null,
        sub_label varchar(255),
        updated_on_date timestamp(6) with time zone not null,
        primary key (id),
        unique (label, sub_label, origin)
    );

    create table feedback_reply (
        id bigserial not null,
        category_id bigint not null,
        content TEXT not null,
        created_by varchar(255) not null,
        created_on_date timestamp(6) with time zone not null,
        locale varchar(255) not null,
        localized_label varchar(255) not null,
        localized_sub_label varchar(255),
        source_id varchar(255),
        updated_by varchar(255) not null,
        updated_on_date timestamp(6) with time zone not null,
        primary key (id)
    );

    create table feedback_reply_aud (
        id bigint not null,
        rev integer not null,
        revtype smallint,
        category_id bigint,
        content TEXT,
        created_by varchar(255),
        created_on_date timestamp(6) with time zone,
        locale varchar(255),
        localized_label varchar(255),
        localized_sub_label varchar(255),
        source_id varchar(255),
        updated_by varchar(255),
        updated_on_date timestamp(6) with time zone,
        primary key (id, rev)
    );


    alter table if exists feedback_approval 
       add constraint fk_approval_to_category 
       foreign key (category_id) 
       references feedback_category;

    alter table if exists feedback_approval 
       add constraint fk_approval_to_reply 
       foreign key (reply_id) 
       references feedback_reply;

    alter table if exists feedback_reply 
       add constraint fk_reply_to_category 
       foreign key (category_id) 
       references feedback_category;

    alter table if exists feedback_reply_aud 
       add constraint FKi3jp43qmxrjylyfgjixp30pqp 
       foreign key (rev) 
       references REVINFO;