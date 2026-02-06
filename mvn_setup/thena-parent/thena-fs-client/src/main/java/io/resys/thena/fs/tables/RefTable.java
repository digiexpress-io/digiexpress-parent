package io.resys.thena.fs.tables;

import java.time.OffsetDateTime;
import java.util.List;

import io.resys.thena.api.annotations.TenantSql;
import io.resys.thena.api.annotations.TenantSql.WrapperType;
import io.resys.thena.datasource.ThenaSqlClient.Sql;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.resys.thena.datasource.ThenaSqlClient.SqlTupleList;
import io.resys.thena.fs.entities.ImmutableRef;
import io.resys.thena.fs.entities.ImmutableRefTransitives;
import io.resys.thena.fs.entities.Ref;
import io.resys.thena.fs.tables.RefTable.RefMapper;
import io.resys.thena.fs.tables.filters.RefTableFilter;
import io.resys.thena.fs.tables.filters.RefTableLockFilter;
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
        r.commit_id,
        c.commit_created_at, 
        c.commit_author, 
        c.commit_message
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
      SELECT r.ref_name, r.commit_id,
             c.commit_created_at, c.commit_author, c.commit_message
      FROM {ref} r
      LEFT JOIN {commit} c ON r.commit_id = c.id
      WHERE r.ref_name = $1
    """,
    rowMapper = RefMapper.class
  )
  SqlTuple getByName(String refName);

  @TenantSql.FindAll(
    sql = """
      SELECT r.ref_name, r.commit_id,
             c.commit_created_at, c.commit_author, c.commit_message
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
      SELECT r.ref_name, r.commit_id,
             c.commit_created_at, c.commit_author, c.commit_message
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
      SELECT * FROM {ref} WHERE ref_name = $1 FOR UPDATE NOWAIT
      JOIN {commit} ON {commit}.id = {ref}.commit_id
      JOIN {tree} ON fs_tree.id = {commit}.tree_id
    """,
    rowMapper = RefLockMapper.class,
    sqlBuilder = RefTableLockFilter.SQL.class
  )
  SqlTuple findOneWithLock(RefTableLockFilter filter);

  class RefLockMapper implements TenantSql.RowMapper<Ref> {
    @Override
    public Ref apply(Row row) {
      final OffsetDateTime commitCreatedAt = row.getOffsetDateTime("commit_created_at");
      final String commitAuthor = row.getString("commit_author");
      final String commitMessage = row.getString("commit_message");

      return ImmutableRef.builder()
          .refName(row.getString("ref_name"))
          .commitId(row.getString("commit_id"))
          .transitives(ImmutableRefTransitives.builder()
              
              .build())
          .build();
    }
  }


  class RefMapper implements TenantSql.RowMapper<Ref> {
    @Override
    public Ref apply(Row row) {
      final OffsetDateTime commitCreatedAt = row.getOffsetDateTime("commit_created_at");
      final String commitAuthor = row.getString("commit_author");
      final String commitMessage = row.getString("commit_message");

      return ImmutableRef.builder()
          .refName(row.getString("ref_name"))
          .commitId(row.getString("commit_id"))
          .transitives(ImmutableRefTransitives.builder()

              .build())
          .build();
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