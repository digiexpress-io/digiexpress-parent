package io.resys.thena.docdb.sql.queries.git;

/*-
 * #%L
 * thena-docdb-pgsql
 * %%
 * Copyright (C) 2021 Copyright 2021 ReSys OÜ
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

import io.resys.thena.docdb.api.models.ImmutableMessage;
import io.resys.thena.docdb.api.models.ThenaGitObject.Blob;
import io.resys.thena.docdb.api.models.ThenaGitObject.Branch;
import io.resys.thena.docdb.api.models.ThenaGitObject.Commit;
import io.resys.thena.docdb.api.models.ThenaGitObject.Tag;
import io.resys.thena.docdb.api.models.ThenaGitObject.Tree;
import io.resys.thena.docdb.spi.ErrorHandler;
import io.resys.thena.docdb.spi.GitDbInserts;
import io.resys.thena.docdb.spi.ImmutableBatch;
import io.resys.thena.docdb.spi.ImmutableInsertResult;
import io.resys.thena.docdb.spi.ImmutableUpsertResult;
import io.resys.thena.docdb.spi.support.RepoAssert;
import io.resys.thena.docdb.sql.SqlBuilder;
import io.resys.thena.docdb.sql.SqlMapper;
import io.resys.thena.docdb.sql.support.Execute;
import io.resys.thena.docdb.sql.support.SqlClientWrapper;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class GitDbInsertsSqlPool implements GitDbInserts {
  private final SqlClientWrapper wrapper;
  private final SqlMapper sqlMapper;
  private final SqlBuilder sqlBuilder;
  private final ErrorHandler errorHandler;
  

  @Override
  public Uni<InsertResult> tag(Tag tag) {
    final var tagInsert = sqlBuilder.tags().insertOne(tag);
    return wrapper.getClient().preparedQuery(tagInsert.getValue()).execute(tagInsert.getProps())
        .onItem().transform(inserted -> (InsertResult) ImmutableInsertResult.builder().duplicate(false).build())
        .onFailure(e -> errorHandler.duplicate(e))
        .recoverWithItem(e -> ImmutableInsertResult.builder().duplicate(true).build())
        .onFailure().invoke(e -> errorHandler.deadEnd("Can't insert into 'TAG': '" + tagInsert.getValue() + "'!", e));
  }

  @Override
  public Uni<UpsertResult> blob(Blob blob) {
    final var blobsInsert = sqlBuilder.blobs().insertOne(blob);
    
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
        .onFailure().invoke(e -> errorHandler.deadEnd("Can't insert into 'BLOB': '" + blobsInsert.getValue() + "'!", e));
  }

  public Uni<UpsertResult> ref(Branch ref, Commit commit) {
    final var findByName = sqlBuilder.refs().getByName(ref.getName());
    return wrapper.getClient().preparedQuery(findByName.getValue())
        .mapping(r -> sqlMapper.ref(r))
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
    final var refInsert = sqlBuilder.refs().updateOne(ref, commit);
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
    final var refsInsert = sqlBuilder.refs().insertOne(ref);
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
        .onFailure().invoke(e -> errorHandler.deadEnd("Can't insert into 'REF': '" + refsInsert.getValue() + "'!", e));
  }

  @Override
  public Uni<UpsertResult> tree(Tree tree) {
    final var treeInsert = sqlBuilder.trees().insertOne(tree);
    final var treeValueInsert = sqlBuilder.treeItems().insertAll(tree);
    
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
    .onFailure().invoke(e -> errorHandler.deadEnd("Can't insert into "
        +"\r\n"
        + "'TREE': " + treeInsert.getValue() 
        + "\r\n"
        + "  and/or"
        + "\r\n "
        + "'TREE_VALUE' : '" + treeValueInsert.getValue() + "'!", e));
  }
  
  @Override
  public Uni<UpsertResult> commit(Commit commit) {
    final var commitsInsert = sqlBuilder.commits().insertOne(commit);
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
        .onFailure().invoke(e -> errorHandler.deadEnd("Can't insert into 'COMMIT': '" + commitsInsert.getValue() + "'!", e));
  }
 
  
  @Override
  public Uni<Batch> batch(Batch output) {    
    final var blobsInsert = sqlBuilder.blobs().insertAll(output.getBlobs());
    final var treeInsert = sqlBuilder.trees().insertOne(output.getTree());
    final var treeValueInsert = sqlBuilder.treeItems().insertAll(output.getTree());
    final var commitsInsert = sqlBuilder.commits().insertOne(output.getCommit());
    
    RepoAssert.isTrue(this.wrapper.getTx().isPresent(), () -> "Transaction must be started!");
    final var tx = wrapper.getClient();    
    
    if(blobsInsert.getProps().isEmpty() && output.getDeleted() == 0) {
      return Uni.createFrom().item(successOutput(output, "No new blobs provided or tree values to delete, nothing to save"));
    } 
    
    final Uni<Batch> blobUni;
    if(blobsInsert.getProps().isEmpty()) {
      blobUni = Uni.createFrom().item(successOutput(output, "Skipping blobs because nothing provided"));
    } else {
      blobUni = Execute.apply(tx, blobsInsert).onItem()
        .transform(row -> successOutput(output, "Blobs saved, number of new entries: " + row.rowCount()))
        .onFailure().recoverWithItem(e -> failOutput(output, "Failed to create blobs", e));
    }
    
    final var treeUni = Execute.apply(tx, treeInsert).onItem()
      .transform(row -> successOutput(output, "Tree saved, number of new entries: " + row.rowCount()))
      .onFailure().recoverWithItem(e -> failOutput(output, "Failed to create tree \r\n" + output.getTree(), e));

    final Uni<Batch> treeValueUni;
    if(treeValueInsert.getProps().isEmpty()) {
      treeValueUni = Uni.createFrom().item(successOutput(output, "Tree Values saved, number of new entries: 0"));    
    } else {
      treeValueUni = Execute.apply(tx, treeValueInsert).onItem()
          .transform(row -> successOutput(output, "Tree Values saved, number of new entries: " + row.rowCount()))
          .onFailure().recoverWithItem(e -> failOutput(output, "Failed to create tree values", e)); 
    }
    
    
    final var commitUni = Execute.apply(tx, commitsInsert).onItem()
        .transform(row -> successOutput(output, "Commit saved, number of new entries: " + row.rowCount()))
        .onFailure().recoverWithItem(e -> failOutput(output, "Failed to create commit", e));
    
    final var refExists = output.getRef().getCreated();
    final var ref = output.getRef().getRef();
    
    
    final Uni<Batch> refUni;
    if(refExists) {
      refUni = Execute.apply(tx, sqlBuilder.refs().updateOne(output.getRef().getRef(), output.getCommit()))
          .onItem().transform(row -> successOutput(output, "Existing ref: " + ref.getName() + ", updated with commit: " + ref.getCommit()))
          .onFailure().recoverWithItem(e -> failOutput(output, "Failed to update ref", e));
    } else {
      refUni = Execute.apply(tx, sqlBuilder.refs().insertOne(output.getRef().getRef()))
          .onItem().transform(row -> successOutput(output, "New ref created: " + ref.getName() + ": " + ref.getCommit()))
          .onFailure().recoverWithItem(e -> failOutput(output, "Failed to create ref", e));
        
    }

    
    return Uni.combine().all().unis(blobUni, treeUni, treeValueUni, commitUni, refUni).asTuple()
        .onItem().transform(tuple -> merge(output, 
            tuple.getItem1(), 
            tuple.getItem2(), 
            tuple.getItem3(), 
            tuple.getItem4(), 
            tuple.getItem5()
        ));
  }

  
  private Batch merge(Batch start, Batch ... current) {
    final var builder = ImmutableBatch.builder().from(start);
    final var log = new StringBuilder(start.getLog().getText());
    var status = start.getStatus();
    for(Batch value : current) {
      if(status != BatchStatus.ERROR) {
        status = value.getStatus();
      }
      log.append("\r\n\r\n").append(value.getLog());
      builder.addAllMessages(value.getMessages());
    }
    
    return builder.status(status).build();
  }
  
  private Batch successOutput(Batch current, String msg) {
    return ImmutableBatch.builder()
      .from(current)
      .status(BatchStatus.OK)
      .addMessages(ImmutableMessage.builder().text(msg).build())
      .build();
  }
  
  private Batch failOutput(Batch current, String msg, Throwable t) {
    log.error("Batch failed because of: " + msg, t);
    return ImmutableBatch.builder()
        .from(current)
        .status(BatchStatus.ERROR)
        .addMessages(ImmutableMessage.builder().text(msg).build())
        .build(); 
  }
}
