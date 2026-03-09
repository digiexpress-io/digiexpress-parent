package io.resys.thena.fs.tables;

/*-
 * #%L
 * thena-fs-client
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import io.resys.thena.api.annotations.TenantSql;
import io.resys.thena.api.annotations.TenantSql.WrapperType;
import io.resys.thena.datasource.ThenaSqlClient.Sql;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.resys.thena.datasource.ThenaSqlClient.SqlTupleList;
import io.resys.thena.fs.entities.ImmutableTag;
import io.resys.thena.fs.entities.ImmutableTagTransitives;
import io.resys.thena.fs.entities.Tag;
import io.resys.thena.fs.tables.filters.TagTableFilter;
import io.resys.thena.support.TableUtils;
import io.vertx.mutiny.sqlclient.Row;

@TenantSql.Table(
  name = "tag",
  order = 700,
  ddl = """
    CREATE TABLE {tag} (
      tag_id UUID PRIMARY KEY,

      tag_name TEXT UNIQUE NOT NULL,
      tag_description TEXT,
      
      tag_starts_at TIMESTAMPTZ,
      tag_ends_at TIMESTAMPTZ,
      
      tag_lifecycle TEXT,
      tag_health TEXT,
      
      tag_created_at TIMESTAMPTZ NOT NULL,
            
      tag_author TEXT NOT NULL,
      tag_extension JSONB,
      tag_errors JSONB,
      tag_report JSONB,
      
      external_id TEXT,
      
      ref_id TEXT, --soft link
      commit_id TEXT NOT NULL REFERENCES {commit}(commit_id)      
    );
    
    CREATE INDEX {tag}_name_idx ON {tag}(tag_name);
    CREATE INDEX {tag}_commit_idx ON {tag}(commit_id);
    CREATE INDEX {tag}_created_at_idx ON {tag}(tag_created_at);
    CREATE INDEX {tag}_external_id_idx ON {tag}(external_id);
    CREATE INDEX {tag}_starts_at_idx ON {tag}(tag_starts_at);
    CREATE INDEX {tag}_ref_idx ON {tag}(ref_id);
    
    COMMENT ON TABLE {tag} IS 'Immutable named markers for specific commits, typically used for releases or important milestones.';
    COMMENT ON COLUMN {tag}.tag_lifecycle IS 'The operational phase of the tag (e.g., in-force, waiting, lapsed).';
    COMMENT ON COLUMN {tag}.tag_health IS 'The current health status or severity level (e.g., OK, WARNING, ERROR).';
    COMMENT ON COLUMN {tag}.tag_id IS 'Unique tag identifier (hash)';
    COMMENT ON COLUMN {tag}.tag_name IS 'Human-readable tag name (e.g., "v1.0.0", "release-2023")';
    COMMENT ON COLUMN {tag}.tag_description IS 'Optional detailed description of this tag';
    COMMENT ON COLUMN {tag}.commit_id IS 'The specific commit this tag points to (immutable)';
    COMMENT ON COLUMN {tag}.tag_created_at IS 'Timestamp when this tag was created, stored in UTC';
    COMMENT ON COLUMN {tag}.tag_author IS 'Author who created this tag';
    COMMENT ON COLUMN {tag}.tag_extension IS 'Additional tag metadata in JSONB format for future extensibility';
    COMMENT ON COLUMN {tag}.tag_errors IS 'Error information and validation issues stored in JSONB format for diagnostic purposes';
    COMMENT ON COLUMN {tag}.external_id IS 'External system identifier for integration and tracking purposes';
    COMMENT ON COLUMN {tag}.tag_starts_at IS 'Scheduled activation timestamp when this tag becomes effective or goes live';
    COMMENT ON COLUMN {tag}.tag_report IS 'Operational reports and status information stored in JSONB format';
    COMMENT ON COLUMN {tag}.ref_id IS 'Branch pointer when created from specific branch, otherwise just commit id is used';
  """,
  constraints = "",
  drop = """
    DROP TABLE IF EXISTS {tag} CASCADE;
  """
)
public interface TagTable {

  @TenantSql.FindAll(
    sql = """
      SELECT tag.*,
             commit.commit_created_at, commit.commit_author as commit_author_name, commit.commit_message,
             cardinality(tree.tree_nodes) as commit_nodes_count
      FROM {tag} as tag
      LEFT JOIN {commit} as commit ON tag.commit_id = commit.commit_id
      JOIN {tree} as tree ON tree.tree_id = commit.tree_id
    """,
    rowMapper = TagMapper.class
  )
  Sql findAll();

  @TenantSql.Find(
    optional = false,
    sql = """
      SELECT tag.*,
             commit.commit_created_at, commit.commit_author as commit_author_name, commit.commit_message,
             cardinality(tree.tree_nodes) as commit_nodes_count
      FROM {tag} as tag
      LEFT JOIN {commit} as commit ON tag.commit_id = commit.commit_id
      JOIN {tree} as tree ON tree.tree_id = commit.tree_id
      WHERE tag.id = $1
    """,
    rowMapper = TagMapper.class
  )
  SqlTuple getById(String id);

  @TenantSql.Find(
    optional = true,
    sql = """
      SELECT tag.*,
             commit.commit_created_at, commit.commit_author, commit.commit_message,
             cardinality(tree.tree_nodes) as commit_nodes_count
      FROM {tag} as tag
      LEFT JOIN {commit} as commit ON tag.commit_id = commit.commit_id
      JOIN {tree} as tree ON tree.tree_id = commit.tree_id
      WHERE tag.tag_name = $1 OR tag.tag_id::text = $1
    """,
    rowMapper = TagMapper.class
  )
  SqlTuple findByTagName(String tagName);
  
  
  @TenantSql.FindAll(
    sql = """
      SELECT tag.*
      FROM {tag} as tag
    """,
    rowMapper = TagMapper.class,
    wrapper = WrapperType.MULTI,
    sqlBuilder = TagTableFilter.SQL.class
  )
  SqlTuple findAllByFilter(TagTableFilter filter);
  

  @TenantSql.FindAll(
    sql = """
      SELECT tag.*,
             commit.commit_created_at, commit.commit_author, commit.commit_message,
             cardinality(tree.tree_nodes) as commit_nodes_count
      FROM {tag} as tag
      LEFT JOIN {commit} as commit ON tag.commit_id = commit.commit_id
      JOIN {tree} as tree ON tree.tree_id = commit.tree_id
      WHERE tag.commit_id = $1
    """,
    rowMapper = TagMapper.class
  )
  SqlTuple findAllByCommitId(String commitId);

  @TenantSql.InsertAll(
    sql = """
      INSERT INTO {tag}
      (tag_id, tag_name, tag_description, commit_id, tag_created_at, tag_author, 
       tag_extension, tag_errors, external_id, tag_ends_at, tag_starts_at, tag_report, ref_id,
       tag_lifecycle, tag_health
      )
      VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14, $15)
    """,
    propsMapper = TagInsertMapper.class
  )
  SqlTupleList insertMany(List<Tag> tags);

  @TenantSql.UpdateAll(
    sql = """
      UPDATE {tag}
      SET tag_name = $1, tag_description = $2, commit_id = $3, 
          tag_created_at = $4, tag_author = $5, tag_extension = $6,
          tag_errors = $7, external_id = $8, tag_ends_at = $9, 
          tag_starts_at = $10, tag_report = $11,
          tag_lifecycle = $12, tag_health = $13
      WHERE tag_id = $14
    """,
    propsMapper = TagUpdateMapper.class
  )
  SqlTupleList updateMany(List<Tag> tags);

  @TenantSql.DeleteAll(
    sql = "DELETE FROM {tag} WHERE tag_id = $1",
    propsMapper = TagDeleteMapper.class
  )
  SqlTupleList deleteAll(List<Tag> tags);

  class TagMapper implements TenantSql.RowMapper<Tag> {
    @Override
    public Tag apply(Row row) {
      final String tagDescription = row.getString("tag_description");
      final String externalId = row.getString("external_id");
      final OffsetDateTime tagEndsAt = row.getOffsetDateTime("tag_ends_at");
      final OffsetDateTime tagStartsAt = row.getOffsetDateTime("tag_starts_at");
      

      return ImmutableTag.builder()
          .id(TableUtils.toStringUUID(row, "tag_id"))
          .refId(Optional.ofNullable(row.getString("ref_id")))
          
          .tagName(row.getString("tag_name"))
          .tagDescription(Optional.ofNullable(tagDescription))
          .commitId(row.getString("commit_id"))
          .tagCreatedAt(row.getOffsetDateTime("tag_created_at"))
          .tagAuthor(row.getString("tag_author"))
          .tagExtension(Optional.ofNullable(row.getJsonObject("tag_extension")))
          .tagErrors(Optional.ofNullable(row.getJsonObject("tag_errors")))
          .externalId(Optional.ofNullable(externalId))
          
          .tagEndsAt(Optional.ofNullable(tagEndsAt))
          .tagStartsAt(Optional.ofNullable(tagStartsAt))
          .tagReport(Optional.ofNullable(row.getJsonObject("tag_report")))
          
          .tagLifecycle(Optional.ofNullable(row.getString("tag_lifecycle")))
          .tagHealth(Optional.ofNullable(row.getString("tag_health")))
          
          .transitives(ImmutableTagTransitives.builder().build())
          .build();
    }
  }

  class TagInsertMapper implements TenantSql.PropsMapper<Tag> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(Tag tag) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        TableUtils.toUuid(tag.getId()),
        tag.getTagName(),
        tag.getTagDescription().orElse(null),
        tag.getCommitId(),
        tag.getTagCreatedAt(),
        tag.getTagAuthor(),
        tag.getTagExtension().orElse(null),
        tag.getTagErrors().orElse(null),
        tag.getExternalId().orElse(null),
        tag.getTagEndsAt().orElse(null),
        tag.getTagStartsAt().orElse(null),
        tag.getTagReport().orElse(null),
        tag.getRefId().orElse(null),
        
        tag.getTagLifecycle().orElse(null),
        tag.getTagHealth().orElse(null),
      });
    }
  }

  class TagUpdateMapper implements TenantSql.PropsMapper<Tag> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(Tag tag) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        tag.getTagName(),
        tag.getTagDescription().orElse(null),
        tag.getCommitId(),
        tag.getTagCreatedAt(),
        tag.getTagAuthor(),
        tag.getTagExtension().orElse(null),
        tag.getTagErrors(),
        tag.getExternalId().orElse(null),
        tag.getTagEndsAt().orElse(null),
        tag.getTagStartsAt().orElse(null),
        tag.getTagReport().orElse(null),
        
        tag.getTagLifecycle().orElse(null),
        tag.getTagHealth().orElse(null),
        
        TableUtils.toUuid(tag.getId())
      });
    }
  }

  class TagDeleteMapper implements TenantSql.PropsMapper<Tag> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(Tag tag) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        TableUtils.toUuid(tag.getId())
      });
    }
  }
}
