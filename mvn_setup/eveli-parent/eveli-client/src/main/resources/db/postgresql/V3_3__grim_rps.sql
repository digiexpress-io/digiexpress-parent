---
-- #%L
-- eveli-client
-- %%
-- Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
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
DO $$
DECLARE
    tenant_record RECORD;
    tenant_prefix TEXT;
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'tenants'
    ) THEN
        RAISE NOTICE 'tenants table not found, skipping grim_rps creation';
        RETURN;
    END IF;

    FOR tenant_record IN (
        SELECT prefix FROM public.tenants WHERE type = 'grim'
    )
    LOOP
        tenant_prefix := lower(tenant_record.prefix);

        EXECUTE format('
            CREATE TABLE IF NOT EXISTS %I
            (
              id            VARCHAR(40) PRIMARY KEY,
              created_at    TIMESTAMPTZ NOT NULL,
              external_id   VARCHAR(255) NOT NULL,
              locale        VARCHAR(40) NOT NULL,
              workflow_name VARCHAR(255) NOT NULL,
              form_name     VARCHAR(255) NOT NULL,
              form_version  VARCHAR(255) NOT NULL,
              rating        INT NOT NULL,
              comment       TEXT NULL
            );', tenant_prefix || 'grim_rps');

        EXECUTE format('
            CREATE INDEX IF NOT EXISTS %I ON %I (workflow_name);',
            tenant_prefix || 'grim_rps_WK_NAME_INDEX',
            tenant_prefix || 'grim_rps');

        EXECUTE format('
            CREATE INDEX IF NOT EXISTS %I ON %I (form_name);',
            tenant_prefix || 'grim_rps_FORM_NAME_INDEX',
            tenant_prefix || 'grim_rps');

        RAISE NOTICE 'Created grim_rps for tenant prefix: %', tenant_prefix;
    END LOOP;
END $$;
