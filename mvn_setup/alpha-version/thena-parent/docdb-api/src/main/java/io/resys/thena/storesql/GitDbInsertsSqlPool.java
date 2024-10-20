package io.resys.thena.storesql;


import io.resys.thena.api.entities.git.Blob;
import io.resys.thena.api.entities.git.Branch;
import io.resys.thena.api.entities.git.Commit;
import io.resys.thena.api.entities.git.Tag;
import io.resys.thena.api.entities.git.Tree;
import io.resys.thena.api.envelope.ImmutableMessage;
import io.resys.thena.api.registry.GitRegistry;
import io.resys.thena.datasource.ThenaSqlDataSource;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler.SqlTupleFailed;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler.SqlTupleListFailed;
import io.resys.thena.storesql.support.Execute;
import io.resys.thena.structures.BatchStatus;
import io.resys.thena.structures.UpsertStatus;
import io.resys.thena.structures.git.GitInserts;
import io.resys.thena.structures.git.ImmutableGitBatch;
import io.resys.thena.structures.git.ImmutableInsertResult;
import io.resys.thena.structures.git.ImmutableUpsertResult;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GitDbInsertsSqlPool implements GitInserts {
  private final ThenaSqlDataSource wrapper;
  private final GitRegistry registry;
  private final ThenaSqlDataSourceErrorHandler errorHandler;
  
  public GitDbInsertsSqlPool(ThenaSqlDataSource dataSource) {
    this.wrapper = dataSource;
    this.registry = dataSource.getRegistry().git();
    this.errorHandler = dataSource.getErrorHandler();
  }

  @Override
  public Uni<InsertResult> tag(Tag tag) {
    final var tagInsert = registry.tags().insertOne(tag);
    return wrapper.getClient().preparedQuery(tagInsert.getValue()).execute(tagInsert.getProps())
        .onItem().transform(inserted -> (InsertResult) ImmutableInsertResult.builder().duplicate(false).build())
        .onFailure(e -> errorHandler.duplicate(e))
        .recoverWithItem(e -> ImmutableInsertResult.builder().duplicate(true).build())
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlTupleFailed("Can't insert into 'TAG'!", tagInsert, e)));
  }

  @Override
  public Uni<UpsertResult> blob(Blob blob) {
    final var blobsInsert = registry.blobs().insertOne(blob);
    
    return wrapper.getClient().preparedQuery(blobsInsert.getValue()).execute(blobsInsert.getProps())
        .onItem()
        .transform(updateResult -> (UpsertResult) ImmutableUpsertResult.builder()
            .id(blob.getId())
            .isModified(true)
            .target(blob)
            .status(UpsertStatus.OK)
            .message(ImmutableMessage.builder()
                .text(new StringBuilder()
                    .append("Blob with id:")
                    .append(" '").append(blob.getId()).append("'")
                    .append(" has been saved.")
                    .toString())
                .build())
            .build()
        )
        .onFailure(e -> errorHandler.duplicate(e))
        .recoverWithItem(e -> (UpsertResult) ImmutableUpsertResult.builder()
            .id(blob.getId())
            .isModified(false)
            .target(blob)
            .status(UpsertStatus.OK)
            .message(ImmutableMessage.builder()
                .text(new StringBuilder()
                    .append("Blob with id:")
                    .append(" '").append(blob.getId()).append("'")
                    .append(" is already saved.")
                    .toString())
                .build())
            .build())
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlTupleFailed("Can't insert into 'BLOB'!", blobsInsert, e)));
  }

  public Uni<UpsertResult> ref(Branch ref, Commit commit) {
    final var findByName = registry.branches().getByName(ref.getName());
    return wrapper.getClient().preparedQuery(findByName.getValue())
        .mapping(registry.branches().defaultMapper())
        .execute(findByName.getProps())
    .onItem().transformToUni(item -> {
      final var exists = item.iterator();
      if(!exists.hasNext()) {
        return createRef(ref, commit);
      }
      return updateRef(exists.next(), commit);
    });
  }
  
  
  
  public Uni<UpsertResult> updateRef(Branch ref, Commit commit) {
    final var refInsert = registry.branches().updateOne(ref, commit);
    return wrapper.getClient().preparedQuery(refInsert.getValue()).execute(refInsert.getProps())
        .onItem()
        .transform(updateResult -> {

          if(updateResult.rowCount() == 1) {
            return (UpsertResult) ImmutableUpsertResult.builder()
                .id(ref.getName())
                .isModified(true)
                .status(UpsertStatus.OK)
                .target(ref)
                .message(ImmutableMessage.builder()
                    .text(new StringBuilder()
                        .append("Ref with id:")
                        .append(" '").append(ref.getName()).append("'")
                        .append(" has been updated.")
                        .toString())
                    .build())
                .build();
          }
          return (UpsertResult) ImmutableUpsertResult.builder()
              .id(ref.getName())
              .isModified(false)
              .status(UpsertStatus.CONFLICT)
              .target(ref)
              .message(ImmutableMessage.builder()
                  .text(new StringBuilder()
                      .append("Ref with")
                      .append(" id: '").append(ref.getName()).append("',")
                      .append(" commit: '").append(ref.getCommit()).append("'")
                      .append(" is behind of the head.")
                      .toString())
                  .build())
              .build();
        });
  }
  
  
  private Uni<UpsertResult> createRef(Branch ref, Commit commit) {
    final var refsInsert = registry.branches().insertOne(ref);
    return wrapper.getClient().preparedQuery(refsInsert.getValue()).execute(refsInsert.getProps())
        .onItem()
        .transform(updateResult -> (UpsertResult) ImmutableUpsertResult.builder()
            .id(ref.getName())
            .isModified(true)
            .target(ref)
            .status(UpsertStatus.OK)
            .message(ImmutableMessage.builder()
                .text(new StringBuilder()
                    .append("Ref with id:")
                    .append(" '").append(ref.getName()).append("'")
                    .append(" has been created.")
                    .toString())
                .build())
            .build()
        )
        .onFailure(e -> errorHandler.duplicate(e))
        .recoverWithItem(e -> (UpsertResult) ImmutableUpsertResult.builder()
          .id(ref.getName())
          .isModified(false)
          .target(ref)
          .status(UpsertStatus.CONFLICT)
          .message(ImmutableMessage.builder()
              .text(new StringBuilder()
                  .append("Ref with id:")
                  .append(" '").append(ref.getName()).append("'")
                  .append(" is already created.")
                  .toString())
              .build())
          .build())
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlTupleFailed("Can't insert into 'REF'!", refsInsert, e)));
  }

  @Override
  public Uni<UpsertResult> tree(Tree tree) {
    final var treeInsert = registry.trees().insertOne(tree);
    final var treeValueInsert = registry.treeValues().insertAll(tree);
    
    RepoAssert.isTrue(this.wrapper.getTx().isPresent(), () -> "Transaction must be started!");
    final var tx = wrapper.getClient();
    
    return tx.preparedQuery(treeInsert.getValue()).execute(treeInsert.getProps())
    .onItem().transformToUni(junk -> tx.preparedQuery(treeValueInsert.getValue()).executeBatch(treeValueInsert.getProps()))
    .onItem().transform(updateResult -> (UpsertResult) ImmutableUpsertResult.builder()
        .id(tree.getId())
        .isModified(true)
        .target(tree)
        .status(UpsertStatus.OK)
        .message(ImmutableMessage.builder()
            .text(new StringBuilder()
                .append("Tree with id:")
                .append(" '").append(tree.getId()).append("'")
                .append(" has been saved.")
                .toString())
            .build())
        .build()
    )
    .onFailure(e -> errorHandler.duplicate(e))
    .recoverWithItem(e -> (UpsertResult) ImmutableUpsertResult.builder()
        .id(tree.getId())
        .isModified(false)
        .target(tree)
        .status(UpsertStatus.OK)
        .message(ImmutableMessage.builder()
            .text(new StringBuilder()
                .append("Tree with id:")
                .append(" '").append(tree.getId()).append("'")
                .append(" is already saved.")
                .toString())
            .build())
        .build())
    .onFailure().invoke(e -> errorHandler.deadEnd(new SqlTupleListFailed("Can't insert into "
        +"\r\n"
        + "'TREE': " + treeInsert.getValue() 
        + "\r\n"
        + "  and/or"
        + "\r\n "
        + "'TREE_VALUE'!", treeValueInsert, e)));
  }
  
  @Override
  public Uni<UpsertResult> commit(Commit commit) {
    final var commitsInsert = registry.commits().insertOne(commit);
    return wrapper.getClient().preparedQuery(commitsInsert.getValue()).execute(commitsInsert.getProps())
        .onItem()
        .transform(updateResult -> (UpsertResult) ImmutableUpsertResult.builder()
            .id(commit.getId())
            .isModified(true)
            .target(commit)
            .status(UpsertStatus.OK)
            .message(ImmutableMessage.builder()
                .text(new StringBuilder()
                    .append("Commit with id:")
                    .append(" '").append(commit.getId()).append("'")
                    .append(" has been saved.")
                    .toString())
                .build())
            .build()
        )
        .onFailure(e -> errorHandler.duplicate(e))
        .recoverWithItem(e -> (UpsertResult) ImmutableUpsertResult.builder()
            .id(commit.getId())
            .isModified(false)
            .target(commit)
            .status(UpsertStatus.CONFLICT)
            .message(ImmutableMessage.builder()
                .text(new StringBuilder()
                    .append("Commit with id:")
                    .append(" '").append(commit.getId()).append("'")
                    .append(" is already saved.")
                    .toString())
                .build())
            .build())
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlTupleFailed("Can't insert into 'COMMIT'!", commitsInsert, e)));
  }
 
  
  @Override
  public Uni<GitBatch> batch(GitBatch output) {    
    final var blobsInsert = registry.blobs().insertAll(output.getBlobs());
    final var treeInsert = registry.trees().insertOne(output.getTree());
    final var treeValueInsert = registry.treeValues().insertAll(output.getTree());
    final var commitsInsert = registry.commits().insertOne(output.getCommit());
    
    RepoAssert.isTrue(this.wrapper.getTx().isPresent(), () -> "Transaction must be started!");
    final var tx = wrapper.getClient();    
    
    if(blobsInsert.getProps().isEmpty() && output.getDeleted() == 0) {
      return Uni.createFrom().item(successOutput(output, "No new blobs provided or tree values to delete, nothing to save"));
    } 
    
    final Uni<GitBatch> blobUni;
    if(blobsInsert.getProps().isEmpty()) {
      blobUni = Uni.createFrom().item(successOutput(output, "Skipping blobs because nothing provided"));
    } else {
      blobUni = Execute.apply(tx, blobsInsert).onItem()
        .transform(row -> successOutput(output, "Blobs saved, number of new entries: " + row.rowCount()))
        .onFailure().transform(e -> failOutput(output, "Failed to create blobs", e));
    }
    
    final var treeUni = Execute.apply(tx, treeInsert).onItem()
      .transform(row -> successOutput(output, "Tree saved, number of new entries: " + row.rowCount()))
      .onFailure().transform(e -> failOutput(output, "Failed to create tree \r\n" + output.getTree(), e));

    final Uni<GitBatch> treeValueUni;
    if(treeValueInsert.getProps().isEmpty()) {
      treeValueUni = Uni.createFrom().item(successOutput(output, "Tree Values saved, number of new entries: 0"));    
    } else {
      treeValueUni = Execute.apply(tx, treeValueInsert).onItem()
          .transform(row -> successOutput(output, "Tree Values saved, number of new entries: " + row.rowCount()))
          .onFailure().transform(e -> failOutput(output, "Failed to create tree values", e)); 
    }
    
    
    final var commitUni = Execute.apply(tx, commitsInsert).onItem()
        .transform(row -> successOutput(output, "Commit saved, number of new entries: " + row.rowCount()))
        .onFailure().transform(e -> failOutput(output, "Failed to create commit", e));
    
    final var refExists = output.getRef().getCreated();
    final var ref = output.getRef().getRef();
    
    
    final Uni<GitBatch> refUni;
    if(refExists) {
      refUni = Execute.apply(tx, registry.branches().updateOne(output.getRef().getRef(), output.getCommit()))
          .onItem().transform(row -> successOutput(output, "Existing ref: " + ref.getName() + ", updated with commit: " + ref.getCommit()))
          .onFailure().transform(e -> failOutput(output, "Failed to update ref", e));
    } else {
      refUni = Execute.apply(tx, registry.branches().insertOne(output.getRef().getRef()))
          .onItem().transform(row -> successOutput(output, "New ref created: " + ref.getName() + ": " + ref.getCommit()))
          .onFailure().transform(e -> failOutput(output, "Failed to create ref", e));
        
    }

    
    return Uni.combine().all().unis(blobUni, treeUni, treeValueUni, commitUni, refUni).asTuple()
        .onItem().transform(tuple -> merge(output, 
            tuple.getItem1(), 
            tuple.getItem2(), 
            tuple.getItem3(), 
            tuple.getItem4(), 
            tuple.getItem5()
        ))
        .onFailure(GitBatchException.class)
        .recoverWithUni((ex) -> {
          final var batchError = (GitBatchException) ex;
          return tx.rollback().onItem().transform(junk -> batchError.getBatch());
        });
  }

  
  private GitBatch merge(GitBatch start, GitBatch ... current) {
    final var builder = ImmutableGitBatch.builder().from(start);
    final var log = new StringBuilder(start.getLog().getText());
    var status = start.getStatus();
    for(GitBatch value : current) {
      if(status != BatchStatus.ERROR) {
        status = value.getStatus();
      }
      log.append("\r\n\r\n").append(value.getLog());
      builder.addAllMessages(value.getMessages());
    }
    
    return builder.status(status).build();
  }
  
  private GitBatch successOutput(GitBatch current, String msg) {
    return ImmutableGitBatch.builder()
      .from(current)
      .status(BatchStatus.OK)
      .addMessages(ImmutableMessage.builder().text(msg).build())
      .build();
  }
  
  private GitBatchException failOutput(GitBatch current, String msg, Throwable t) {
    log.error("Batch failed because of: " + msg, t);
    return new GitBatchException(ImmutableGitBatch.builder()
        .from(current)
        .status(BatchStatus.ERROR)
        .addMessages(ImmutableMessage.builder().text(t.getMessage()).build())
        .addMessages(ImmutableMessage.builder().text(msg).build())
        .build()); 
  }
  
  public static class GitBatchException extends RuntimeException {
    private static final long serialVersionUID = -7251738425609399151L;
    private final GitBatch batch;
    
    public GitBatchException(GitBatch batch) {
      this.batch = batch;
    }
    public GitBatch getBatch() {
      return batch;
    }
  } 
}
