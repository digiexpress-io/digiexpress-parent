package io.digiexpress.client.tests.support;

import java.time.Duration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.digiexpress.client.api.Client;
import io.digiexpress.client.api.ClientStore;
import io.digiexpress.client.spi.ClientImpl;
import io.resys.thena.docdb.api.DocDB;
import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.spi.DbCollections;
import io.resys.thena.docdb.spi.DbState;
import io.resys.thena.docdb.sql.DbStateSqlImpl;
import io.resys.thena.docdb.sql.PgErrors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestCaseBuilder {
  public final ObjectMapper objectMapper;
  private final Client client;
  private final DocDB doc;
  private final DbState docState;
  private TestCaseReader testCaseReader;
  
  public TestCaseBuilder(io.vertx.mutiny.pgclient.PgPool pgPool) {
    this.objectMapper = new ObjectMapper().registerModules(new JavaTimeModule(), new Jdk8Module(), new GuavaModule());
    this.doc = getClient(pgPool, "junit");
    this.docState = DbStateSqlImpl.state(DbCollections.defaults("junit"), pgPool, new PgErrors());
    this.client = ClientImpl.builder()
        .om(objectMapper)
        .defaultDialobEventPub()
        .defaultDialobFr()
        .defaultHdesDjc()
        .defaultHdesServiceInit()
        .repoStencil("")
        .repoDialob("")
        .repoHdes("")
        .repoService("")
        .doc(doc)
        .build();
  }
  
  public TestCaseBuilder testcases(String src) {
    this.testCaseReader = new TestCaseReader(objectMapper, src);
    return this;
  }
  
  public TestCaseReader reader() {
    return this.testCaseReader;
  }
  
  public String print(ClientStore store) {
    doc.repo().projectsQuery().findAll().collect().asList().await().atMost(Duration.ofMinutes(1))
    .forEach(e -> log.info("queried repo: " + e));
    
    Repo repo = doc.repo().projectsQuery().id(store.getRepoName()).get()
        .await().atMost(Duration.ofMinutes(1));
    
    return new RepoPrinter(docState, objectMapper).print(repo);
  }
  private DocDB getClient(io.vertx.mutiny.pgclient.PgPool pgPool, String db) {
    return DbStateSqlImpl.create().client(pgPool).db(db).errorHandler(new PgErrors()).build();
  }
  public Client getClient() {
    return client;
  }
}
