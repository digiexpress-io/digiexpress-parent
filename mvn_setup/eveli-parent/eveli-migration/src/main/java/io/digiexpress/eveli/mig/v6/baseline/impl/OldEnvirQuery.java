package io.digiexpress.eveli.mig.v6.baseline.impl;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import io.digiexpress.eveli.mig.v6.baseline.OldEnvir;
import io.digiexpress.eveli.mig.v6.baseline.logger.BaselineLogger;
import io.resys.thena.api.entities.ImmutableTenant;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.sqlclient.Pool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class OldEnvirQuery {
  private final BaselineLogger logger = new BaselineLogger();
  private final Pool pool;
  
  public Uni<OldEnvir.OldEnvirObjects> findAll(String tenanPrefix) {
     return findTenant(tenanPrefix).onItem().transformToUni(tenant -> 
       Uni.combine().all().unis(
           getDocs(tenant.getPrefix()),
           getBranches(tenant.getPrefix()),
           Uni.createFrom().item(tenant)
       ).asTuple()
     )
     .onItem().transform(sources -> {


       final var result = new OldEnvir.OldEnvirObjects(
         sources.getItem3(), 
         sources.getItem2(), 
         sources.getItem1()
       );
       
       logger.ok(result);
       return result;
     })
     .onFailure().invoke(e -> logger.fail(e));
  }
  
  private Uni<Tenant> findTenant(String tenanPrefix) {
    final var sql = "select * from tenants";
    return processAnyQuery(Tenant.class, sql, (row) -> {
      
      final Tenant tenant = ImmutableTenant.builder()
        .id(row.getString("id"))
        .rev(row.getString("rev"))
        .prefix(row.getString("prefix"))
        .name(row.getString("name"))
        .type(StructureType.valueOf(row.getString("type")))
        .build();
      
      return tenant;
    }).onItem().transform(tenants -> {
      final var found = tenants.stream()
          .filter(t -> t.getType() == StructureType.doc)
          .filter(tenant -> {
            
            return tenant.getName().equals(tenanPrefix) || 
                tenant.getPrefix().equals(tenanPrefix) || 
                tenant.getId().equals(tenanPrefix);
          })
          .findFirst();
      return found.orElseThrow(() -> new RuntimeException("Cant find tenants: " + tenanPrefix));
    });
  }
  
  private Uni<List<OldEnvir.DocBranch>> getDocs(String tenanPrefix) {
    final var sql = "select * from " + tenanPrefix + "doc_branch";
    
    return processAnyQuery(OldEnvir.DocBranch.class, sql, (row) -> {
      return OldEnvir.DocBranch.builder()
        .docId(row.getString("doc_id"))
        .branchId(row.getString("branch_id"))
        .commitId(row.getString("commit_id"))
        .createdWithCommitId(row.getString("created_with_commit_id"))
        .branchName(row.getString("branch_name"))
        .branchStatus(row.getString("branch_status"))
        .value(getJsonValue(row, "value"))
        .valueStartsAt(Optional.ofNullable(row.getOffsetDateTime("value_starts_at")))
        .valueEndsAt(Optional.ofNullable(row.getOffsetDateTime("value_ends_at")))
        .valueName(Optional.ofNullable(row.getString("value_name")))
        .valueDescription(Optional.ofNullable(row.getString("value_description")))
        .valueStatus(Optional.ofNullable(row.getString("value_status")))
        .build();
    });
  }
  private Uni<List<OldEnvir.Doc>> getBranches(String tenanPrefix) {
    final var sql = "select * from " + tenanPrefix + "doc";
    return processAnyQuery(OldEnvir.Doc.class, sql, (row) -> {
     
      return OldEnvir.Doc.builder()
        .id(row.getString("id"))
        .commitId(row.getString("commit_id"))
        .createdWithCommitId(row.getString("created_with_commit_id"))
        .externalId(Optional.ofNullable(row.getString("external_id")))
        .ownerId(Optional.ofNullable(row.getString("owner_id")))
        .docParentId(Optional.ofNullable(row.getString("doc_parent_id")))
        .docType(row.getString("doc_type"))
        .docStatus(row.getString("doc_status"))
        .docStartsAt(Optional.ofNullable(row.getOffsetDateTime("doc_starts_at")))
        .docEndsAt(Optional.ofNullable(row.getOffsetDateTime("doc_ends_at")))
        .docName(Optional.ofNullable(row.getString("doc_name")))
        .docDescription(Optional.ofNullable(row.getString("doc_description")))
        .docSubStatus(Optional.ofNullable(row.getString("doc_sub_status")))
        .docMeta(Optional.ofNullable(getJsonValue(row, "doc_meta")))
        .build();
    });
  }
  
  
  private JsonObject getJsonValue(Row row, String column) {
    try {
      return new JsonObject(row.getString(column));
    } catch(Exception e) {
      return row.getJsonObject(column);      
    }
  }
  
  private <T> Uni<List<T>> processAnyQuery(Class<T> type, String sql, Function<io.vertx.mutiny.sqlclient.Row, T> mapper) {
    final var logger = this.logger.entityQuery(type);
    logger.query(sql);
    return pool.preparedQuery(sql)
      .mapping(row -> {
        try {
          final T result = mapper.apply(row);
          logger.mappingOk(result);
          return result;
        } catch(Exception e) {
          logger.mappingFail(row, e);
          return null;
        }
      })
      .execute()
      .onItem()
      .transformToMulti(RowSet::toMulti).collect().asList()
      .onItem().transform(data -> data.stream().filter(e -> e != null).toList())
      .onItem().invoke(e -> logger.queryOk(e))
      .onFailure().invoke(e -> logger.queryFail(e));
  }
}
