package io.resys.thena.tasks.client.spi.actions;

/*-
 * #%L
 * thena-tasks-client
 * %%
 * Copyright (C) 2021 - 2023 Copyright 2021 ReSys OÜ
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

import java.time.LocalDate;
import java.time.LocalDateTime;

import io.resys.thena.docdb.spi.support.RepoAssert;
import io.resys.thena.tasks.client.api.actions.ExportActions;
import io.resys.thena.tasks.client.api.model.Export;
import io.resys.thena.tasks.client.spi.store.DocumentStore;
import io.resys.thena.tasks.client.spi.visitors.ExportVisitor;
import io.smallrye.mutiny.Uni;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
public class ExportActionsImpl implements ExportActions {
  private final DocumentStore ctx;

  @Override
  public ExportQuery export() {
    return new ExportQueryImpl(ctx);
  }

  @RequiredArgsConstructor
  @Accessors(fluent = true, chain = true)
  @Getter(AccessLevel.NONE)
  @Data
  private static class ExportQueryImpl implements ExportQuery {
    private final DocumentStore ctx;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime targetDate;

    @Override
    public Uni<Export> build() {
      RepoAssert.notEmpty(name, () -> "name must be defined!");
      RepoAssert.notNull(startDate, () -> "startDate must be defined!");
      RepoAssert.notNull(endDate, () -> "endDate must be defined!");
      RepoAssert.isTrue(!endDate.isBefore(startDate), () -> "endDate cannot be before startDate!");
      
      return ctx.getConfig().accept(new ExportVisitor(name, startDate, endDate, targetDate));

    }
  }
}
