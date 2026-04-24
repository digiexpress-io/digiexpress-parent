package io.resys.limaone.persistence.fs;

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
import java.util.Arrays;
import java.util.concurrent.ScheduledExecutorService;

import io.resys.limaone.authoring.Authoring.WorldFsQuery;
import io.resys.limaone.fs.WorldFs;
import io.resys.limaone.model.Model.BodyType;
import io.resys.thena.fs.api.FileSystem;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@RequiredArgsConstructor
@Slf4j
public class WorldFsQueryImpl implements WorldFsQuery {
  private final FileSystem filesystem;
  private final ScheduledExecutorService workerPool;
  private final Duration workerTimeout;
  private final String branchName;

  @Override
  public Uni<WorldFs> findAll() {
    final var tenant = filesystem.withTenant();    
    return tenant
      .branchQuery()
      .branchName(name -> name.equals(branchName))
      .blobTypes(
          Arrays.asList(BodyType.without(BodyType.DEPLOYMENT))
          .stream().map(e -> e.name())
          .toList().toArray(new String[]{}))
      .getOne()
      .onItem().transform(ref -> new WorldFsFactory(ref).create());
  }

  @Override
  public WorldFs findAllSync() {
    return findAll()
      .runSubscriptionOn(workerPool)
      .await().atMost(workerTimeout);
  }
}
