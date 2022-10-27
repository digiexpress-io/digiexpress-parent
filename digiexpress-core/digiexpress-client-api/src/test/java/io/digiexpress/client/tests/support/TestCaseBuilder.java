package io.digiexpress.client.tests.support;

import java.time.Duration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.digiexpress.client.api.ServiceClient;
import io.digiexpress.client.api.ServiceStore;
import io.digiexpress.client.spi.ServiceClientImpl;
import io.resys.thena.docdb.api.DocDB;
import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.spi.ClientCollections;
import io.resys.thena.docdb.spi.ClientState;
import io.resys.thena.docdb.spi.pgsql.PgErrors;
import io.resys.thena.docdb.sql.DocDBFactorySql;


public class TestCaseBuilder {
  private final ObjectMapper objectMapper;
  private final ServiceClient client;
  private final DocDB doc;
  private final ClientState docState;
  private TestCaseReader testCaseReader;
  
  public TestCaseBuilder(io.vertx.mutiny.pgclient.PgPool pgPool) {
    this.objectMapper = new ObjectMapper().registerModules(new JavaTimeModule(), new Jdk8Module(), new GuavaModule());
    this.doc = getClient(pgPool, "junit");
    this.docState = DocDBFactorySql.state(ClientCollections.defaults("junit"), pgPool, new PgErrors());
    this.client = ServiceClientImpl.builder()
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
  
  public String print(ServiceStore store) {
    Repo repo = doc.repo().query().id(store.getRepoName()).get()
        .await().atMost(Duration.ofMinutes(1));
    
    return new RepoPrinter(docState, objectMapper).print(repo);
  }
  
  private DocDB getClient(io.vertx.mutiny.pgclient.PgPool pgPool, String db) {
    return DocDBFactorySql.create().client(pgPool).db(db).errorHandler(new PgErrors()).build();
  }

  public ServiceClient getClient() {
    return client;
  }
   
}
