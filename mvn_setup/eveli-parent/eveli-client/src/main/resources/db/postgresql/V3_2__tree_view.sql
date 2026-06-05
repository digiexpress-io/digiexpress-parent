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
    func_record RECORD;
    sql_statement TEXT;
    new_function_definition TEXT;
    tenant_prefix TEXT;
BEGIN
    -- Loop through all functions matching your pattern
    FOR func_record IN (
        SELECT
            p.proname AS function_name,
            pg_get_functiondef(p.oid) AS function_definition
        FROM pg_proc p
        JOIN pg_namespace n ON p.pronamespace = n.oid
        WHERE n.nspname = 'public'
        AND p.proname LIKE '%_tree_view'  -- Adjust pattern as needed
    )
    LOOP
        -- Extract tenant prefix from function name (e.g., "tenant1_tree_view" -> "tenant1")
        tenant_prefix := regexp_replace(func_record.function_name, '_tree_view$', '');

        -- Create the new function definition with proper table references
        new_function_definition := format('
CREATE OR REPLACE FUNCTION %I(
    _tree_id UUID,
    _hydrate_all BOOLEAN,
    _hydrate_ids JSONB DEFAULT ''[]''::jsonb,
    _hydrate_types JSONB DEFAULT ''[]''::jsonb
)
RETURNS TABLE (
    tree_id UUID,
    tree_node_blob JSONB
) AS $func$
BEGIN
RETURN QUERY
SELECT
    tree.tree_id,
    jsonb_agg(
        json_build_object(
            ''node_id'', nodes.node_id,
            ''object_id'', nodes.object_id,
            ''node_path'', nodes.node_path,
            ''node_name'', nodes.node_name,

            ''node_full_name'', CONCAT_WS(''/'', NULLIF(nodes.node_path, ''''), nodes.node_name),

            ''created_at'',        idx.created_at,
            ''updated_at'',        idx.updated_at,
            ''created_by_author'', idx.created_by_author,
            ''created_by'',        idx.created_by,
            ''updated_by_author'', idx.updated_by_author,
            ''updated_by'',        idx.updated_by,

            ''tree_id'', tree.tree_id,

            ''blob_id'', nodes.blob_id,
            ''blob_type'', blobs.blob_type,
            ''blob_class'', blobs.blob_class,

            -- HYDRATE BLOB VALUE
            ''blob_value'', CASE
                WHEN jsonb_array_length(_hydrate_ids) > 0 AND (
                    _hydrate_ids @> jsonb_build_array(nodes.object_id)
                    OR
                    _hydrate_ids @> jsonb_build_array(CONCAT_WS(''/'', NULLIF(nodes.node_path, ''''), nodes.node_name))
                ) THEN blobs.blob_value

                WHEN jsonb_array_length(_hydrate_types) > 0 AND (
                    _hydrate_types @> jsonb_build_array(blobs.blob_type)
                ) THEN blobs.blob_value

                WHEN _hydrate_all THEN blobs.blob_value
                ELSE NULL
            END,

            ''props_id'', nodes.props_id,
            ''props_labels'', props.props_labels,
            ''props_flags'', props.props_flags,
            ''props_comments'', props.props_comments,
            ''props_permissions'', props.props_permissions,
            ''props_description'', props.props_description
        )
    ) as tree_node_blob

FROM %I as tree
CROSS JOIN LATERAL unnest(tree.tree_nodes) AS nodes

-- Find the specific index row for this tree and the specific node metadata in the array
LEFT JOIN LATERAL (
    SELECT
        tree_ancestry.object_id,
        tree_ancestry.created_by,
        tree_ancestry.updated_by,
        updated_commit.commit_author as updated_by_author,
        created_commit.commit_author as created_by_author,

        created_commit.commit_created_at AS created_at,
        updated_commit.commit_created_at AS updated_at

    FROM %I AS tree_index

    CROSS JOIN LATERAL unnest(tree_index.tree_ancestry) AS tree_ancestry
    LEFT JOIN %I AS created_commit ON tree_ancestry.created_by = created_commit.commit_id
    LEFT JOIN %I AS updated_commit ON tree_ancestry.updated_by = updated_commit.commit_id

    WHERE tree_index.tree_id = tree.tree_id AND tree_ancestry.object_id = nodes.object_id
) AS idx ON TRUE

LEFT JOIN %I as props
    ON props.props_id = nodes.props_id
    AND nodes.props_id IS NOT NULL

LEFT JOIN %I as blobs
    ON blobs.blob_id = nodes.blob_id
    AND nodes.blob_id IS NOT NULL

WHERE (_tree_id IS NULL OR tree.tree_id = _tree_id)
GROUP BY tree.tree_id;
END;
$func$ LANGUAGE plpgsql STABLE;',
            func_record.function_name,  -- Function name
            tenant_prefix || '_tree',    -- {tree} table
            tenant_prefix || '_tree_index', -- {tree_index} table
            tenant_prefix || '_commit',  -- {commit} table (for created_commit)
            tenant_prefix || '_commit',  -- {commit} table (for updated_commit)
            tenant_prefix || '_props',   -- {props} table
            tenant_prefix || '_blob'     -- {blob} table
        );

        -- Execute the CREATE OR REPLACE statement
        RAISE NOTICE 'Replacing function: %', func_record.function_name;
        EXECUTE new_function_definition;
    END LOOP;
END $$;
