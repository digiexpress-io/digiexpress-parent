package io.resys.thena.docdb.test.config;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2021 - 2022 Copyright 2021 ReSys OÜ
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

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.resys.thena.api.ThenaClient;
import io.resys.thena.api.actions.TenantActions.RepoResult;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.Tenant.RepoType;
import io.resys.thena.jackson.VertexExtModule;
import io.resys.thena.spi.DbCollections;
import io.resys.thena.spi.DbState;
import io.resys.thena.storefile.DocDBFactoryFile;
import io.resys.thena.storefile.FileErrors;
import io.resys.thena.storefile.spi.FilePoolImpl;
import io.resys.thena.structures.git.GitPrinter;
import io.vertx.core.json.jackson.VertxModule;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class FileTestTemplate {
  private ThenaClient client;

  //private File file = new File("src/test/resources");
  private final String db = "junit";
  private File file = new File("target");
  private ObjectMapper objectMapper = new ObjectMapper().registerModules(
      new JavaTimeModule(), 
      new Jdk8Module(), 
      new GuavaModule(),
      new VertxModule(),
      new VertexExtModule());
  private static AtomicInteger index = new AtomicInteger(1);
  private BiConsumer<ThenaClient, Tenant> callback;
  private Tenant repo;
  
  public FileTestTemplate() {
  }
  public FileTestTemplate(BiConsumer<ThenaClient, Tenant> callback) {
    this.callback = callback;
  }  
  public ThenaClient getClient() {
    return client;
  }
  public Tenant getRepo() {
    return repo;
  }
  
  @BeforeEach
  public void setUp() {
    final var ctx = DbCollections.defaults(db);
    final var file = new File(this.file, ctx.getDb());
    file.mkdir();

    try {
      FileUtils.cleanDirectory(file); 
    } catch(IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }

    this.client = DocDBFactoryFile.create()
        .db(db)
        .client(new FilePoolImpl(this.file, objectMapper))
        .errorHandler(new FileErrors())
        .build();
    
    repo = this.client.tenants().commit().name("junit" + index.incrementAndGet(), RepoType.git).build().await().atMost(Duration.ofSeconds(10)).getRepo();
    if(callback != null) {
      callback.accept(client, repo);
    }
  }
  
  @AfterEach
  public void tearDown() {
  }

    
  public RepoResult createRepo(String name) {
    
    RepoResult repo = client.tenants().commit()
        .name(name, RepoType.git)
        .build()
        .await().atMost(Duration.ofMinutes(1));
    
    log.debug("created repo {}", repo);
    return repo;
  }
  
  public ThenaClient getClient(Tenant repo) {
    // final var ctx = ClientCollections.defaults(db).toRepo(repo);
    return client;
  }
  
  public DbState createState() {
    final var ctx = DbCollections.defaults(db);
    return DocDBFactoryFile.state(ctx, new FilePoolImpl(file, objectMapper), new FileErrors());
  }
  
  public void printRepo(Tenant repo) {
    final var ctx = DbCollections.defaults(db);
    final var state = DocDBFactoryFile.state(ctx, new FilePoolImpl(file, objectMapper), new FileErrors());
    
    final String result = new GitPrinter(state).print(repo);
    log.debug(result);
  }

}
