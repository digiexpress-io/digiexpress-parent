package io.resys.limaone.program;

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

import java.io.Serializable;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

import org.immutables.value.Value;

import io.resys.limaone.ast.AST_Parser;
import io.resys.limaone.persistence.ModelWorldDb;
import io.resys.limaone.program.Compiler.Bundle;
import io.resys.limaone.spi.dialob.FormDb;
import io.resys.thena.api.actions.TenantActions.TenantDb;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;

public interface Runtime extends Serializable {
  Heap getHeap();

  EnvironmentProperties getProperties();

  Bundle getBundle();

  Bundle getCachelessBundle();

  /**
   * @param refId tenant id or lexical name or tenant member id(aka user)
   * @return
   */
  Runtime withTenant(Optional<String> tid);

  // dump everything from runtime
  interface Heap extends Serializable {
  }

  interface EnvironmentProperties {
    ScheduledExecutorService getWorkerPool();

    Duration getWorkerPoolMaxTimeout();

    String getDefaultTenantName();

    boolean isDev();

    AST_Parser getAstParser();

    ModelWorldDb getModelDb();

    FormDb getFormDb();

    TenantDb getTenantDb();
    
    TagomiPdfRenderer getTagomiPdfRenderer();

    <T> T getBean(Class<T> type);

    Supplier<CurrentUser> getCurrentUser();
  }

  @FunctionalInterface
  public interface TagomiPdfRenderer {
    Uni<TagomiProgram.PdfResult> render(
      List<TagomiProgram.LocalizedPrintout> localizedPrintouts,
      String serviceName,
      String orchestratorName,
      Runtime runtime, String locale, JsonObject props);
  }

  @Value.Immutable
  interface CurrentUser {
    String getUserId();

    String getUserName();
  }
}