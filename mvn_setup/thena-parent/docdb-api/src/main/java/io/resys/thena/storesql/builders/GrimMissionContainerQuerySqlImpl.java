package io.resys.thena.storesql.builders;

import java.util.stream.Collectors;

import io.resys.thena.api.LogConstants;
import io.resys.thena.api.entities.grim.GrimAssignment;
import io.resys.thena.api.entities.grim.GrimCommands;
import io.resys.thena.api.entities.grim.GrimCommit;
import io.resys.thena.api.entities.grim.GrimLabel;
import io.resys.thena.api.entities.grim.GrimMission;
import io.resys.thena.api.entities.grim.GrimMissionData;
import io.resys.thena.api.entities.grim.GrimMissionLabel;
import io.resys.thena.api.entities.grim.GrimMissionLink;
import io.resys.thena.api.entities.grim.GrimObjective;
import io.resys.thena.api.entities.grim.GrimObjectiveGoal;
import io.resys.thena.api.entities.grim.GrimRemark;
import io.resys.thena.api.entities.grim.ImmutableGrimMissionContainer;
import io.resys.thena.api.entities.grim.ThenaGrimContainers.GrimMissionContainer;
import io.resys.thena.api.registry.GrimRegistry;
import io.resys.thena.datasource.ThenaSqlDataSource;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler.SqlFailed;
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
  
  public GrimMissionContainerQuerySqlImpl(ThenaSqlDataSource dataSource) {
    super();
    this.dataSource = dataSource;
    this.registry = dataSource.getRegistry().grim();
    this.errorHandler = dataSource.getErrorHandler();
  }

  @Override
  public MissionQuery addMissionIdFilter(String missionId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public MissionQuery addAssignmentFilter(String assignmentType, String assignmentValue) {
    // TODO Auto-generated method stub
    return null;
  }
  @Override
  public Uni<GrimMissionContainer> getById(String missionId) {
    // TODO Auto-generated method stub
    return null;
  }
  @Override
  public Multi<GrimMissionContainer> findAll() {
    return Uni.combine().all().unis(
      findAllLabels(),
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
    final var sql = registry.commits().findAll();
    if(log.isDebugEnabled()) {
      log.debug("User findAllCommits query, with props: {} \r\n{}", 
          "",
          sql.getValue());
    }
    return dataSource.getClient().preparedQuery(sql.getValue())
        .mapping(registry.commits().defaultMapper())
        .execute()
        .onItem()
        .transformToMulti((RowSet<GrimCommit> rowset) -> Multi.createFrom().iterable(rowset))
        .collect().asList()
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlFailed("Can't find 'COMMIT'!", sql, e)))
        .onItem().transform(items -> ImmutableGrimMissionContainer
            .builder().commits(items.stream().collect(Collectors.toMap(e -> e.getCommitId(), e -> e)))
            .build()
        );
  }
  private Uni<GrimMissionContainer> findAllAssignments() {
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
        .transformToMulti((RowSet<GrimAssignment> rowset) -> Multi.createFrom().iterable(rowset))
        .collect().asList()
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlFailed("Can't find 'ASSIGNMENT'!", sql, e)))
        .onItem().transform(items -> ImmutableGrimMissionContainer
            .builder().assignments(items.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
            .build()
        );
  }
  private Uni<GrimMissionContainer> findAllGoals() {
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
        .transformToMulti((RowSet<GrimObjectiveGoal> rowset) -> Multi.createFrom().iterable(rowset))
        .collect().asList()
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlFailed("Can't find 'GOALS'!", sql, e)))
        .onItem().transform(items -> ImmutableGrimMissionContainer
            .builder().goals(items.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
            .build()
        );
  }
  private Uni<GrimMissionContainer> findAllData() {
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
        .transformToMulti((RowSet<GrimMissionData> rowset) -> Multi.createFrom().iterable(rowset))
        .collect().asList()
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlFailed("Can't find 'MISSION_DATA'!", sql, e)))
        .onItem().transform(items -> ImmutableGrimMissionContainer
            .builder().data(items.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
            .build()
        );
  }
  private Uni<GrimMissionContainer> findAllObjectives() {
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
        .transformToMulti((RowSet<GrimObjective> rowset) -> Multi.createFrom().iterable(rowset))
        .collect().asList()
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlFailed("Can't find 'LABEL'!", sql, e)))
        .onItem().transform(items -> ImmutableGrimMissionContainer
            .builder().objectives(items.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
            .build()
        );
  }
  
  private Uni<GrimMissionContainer> findAllRemarks() {
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
        .transformToMulti((RowSet<GrimRemark> rowset) -> Multi.createFrom().iterable(rowset))
        .collect().asList()
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlFailed("Can't find 'LABEL'!", sql, e)))
        .onItem().transform(items -> ImmutableGrimMissionContainer
            .builder().remarks(items.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
            .build()
        );
  }

  private Uni<GrimMissionContainer> findAllLinks() {
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
        .transformToMulti((RowSet<GrimMissionLink> rowset) -> Multi.createFrom().iterable(rowset))
        .collect().asList()
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlFailed("Can't find 'LABEL'!", sql, e)))
        .onItem().transform(items -> ImmutableGrimMissionContainer
            .builder().links(items.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
            .build()
        );
  }
  
  private Uni<GrimMissionContainer> findAllLabels() {
    final var sql = registry.labels().findAll();
    if(log.isDebugEnabled()) {
      log.debug("User findAllLabels query, with props: {} \r\n{}", 
          "",
          sql.getValue());
    }
    return dataSource.getClient().preparedQuery(sql.getValue())
        .mapping(registry.labels().defaultMapper())
        .execute()
        .onItem()
        .transformToMulti((RowSet<GrimLabel> rowset) -> Multi.createFrom().iterable(rowset))
        .collect().asList()
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlFailed("Can't find 'LABEL'!", sql, e)))
        .onItem().transform(items -> ImmutableGrimMissionContainer
            .builder().labels(items.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
            .build()
        );
  }
  private Uni<GrimMissionContainer> findAllMissions() {
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
        .transformToMulti((RowSet<GrimMission> rowset) -> Multi.createFrom().iterable(rowset))
        .collect().asList()
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlFailed("Can't find 'MISSION'!", sql, e)))
        .onItem().transform(items -> ImmutableGrimMissionContainer
            .builder().missions(items.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
            .build()
        );
  }
  private Uni<GrimMissionContainer> findAllMissionLabels() {
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
        .transformToMulti((RowSet<GrimMissionLabel> rowset) -> Multi.createFrom().iterable(rowset))
        .collect().asList()
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlFailed("Can't find 'MISSION_LABEL'!", sql, e)))
        .onItem().transform(items -> ImmutableGrimMissionContainer
            .builder().missionLabels(items.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
            .build()
        );
  }
  private Uni<GrimMissionContainer> findAllCommands() {
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
        .transformToMulti((RowSet<GrimCommands> rowset) -> Multi.createFrom().iterable(rowset))
        .collect().asList()
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlFailed("Can't find 'MISSION_COMMANDS'!", sql, e)))
        .onItem().transform(items -> ImmutableGrimMissionContainer
            .builder().commands(items.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
            .build()
        );
  }
}
