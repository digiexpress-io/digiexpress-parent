package io.dialob.client.spi.store;

import java.io.File;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.dialob.client.api.DialobStore;
import io.dialob.client.api.ImmutableStoreEntity;
import io.dialob.client.spi.DialobStoreTemplate;
import io.dialob.client.spi.store.DialobStoreConfig.AuthorProvider;
import io.dialob.client.spi.store.DialobStoreConfig.GidProvider;
import io.dialob.client.spi.support.DialobAssert;
import io.dialob.client.spi.support.OidUtils;
import io.resys.thena.api.ThenaClient;
import io.resys.thena.storefile.DocDBFactoryFile;
import io.resys.thena.storefile.FileErrors;
import io.resys.thena.storefile.spi.FilePoolImpl;
import io.resys.thena.storefile.tables.Table.FilePool;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DialobStoreFileImpl extends DialobStoreTemplate implements DialobStore {

  public DialobStoreFileImpl(DialobStoreConfig config) {
    super(config);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String repoName;
    private String headName;
    private ObjectMapper objectMapper;
    private GidProvider gidProvider;
    private AuthorProvider authorProvider;
    private String db;
    private FilePool pool;

    public Builder repoName(String repoName) {
      this.repoName = repoName;
      return this;
    }
    public Builder objectMapper(ObjectMapper objectMapper) {
      this.objectMapper = objectMapper;
      return this;
    }
    public Builder gidProvider(GidProvider gidProvider) {
      this.gidProvider = gidProvider;
      return this;
    }
    public Builder authorProvider(AuthorProvider authorProvider) {
      this.authorProvider = authorProvider;
      return this;
    }
    public Builder pgPool(FilePool pgPool) {
      this.pool = pgPool;
      return this;
    }
    public Builder headName(String headName) {
      this.headName = headName;
      return this;
    }
    public Builder pgDb(String pgDb) {
      this.db = pgDb;
      return this;
    }

    private GidProvider getGidProvider() {
      return this.gidProvider == null ? type -> {
        return OidUtils.gen();
     } : this.gidProvider;
    }

    private AuthorProvider getAuthorProvider() {
      return this.authorProvider == null ? ()-> "not-configured" : this.authorProvider;
    }

    private ObjectMapper getObjectMapper() {
      if(this.objectMapper != null) {
        return this.objectMapper;
      }
      return new ObjectMapper()
          .registerModules(
            new GuavaModule(),
            new JavaTimeModule(),
            new Jdk8Module()
          );
    }

    public DialobStoreTemplate build() {
      DialobAssert.notNull(repoName, () -> "repoName must be defined!");

      final var headName = this.headName == null ? "main": this.headName;
      if(log.isDebugEnabled()) {
        log.debug("""
          Configuring Thena:
            repoName: {}
            headName: {}
            objectMapper: {}
            gidProvider: {}
            authorProvider: {}
            db: {}
          """,
          this.repoName,
          headName,
          this.objectMapper == null ? "configuring" : "provided",
          this.gidProvider == null ? "configuring" : "provided",
          this.authorProvider == null ? "configuring" : "provided",
          this.db);
      }

      final ThenaClient thena;
      if(pool == null) {
        DialobAssert.notNull(db, () -> "db must be defined!");
        final var pgPool = new FilePoolImpl(new File(db), objectMapper);

        thena = DocDBFactoryFile.create().client(pgPool).db(repoName).errorHandler(new FileErrors()).build();
      } else {
        thena = DocDBFactoryFile.create().client(pool).db(repoName).errorHandler(new FileErrors()).build();
      }

      final ObjectMapper objectMapper = getObjectMapper();
      final ImmutableDialobStoreConfig config = ImmutableDialobStoreConfig.builder()
          .client(thena).repoName(repoName).headName(headName)
          .gidProvider(getGidProvider())
          .serializer((entity) -> JsonObject.mapFrom(ImmutableStoreEntity.builder().from(entity).build()))
          .deserializer(new BlobDeserializer(objectMapper))
          .authorProvider(getAuthorProvider())
          .build();
      return new DialobStoreTemplate(config);
    }
  }


}
