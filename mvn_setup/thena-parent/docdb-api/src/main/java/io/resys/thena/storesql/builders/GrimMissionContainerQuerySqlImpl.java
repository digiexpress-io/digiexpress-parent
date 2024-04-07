package io.resys.thena.storesql.builders;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.resys.thena.api.LogConstants;
import io.resys.thena.api.actions.GrimQueryActions.GrimArchiveQueryType;
import io.resys.thena.api.entities.grim.ImmutableGrimMissionContainer;
import io.resys.thena.api.entities.grim.ThenaGrimContainers.GrimMissionContainer;
import io.resys.thena.api.entities.grim.ThenaGrimObject.GrimDocType;
import io.resys.thena.api.registry.GrimRegistry;
import io.resys.thena.api.registry.grim.ImmutableGrimAssignmentFilter;
import io.resys.thena.api.registry.grim.ImmutableGrimMissionFilter;
import io.resys.thena.datasource.ThenaSqlDataSource;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler;
import io.resys.thena.structures.grim.GrimQueries;
import io.resys.thena.structures.grim.GrimQueries.InternalMissionQuery;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.RowSet;
import lombok.extern.slf4j.Slf4j;


@Slf4j(topic = LogConstants.SHOW_SQL)
public class GrimMissionContainerQuerySqlImpl implements GrimQueries.InternalMissionQuery {

  private final ThenaSqlDataSource dataSource;
  private final GrimRegistry registry;
  private final ThenaSqlDataSourceErrorHandler errorHandler;
  private final Collection<GrimDocType> docsToExclude = new LinkedHashSet<>();
  private final ImmutableGrimMissionFilter.Builder builder = ImmutableGrimMissionFilter.builder();
  private ImmutableGrimMissionFilter filter;
  private String usedBy, usedFor;
  private List<String> missionIds;
  
  
  public GrimMissionContainerQuerySqlImpl(ThenaSqlDataSource dataSource) {
    super();
    this.dataSource = dataSource;
    this.registry = dataSource.getRegistry().grim();
    this.errorHandler = dataSource.getErrorHandler();
  }
  @Override
  public InternalMissionQuery excludeDocs(GrimDocType... docs) {
    docsToExclude.addAll(Arrays.asList(docs));
    return this;
  }
  @Override
  public InternalMissionQuery missionId(String... missionId) {
    if(this.missionIds == null) {
      this.missionIds = new ArrayList<>();
    }
    this.missionIds.addAll(Arrays.asList(missionId));
    return this;
  }
  @Override
  public InternalMissionQuery addAssignment(String assignmentType, String assignmentValue) {
    builder.addAssignments(ImmutableGrimAssignmentFilter.builder().assignmentType(assignmentType).assignmentValue(assignmentValue).build());
    return this;
  }
  @Override
  public InternalMissionQuery viewer(String usedBy, String usedFor) {
    this.usedBy = usedBy;
    this.usedFor = usedFor;
    return this;
  }
  @Override
  public InternalMissionQuery archived(GrimArchiveQueryType includeArchived) {
    this.builder.archived(includeArchived);
    return this;
  }
  @Override
  public InternalMissionQuery reporterId(String reporterId) {
    this.builder.reporterId(reporterId);
    return this;
  }
  @Override
  public InternalMissionQuery likeTitle(String likeTitle) {
    this.builder.likeTitle(likeTitle);
    return this;
  }
  @Override
  public InternalMissionQuery likeDescription(String likeDescription) {
    this.builder.likeDescription(likeDescription);
    return this;
  }
  @Override
  public InternalMissionQuery fromCreatedOrUpdated(LocalDate fromCreatedOrUpdated) {
    this.builder.fromCreatedOrUpdated(fromCreatedOrUpdated);
    return this;
  }
  @Override
  public Uni<GrimMissionContainer> getById(String missionId) {
    builder.missionIds(Arrays.asList(missionId));
    this.filter = builder.build();
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
        findAllCommands(),
        findAllViewers()
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
    this.filter = builder.missionIds(Optional.ofNullable(missionIds)).build();
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
      findAllCommands(),
      findAllViewers()
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

    final var sql = registry.commits().findAllByMissionIds(filter);
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

    // query ASSIGNMENTS by mission id
    final var sql = registry.assignments().findAllByMissionIds(filter);
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
    
    final var sql = registry.goals().findAllByMissionIds(filter);
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

    final var sql = registry.missionData().findAllByMissionIds(filter);
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

    final var sql = registry.objectives().findAllByMissionIds(filter);
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

    final var sql = registry.remarks().findAllByMissionIds(filter);
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

    final var sql = registry.missionLinks().findAllByMissionIds(filter);
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

    final var sql = registry.missions().findAllByMissionIds(filter);
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

    
    final var sql = registry.missionLabels().findAllByMissionIds(filter);
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

    
    final var sql = registry.commands().findAllByMissionIds(filter);
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
  
  private Uni<GrimMissionContainer> findAllViewers() {
    if(docsToExclude.contains(GrimDocType.GRIM_COMMIT_VIEWER) || usedBy == null || usedFor == null) {
      return Uni.createFrom().item(ImmutableGrimMissionContainer.builder().build());
    }
    if(this.missionIds == null) {
      final var sql = registry.commitViewers().findAll();
      if(log.isDebugEnabled()) {
        log.debug("User findAllViewers query, with props: {} \r\n{}", 
            "",
            sql.getValue());
      }
      return dataSource.getClient().preparedQuery(sql.getValue())
          .mapping(registry.commitViewers().defaultMapper())
          .execute()
          .onItem()
          .transformToMulti(RowSet::toMulti).collect().asList()
          .onFailure().invoke(e -> errorHandler.deadEnd(sql.failed(e, "Can't find '%s'!", GrimDocType.GRIM_COMMIT_VIEWER)))
          .onItem().transform(items -> ImmutableGrimMissionContainer
              .builder().views(items.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
              .build()
          );
    }
    
    final var sql = registry.commitViewers().findAllByMissionIdsUsedByAndCommit(this.missionIds, usedBy, usedFor);
    if(log.isDebugEnabled()) {
      log.debug("User findAllViewers query, with props: {} \r\n{}", 
          sql.getPropsDeepString(),
          sql.getValue());
    }
    return dataSource.getClient().preparedQuery(sql.getValue())
        .mapping(registry.commitViewers().defaultMapper())
        .execute(sql.getProps())
        .onItem()
        .transformToMulti(RowSet::toMulti).collect().asList()
        .onFailure().invoke(e -> errorHandler.deadEnd(sql.failed(e, "Can't find '%s'!", GrimDocType.GRIM_COMMIT_VIEWER)))
        .onItem().transform(items -> ImmutableGrimMissionContainer
            .builder().views(items.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
            .build()
        );
  }
}
