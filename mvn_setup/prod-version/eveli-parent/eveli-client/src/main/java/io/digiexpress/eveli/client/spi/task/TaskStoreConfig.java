package io.digiexpress.eveli.client.spi.task;

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
