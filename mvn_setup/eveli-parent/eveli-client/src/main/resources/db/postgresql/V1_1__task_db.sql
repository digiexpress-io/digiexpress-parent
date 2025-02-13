
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
    create table process (
        id bigserial not null,
        article_name varchar(255),
        created timestamp(6) not null,
        expires_at timestamp(6),
        expires_in_seconds bigint,
        flow_body jsonb,
        flow_name varchar(255),
        form_body jsonb,
        form_name varchar(255),
        form_tag_name varchar(255),
        parent_article_name varchar(255),
        questionnaire_id varchar(255),
        status varchar(255) check (status in ('CREATED','ANSWERING','ANSWERED','IN_PROGRESS','WAITING','COMPLETED','REJECTED','WAITING_FOR_SYNC')),
        stencil_tag_name varchar(255),
        task_id varchar(255),
        updated timestamp(6) not null,
        user_id varchar(255),
        workflow_name varchar(255) not null,
        workflow_tag_name varchar(255),
        wrench_tag_name varchar(255),
        primary key (id)
    );