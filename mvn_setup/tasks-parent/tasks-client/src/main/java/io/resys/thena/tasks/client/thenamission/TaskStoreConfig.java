package io.resys.thena.tasks.client.thenamission;

import java.util.List;

import javax.annotation.Nullable;

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

import org.immutables.value.Value;

import io.resys.thena.api.ThenaClient;
import io.resys.thena.api.ThenaClient.GrimStructuredTenant;
import io.resys.thena.api.actions.GrimCommitActions.CreateManyMissions;
import io.resys.thena.api.actions.GrimCommitActions.ManyMissionsEnvelope;
import io.resys.thena.api.actions.GrimQueryActions.MissionQuery;
import io.resys.thena.api.entities.grim.GrimMission;
import io.resys.thena.api.entities.grim.ThenaGrimContainers.GrimMissionContainer;
import io.resys.thena.api.envelope.QueryEnvelopeList;
import io.smallrye.mutiny.Uni;


@Value.Immutable
public interface TaskStoreConfig {
  ThenaClient getClient();
  String getTenantName();
  TaskAuthorProvider getAuthor();
  
  @FunctionalInterface
  interface TaskAuthorProvider {
    String get();
  }
  
  interface CreateManyMissionsVisitor<T> { 
    CreateManyMissions start(GrimStructuredTenant config, CreateManyMissions builder);
    @Nullable List<GrimMission> visitEnvelope(GrimStructuredTenant config, ManyMissionsEnvelope envelope);
    Uni<List<T>> end(GrimStructuredTenant config, @Nullable List<GrimMission> commit);
  }

  interface MissionQueryVisitor<T> {
    MissionQuery start(GrimStructuredTenant config, MissionQuery builder);
    @Nullable List<GrimMissionContainer> visitEnvelope(GrimStructuredTenant config, QueryEnvelopeList<GrimMissionContainer> envelope);
    Uni<T> end(GrimStructuredTenant config, @Nullable List<GrimMissionContainer> commit);    
  }
  
  default <T> Uni<T> accept(MissionQueryVisitor<T> visitor) {
    final var grim = getClient().grim(getTenantName());
    final var prefilled = grim.find().missionQuery();
    
    final Uni<QueryEnvelopeList<GrimMissionContainer>> query = visitor.start(grim, prefilled).findAll();
    return query
        .onItem().transform(envelope -> visitor.visitEnvelope(grim, envelope))
        .onItem().transformToUni(ref -> visitor.end(grim, ref));
  }
  
  default <T> Uni<List<T>> accept(CreateManyMissionsVisitor<T> visitor) {
    final var grim = getClient().grim(getTenantName());
    final var prefilled = grim
        .commit().createManyMissions()
        .commitAuthor(getAuthor().get());
    
    final Uni<ManyMissionsEnvelope> query = visitor.start(grim, prefilled).build();
    return query
        .onItem().transform(envelope -> visitor.visitEnvelope(grim, envelope))
        .onItem().transformToUni(ref -> visitor.end(grim, ref));
  }
}
