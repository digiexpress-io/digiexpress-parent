package io.resys.limaone.spi.runtime;

/*-
 * #%L
 * limaone-compiler
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
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
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

import io.resys.limaone.ast.AST_Parser;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.limaone.persistence.ModelWorldDb;
import io.resys.limaone.persistence.world.ModelWorldDb_FS;
import io.resys.limaone.persistence.world.WorldBuilderImpl;
import io.resys.limaone.program.ImmutableCurrentUser;
import io.resys.limaone.program.Runtime;
import io.resys.limaone.program.Runtime.CurrentUser;
import io.resys.limaone.program.Runtime.EnvironmentProperties;
import io.resys.limaone.program.Runtime.TagomiPdfRenderer;
import io.resys.limaone.program.TagomiProgram;
import io.resys.limaone.spi.ast.AST_ParserImpl;
import io.resys.limaone.spi.dialob.FormDb;
import io.resys.thena.api.actions.TenantActions.TenantAware;
import io.resys.thena.api.actions.TenantActions.TenantDb;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.resys.thena.fs.spi.FileSystem_ThenaImpl;
import io.resys.thena.storesql.PgErrors;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.vertx.mutiny.sqlclient.Pool;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
public class DefaultEnvironmentProperties implements EnvironmentProperties {
  
  private final ScheduledExecutorService workerPool;
  private final Duration workerPoolMaxTimeout;
  private final boolean isDev;
  
  private final FormDb formDb;
  private final String defaultTenantName;
  private final DI di; 
  private final TenantDb tenantDb;
  private final ModelWorldDb modelDb;
  private final Supplier<CurrentUser> currentUser;
  private final AST_Parser astParser;
  private final TagomiPdfRenderer tagomiPdfRenderer;

  public DefaultEnvironmentProperties(
      ScheduledExecutorService workerPool, Duration workerPoolMaxTimeout,
      Boolean isDev,
      String defaultTenantName,
      FormDb formDb,
      DI di, boolean tid,
      ModelWorldDb modelDb,
      AST_Parser ast,
      Supplier<CurrentUser> currentUser,
      TagomiPdfRenderer tagomiPdfRenderer) {
    super();
    this.workerPool = Objects.requireNonNull(workerPool, () -> "workerPool can't be null");;
    this.workerPoolMaxTimeout = Objects.requireNonNull(workerPoolMaxTimeout, () -> "workerPoolMaxTimeout can't be null");;
    this.isDev = Objects.requireNonNull(isDev, () -> "isDev can't be null");;
    this.formDb = Objects.requireNonNull(formDb, () -> "formDb can't be null");;
    this.defaultTenantName = Objects.requireNonNull(defaultTenantName, () -> "defaultTenantName can't be null");
    this.di = Objects.requireNonNull(di, () -> "di can't be null");


    this.currentUser = Objects.requireNonNull(currentUser, () -> "currentUser can't be null");
    this.astParser = Objects.requireNonNull(ast, () -> "ast can't be null");
    this.tagomiPdfRenderer = Objects.requireNonNull(tagomiPdfRenderer, () -> "tagomiPdfRenderer can't be null");

    if(modelDb instanceof TenantAware && tid) {
      this.tenantDb = new TID_Resolver(this, (TenantAware<?>) modelDb); 
      this.modelDb = (ModelWorldDb) ((TenantAware<?>) modelDb).withTenantDb(tenantDb);
    } else {
      this.tenantDb = new DefaultTenant_TID(this);
      this.modelDb = modelDb;
    }
    
    Objects.requireNonNull(modelDb, () -> "modelDb can't be null");
    Objects.requireNonNull(tenantDb, () -> "tenantDb can't be null");
  }

  @Override
  public <T> T getBean(Class<T> type) {
    return di.getBean(type);
  }
  
  @FunctionalInterface
  public interface DI {
    <T> T getBean(Class<T> type);  
  }
  @FunctionalInterface
  public interface WSP {
    ModelWorld getWorld();
  } 

  private static class DI_NotSupported implements DI {
    @Override
    public <T> T getBean(Class<T> type) {
      throw new UnsupportedOperationException("DI is not enabled in this envir!");
    }
  }

  private static class TagomiPdfRenderer_NotSupported implements TagomiPdfRenderer {
    @Override
    public Uni<TagomiProgram.PdfResult> render(
        List<TagomiProgram.LocalizedPrintout> localizedPrintouts,
        String serviceName, String orchestratorName,
        Runtime runtime, String locale, JsonObject props) {
      throw new UnsupportedOperationException("TagomiPdfRenderer is not configured!");
    }
  }

  @RequiredArgsConstructor
  private static class DefaultTenant_TID implements TenantDb {
    private final EnvironmentProperties envir;
    @Override
    public String getTenantByAnything(String id) {
      return envir.getDefaultTenantName();
    }

    @Override
    public String getCurrentUserTenant() {
      return null;
    }
  }
  
  
  public record ModelDbConfig(WSP wsp, Pool pgPool, FileSystem_ThenaImpl filesystem){
    public static ModelDbConfig postgreSQL(io.vertx.mutiny.sqlclient.Pool pgPool) {
      return new ModelDbConfig(null, Objects.requireNonNull(pgPool, () -> "pgPool must be provided"), null);
    }
    public static ModelDbConfig external(WSP userGiven) {
      return new ModelDbConfig(Objects.requireNonNull(userGiven, () -> "external asset source must be provided"), null, null);
    }
    public static ModelDbConfig filesystem(FileSystem_ThenaImpl fileSystem) {
      return new ModelDbConfig(null, null, Objects.requireNonNull(fileSystem, () -> "fileSystem must be provided"));
    }
  }
  
  
  public static Builder builder() {
    return new Builder();
  }
  
  @Setter @Accessors(chain = true, fluent = true)
  public static class Builder {
    private ScheduledExecutorService workerPool;
    private Duration workerPoolMaxTimeout;
    private FormDb formDb;
    private String defaultTenantName;
    private DI di; 
    private boolean tid = false;
    private Boolean developmentMode = false;
    private ModelDbConfig dbConfig;
    private Supplier<CurrentUser> currentUser;
    private AST_Parser ast;
    private TagomiPdfRenderer tagomiPdfRenderer;

    public DefaultEnvironmentProperties build() {
      final var workerPool = this.workerPool == null ? Infrastructure.getDefaultWorkerPool() : this.workerPool;
      final var workerPoolMaxTimeout = this.workerPoolMaxTimeout == null ? Duration.ofMinutes(1) : this.workerPoolMaxTimeout;
      final var di = this.di == null ? new DI_NotSupported() : this.di;

      final var formDb = this.formDb == null ? new FormDb_NotSupported(): this.formDb;
      
      Objects.requireNonNull(defaultTenantName, () -> "defaultTenantName must be provided");
      Objects.requireNonNull(dbConfig, () -> "dbConfig must be provided");
      
      final var astParser = this.ast == null ? AST_ParserImpl.builder().dev(developmentMode).build() : this.ast;

      final ModelWorldDb modelWorldDb;
      if(dbConfig.pgPool() != null) {

        final var fs = FileSystem_ThenaImpl.createInstance()
          .tenantName(defaultTenantName)
          .client(dbConfig.pgPool())
          .errorHandler(new PgErrors())
          .build();
        
        modelWorldDb = new ModelWorldDb_FS(formDb, fs, workerPool, workerPoolMaxTimeout, WorldBuilderImpl.branchName, Optional.empty());
      } else if(dbConfig.wsp() != null) {
        modelWorldDb = new ModelWorldDb_File(dbConfig.wsp(), formDb);
      } else if(dbConfig.filesystem() != null) {
        modelWorldDb = new ModelWorldDb_FS(formDb, dbConfig.filesystem(), workerPool, workerPoolMaxTimeout, WorldBuilderImpl.branchName, Optional.empty());
      } else {
        throw new NullPointerException("dbConfig.wsp or dbConfig.pgPool must be provided");  
      }
      
      if(currentUser == null) {
        currentUser = () -> ImmutableCurrentUser.builder().userId("not-used").userName("not used").build();  
      }
      
      final var tagomiPdfRenderer = this.tagomiPdfRenderer == null ? new TagomiPdfRenderer_NotSupported() : this.tagomiPdfRenderer;

      return new DefaultEnvironmentProperties(
          workerPool, workerPoolMaxTimeout,
          developmentMode, defaultTenantName,
          formDb, di, tid, modelWorldDb,
          astParser,
          currentUser,
          tagomiPdfRenderer
      );
    }    
  }

}
