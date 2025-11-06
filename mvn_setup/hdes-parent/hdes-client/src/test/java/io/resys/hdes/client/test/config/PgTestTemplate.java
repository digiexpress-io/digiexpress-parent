package io.resys.hdes.client.test.config;

/*-
 * #%L
 * hdes-client-api
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
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

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;

import io.resys.hdes.client.api.HdesClient;
import io.resys.hdes.client.api.HdesComposer;
import io.resys.hdes.client.spi.HdesClientImpl;
import io.resys.hdes.client.spi.HdesComposerImpl;
import io.resys.hdes.client.spi.config.HdesClientConfig.DependencyInjectionContext;
import io.resys.hdes.client.spi.config.HdesClientConfig.ServiceInit;
import io.resys.hdes.client.spi.store.ThenaStore;
import io.resys.hdes.client.spi.util.RepositoryToStaticData;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.datasource.TenantCacheImpl;
import io.resys.thena.datasource.TenantContext;
import io.resys.thena.git.api.GitClient;
import io.resys.thena.git.api.GitDataSource;
import io.resys.thena.git.spi.GitDataSourceImpl;
import io.resys.thena.git.spi.GitPrinter;
import io.resys.thena.test.ThenaTest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ThenaTest
public class PgTestTemplate {
  private ThenaStore store;
  
  io.vertx.mutiny.sqlclient.Pool pgPool;

  @BeforeEach
  public void setUp(io.vertx.mutiny.sqlclient.Pool pgPool) {
    this.pgPool = pgPool;
    final AtomicInteger gid = new AtomicInteger(0);
    this.store = ThenaStore.builder()
        .repoName("")
        .pgPool(pgPool)
        .objectMapper(TestUtils.objectMapper)
        .gidProvider((type) -> type + "-" + gid.incrementAndGet())
        .build();
  }

  private GitDataSource createState(String repoName) {
    final var ctx = TenantContext.defaults(repoName);
    return GitDataSourceImpl.create(ctx, pgPool, new TenantCacheImpl());
  }
  
  public void printRepo(Tenant repo) {
    final String result = new GitPrinter(createState(repo.getName())).print(repo);
    log.debug(result);
  }

  public String toRepoExport(String repoName) {
    final var repo = getThena().git(repoName).tenants().get()
        .await().atMost(Duration.ofMinutes(1)).getRepo();
    final String result = new RepositoryToStaticData(createState(repoName)).print(repo);
    return result;
  }

  public GitClient getThena() {
    return store.getConfig().getClient();
  }
  
  public HdesClient getClient() {
    return HdesClientImpl.builder().objectMapper(TestUtils.objectMapper).store(store)
        .dependencyInjectionContext(new DependencyInjectionContext() {
          @Override
          public <T> T get(Class<T> type) {
            return null;
          }
        })
        .serviceInit(new ServiceInit() {
            @Override
            public <T> T get(Class<T> type) {
              try {
                return type.getDeclaredConstructor().newInstance();
              } catch(Exception e) {
                throw new RuntimeException(e.getMessage(), e);
              }
            }
          })
        .build();
  }
  
  public HdesComposer getComposer() {
    return new HdesComposerImpl(getClient());
  }
  
}
