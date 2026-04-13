package io.resys.limaone.persistence.world;

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

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;

import com.google.common.hash.Hashing;

import io.resys.limaone.authoring.Authoring.WorldRef;
import io.resys.limaone.authoring.Authoring.WorldRefQuery;
import io.resys.limaone.authoring.ImmutableWorldRef;
import io.resys.thena.fs.api.FileSystem;
import io.resys.thena.fs.entities.Ref;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class WorldRefQueryImpl implements WorldRefQuery {
  private final FileSystem fileSystem;
  private final ScheduledExecutorService workerPool;
  private final Duration workerTimeout;
  private final String branchName;
  
  @Override
  public Uni<Optional<WorldRef>> findOne() {
    return fileSystem.withTenant()
        .branchQuery()
        .branchName(filter -> filter.equals(branchName))
        .findRefOnly()
        .onItem().transform(ref -> ref.map(this::mapTo));
  }

  @Override
  public Optional<WorldRef> findOneSync() {
    return findOne()
        .runSubscriptionOn(workerPool)
        .await().atMost(workerTimeout);
  }
  
  private WorldRef mapTo(Ref ref) {
    final var content = new StringBuilder()
        .append(branchName)
        .append("/").append(fileSystem.getTenantName())
        .append("/").append(ref.getCommitId());
    
    final var hash = Hashing.murmur3_128().hashString(content.toString(), StandardCharsets.UTF_8).toString();
    
    return ImmutableWorldRef.builder()
        .commitId(ref.getCommitId())
        .branchName(branchName)
        .tenantName(fileSystem.getTenantName())
        .hash(hash)
        .build();
  } 
}
