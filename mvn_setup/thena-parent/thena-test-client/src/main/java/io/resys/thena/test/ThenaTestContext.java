package io.resys.thena.test;

/*-
 * #%L
 * thena-test-client
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

import java.time.Duration;

import org.testcontainers.containers.PostgreSQLContainer;

import io.vertx.core.VertxOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.pgclient.PgBuilder;
import io.vertx.mutiny.sqlclient.Pool;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.PoolOptions;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class ThenaTestContext {
  private static PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17");
  private Vertx vertx;
  private Pool pool;
  
  public void initialize(ThenaTest data) {

    vertx = Vertx.vertx(new VertxOptions()
    // .setWorkerPoolSize(10)
    // .setEventLoopPoolSize(4)
    );
    

    
    if(data.database().enabled()) {
      log.debug("using real db host: {}", data.database().host());
      this.pool = PgBuilder.pool()
        .with( new PoolOptions().setMaxSize(6))
        .connectingTo(new PgConnectOptions()
          .setDatabase(data.database().database())
          .setHost(data.database().host())
          .setPort(data.database().port())
          .setUser(data.database().user())
          .setPassword(data.database().password()))
        .using(vertx)
        .build();
    } else {
      
      POSTGRES.start();
      log.debug("using test container, container id: {}, jdbc: {}", 
        POSTGRES.getContainerId(),
        POSTGRES.getJdbcUrl()
      );
      this.pool = PgBuilder.pool()
        .with(new PoolOptions().setMaxSize(6))
        .connectingTo(new PgConnectOptions()
          .setDatabase(POSTGRES.getDatabaseName())
          .setHost(POSTGRES.getHost())
          .setPort(POSTGRES.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT))
          .setUser(POSTGRES.getUsername())
          .setPassword(POSTGRES.getPassword()))
        .using(vertx)
        .build();
    }
    
    // some hackaruu
    waitUntilPostgresqlAcceptsConnections(pool);
  }
  
  public void cleanup() {
    vertx.close()
      .onFailure()
      .retry().withBackOff(Duration.ofMillis(10), Duration.ofSeconds(3)).atMost(20)
      .await().atMost(Duration.ofSeconds(60));
    //POSTGRES.stop();
  }
  
  
  
  private void waitUntilPostgresqlAcceptsConnections(Pool pool) {
    // On some platforms there may be some delay before postgresql starts to respond.
    // Try until postgresql connection is successfully opened.
    final var connection = pool.getConnection()
      .onFailure()
      .retry().withBackOff(Duration.ofMillis(10), Duration.ofSeconds(3)).atMost(20)
      .await().atMost(Duration.ofSeconds(60));
    connection.closeAndForget();
  }
}
