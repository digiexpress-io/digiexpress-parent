package io.resys.thena.docdb.file;

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

import io.resys.thena.docdb.api.DocDB;
import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.file.builders.ClientInsertBuilderFilePool;
import io.resys.thena.docdb.file.builders.RepoBuilderFilePool;
import io.resys.thena.docdb.file.tables.ImmutableFileClientWrapper;
import io.resys.thena.docdb.file.tables.Table.FileMapper;
import io.resys.thena.docdb.file.tables.Table.FilePool;
import io.resys.thena.docdb.spi.DbCollections;
import io.resys.thena.docdb.spi.DbState;
import io.resys.thena.docdb.spi.DocDBDefault;
import io.resys.thena.docdb.spi.DocDbState;
import io.resys.thena.docdb.spi.ErrorHandler;
import io.resys.thena.docdb.spi.GitDbState;
import io.resys.thena.docdb.spi.GitDbInserts;
import io.resys.thena.docdb.spi.GitDbQueries;
import io.resys.thena.docdb.spi.support.RepoAssert;
import io.smallrye.mutiny.Uni;

public class DocDBFactoryFile {

  public static Builder create() {
    return new Builder();
  }

  public static DbState state(DbCollections ctx, FilePool client, ErrorHandler handler) {
    return new DbState() {
      @Override
      public ErrorHandler getErrorHandler() {
        return handler;
      }
      @Override
      public DbCollections getCollections() {
        return ctx;
      }
      @Override
      public RepoBuilder project() {
        return new RepoBuilderFilePool(client, ctx, sqlMapper(ctx), sqlBuilder(ctx), handler);
      }
      @Override
      public GitDbState toGitState() {
        return new GitDbState() {
          @Override
          public <R> Uni<R> withTransaction(String repoId, String headName, TransactionFunction<R> callback) {
            return project().getByNameOrId(repoId).onItem().transformToUni(repo -> {
              final GitRepo repoState = withRepo(repo);
              return callback.apply(repoState);
            });
          }
          @Override
          public Uni<GitRepo> withRepo(String repoNameOrId) {
            return project().getByNameOrId(repoNameOrId).onItem().transform(repo -> withRepo(repo));
          }
          @Override
          public Uni<GitDbInserts> insert(String repoNameOrId) {
            return project().getByNameOrId(repoNameOrId).onItem().transform(repo -> insert(repo));
          }
          @Override
          public GitDbInserts insert(Repo repo) {
            final var wrapper = ImmutableFileClientWrapper.builder()
                .repo(repo)
                .client(client)
                .names(ctx.toRepo(repo))
                .build();
            return new ClientInsertBuilderFilePool(wrapper.getClient(), sqlMapper(wrapper.getNames()), sqlBuilder(wrapper.getNames()), handler);
          }
          @Override
          public Uni<GitDbQueries> query(String repoNameOrId) {
            return project().getByNameOrId(repoNameOrId).onItem().transform(repo -> query(repo));
          }
          @Override
          public GitDbQueries query(Repo repo) {
            final var wrapper = ImmutableFileClientWrapper.builder()
                .repo(repo)
                .client(client)
                .names(ctx.toRepo(repo))
                .build();
            return new ClientQueryFilePool(wrapper, sqlMapper(wrapper.getNames()), sqlBuilder(wrapper.getNames()), handler);
          }
          @Override
          public GitRepo withRepo(Repo repo) {
            final var wrapper = ImmutableFileClientWrapper.builder()
                .repo(repo)
                .client(client)
                .names(ctx.toRepo(repo))
                .build();
            return new GitRepo() {
              @Override
              public Repo getRepo() {
                return wrapper.getRepo();
              }
              @Override
              public String getRepoName() {
                return repo.getName();
              }
              @Override
              public GitDbQueries query() {
                return new ClientQueryFilePool(wrapper, sqlMapper(wrapper.getNames()), sqlBuilder(wrapper.getNames()), handler);
              }
              @Override
              public GitDbInserts insert() {
                return new ClientInsertBuilderFilePool(wrapper.getClient(), sqlMapper(wrapper.getNames()), sqlBuilder(wrapper.getNames()), handler);
              }
            };
          }
        };
      }
      @Override
      public DocDbState toDocState() {
        // TODO Auto-generated method stub
        throw new RuntimeException("not implemented");
      }
    };
  }

  public static FileBuilder sqlBuilder(DbCollections ctx) {
    return new DefaultFileBuilder(ctx);
  }
  public static FileMapper sqlMapper(DbCollections ctx) {
    return new DefaultFileMapper();
  }
  
  public static class Builder {
    private FilePool client;
    private String db = "docdb";
    private ErrorHandler errorHandler;
    
    public Builder errorHandler(ErrorHandler errorHandler) {
      this.errorHandler = errorHandler;
      return this;
    }
    public Builder db(String db) {
      this.db = db;
      return this;
    }
    public Builder client(FilePool client) {
      this.client = client;
      return this;
    }
    public DocDB build() {
      RepoAssert.notNull(client, () -> "client must be defined!");
      RepoAssert.notNull(db, () -> "db must be defined!");
      RepoAssert.notNull(errorHandler, () -> "errorHandler must be defined!");

      final var ctx = DbCollections.defaults(db);
      return new DocDBDefault(state(ctx, client, errorHandler));
    }
  }
}
