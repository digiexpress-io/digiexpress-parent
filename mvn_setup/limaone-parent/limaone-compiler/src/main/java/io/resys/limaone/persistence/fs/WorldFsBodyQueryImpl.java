package io.resys.limaone.persistence.fs;

import java.time.Duration;

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

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;

import io.resys.limaone.authoring.Authoring.WorldFsBodyQuery;
import io.resys.limaone.fs.ImmutableArticlePageBody;
import io.resys.limaone.fs.WorldFsBody;
import io.resys.limaone.model.ArticlePage;
import io.resys.limaone.model.Model.BodyType;
import io.resys.thena.fs.api.FileSystem;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@RequiredArgsConstructor
@Slf4j
public class WorldFsBodyQueryImpl implements WorldFsBodyQuery {
  private final FileSystem filesystem;
  private final ScheduledExecutorService workerPool;
  private final Duration workerTimeout;
  private final String branchName;
  private BodyType bodyType;
  private String id;
  
  @Override
  public WorldFsBodyQuery id(String id) {
    this.id = id;
    return this;
  }
  @Override
  public WorldFsBodyQuery bodyType(BodyType bodyType) {
    this.bodyType = bodyType;
    return this;
  }
  
  @Override
  public Uni<WorldFsBody> getOne() {
    Objects.requireNonNull(id, () -> "id must be defined");
    Objects.requireNonNull(bodyType, () -> "bodyType must be defined");
    
    final var tenant = filesystem.withTenant();
    return tenant
      .branchQuery()
      .branchName(name -> name.equals(branchName))
      .blobTypes(Arrays.asList(BodyType.without(BodyType.DEPLOYMENT))
          .stream().map(e -> e.name())
          .toList().toArray(new String[]{}))
      .getOne()
      .onItem().transform(ref -> {
        final var node = ref.getTransitives().findOneNode(id)
            .orElseThrow(() -> new IllegalArgumentException("Node not found " + id));
      
      switch (bodyType) {
        case ARTICLE_PAGE: {
          final var page = node.getTransitives().getBlob().getBlobValue().mapTo(ArticlePage.class);
          return ImmutableArticlePageBody.builder()
              .content(page.getContent())
              .build();
         }
        default: throw new IllegalArgumentException("Unsupported body type: " + bodyType);
       }
      });
    
  }
  
  @Override
  public WorldFsBody getOneSync() {
    return getOne()
        .runSubscriptionOn(workerPool)
        .await().atMost(workerTimeout);
  }


}
