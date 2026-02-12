package io.resys.thena.fs.tables.mappers;

import java.util.Optional;

import io.resys.thena.api.annotations.TenantSql;
import io.resys.thena.fs.entities.Commit;
import io.resys.thena.fs.entities.ImmutableRef;
import io.resys.thena.fs.entities.Ref;
import io.resys.thena.support.TableUtils;
import io.smallrye.mutiny.tuples.Tuple2;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.sqlclient.Row;

public class CommitOrRefMapper implements TenantSql.RowMapper<Tuple2<Commit, Optional<Ref>>> {
  
  @Override
  public Tuple2<Commit, Optional<Ref>> apply(Row row) {
    final var commit = new CommitMapper().apply(row);
    
    final Ref ref;
    if(row.getString("found_by").equals("commit")) {
      ref = null;
      
    } else {
      final var refBuilder = ImmutableRef.builder()
          .id(TableUtils.toStringUUID(row, "ref_id"))
          .refName(row.getString("ref_name"))
          .commitId(row.getString("id"));
      
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
      
      ref = refBuilder.build();
    }

    return Tuple2.of(commit, Optional.ofNullable(ref));
  }
}