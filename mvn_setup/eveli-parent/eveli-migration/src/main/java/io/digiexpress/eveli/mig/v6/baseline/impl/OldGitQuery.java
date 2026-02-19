package io.digiexpress.eveli.mig.v6.baseline.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.digiexpress.eveli.mig.v6.baseline.OldGit;
import io.digiexpress.eveli.mig.v6.baseline.OldGit.Blob;
import io.digiexpress.eveli.mig.v6.baseline.OldGit.Branch;
import io.digiexpress.eveli.mig.v6.baseline.OldGit.Commit;
import io.digiexpress.eveli.mig.v6.baseline.OldGit.Tag;
import io.digiexpress.eveli.mig.v6.baseline.OldGit.Tree;
import io.digiexpress.eveli.mig.v6.baseline.OldGit.TreeValue;
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
public class OldGitQuery {
  private final BaselineLogger logger = new BaselineLogger();
  private final Pool pool;
  
  public Uni<OldGit.OldGitObjects> findAll(String tenanPrefix) {
     return findTenant(tenanPrefix).onItem().transformToUni(tenant -> 
       Uni.combine().all().unis(
           getBlobs(tenant.getPrefix()),
           getBranches(tenant.getPrefix()),
           getCommits(tenant.getPrefix()),
           getTags(tenant.getPrefix()),
           getTrees(tenant.getPrefix()),
           getTreeValues(tenant.getPrefix())
       ).asTuple()
     )
     .onItem().transform(sources -> {

       final List<Branch> branches = sources.getItem2();
       final List<Tag> tags = sources.getItem4();
       final List<TreeValue> treeValues = sources.getItem6();

       final Map<String, Blob> blobs = sources.getItem1().stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
       final Map<String, Commit> commits = sources.getItem3().stream().collect(Collectors.toMap(e -> e.getId(), e -> e));

       final Map<String, Tree> trees = new HashMap<>();
       
       for(final Tree tree : sources.getItem5()) {
         trees.put(tree.getId(), tree);
       }

       for(final TreeValue treeValue : treeValues) {
         trees.get(treeValue.getTree()).getValues().add(treeValue);
       }

       final var result = new OldGit.OldGitObjects(
         tenanPrefix, 
         branches,
         tags, 
         new ArrayList<>(trees.values()), 
         blobs, commits
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
          .filter(tenant -> {
            
            return tenant.getName().equals(tenanPrefix) || 
                tenant.getPrefix().equals(tenanPrefix) || 
                tenant.getId().equals(tenanPrefix);
          })
          .findFirst();
      return found.orElseThrow(() -> new RuntimeException("Cant find tenants: " + tenanPrefix));
    });
  }
  
  private Uni<List<OldGit.Blob>> getBlobs(String tenanPrefix) {
    final var sql = "select * from " + tenanPrefix + "git_blobs";
    return processAnyQuery(OldGit.Blob.class, sql, (row) -> new OldGit.Blob(row.getString("id"), getJsonValue(row, "value")));
  }
  private Uni<List<OldGit.Branch>> getBranches(String tenanPrefix) {
    final var sql = "select * from " + tenanPrefix + "git_refs";
    return processAnyQuery(OldGit.Branch.class, sql, (row) -> new OldGit.Branch(
        row.getString("name"),
        row.getString("commit"))
    );
  }
  
  private Uni<List<OldGit.Commit>> getCommits(String tenanPrefix) {
    final var sql = "select * from " + tenanPrefix + "git_commits";
    return processAnyQuery(OldGit.Commit.class, sql, (row) -> new OldGit.Commit(
        row.getString("id"),
        row.getString("author"),
        LocalDateTime.parse(row.getString("datetime")),
        row.getString("message"),
        Optional.ofNullable(row.getString("parent")),
        Optional.ofNullable(row.getString("merge")),
        row.getString("tree")));
  }
  
  private Uni<List<OldGit.Tag>> getTags(String tenanPrefix) {
    final var sql = "select * from " + tenanPrefix + "git_tags";
    return processAnyQuery(OldGit.Tag.class, sql, (row) -> new OldGit.Tag(
        row.getString("id"),
        row.getString("commit"),
        LocalDateTime.parse(row.getString("datetime")),
        row.getString("author"),
        row.getString("message")
    ));
  }
  
  private Uni<List<OldGit.TreeValue>> getTreeValues(String tenanPrefix) {
    final var sql = "select * from " + tenanPrefix + "git_treeitems";
    return processAnyQuery(OldGit.TreeValue.class, sql, (row) -> new OldGit.TreeValue(
        row.getString("name"),
        row.getString("blob"),
        row.getString("tree")
      )
    );
  }
  
  
  private Uni<List<OldGit.Tree>> getTrees(String tenanPrefix) {
    final var sql = "select * from " + tenanPrefix + "git_trees";
    return processAnyQuery(OldGit.Tree.class, sql, (row) -> new OldGit.Tree(
        row.getString("id"),
        new ArrayList<>()
      ));
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
