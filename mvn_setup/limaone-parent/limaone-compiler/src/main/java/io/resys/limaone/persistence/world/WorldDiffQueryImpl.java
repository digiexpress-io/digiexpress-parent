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

import static com.github.difflib.UnifiedDiffUtils.generateUnifiedDiff;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.difflib.DiffUtils;

import io.resys.limaone.authoring.Authoring.WorldDiffQuery;
import io.resys.limaone.model.ImmutableModelWorldDiff;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.limaone.model.Model.ModelWorldDiff;
import io.resys.limaone.persistence.AuthoringImpl.AuthoringConfig;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class WorldDiffQueryImpl implements WorldDiffQuery {

  private static final int NO_OF_CONTEXT_LINES = 2;
  private static final String LN = "\n";
  private static final String NEW_FILE_FLAG = "new file mode 100644"; 
  private static final String DELETED_FILE_FLAG = "deleted file mode 100644";

  private final AuthoringConfig config;
  private UUID baseId;
  private UUID targetId;

  @Override
  public WorldDiffQuery baseId(UUID baseId) {
    this.baseId = Objects.requireNonNull(baseId, () -> "baseId can not be null!");
    return this;
  }

  @Override
  public WorldDiffQuery targetId(UUID targetId) {
    this.targetId = Objects.requireNonNull(targetId, () -> "targetId can not be null!");
    return this;
  }

  @Override
  public Uni<ModelWorldDiff> findAll() {
    Objects.requireNonNull(targetId, () -> "targetId can not be null!");
    Objects.requireNonNull(baseId, () -> "baseId can not be null!");

    final var base_uni = config.getPersistence().worldQuery().commitId(baseId).findAll();
    final var target_uni = config.getPersistence().worldQuery().commitId(targetId).findAll();
    
    return Uni.combine().all().unis(base_uni, target_uni)
        .asTuple()
        .onItem().transform(tuple -> build(tuple.getItem1(), tuple.getItem2()));
  }

  @Override
  public ModelWorldDiff findAllSync() {
    return findAll()
      .runSubscriptionOn(config.getEnvir().getWorkerPool())
      .await().atMost(config.getEnvir().getWorkerPoolMaxTimeout());
  }
  
  public ModelWorldDiff build(ModelWorld base, ModelWorld target) {
    final var baseTag = new WorldSummaryQueryImpl(config).build(base);
    final var targetTag =  new WorldSummaryQueryImpl(config).build(target);

    final var baseAssets = Stream.of(
        baseTag.getFlows(),
        baseTag.getDecisions(),
        baseTag.getServices()
    ).flatMap(List::stream).collect(Collectors.toList());
    final var targetAssets = Stream.of(
        targetTag.getFlows(),
        targetTag.getDecisions(),
        targetTag.getServices()
    ).flatMap(List::stream).collect(Collectors.toList());

    final var diffBody = new StringBuilder();

    for (final var baseAsset : baseAssets) {
      final var targetAsset = targetAssets.stream()
          .filter(t -> t.getId().equals(baseAsset.getId())).findFirst();
      if (targetAsset.isEmpty()) {
        final var diff = String.join(LN, generateDiff(
            baseAsset.getBody(), null,
            baseAsset.getName(), null)
        );
        diffBody.append(LN).append(diff).append(LN).append(DELETED_FILE_FLAG);
        continue;
      }
      final var diff = String.join(LN, generateDiff(
          baseAsset.getBody(), targetAsset.get().getBody(),
          baseAsset.getName(), targetAsset.get().getName())
      );
      diffBody.append(LN).append(diff);
    }

    final var newAssets = targetAssets.stream()
        .filter(t -> baseAssets.stream().noneMatch(b -> b.getId().equals(t.getId())))
        .collect(Collectors.toList());

    for (final var newAsset : newAssets) {
      final var diff = String.join(LN, generateDiff(
          null, newAsset.getBody(),
          null, newAsset.getName())
      );
      diffBody.append(LN).append(diff).append(LN).append(NEW_FILE_FLAG);
    }

    return ImmutableModelWorldDiff.builder()
        .baseId(base.getName())
        .targetId(target.getName())
        .created(OffsetDateTime.now())
        .baseName(baseTag.getTagName())
        .targetName(targetTag.getTagName())
        .body(diffBody.toString())
        .build();

  }

  private List<String> generateDiff(String baseBody, String targetBody, String baseName, String targetName) {
    List<String> baseLines = baseBody != null ? baseBody.lines().collect(Collectors.toList()) : Collections.emptyList();
    List<String> targetLines = targetBody != null ? targetBody.lines().collect(Collectors.toList()) : Collections.emptyList();
    final var patch = DiffUtils.diff(baseLines, targetLines);
    return generateUnifiedDiff(baseName, targetName, baseLines, patch, NO_OF_CONTEXT_LINES);
  }
}
