package io.digiexpress.eveli.client.spi.task;

/*-
 * #%L
 * eveli-client
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
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

import java.util.List;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import io.resys.thena.api.ThenaClient;
import io.resys.thena.api.ThenaClient.GrimStructuredTenant;
import io.resys.thena.api.actions.GrimCommitActions.CreateManyMissions;
import io.resys.thena.api.actions.GrimCommitActions.CreateOneMission;
import io.resys.thena.api.actions.GrimCommitActions.ManyMissionsEnvelope;
import io.resys.thena.api.actions.GrimCommitActions.ModifyManyMissions;
import io.resys.thena.api.actions.GrimCommitActions.ModifyOneMission;
import io.resys.thena.api.actions.GrimCommitActions.OneMissionEnvelope;
import io.resys.thena.api.actions.GrimQueryActions.MissionQuery;
import io.resys.thena.api.actions.GrimQueryActions.MissionRemarkQuery;
import io.resys.thena.api.entities.grim.GrimMission;
import io.resys.thena.api.entities.grim.ThenaGrimContainers.GrimMissionContainer;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelopeList;
import io.smallrye.mutiny.Uni;


@Value.Immutable
public interface TaskStoreConfig {
  ThenaClient getClient();
  String getTenantName();
  
  @FunctionalInterface
  interface TaskAuthorProvider {
    String get();
  }

  /**
   * Visitor for task QUERIES
   */
  interface QueryTasksVisitor<T> {
    MissionQuery start(GrimStructuredTenant config, MissionQuery builder);
    @Nullable List<GrimMissionContainer> visitEnvelope(GrimStructuredTenant config, QueryEnvelopeList<GrimMissionContainer> envelope);
    Uni<T> end(GrimStructuredTenant config, @Nullable List<GrimMissionContainer> commit);    
  }
    
  default <T> Uni<T> accept(QueryTasksVisitor<T> visitor) {
    final var grim = getClient().grim(getTenantName());
    final var prefilled = grim.find().missionQuery();
    
    final Uni<QueryEnvelopeList<GrimMissionContainer>> query = visitor.start(grim, prefilled).findAll();
    return query
        .onItem().transform(envelope -> visitor.visitEnvelope(grim, envelope))
        .onItem().transformToUni(ref -> visitor.end(grim, ref));
  }
  
  
  /**
   * Visitor for SINGLE task comment(s) QUERIES
   */
  interface QueryOneTaskCommentsVisitor<T> {
    Uni<QueryEnvelope<GrimMissionContainer>> start(GrimStructuredTenant config, MissionRemarkQuery builder);
    @Nullable GrimMissionContainer visitEnvelope(GrimStructuredTenant config, QueryEnvelope<GrimMissionContainer> envelope);
    Uni<T> end(GrimStructuredTenant config, @Nullable GrimMissionContainer commit);    
  }
    
  default <T> Uni<T> accept(QueryOneTaskCommentsVisitor<T> visitor) {
    final var grim = getClient().grim(getTenantName());
    final var prefilled = grim.find().missionRemarkQuery();
    
    final Uni<QueryEnvelope<GrimMissionContainer>> query = visitor.start(grim, prefilled);
    return query
        .onItem().transform(envelope -> visitor.visitEnvelope(grim, envelope))
        .onItem().transformToUni(ref -> visitor.end(grim, ref));
  }
  
  

  /**
   * Visitors for merging ONE task
   */
  
  interface MergeTaskVisitor<T> { 
    ModifyOneMission start(GrimStructuredTenant config, ModifyOneMission builder);
    @Nullable OneMissionEnvelope visitEnvelope(GrimStructuredTenant config, OneMissionEnvelope envelope);
    Uni<T> end(GrimStructuredTenant config, @Nullable OneMissionEnvelope commit);
  }
  default <T> Uni<T> accept(MergeTaskVisitor<T> visitor) {
    final var grim = getClient().grim(getTenantName());
    final var prefilled = grim.commit().modifyOneMission();
    
    final Uni<OneMissionEnvelope> query = visitor.start(grim, prefilled).build();
    return query
        .onItem().transform(envelope -> visitor.visitEnvelope(grim, envelope))
        .onItem().transformToUni(ref -> visitor.end(grim, ref));
  }
  
  
  
  /**
   * Visitors for merging tasks in BATCH
   */
  interface MergeTasksVisitor<T> { 
    ModifyManyMissions start(GrimStructuredTenant config, ModifyManyMissions builder);
    @Nullable List<GrimMission> visitEnvelope(GrimStructuredTenant config, ManyMissionsEnvelope envelope);
    Uni<T> end(GrimStructuredTenant config, @Nullable List<GrimMission> commit);
  }
  default <T> Uni<T> accept(MergeTasksVisitor<T> visitor) {
    final var grim = getClient().grim(getTenantName());
    final var prefilled = grim
        .commit().modifyManyMissions();
    
    final Uni<ManyMissionsEnvelope> query = visitor.start(grim, prefilled).build();
    return query
        .onItem().transform(envelope -> visitor.visitEnvelope(grim, envelope))
        .onItem().transformToUni(ref -> visitor.end(grim, ref));
  }
  
  
  /**
   * Visitor for creating tasks in BATCH
   */
  interface CreateManyTasksVisitor<T> { 
    CreateManyMissions start(GrimStructuredTenant config, CreateManyMissions builder);
    @Nullable List<GrimMission> visitEnvelope(GrimStructuredTenant config, ManyMissionsEnvelope envelope);
    Uni<List<T>> end(GrimStructuredTenant config, @Nullable List<GrimMission> commit);
  }
  default <T> Uni<List<T>> accept(CreateManyTasksVisitor<T> visitor) {
    final var grim = getClient().grim(getTenantName());
    final var prefilled = grim
        .commit().createManyMissions();
    
    final Uni<ManyMissionsEnvelope> query = visitor.start(grim, prefilled).build();
    return query
        .onItem().transform(envelope -> visitor.visitEnvelope(grim, envelope))
        .onItem().transformToUni(ref -> visitor.end(grim, ref));
  }
  
  
  /**
   * Visitor for creating ONE task
   */
  interface CreateOneTaskVisitor<T> { 
    CreateOneMission start(GrimStructuredTenant config, CreateOneMission builder);
    @Nullable OneMissionEnvelope visitEnvelope(GrimStructuredTenant config, OneMissionEnvelope envelope);
    Uni<T> end(GrimStructuredTenant config, @Nullable OneMissionEnvelope commit);
  }
  
  default <T> Uni<T> accept(CreateOneTaskVisitor<T> visitor) {
    final var grim = getClient().grim(getTenantName());
    final var prefilled = grim
        .commit().createOneMission();
    
    final Uni<OneMissionEnvelope> query = visitor.start(grim, prefilled).build();
    return query
        .onItem().transform(envelope -> visitor.visitEnvelope(grim, envelope))
        .onItem().transformToUni(ref -> visitor.end(grim, ref));
  }
}
