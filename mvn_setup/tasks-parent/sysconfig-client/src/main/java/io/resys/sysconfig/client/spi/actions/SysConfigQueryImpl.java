package io.resys.sysconfig.client.spi.actions;

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

import java.time.Instant;
import java.util.Collection;
import java.util.List;

import io.resys.sysconfig.client.api.SysConfigClient.SysConfigQuery;
import io.resys.sysconfig.client.api.model.SysConfig;
import io.resys.sysconfig.client.spi.SysConfigStore;
import io.resys.sysconfig.client.spi.visitors.DeleteAllSysConfigsVisitor;
import io.resys.sysconfig.client.spi.visitors.FindAllSysConfigsVisitor;
import io.resys.sysconfig.client.spi.visitors.GetSysConfigVisitor;
import io.resys.sysconfig.client.spi.visitors.GetSysConfigsByIdsVisitor;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class SysConfigQueryImpl implements SysConfigQuery {
  private final SysConfigStore ctx;
  
  @Override
  public Uni<SysConfig> get(String id) {
    return ctx.getConfig().accept(new GetSysConfigVisitor(id));
  }
  
  @Override
  public Uni<List<SysConfig>> findAll() {
    return ctx.getConfig().accept(new FindAllSysConfigsVisitor());
  }

  @Override
  public Uni<List<SysConfig>> deleteAll(String userId, Instant targetDate) {
    return ctx.getConfig().accept(new DeleteAllSysConfigsVisitor(userId, targetDate))
        .onItem().transformToUni(unwrap -> unwrap);
  }
  
  @Override
  public Uni<List<SysConfig>> findByIds(Collection<String> customerIds) {
    return ctx.getConfig().accept(new GetSysConfigsByIdsVisitor(customerIds));
  }
}
