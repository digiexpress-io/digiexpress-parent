package io.digiexpress.tagomi.tests.config;

/*-
 * #%L
 * tagomi-client
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import io.digiexpress.tagomi.api.ImmutableImage;
import io.digiexpress.tagomi.api.ImmutableImageEnvlope;
import io.digiexpress.tagomi.api.ImmutableTagomiStoreConfig;
import io.digiexpress.tagomi.api.TagomiComposer;
import io.digiexpress.tagomi.api.TagomiImageStorage;
import io.digiexpress.tagomi.spi.TagomiComposerImpl;
import io.digiexpress.tagomi.spi.TagomiStoreImpl;
import io.digiexpress.tagomi.spi.json.FromJsonObject;
import io.digiexpress.tagomi.spi.json.ToJsonObject;
import io.resys.thena.api.actions.TenantActions.TenantCommitResult;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.datasource.TenantCacheImpl;
import io.resys.thena.datasource.TenantContext;
import io.resys.thena.git.api.GitClient;
import io.resys.thena.git.api.GitDataSource;
import io.resys.thena.git.spi.GitDataSourceImpl;
import io.resys.thena.git.spi.GitPrinter;
import io.resys.thena.test.ThenaTest;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@ThenaTest
public class PgTestTemplate {
  private GitClient client;

  io.vertx.mutiny.sqlclient.Pool pgPool;

  @BeforeEach
  public void setUp(io.vertx.mutiny.sqlclient.Pool pgPool) {
    this.pgPool = pgPool;
    this.client = GitDataSourceImpl.create()
        .db("junit")
        .client(pgPool)
        .build();
    this.client.tenants().commit().name("junit", StructureType.git).build();
  }
  
  @AfterEach
  public void tearDown() {
  }


  public GitClient getClient() {
    return client;
  }
  
  public GitDataSource createState() {
    final var ctx = TenantContext.defaults("junit");
    return GitDataSourceImpl.create(ctx, pgPool, new TenantCacheImpl());
  }
  
  public void printRepo(Tenant repo) {
    final String result = new GitPrinter(createState()).print(repo);
    log.debug(result);
  }
  
  public void prettyPrint(String repoId) {
    Tenant repo = getClient().git(repoId).tenants().get()
        .await().atMost(Duration.ofMinutes(1)).getRepo();
    
    printRepo(repo);
  }

  public String toRepoExport(String repoId) {
    Tenant repo = getClient().git(repoId).tenants().get()
        .await().atMost(Duration.ofMinutes(1)).getRepo();
    final String result = new GitPrinter(createState()).printWithStaticIds(repo);
    
    
    return result;
  }

  
  @SuppressWarnings("unused")
  public TagomiComposer createTenant(String repoId) {
    
    final GitClient client = getClient();
    final TenantCommitResult repo = getClient()
      .tenants()
      .commit()
      .name(repoId, StructureType.git)
      .build()
      .await().atMost(Duration.ofMinutes(1));
    final AtomicInteger gid = new AtomicInteger(0);
    
    final var config = ImmutableTagomiStoreConfig.builder()
      .client(client)
      .tenantName(repoId)
      .headName("tagomi-main")
      .deserializer(new FromJsonObject())
      .serializer(new ToJsonObject())
      .authorProvider(() -> "junit-test")
      .imageStorage(new TagomiImageStorage() {
        @Override
        public Uni<ImageEnvlope> write(byte[] body) {
          return Uni.createFrom()
              .item(ImmutableImageEnvlope.builder()
              .operationStatus(OperationStatus.OK)
              .object(ImmutableImage.builder().id("1234")
                  .body(body)
                  .build())
              .build());
        }
        @Override
        public Uni<ImageEnvlope> read(String id) {
          return Uni.createFrom().item(ImmutableImageEnvlope
              .builder()
              .operationStatus(OperationStatus.OK)
              .object(ImmutableImage.builder().id("1234")
                  .body(new byte[] {})
                  .build())
              .build());
        }
      })
      .build();
    
    return new TagomiComposerImpl(new TagomiStoreImpl(config), config.getImageStorage());
  }
 
  
  public static String toString( String resource) {
    try {
      return IOUtils.toString(PgTestTemplate.class.getClassLoader().getResource(resource), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
}
