package io.resys.thena.storesql.builders;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

import io.resys.thena.api.LogConstants;
import io.resys.thena.api.entities.grim.ImmutableGrimMissionContainer;
import io.resys.thena.api.entities.grim.ThenaGrimContainers.GrimMissionContainer;
import io.resys.thena.api.entities.grim.ThenaGrimObject.GrimDocType;
import io.resys.thena.api.registry.GrimRegistry;
import io.resys.thena.datasource.ThenaSqlDataSource;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler;
import io.resys.thena.structures.grim.GrimQueries;
import io.resys.thena.structures.grim.GrimQueries.MissionQuery;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.RowSet;
import lombok.extern.slf4j.Slf4j;


@Slf4j(topic = LogConstants.SHOW_SQL)
public class GrimMissionContainerQuerySqlImpl implements GrimQueries.MissionQuery {

  private final ThenaSqlDataSource dataSource;
  private final GrimRegistry registry;
  private final ThenaSqlDataSourceErrorHandler errorHandler;
  private final Collection<GrimDocType> docsToExclude = new LinkedHashSet<>();
  private final Collection<String> missionIds = new LinkedHashSet<>();
  private final Collection<AssignmentFilter> assignments = new LinkedHashSet<>();
  
  @lombok.Data @lombok.Builder
  private static class AssignmentFilter {
    private final String assignmentType;
    private final String assignmentValue; 
  }
  
  public GrimMissionContainerQuerySqlImpl(ThenaSqlDataSource dataSource) {
    super();
    this.dataSource = dataSource;
    this.registry = dataSource.getRegistry().grim();
    this.errorHandler = dataSource.getErrorHandler();
  }
  @Override
  public MissionQuery excludeDocs(GrimDocType... docs) {
    docsToExclude.addAll(Arrays.asList(docs));
    return this;
  }
  @Override
  public MissionQuery addMissionIdFilter(String... missionId) {
    missionIds.addAll(Arrays.asList(missionId));
    return this;
  }
  @Override
  public MissionQuery addAssignmentFilter(String assignmentType, String assignmentValue) {
    assignments.add(AssignmentFilter.builder().assignmentType(assignmentType).assignmentValue(assignmentValue).build());
    return this;
  }
  @Override
  public Uni<GrimMissionContainer> getById(String missionId) {
    this.missionIds.clear();
    this.missionIds.add(missionId);
    return Uni.combine().all().unis(
        findAllLinks(),
        findAllRemarks(),
        findAllObjectives(),
        findAllData(),
        findAllGoals(),
        findAllAssignments(),
        findAllCommits(),
        findAllMissions(),
        findAllMissionLabels(),
        findAllCommands()
      ).with(GrimMissionContainer.class, (containers) -> {
        final var combined = ImmutableGrimMissionContainer.builder();
        containers.forEach(container -> combined.from(container));
        final GrimMissionContainer built = combined.build();
        final var result = built.groupByMission();
        if(result.isEmpty()) {
          return null;
        }
        return result.iterator().next();
      });
  }
  @Override
  public Multi<GrimMissionContainer> findAll() {
    return Uni.combine().all().unis(
      findAllLinks(),
      findAllRemarks(),
      findAllObjectives(),
      findAllData(),
      findAllGoals(),
      findAllAssignments(),
      findAllCommits(),
      findAllMissions(),
      findAllMissionLabels(),
      findAllCommands()
    ).with(GrimMissionContainer.class, (containers) -> {
      final var combined = ImmutableGrimMissionContainer.builder();
      containers.forEach(container -> combined.from(container));
      final GrimMissionContainer built = combined.build();
      return built.groupByMission();
    }).onItem().transformToMulti(e -> Multi.createFrom().items(e.stream()));
  }
  
  
  private Uni<GrimMissionContainer> findAllCommits() {
    if(docsToExclude.contains(GrimDocType.GRIM_COMMIT)) {
      return Uni.createFrom().item(ImmutableGrimMissionContainer.builder().build());
    }
    // query ALL COMMITS everything
    if(missionIds.isEmpty()) {
      final var sql = registry.commits().findAll();
      if(log.isDebugEnabled()) {
        log.debug("User findAllCommits query, with props: {} \r\n{}", 
            "",
            sql.getValue());
      }
      return dataSource.getClient().preparedQuery(sql.getValue())
          .mapping(registry.commits().defaultMapper())
          .execute().onItem()
          .transformToMulti(RowSet::toMulti).collect().asList()
          .onFailure().invoke(e -> errorHandler.deadEnd(sql.failed(e, "Can't find '%s'!", GrimDocType.GRIM_COMMIT)))
          .onItem().transform(items -> ImmutableGrimMissionContainer
              .builder().commits(items.stream().collect(Collectors.toMap(e -> e.getCommitId(), e -> e)))
              .build()
          );
    }
    
    // query COMMITS by mission id
    final var sql = registry.commits().findAllByMissionIds(missionIds);
    if(log.isDebugEnabled()) {
      log.debug("User findAllCommits query, with props: {} \r\n{}", 
          sql.getPropsDeepString(),
          sql.getValue());
    }
    return dataSource.getClient().preparedQuery(sql.getValue())
        .mapping(registry.commits().defaultMapper())
        .execute(sql.getProps()).onItem()
        .transformToMulti(RowSet::toMulti).collect().asList()
        .onFailure().invoke(e -> errorHandler.deadEnd(sql.failed(e, "Can't find '%s'!", GrimDocType.GRIM_COMMIT)))
        .onItem().transform(items -> ImmutableGrimMissionContainer
            .builder().commits(items.stream().collect(Collectors.toMap(e -> e.getCommitId(), e -> e)))
            .build()
        );
  }
  private Uni<GrimMissionContainer> findAllAssignments() {
    if(docsToExclude.contains(GrimDocType.GRIM_ASSIGNMENT)) {
      return Uni.createFrom().item(ImmutableGrimMissionContainer.builder().build());
    }
    // query ALL ASSIGNMENTS everything
    if(missionIds.isEmpty()) {
      final var sql = registry.assignments().findAll();
      if(log.isDebugEnabled()) {
        log.debug("User findAllAssignments query, with props: {} \r\n{}", 
            "",
            sql.getValue());
      }
      return dataSource.getClient().preparedQuery(sql.getValue())
          .mapping(registry.assignments().defaultMapper())
          .execute()
          .onItem()
          .transformToMulti(RowSet::toMulti).collect().asList()
          .onFailure().invoke(e -> errorHandler.deadEnd(sql.failed(e, "Can't find '%s'!", GrimDocType.GRIM_ASSIGNMENT)))
          .onItem().transform(items -> ImmutableGrimMissionContainer
              .builder().assignments(items.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
              .build()
          );
    }
    
    // query ASSIGNMENTS by mission id
    final var sql = registry.assignments().findAllByMissionIds(missionIds);
    if(log.isDebugEnabled()) {
      log.debug("User findAllAssignments query, with props: {} \r\n{}", 
          sql.getPropsDeepString(),
          sql.getValue());
    }
    return dataSource.getClient().preparedQuery(sql.getValue())
        .mapping(registry.assignments().defaultMapper())
        .execute(sql.getProps())
        .onItem()
        .transformToMulti(RowSet::toMulti).collect().asList()
        .onFailure().invoke(e -> errorHandler.deadEnd(sql.failed(e, "Can't find '%s'!", GrimDocType.GRIM_ASSIGNMENT)))
        .onItem().transform(items -> ImmutableGrimMissionContainer
            .builder().assignments(items.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
            .build()
        );
  }
  private Uni<GrimMissionContainer> findAllGoals() {
    if(docsToExclude.contains(GrimDocType.GRIM_OBJECTIVE_GOAL)) {
      return Uni.createFrom().item(ImmutableGrimMissionContainer.builder().build());
    }
    
    // query ALL GOALS everything
    if(missionIds.isEmpty()) {
      final var sql = registry.goals().findAll();
      if(log.isDebugEnabled()) {
        log.debug("User findAllGoals query, with props: {} \r\n{}", 
            "",
            sql.getValue());
      }
      return dataSource.getClient().preparedQuery(sql.getValue())
          .mapping(registry.goals().defaultMapper())
          .execute()
          .onItem()
          .transformToMulti(RowSet::toMulti).collect().asList()
          .onFailure().invoke(e -> errorHandler.deadEnd(sql.failed(e, "Can't find '%s'!", GrimDocType.GRIM_OBJECTIVE_GOAL)))
          .onItem().transform(items -> ImmutableGrimMissionContainer
              .builder().goals(items.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
              .build()
          );
    }
    
    final var sql = registry.goals().findAllByMissionIds(missionIds);
    if(log.isDebugEnabled()) {
      log.debug("User findAllGoals query, with props: {} \r\n{}", 
          sql.getPropsDeepString(),
          sql.getValue());
    }
    return dataSource.getClient().preparedQuery(sql.getValue())
        .mapping(registry.goals().defaultMapper())
        .execute(sql.getProps())
        .onItem()
        .transformToMulti(RowSet::toMulti).collect().asList()
        .onFailure().invoke(e -> errorHandler.deadEnd(sql.failed(e, "Can't find '%s'!", GrimDocType.GRIM_OBJECTIVE_GOAL)))
        .onItem().transform(items -> ImmutableGrimMissionContainer
            .builder().goals(items.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
            .build()
        );
  }
  private Uni<GrimMissionContainer> findAllData() {
    if(docsToExclude.contains(GrimDocType.GRIM_MISSION_DATA)) {
      return Uni.createFrom().item(ImmutableGrimMissionContainer.builder().build());
    }
    
    if(missionIds.isEmpty()) {
      final var sql = registry.missionData().findAll();
      if(log.isDebugEnabled()) {
        log.debug("User findAllData query, with props: {} \r\n{}", 
            "",
            sql.getValue());
      }
      return dataSource.getClient().preparedQuery(sql.getValue())
          .mapping(registry.missionData().defaultMapper())
          .execute()
          .onItem()
          .transformToMulti(RowSet::toMulti).collect().asList()
          .onFailure().invoke(e -> errorHandler.deadEnd(sql.failed(e, "Can't find '%s'!", GrimDocType.GRIM_MISSION_DATA)))
          .onItem().transform(items -> ImmutableGrimMissionContainer
              .builder().data(items.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
              .build()
          );
    }
    
    final var sql = registry.missionData().findAllByMissionIds(missionIds);
    if(log.isDebugEnabled()) {
      log.debug("User findAllData query, with props: {} \r\n{}", 
          sql.getPropsDeepString(),
          sql.getValue());
    }
    return dataSource.getClient().preparedQuery(sql.getValue())
        .mapping(registry.missionData().defaultMapper())
        .execute(sql.getProps())
        .onItem()
        .transformToMulti(RowSet::toMulti).collect().asList()
        .onFailure().invoke(e -> errorHandler.deadEnd(sql.failed(e, "Can't find '%s'!", GrimDocType.GRIM_MISSION_DATA)))
        .onItem().transform(items -> ImmutableGrimMissionContainer
            .builder().data(items.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
            .build()
        );
  }
  private Uni<GrimMissionContainer> findAllObjectives() {
    if(docsToExclude.contains(GrimDocType.GRIM_OBJECTIVE)) {
      return Uni.createFrom().item(ImmutableGrimMissionContainer.builder().build());
    }
    if(missionIds.isEmpty()) {
      final var sql = registry.objectives().findAll();
      if(log.isDebugEnabled()) {
        log.debug("User findAllObjectives query, with props: {} \r\n{}", 
            "",
            sql.getValue());
      }
      return dataSource.getClient().preparedQuery(sql.getValue())
          .mapping(registry.objectives().defaultMapper())
          .execute()
          .onItem()
          .transformToMulti(RowSet::toMulti).collect().asList()
          .onFailure().invoke(e -> errorHandler.deadEnd(sql.failed(e, "Can't find '%s'!", GrimDocType.GRIM_OBJECTIVE)))
          .onItem().transform(items -> ImmutableGrimMissionContainer
              .builder().objectives(items.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
              .build()
          );
    }
    
    final var sql = registry.objectives().findAllByMissionIds(missionIds);
    if(log.isDebugEnabled()) {
      log.debug("User findAllObjectives query, with props: {} \r\n{}", 
          sql.getPropsDeepString(),
          sql.getValue());
    }
    return dataSource.getClient().preparedQuery(sql.getValue())
        .mapping(registry.objectives().defaultMapper())
        .execute(sql.getProps())
        .onItem()
        .transformToMulti(RowSet::toMulti).collect().asList()
        .onFailure().invoke(e -> errorHandler.deadEnd(sql.failed(e, "Can't find '%s'!", GrimDocType.GRIM_OBJECTIVE)))
        .onItem().transform(items -> ImmutableGrimMissionContainer
            .builder().objectives(items.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
            .build()
        );
  }
  
  private Uni<GrimMissionContainer> findAllRemarks() {
    if(docsToExclude.contains(GrimDocType.GRIM_REMARK)) {
      return Uni.createFrom().item(ImmutableGrimMissionContainer.builder().build());
    }
    if(missionIds.isEmpty()) {
      final var sql = registry.remarks().findAll();
      if(log.isDebugEnabled()) {
        log.debug("User findAllRemarks query, with props: {} \r\n{}", 
            "",
            sql.getValue());
      }
      return dataSource.getClient().preparedQuery(sql.getValue())
          .mapping(registry.remarks().defaultMapper())
          .execute()
          .onItem()
          .transformToMulti(RowSet::toMulti).collect().asList()
          .onFailure().invoke(e -> errorHandler.deadEnd(sql.failed(e, "Can't find '%s'!", GrimDocType.GRIM_REMARK)))
          .onItem().transform(items -> ImmutableGrimMissionContainer
              .builder().remarks(items.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
              .build()
          );
    }
    final var sql = registry.remarks().findAllByMissionIds(missionIds);
    if(log.isDebugEnabled()) {
      log.debug("User findAllRemarks query, with props: {} \r\n{}", 
          sql.getPropsDeepString(),
          sql.getValue());
    }
    return dataSource.getClient().preparedQuery(sql.getValue())
        .mapping(registry.remarks().defaultMapper())
        .execute(sql.getProps())
        .onItem()
        .transformToMulti(RowSet::toMulti).collect().asList()
        .onFailure().invoke(e -> errorHandler.deadEnd(sql.failed(e, "Can't find '%s'!", GrimDocType.GRIM_REMARK)))
        .onItem().transform(items -> ImmutableGrimMissionContainer
            .builder().remarks(items.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
            .build()
        );
  }

  private Uni<GrimMissionContainer> findAllLinks() {
    if(docsToExclude.contains(GrimDocType.GRIM_MISSION_LINKS)) {
      return Uni.createFrom().item(ImmutableGrimMissionContainer.builder().build());
    }
    if(missionIds.isEmpty()) {
      final var sql = registry.missionLinks().findAll();
      if(log.isDebugEnabled()) {
        log.debug("User findAllLinks query, with props: {} \r\n{}", 
            "",
            sql.getValue());
      }
      return dataSource.getClient().preparedQuery(sql.getValue())
          .mapping(registry.missionLinks().defaultMapper())
          .execute()
          .onItem()
          .transformToMulti(RowSet::toMulti).collect().asList()
          .onFailure().invoke(e -> errorHandler.deadEnd(sql.failed(e, "Can't find '%s'!", GrimDocType.GRIM_MISSION_LINKS)))
          .onItem().transform(items -> ImmutableGrimMissionContainer
              .builder().links(items.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
              .build()
          );
    }
    final var sql = registry.missionLinks().findAllByMissionIds(missionIds);
    if(log.isDebugEnabled()) {
      log.debug("User findAllLinks query, with props: {} \r\n{}", 
          sql.getPropsDeepString(),
          sql.getValue());
    }
    return dataSource.getClient().preparedQuery(sql.getValue())
        .mapping(registry.missionLinks().defaultMapper())
        .execute(sql.getProps())
        .onItem()
        .transformToMulti(RowSet::toMulti).collect().asList()
        .onFailure().invoke(e -> errorHandler.deadEnd(sql.failed(e, "Can't find '%s'!", GrimDocType.GRIM_MISSION_LINKS)))
        .onItem().transform(items -> ImmutableGrimMissionContainer
            .builder().links(items.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
            .build()
        );
  }

  private Uni<GrimMissionContainer> findAllMissions() {
    if(docsToExclude.contains(GrimDocType.GRIM_MISSION)) {
      return Uni.createFrom().item(ImmutableGrimMissionContainer.builder().build());
    }
    
    if(missionIds.isEmpty()) {
      final var sql = registry.missions().findAll();
      if(log.isDebugEnabled()) {
        log.debug("User findAllMissions query, with props: {} \r\n{}", 
            "",
            sql.getValue());
      }
      return dataSource.getClient().preparedQuery(sql.getValue())
          .mapping(registry.missions().defaultMapper())
          .execute()
          .onItem()
          .transformToMulti(RowSet::toMulti).collect().asList()
          .onFailure().invoke(e -> errorHandler.deadEnd(sql.failed(e, "Can't find '%s'!", GrimDocType.GRIM_MISSION)))
          .onItem().transform(items -> ImmutableGrimMissionContainer
              .builder().missions(items.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
              .build()
          );
    }
    
    final var sql = registry.missions().findAllByMissionIds(missionIds);
    if(log.isDebugEnabled()) {
      log.debug("User findAllMissions query, with props: {} \r\n{}", 
          sql.getPropsDeepString(),
          sql.getValue());
    }
    return dataSource.getClient().preparedQuery(sql.getValue())
        .mapping(registry.missions().defaultMapper())
        .execute(sql.getProps())
        .onItem()
        .transformToMulti(RowSet::toMulti).collect().asList()
        .onFailure().invoke(e -> errorHandler.deadEnd(sql.failed(e, "Can't find '%s'!", GrimDocType.GRIM_MISSION)))
        .onItem().transform(items -> ImmutableGrimMissionContainer
            .builder().missions(items.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
            .build()
        );
  }
  private Uni<GrimMissionContainer> findAllMissionLabels() {
    if(docsToExclude.contains(GrimDocType.GRIM_MISSION_LABEL)) {
      return Uni.createFrom().item(ImmutableGrimMissionContainer.builder().build());
    }
    
    if(missionIds.isEmpty()) {
      final var sql = registry.missionLabels().findAll();
      if(log.isDebugEnabled()) {
        log.debug("User findAllMissionLabels query, with props: {} \r\n{}", 
            "",
            sql.getValue());
      }
      return dataSource.getClient().preparedQuery(sql.getValue())
          .mapping(registry.missionLabels().defaultMapper())
          .execute()
          .onItem()
          .transformToMulti(RowSet::toMulti).collect().asList()
          .onFailure().invoke(e -> errorHandler.deadEnd(sql.failed(e, "Can't find '%s'!", GrimDocType.GRIM_MISSION_LABEL)))
          .onItem().transform(items -> ImmutableGrimMissionContainer
              .builder().missionLabels(items.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
              .build()
          );
    }
    
    final var sql = registry.missionLabels().findAllByMissionIds(missionIds);
    if(log.isDebugEnabled()) {
      log.debug("User findAllMissionLabels query, with props: {} \r\n{}", 
          sql.getPropsDeepString(),
          sql.getValue());
    }
    return dataSource.getClient().preparedQuery(sql.getValue())
        .mapping(registry.missionLabels().defaultMapper())
        .execute(sql.getProps())
        .onItem()
        .transformToMulti(RowSet::toMulti).collect().asList()
        .onFailure().invoke(e -> errorHandler.deadEnd(sql.failed(e, "Can't find '%s'!", GrimDocType.GRIM_MISSION_LABEL)))
        .onItem().transform(items -> ImmutableGrimMissionContainer
            .builder().missionLabels(items.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
            .build()
        );
  }
  private Uni<GrimMissionContainer> findAllCommands() {
    if(docsToExclude.contains(GrimDocType.GRIM_COMMANDS)) {
      return Uni.createFrom().item(ImmutableGrimMissionContainer.builder().build());
    }
    if(missionIds.isEmpty()) {
      final var sql = registry.commands().findAll();
      if(log.isDebugEnabled()) {
        log.debug("User findAllCommands query, with props: {} \r\n{}", 
            "",
            sql.getValue());
      }
      return dataSource.getClient().preparedQuery(sql.getValue())
          .mapping(registry.commands().defaultMapper())
          .execute()
          .onItem()
          .transformToMulti(RowSet::toMulti).collect().asList()
          .onFailure().invoke(e -> errorHandler.deadEnd(sql.failed(e, "Can't find '%s'!", GrimDocType.GRIM_COMMANDS)))
          .onItem().transform(items -> ImmutableGrimMissionContainer
              .builder().commands(items.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
              .build()
          );
    }
    
    final var sql = registry.commands().findAllByMissionIds(missionIds);
    if(log.isDebugEnabled()) {
      log.debug("User findAllCommands query, with props: {} \r\n{}", 
          sql.getPropsDeepString(),
          sql.getValue());
    }
    return dataSource.getClient().preparedQuery(sql.getValue())
        .mapping(registry.commands().defaultMapper())
        .execute(sql.getProps())
        .onItem()
        .transformToMulti(RowSet::toMulti).collect().asList()
        .onFailure().invoke(e -> errorHandler.deadEnd(sql.failed(e, "Can't find '%s'!", GrimDocType.GRIM_COMMANDS)))
        .onItem().transform(items -> ImmutableGrimMissionContainer
            .builder().commands(items.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
            .build()
        );
  }
}
