package io.digiexpress.eveli.client.test.feedback;

/*-
 * #%L
 * eveli-client
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

import org.testcontainers.containers.PostgreSQLContainer;

import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.persistence.repositories.ProcessRepository;
import io.digiexpress.eveli.client.spi.crm.CustomerAccountClientImpl;
import io.digiexpress.eveli.client.spi.process.ProcessClientImpl;
import io.digiexpress.eveli.client.spi.task.ImmutableTaskStoreConfig;
import io.digiexpress.eveli.client.spi.task.TaskClientImpl;
import io.digiexpress.eveli.client.spi.task.TaskStoreImpl;
import io.resys.thena.api.actions.TenantActions.CreatedTenant;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.datasource.TenantCacheImpl;
import io.resys.thena.datasource.TenantContext;
import io.resys.thena.git.spi.GitDataSourceImpl;
import io.resys.thena.git.spi.GitPrinter;
import io.resys.thena.grim.api.GrimClient;
import io.resys.thena.grim.spi.GrimClientImpl;
import io.vertx.mutiny.sqlclient.Pool;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.PoolOptions;
import lombok.extern.slf4j.Slf4j;



@Slf4j
public class FeedbackTaskEnvirSetup {
  private final GrimClient dbState;
  private final io.vertx.mutiny.pgclient.PgPool pgPool;
  private final String repoId;
  
  public FeedbackTaskEnvirSetup(PostgreSQLContainer<?> cont, String repoId) {
    this.pgPool = io.vertx.mutiny.pgclient.PgPool.pool(
        new PgConnectOptions()
          .setHost(cont.getHost())
          .setPort(cont.getFirstMappedPort())
          .setDatabase(cont.getDatabaseName())
          .setUser(cont.getUsername())
          .setPassword(cont.getPassword()), 
        new PoolOptions().setMaxSize(5));
    waitUntilPostgresqlAcceptsConnections(pgPool);
    this.dbState = GrimClientImpl.create()
        .db("junit")
        .client(pgPool)
        .build();
    this.repoId = repoId;
  }


  private void waitUntilPostgresqlAcceptsConnections(Pool pool) {
    // On some platforms there may be some delay before postgresql starts to respond.
    // Try until postgresql connection is successfully opened.
    var connection = pool.getConnection()
            .onFailure()
            .retry().withBackOff(Duration.ofMillis(10), Duration.ofSeconds(3)).atMost(20)
            .await().atMost(Duration.ofSeconds(60));
    connection.closeAndForget();
  }

  public GrimClient getDbState() {
    return dbState;
  }
  
  public GitDataSourceImpl createState() {
    final var ctx = TenantContext.defaults("junit");
    return GitDataSourceImpl.create(ctx, pgPool, new TenantCacheImpl());
  }
  
  public void printRepo(Tenant repo) {
    final String result = new GitPrinter(createState()).print(repo);
    log.debug(result);
  }
  
  public void prettyPrint(String repoId) {
    Tenant repo = getDbState().grim(repoId).tenants().get()
        .await().atMost(Duration.ofMinutes(1)).getRepo();
    
    printRepo(repo);
  }

  public String toRepoExport(String repoId) {
    getDbState().grim(repoId).tenants().get()
        .await().atMost(Duration.ofMinutes(1)).getRepo();
    final String result = null;//new TestExporter(createState()).print(repo);
    return result;
  }

  
  public TaskClient getTaskClient(ProcessRepository proc) {
    final var config = ImmutableTaskStoreConfig.builder()
        .tenantName(repoId)
        .client(dbState)
        .build();
    final var store = new TaskStoreImpl(config);
    
    // create task project
    CreatedTenant repo = dbState.tenants().createOneTenant()
        .name(repoId, StructureType.grim)
        .build()
        .await().atMost(Duration.ofMinutes(1));
    log.info("Repo created: " + repo);
    
    final var customer = new CustomerAccountClientImpl(new ProcessClientImpl(proc, null, null));
    return new TaskClientImpl(null, null, null, store, customer);
  }
  
}
