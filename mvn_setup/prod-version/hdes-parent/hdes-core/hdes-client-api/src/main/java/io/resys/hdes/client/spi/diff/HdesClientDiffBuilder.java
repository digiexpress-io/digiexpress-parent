package io.resys.hdes.client.spi.diff;

/*-
 * #%L
 * hdes-client-api
 * %%
 * Copyright (C) 2020 - 2023 Copyright 2020 ReSys OÜ
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


import com.github.difflib.DiffUtils;
import io.resys.hdes.client.api.HdesClient.DiffBuilder;
import io.resys.hdes.client.api.HdesStore.StoreEntity;
import io.resys.hdes.client.api.ast.AstTagSummary;
import io.resys.hdes.client.api.diff.ImmutableTagDiff;
import io.resys.hdes.client.api.diff.TagDiff;
import io.resys.hdes.client.spi.summary.HdesClientSummaryBuilder;
import io.resys.hdes.client.spi.util.HdesAssert;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.difflib.UnifiedDiffUtils.generateUnifiedDiff;

@Slf4j
public class HdesClientDiffBuilder implements DiffBuilder {

  private static final int NO_OF_CONTEXT_LINES = 2;
  private static final String LN = "\n";
  private static final String NEW_FILE_FLAG = "new file mode 100644"; 
  private static final String DELETED_FILE_FLAG = "deleted file mode 100644";

  private String baseId;
  private String targetId;
  private Collection<StoreEntity> tags;
  private LocalDateTime targetDate;

  private HdesClientSummaryBuilder summaryBuilder = new HdesClientSummaryBuilder();


  public HdesClientDiffBuilder tags(Collection<StoreEntity> tags) {
    this.tags = tags;
    return this;
  }

  public HdesClientDiffBuilder baseId(String baseId) {
    this.baseId = baseId;
    return this;
  }
  
  public HdesClientDiffBuilder targetId(String targetId) {
    this.targetId = targetId;
    return this;
  }

  public HdesClientDiffBuilder targetDate(LocalDateTime targetDate) {
    this.targetDate = targetDate;
    return this;
  }

  public TagDiff build() {
    HdesAssert.notNull(tags, () -> "tags must be defined!");
    HdesAssert.notEmpty(baseId, () -> "baseId must be defined!");
    HdesAssert.notEmpty(targetId, () -> "targetId must be defined!");
    HdesAssert.notNull(targetDate, () -> "targetDate must be defined!");

    AstTagSummary baseTag = summaryBuilder.tags(tags).tagId(baseId).build();
    AstTagSummary targetTag = summaryBuilder.tags(tags).tagId(targetId).build();

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

    return ImmutableTagDiff.builder()
        .baseId(baseId)
        .targetId(targetId)
        .created(targetDate)
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
