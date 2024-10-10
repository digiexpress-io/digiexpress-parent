package io.resys.thena.docdb.file.builders;

import java.util.List;

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
import io.resys.thena.docdb.api.models.Objects.Blob;
import io.resys.thena.docdb.api.models.Objects.Commit;
import io.resys.thena.docdb.api.models.Objects.Ref;
import io.resys.thena.docdb.api.models.Objects.Tag;
import io.resys.thena.docdb.api.models.Objects.Tree;
import io.resys.thena.docdb.file.FileBuilder;
import io.resys.thena.docdb.file.tables.Table.FileMapper;
import io.resys.thena.docdb.file.tables.Table.FilePool;
import io.resys.thena.docdb.file.tables.Table.FileTuple;
import io.resys.thena.docdb.file.tables.Table.FileTupleList;
import io.resys.thena.docdb.spi.ClientInsertBuilder;
import io.resys.thena.docdb.spi.ErrorHandler;
import io.resys.thena.docdb.spi.ImmutableInsertResult;
import io.resys.thena.docdb.spi.ImmutableUpsertResult;
import io.resys.thena.docdb.spi.commits.CommitVisitor.CommitOutput;
import io.resys.thena.docdb.spi.commits.CommitVisitor.CommitOutputStatus;
import io.resys.thena.docdb.spi.commits.ImmutableCommitOutput;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class ClientInsertBuilderFilePool implements ClientInsertBuilder {
  private final FilePool client;
  private final FileMapper mapper;
  private final FileBuilder sqlBuilder;
  private final ErrorHandler errorHandler;
  

  @Override
  public Uni<InsertResult> tag(Tag tag) {
    final var tagInsert = sqlBuilder.tags().insertOne(tag);
    return client.preparedQuery(tagInsert).execute()
        .onItem().transform(inserted -> (InsertResult) ImmutableInsertResult.builder().duplicate(false).build())
        .onFailure(e -> errorHandler.duplicate(e))
        .recoverWithItem(e -> ImmutableInsertResult.builder().duplicate(true).build())
        .onFailure().invoke(e -> errorHandler.deadEnd("Can't insert into 'TAG': '" + tagInsert.getValue() + "'!", e));
  }

  @Override
  public Uni<UpsertResult> blob(Blob blob) {
    final var blobsInsert = sqlBuilder.blobs().insertOne(blob);
    
    return client.preparedQuery(blobsInsert).execute()
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

  public Uni<UpsertResult> ref(Ref ref, Commit commit) {
    final var findByName = sqlBuilder.refs().getByName(ref.getName());
    return client.preparedQuery(findByName)
        .mapping(r -> mapper.ref(r))
        .execute()
    .onItem().transformToUni(item -> {
      final var exists = item.iterator();
      if(!exists.hasNext()) {
        return createRef(ref, commit);
      }
      return updateRef(exists.next(), commit);
    });
  }
  
  
  
  public Uni<UpsertResult> updateRef(Ref ref, Commit commit) {
    final var refInsert = sqlBuilder.refs().updateOne(ref, commit);
    return client.preparedQuery(refInsert).execute()
        .onItem()
        .transform(updateResult -> {

          if(updateResult.size() == 1) {
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
  
  
  private Uni<UpsertResult> createRef(Ref ref, Commit commit) {
    final var refsInsert = sqlBuilder.refs().insertOne(ref);
    return client.preparedQuery(refsInsert).execute()
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

    return client.preparedQuery(treeInsert).execute()
    .onItem().transformToUni(junk -> client.preparedQuery(treeValueInsert).execute())
    .onItem()
    .transform(updateResult -> (UpsertResult) ImmutableUpsertResult.builder()
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
    return client.preparedQuery(commitsInsert).execute()
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
  public Uni<CommitOutput> output(CommitOutput output) {    
    final var blobsInsert = sqlBuilder.blobs().insertAll(output.getBlobs());
    final var treeInsert = sqlBuilder.trees().insertOne(output.getTree());
    final var treeValueInsert = sqlBuilder.treeItems().insertAll(output.getTree());
    final var commitsInsert = sqlBuilder.commits().insertOne(output.getCommit());
    final var findRefByName = sqlBuilder.refs().getByName(output.getRef().getName());
    
    
      final Uni<CommitOutput> start;
      if(blobsInsert.getProps().isEmpty()) {
        start = Uni.createFrom().item(successOutput(output, "No new blobs provided, nothing to save"));
      } else {
        start = apply(client, blobsInsert).onItem()
        .transform(row -> successOutput(output, "Blobs saved, number of new entries: " + row.size()))
        .onFailure().recoverWithItem(e -> failOutput(output, "Failed to create blobs", e));
      }
      
      return start.chain(next -> {
        
        if(next.getStatus() == CommitOutputStatus.OK) {
          return apply(client, treeInsert).onItem()
              .transform(row -> successOutput(next, "Tree saved, number of new entries: " + row.size()))
              .onFailure().recoverWithItem(e -> failOutput(next, "Failed to create tree \r\n" + output.getTree(), e));
        }
        return Uni.createFrom().item(next);
      }).chain(next -> {
        if(next.getStatus() == CommitOutputStatus.OK) {
          if(treeValueInsert.getProps().isEmpty()) {
            return Uni.createFrom().item(successOutput(next, "Tree Values saved, number of new entries: 0"));    
          }
          
          return apply(client, treeValueInsert).onItem()
              .transform(row -> successOutput(next, "Tree Values saved, number of new entries: " + row.size()))
              .onFailure().recoverWithItem(e -> failOutput(next, "Failed to create tree values", e));
        }
        return Uni.createFrom().item(next);
      }).chain(next -> {
        if(next.getStatus() == CommitOutputStatus.OK) {
          return apply(client, commitsInsert).onItem()
              .transform(row -> successOutput(next, "Commit saved, number of new entries: " + row.size()))
              .onFailure().recoverWithItem(e -> failOutput(next, "Failed to create commit", e));
        }
        return Uni.createFrom().item(next);
      }).chain(next -> {
        if(next.getStatus() == CommitOutputStatus.OK) {
          return apply(client, findRefByName).onItem().transformToUni(item -> {
            final var exists = item.iterator();
            if(!exists.hasNext()) {
              return apply(client, sqlBuilder.refs().insertOne(next.getRef()))
                  .onItem().transform(row -> successOutput(next, "New ref created: " + next.getRef().getName() + ": " + next.getRef().getCommit()));
            }
            return apply(client, sqlBuilder.refs().updateOne(next.getRef(), next.getCommit()))
                .onItem().transform(row -> successOutput(next, "Existing ref: " + next.getRef().getName() + ", updated with commit: " + next.getRef().getCommit()));
          })
          .onFailure().recoverWithItem(e -> failOutput(output, "Failed to create/update ref", e)); 
        }
        return Uni.createFrom().item(next);
      });
  }
  
  private CommitOutput successOutput(CommitOutput current, String msg) {
    return ImmutableCommitOutput.builder()
      .from(current)
      .status(CommitOutputStatus.OK)
      .addMessages(ImmutableMessage.builder().text(msg).build())
      .build();
  }
  
  private CommitOutput failOutput(CommitOutput current, String msg, Throwable t) {
    return ImmutableCommitOutput.builder()
        .from(current)
        .status(CommitOutputStatus.ERROR)
        .addMessages(ImmutableMessage.builder().text(msg).build())
        .build(); 
  }
  
  public static Uni<List<Object>> apply(FilePool client, FileTupleList sql) {
    return client.preparedQuery(sql).execute()
      .onFailure().invoke(e -> {
      log.error(System.lineSeparator() +
          "Failed to execute command." + System.lineSeparator() +
          "  sql: " + sql.getValue() + System.lineSeparator() +
          "  error:" + e.getMessage(), e);
    });
  }
  
  
  public static Uni<List<Object>> apply(FilePool client, FileTuple sql) {
    return client.preparedQuery(sql).execute()
      .onFailure().invoke(e -> {
      log.error(System.lineSeparator() +
          "Failed to execute command." + System.lineSeparator() +
          "  sql: " + sql.getValue() + System.lineSeparator() +
          "  error:" + e.getMessage(), e);
    });
  }
}
