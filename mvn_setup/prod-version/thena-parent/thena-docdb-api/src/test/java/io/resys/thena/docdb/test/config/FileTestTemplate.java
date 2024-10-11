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
import java.time.Duration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.resys.thena.docdb.api.DocDB;
import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.file.DocDBFactoryFile;
import io.resys.thena.docdb.file.FileErrors;
import io.resys.thena.docdb.file.spi.FilePoolImpl;
import io.resys.thena.docdb.spi.ClientCollections;
import io.resys.thena.docdb.spi.ClientState;
import io.resys.thena.docdb.spi.DocDBPrettyPrinter;

public class FileTestTemplate {
  private DocDB client;

  //private File file = new File("src/test/resources");
  private final String db = "junit";
  private File file = new File("target");
  private ObjectMapper objectMapper = new ObjectMapper().registerModules(new JavaTimeModule(), new Jdk8Module(), new GuavaModule());
  
  @BeforeEach
  public void setUp() {
    final var ctx = ClientCollections.defaults(db);
    final var file = new File(this.file, ctx.getDb());
    file.mkdir();

    new File(file, ctx.getRepos()).delete();
    new File(file, ctx.getBlobs()).delete();
    new File(file, ctx.getCommits()).delete();
    new File(file, ctx.getRefs()).delete();
    new File(file, ctx.getTags()).delete();
    new File(file, ctx.getTrees()).delete();
    new File(file, ctx.getTreeItems()).delete();
    
    this.client = DocDBFactoryFile.create()
        .db(db)
        .client(new FilePoolImpl(this.file, objectMapper))
        .errorHandler(new FileErrors())
        .build();
    
    this.client.repo().create().name(db).build()
    .await()
    .atMost(Duration.ofSeconds(1));
  }
  
  @AfterEach
  public void tearDown() {
  }

  public DocDB getClient() {
    return client;
  }
  
  public ClientState createState() {
    final var ctx = ClientCollections.defaults(db);
    return DocDBFactoryFile.state(ctx, new FilePoolImpl(file, objectMapper), new FileErrors());
  }
  
  public void printRepo(Repo repo) {
    final String result = new DocDBPrettyPrinter(createState()).print(repo);
    System.out.println(result);
  }

}
