package io.resys.thena.fs.tables;

import java.util.List;

import io.resys.thena.api.annotations.TenantSql;
import io.resys.thena.api.annotations.TenantSql.WrapperType;
import io.resys.thena.datasource.ThenaSqlClient.Sql;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.resys.thena.datasource.ThenaSqlClient.SqlTupleList;
import io.resys.thena.fs.entities.Commit;
import io.resys.thena.fs.entities.ImmutableCommit;
import io.resys.thena.fs.entities.ImmutableRef;
import io.resys.thena.fs.entities.ImmutableRefTransitives;
import io.resys.thena.fs.entities.Ref;
import io.resys.thena.fs.tables.filters.RefTableFilter;
import io.resys.thena.fs.tables.filters.RefTableLockFilter;
import io.smallrye.mutiny.tuples.Tuple2;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.sqlclient.Row;

@TenantSql.Table(
  name = "ref",
  order = 600,
  ddl = """
    CREATE TABLE {ref} (
      ref_name TEXT PRIMARY KEY,
      ref_description TEXT,
      ref_props JSONB,
      ref_permissions JSONB,
      ref_flags JSONB,
      ref_author TEXT,
      
      commit_id TEXT NOT NULL REFERENCES {commit}(id)
    );
    
    CREATE INDEX {ref}_commit_idx ON {ref}(commit_id);
    
    COMMENT ON TABLE {ref} IS 'Named references to commits, typically representing branches or bookmarks that can move to point to different commits over time.';
    COMMENT ON COLUMN {ref}.ref_name IS 'Reference name (e.g., "main", "develop", "feature/xyz")';
    COMMENT ON COLUMN {ref}.commit_id IS 'Current commit that this reference points to';
  """,
  constraints = "",
  drop = """
    DROP TABLE IF EXISTS {ref} CASCADE;
  """
)
public interface RefTable {

  @TenantSql.FindAll(
    sql = """
      SELECT 
        r.ref_name, 
        r.ref_description,
        r.ref_props,
        r.ref_permissions,
        r.ref_flags,
        r.ref_author,
        r.commit_id,
        c.commit_created_at, 
        c.commit_author, 
        c.commit_message,
        c.tree_id,
        c.parent_id,
        c.merge_id
      FROM {ref} r
      LEFT JOIN {commit} c ON r.commit_id = c.id
    """,
    rowMapper = RefMapper.class,
    wrapper = WrapperType.MULTI
  )
  Sql findAll();

  @TenantSql.Find(
    optional = false,
    sql = """
      SELECT r.ref_name, r.ref_description, r.ref_props, r.ref_permissions, r.ref_flags, r.ref_author,
             r.commit_id, c.commit_created_at, c.commit_author, c.commit_message, c.tree_id, c.parent_id, c.merge_id
      FROM {ref} r
      LEFT JOIN {commit} c ON r.commit_id = c.id
      WHERE r.ref_name = $1
    """,
    rowMapper = RefMapper.class
  )
  SqlTuple getByName(String refName);

  @TenantSql.FindAll(
    sql = """
      SELECT r.ref_name, r.ref_description, r.ref_props, r.ref_permissions, r.ref_flags, r.ref_author,
             r.commit_id, c.commit_created_at, c.commit_author, c.commit_message, c.tree_id, c.parent_id, c.merge_id
      FROM {ref} r
      LEFT JOIN {commit} c ON r.commit_id = c.id
      WHERE r.commit_id = $1
    """,
    wrapper = WrapperType.MULTI,
    rowMapper = RefMapper.class
  )
  SqlTuple findAllByCommitId(String commitId);

  @TenantSql.InsertAll(
    sql = """
      INSERT INTO {ref}
      (ref_name, commit_id)
      VALUES($1, $2)
    """,
    propsMapper = RefInsertMapper.class
  )
  SqlTupleList insertMany(List<Ref> refs);

  @TenantSql.UpdateAll(
    sql = """
      UPDATE {ref}
      SET commit_id = $1
      WHERE ref_name = $2
    """,
    propsMapper = RefUpdateMapper.class
  )
  SqlTupleList updateMany(List<Ref> refs);

  @TenantSql.DeleteAll(
    sql = "DELETE FROM {ref} WHERE ref_name = $1",
    propsMapper = RefDeleteMapper.class
  )
  SqlTupleList deleteAll(List<Ref> refs);
  
  
  @TenantSql.FindAll(
    sql = """
      SELECT r.ref_name, r.ref_description, r.ref_props, r.ref_permissions, r.ref_flags, r.ref_author,
             r.commit_id, c.commit_created_at, c.commit_author, c.commit_message, c.tree_id, c.parent_id, c.merge_id
      FROM {ref} r
      LEFT JOIN {commit} c ON r.commit_id = c.id
    """,
    wrapper = WrapperType.MULTI,
    rowMapper = RefMapper.class,
    sqlBuilder = RefTableFilter.SQL.class
  )
  SqlTuple findAllByFilter(RefTableFilter filter);

  
  // -- Lock branch, get current commit tree
  @TenantSql.Find(
    sql = """
      SELECT 
        r.ref_name, r.ref_description, r.ref_props, r.ref_permissions, r.ref_flags, r.ref_author, r.commit_id, 
        c.commit_created_at, c.commit_author, c.commit_message, c.tree_id, c.parent_id, c.merge_id,
        t.tree_nodes
      FROM (SELECT * FROM {ref} WHERE ref_name = $1 FOR UPDATE NOWAIT) as r
      JOIN {commit} as c ON c.id = r.commit_id
      JOIN {tree} as t ON t.id = c.tree_id
    """,
    rowMapper = RefLockMapper.class,
    sqlBuilder = RefTableLockFilter.SQL.class
  )
  SqlTuple findOneWithLock(RefTableLockFilter filter);

  class RefLockMapper implements TenantSql.RowMapper<Ref> {
    @Override
    public Ref apply(Row row) {
      final var baseline = RefMapper.baseline(row);
      final var trs = ImmutableRefTransitives.builder()
          .commit(baseline.getItem2())
          .build();
      return baseline.getItem1().transitives(trs).build();
    }
  }


  class RefMapper implements TenantSql.RowMapper<Ref> {
    
    public static Tuple2<ImmutableRef.Builder, Commit> baseline(Row row) {
      final var refBuilder = ImmutableRef.builder()
          .refName(row.getString("ref_name"))
          .commitId(row.getString("commit_id"));
      
      // Add optional ref properties
      final String refDescription = row.getString("ref_description");
      if (refDescription != null) {
        refBuilder.branchDescription(refDescription);
      }
      
      final JsonObject refProps = row.getJsonObject("ref_props");
      if (refProps != null) {
        refBuilder.branchProps(refProps);
      }
      
      final JsonObject refPermissions = row.getJsonObject("ref_permissions");
      if (refPermissions != null) {
        refBuilder.branchPermissions(refPermissions);
      }
      
      final JsonObject refFlags = row.getJsonObject("ref_flags");
      if (refFlags != null) {
        refBuilder.branchFlags(refFlags);
      }
      
      final String refAuthor = row.getString("ref_author");
      if (refAuthor != null) {
        refBuilder.branchAuthor(refAuthor);
      }
      
      // Build commit object from joined data
      final String commitId = row.getString("commit_id");
      
    
      final var commitBuilder = ImmutableCommit.builder()
          .id(commitId)
          .commitCreatedAt(row.getOffsetDateTime("commit_created_at"))
          .commitAuthor(row.getString("commit_author"))
          .commitMessage(row.getString("commit_message"))
          .treeId(row.getString("tree_id"));
      
      final String parentId = row.getString("parent_id");
      if (parentId != null) {
        commitBuilder.parentId(parentId);
      }
      
      final String mergeId = row.getString("merge_id");
      if (mergeId != null) {
        commitBuilder.mergeId(mergeId);
      }
      
      final var commit = commitBuilder.build();
      return Tuple2.of(refBuilder, commit);
    }

    @Override
    public Ref apply(Row row) {
      final var baseline = baseline(row);
      final var trs = ImmutableRefTransitives.builder().commit(baseline.getItem2()).build();
      return baseline.getItem1().transitives(trs).build();
    }
  }

  class RefInsertMapper implements TenantSql.PropsMapper<Ref> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(Ref ref) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        ref.getRefName(),
        ref.getCommitId()
      });
    }
  }

  class RefUpdateMapper implements TenantSql.PropsMapper<Ref> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(Ref ref) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        ref.getCommitId(),
        ref.getRefName()
      });
    }
  }

  class RefDeleteMapper implements TenantSql.PropsMapper<Ref> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(Ref ref) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        ref.getRefName()
      });
    }
  }
}