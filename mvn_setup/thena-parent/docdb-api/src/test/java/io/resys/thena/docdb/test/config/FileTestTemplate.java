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

import io.resys.thena.docdb.api.DocDB;
import io.resys.thena.docdb.api.actions.RepoActions.RepoResult;
import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.api.models.Repo.RepoType;
import io.resys.thena.docdb.file.DocDBFactoryFile;
import io.resys.thena.docdb.file.FileErrors;
import io.resys.thena.docdb.file.spi.FilePoolImpl;
import io.resys.thena.docdb.spi.ClientCollections;
import io.resys.thena.docdb.spi.ClientState;
import io.resys.thena.docdb.spi.DocDBPrettyPrinter;
import io.resys.thena.docdb.spi.jackson.VertexExtModule;
import io.vertx.core.json.jackson.VertxModule;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class FileTestTemplate {
  private DocDB client;

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
  private BiConsumer<DocDB, Repo> callback;
  private Repo repo;
  
  public FileTestTemplate() {
  }
  public FileTestTemplate(BiConsumer<DocDB, Repo> callback) {
    this.callback = callback;
  }  
  public DocDB getClient() {
    return client;
  }
  public Repo getRepo() {
    return repo;
  }
  
  @BeforeEach
  public void setUp() {
    final var ctx = ClientCollections.defaults(db);
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
    
    repo = this.client.repo().projectBuilder().name("junit" + index.incrementAndGet(), RepoType.git).build().await().atMost(Duration.ofSeconds(10)).getRepo();
    if(callback != null) {
      callback.accept(client, repo);
    }
  }
  
  @AfterEach
  public void tearDown() {
  }

    
  public RepoResult createRepo(String name) {
    
    RepoResult repo = client.repo().projectBuilder()
        .name(name, RepoType.git)
        .build()
        .await().atMost(Duration.ofMinutes(1));
    
    log.debug("created repo {}", repo);
    return repo;
  }
  
  public DocDB getClient(Repo repo) {
    // final var ctx = ClientCollections.defaults(db).toRepo(repo);
    return client;
  }
  
  public ClientState createState() {
    final var ctx = ClientCollections.defaults(db);
    return DocDBFactoryFile.state(ctx, new FilePoolImpl(file, objectMapper), new FileErrors());
  }
  
  public void printRepo(Repo repo) {
    final var ctx = ClientCollections.defaults(db);
    final var state = DocDBFactoryFile.state(ctx, new FilePoolImpl(file, objectMapper), new FileErrors());
    
    final String result = new DocDBPrettyPrinter(state).print(repo);
    log.debug(result);
  }

}
