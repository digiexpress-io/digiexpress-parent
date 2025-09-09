package io.digiexpress.eveli.client.spi.health;

/*-
 * #%L
 * eveli-client
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
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


import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.digiexpress.eveli.client.api.HealthClient.UserActivity;
import io.digiexpress.eveli.client.api.HealthClient.UserActivityType;
import io.digiexpress.eveli.client.api.ImmutableUserActivity;
import io.digiexpress.eveli.client.api.TaskClient;
import io.resys.thena.api.entities.grim.GrimCommitTree;
import io.resys.thena.api.entities.grim.GrimCommitViewer;
import io.resys.thena.api.entities.grim.ThenaGrimContainers.GrimProjectObjects;
import io.resys.thena.api.entities.grim.ThenaGrimObject.GrimDocType;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelopeList;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class UserActivityVisitor {
  private final TaskClient taskClient;

  public Multi<UserActivity> accept(OffsetDateTime createdFromInclusive) {
    // integrate against task storage
    final var config = taskClient.unwrap().getConfig();
    final var grim = config.getClient().grim(config.getTenantName());
    
    // join all queries and map to Activity object
    return Uni.combine().all().unis(
        grim.find().commitQuery().findAllCommitsGteCreateAt(createdFromInclusive), 
        grim.find().commitViewersQuery().gteCreateAt(createdFromInclusive).findAll()
    ).asTuple()
    .onItem().transformToMulti(tuple -> createActivity(tuple.getItem1(), tuple.getItem2()));        
  }
  
  
  private Multi<UserActivity> createActivity(QueryEnvelope<GrimProjectObjects> commits, QueryEnvelopeList<GrimCommitViewer> viewers) {
    final Set<String> taskIds = new HashSet<>();
    
    // Resolve views
    final List<UserActivity> access = viewers.getObjects().stream().map(entry -> {
      taskIds.add(entry.getMissionId());
      
      final UserActivity mapped = ImmutableUserActivity.builder()
        .type(UserActivityType.ACCESS)
        .id(entry.getId())
        .userName(entry.getUsedBy())
        .usedFor(entry.getUsedFor())
        .targetId(entry.getObjectId())
        .targetIdType(entry.getObjectType().name())
        .createdAt(entry.getUpdatedAt())
        .build();
      return mapped;
    }).toList();
    
    final var treesByCommit = commits.getObjects().getCommitTrees().values().stream().collect(Collectors.groupingBy(GrimCommitTree::getCommitId));
    
    
    // Resolve commits
    final List<UserActivity> changes = commits.getObjects().getCommits().values().stream().map(entry -> {
      taskIds.add(entry.getMissionId());
      
      final var trees = treesByCommit.getOrDefault(entry.getCommitId(), Collections.emptyList());
      final UserActivity mapped = ImmutableUserActivity.builder()
        .type(UserActivityType.CHANGE)
        .id(entry.getCommitId())
        .change(trees)
        .userName(entry.getCommitAuthor())
        .usedFor("")
        .targetId(entry.getMissionId())
        .targetIdType(GrimDocType.GRIM_MISSION.name())
        .createdAt(entry.getCreatedAt())
        .build();
      return mapped;
    }).toList();
    
    
    return taskClient.queryTasks().findAll(new ArrayList<>(taskIds))
      .onItem().transform(tasks -> tasks.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
      .onItem().transformToMulti(tasks -> Multi.createFrom().items(Stream.concat(access.stream(), changes.stream()).map(entry -> {
        
        final var task = Optional.ofNullable(tasks.get(entry.getTargetId()));
      
        final UserActivity mapped = ImmutableUserActivity.builder()
            .from(entry)
            .taskRef(task.map(e -> e.getTaskRef()).orElse(null))
            .build();
        return mapped;
      })));
  }
}
