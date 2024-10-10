package io.resys.thena.docdb.spi.mongo;

/*-
 * #%L
 * thena-docdb-mongo
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

import com.mongodb.ErrorCategory;
import com.mongodb.MongoWriteException;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import io.resys.thena.docdb.api.models.ImmutableMessage;
import io.resys.thena.docdb.api.models.Objects.Blob;
import io.resys.thena.docdb.api.models.Objects.Commit;
import io.resys.thena.docdb.api.models.Objects.Ref;
import io.resys.thena.docdb.api.models.Objects.Tag;
import io.resys.thena.docdb.api.models.Objects.Tree;
import io.resys.thena.docdb.spi.ClientInsertBuilder;
import io.resys.thena.docdb.spi.ImmutableInsertResult;
import io.resys.thena.docdb.spi.ImmutableUpsertResult;
import io.resys.thena.docdb.spi.codec.RefCodec;
import io.resys.thena.docdb.spi.commits.CommitVisitor.CommitOutput;
import io.resys.thena.docdb.spi.commits.CommitVisitor.CommitOutputStatus;
import io.resys.thena.docdb.spi.commits.ImmutableCommitOutput;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public class MongoClientInsertBuilder implements ClientInsertBuilder {

  private final MongoClientWrapper wrapper;
  
  public MongoClientInsertBuilder(MongoClientWrapper wrapper) {
    this.wrapper = wrapper;
  }

  @Override
  public Uni<InsertResult> tag(Tag tag) {
    final var ctx = this.wrapper.getNames();
    return wrapper.getClient()
        .getDatabase(ctx.getDb())
        .getCollection(ctx.getTags(), Tag.class)
        .insertOne(tag)
        .onItem().transform(inserted -> (InsertResult) ImmutableInsertResult.builder().duplicate(false).build())
        .onFailure(e  -> {
          com.mongodb.MongoWriteException t = (MongoWriteException) e;
          return t.getError().getCategory() == ErrorCategory.DUPLICATE_KEY;
        })
        .recoverWithItem(e -> ImmutableInsertResult.builder().duplicate(true).build());
  }

  @Override
  public Uni<UpsertResult> blob(Blob blob) {
    final var ctx = this.wrapper.getNames();
    return wrapper.getClient()
        .getDatabase(ctx.getDb())
        .getCollection(ctx.getBlobs(), Blob.class)
        .insertOne(blob)
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
        .onFailure(e  -> {
          if(!(e instanceof com.mongodb.MongoWriteException)) {
            return false;
          }
          com.mongodb.MongoWriteException t = (MongoWriteException) e;
          return t.getError().getCategory() == ErrorCategory.DUPLICATE_KEY;
        })
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
            .build());
  }

  public Uni<UpsertResult> ref(Ref ref, Commit commit) {
    final var ctx = this.wrapper.getNames();
    return wrapper.getClient()
    .getDatabase(ctx.getDb())
    .getCollection(ctx.getRefs(), Ref.class)
    .find(Filters.eq(RefCodec.NAME, ref.getName()))
    .collect().first().onItem()
    .transformToUni(item -> {
      if(item == null) {
        return createRef(ref, commit);
      }
      return updateRef(ref, commit);
    });
  }
  
  public Uni<UpsertResult> updateRef(Ref ref, Commit commit) {
    final var filters = Filters.and(
        Filters.eq(RefCodec.NAME, ref.getName()),
        Filters.eq(RefCodec.COMMIT, commit.getParent().get())
      );
    final var updates = Updates.set(RefCodec.COMMIT, ref.getCommit());
    final var ctx = this.wrapper.getNames();
    return wrapper.getClient()
        .getDatabase(ctx.getDb())
        .getCollection(ctx.getRefs(), Ref.class)
        .updateOne(filters, updates)
        .onItem()
        .transform(updateResult -> {
          if(updateResult.getModifiedCount() == 1) {
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
    final var ctx = this.wrapper.getNames();
    return wrapper.getClient()
        .getDatabase(ctx.getDb())
        .getCollection(ctx.getRefs(), Ref.class)
        .insertOne(ref)
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
        .onFailure(e  -> {
          com.mongodb.MongoWriteException t = (MongoWriteException) e;
          return t.getError().getCategory() == ErrorCategory.DUPLICATE_KEY;
        })
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
          .build());
  }

  @Override
  public Uni<UpsertResult> tree(Tree tree) {
    final var ctx = this.wrapper.getNames();
    return wrapper.getClient()
        .getDatabase(ctx.getDb())
        .getCollection(ctx.getTrees(), Tree.class)
        .insertOne(tree)
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
        .onFailure(e  -> {
          com.mongodb.MongoWriteException t = (MongoWriteException) e;
          return t.getError().getCategory() == ErrorCategory.DUPLICATE_KEY;
        })
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
            .build());
  }

  @Override
  public Uni<UpsertResult> commit(Commit commit) {
    final var ctx = this.wrapper.getNames();
    return wrapper.getClient()
        .getDatabase(ctx.getDb())
        .getCollection(ctx.getCommits(), Commit.class)
        .insertOne(commit)
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
        .onFailure(e  -> {
          com.mongodb.MongoWriteException t = (MongoWriteException) e;
          return t.getError().getCategory() == ErrorCategory.DUPLICATE_KEY;
        })
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
            .build());
  }

  @Override
  public Uni<CommitOutput> output(CommitOutput output) {
    
    // save blobs
    return Multi.createFrom().items(output.getBlobs().stream())
    .onItem().transformToUni(blob -> blob(blob))
    .merge().collect().asList()
    .onItem().transform(upserts -> {
      final var result = ImmutableCommitOutput.builder()
          .from(output)
          .status(CommitOutputStatus.OK);
      upserts.forEach(blob -> result.addMessages(blob.getMessage()));
      return (CommitOutput) result.build();
    })
    
    // save tree
    .onItem().transformToUni(current -> tree(current.getTree())
      .onItem().transform(upsert -> (CommitOutput) ImmutableCommitOutput.builder()
        .from(current)
        .status(visitStatus(upsert))
        .addMessages(upsert.getMessage())
        .build())
    )
    
    // save commit
    .onItem().transformToUni(current -> {
      if(current.getStatus() == CommitOutputStatus.OK) {
        return commit(current.getCommit())
            .onItem().transform(upsert -> (CommitOutput) ImmutableCommitOutput.builder()
              .from(current)
              .status(visitStatus(upsert))
              .addMessages(upsert.getMessage())
              .build());
      }
      return Uni.createFrom().item(current);
    })
    
    // save ref
    .onItem().transformToUni(current -> {      
      if(current.getStatus() == CommitOutputStatus.OK) {
        return ref(output.getRef(), output.getCommit())
            .onItem().transform(upsert -> transformRef(upsert, current));
      }
      return Uni.createFrom().item(current);
    });
  }
  
  private CommitOutput transformRef(UpsertResult upsert, CommitOutput current) {
    return (CommitOutput) ImmutableCommitOutput.builder()
        .from(current)
        .status(visitStatus(upsert))
        .addMessages(upsert.getMessage())
        .build();
  }  
  
  private CommitOutputStatus visitStatus(UpsertResult upsert) {
    if(upsert.getStatus() == UpsertStatus.OK) {
      return CommitOutputStatus.OK;
    } else if(upsert.getStatus() == UpsertStatus.DUPLICATE) {
      return CommitOutputStatus.EMPTY;
    } else if(upsert.getStatus() == UpsertStatus.CONFLICT) {
      return CommitOutputStatus.CONFLICT;
    }
    return CommitOutputStatus.ERROR;
    
  }
}
