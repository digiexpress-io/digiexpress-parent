package io.resys.thena.structures.grim.actions;

import java.util.ArrayList;
import java.util.Collections;

/*-
 * #%L
 * thena-docdb-api
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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;

import io.resys.thena.api.actions.GrimQueryActions.MissionCommitQuery;
import io.resys.thena.api.entities.grim.GrimAssignment;
import io.resys.thena.api.entities.grim.GrimCommands;
import io.resys.thena.api.entities.grim.GrimCommit;
import io.resys.thena.api.entities.grim.GrimCommitTree;
import io.resys.thena.api.entities.grim.GrimCommitTree.GrimCommitTreeOperation;
import io.resys.thena.api.entities.grim.GrimMission;
import io.resys.thena.api.entities.grim.GrimMissionData;
import io.resys.thena.api.entities.grim.GrimMissionLabel;
import io.resys.thena.api.entities.grim.GrimMissionLink;
import io.resys.thena.api.entities.grim.GrimObjective;
import io.resys.thena.api.entities.grim.GrimObjectiveGoal;
import io.resys.thena.api.entities.grim.GrimRemark;
import io.resys.thena.api.entities.grim.ImmutableGrimCommands;
import io.resys.thena.api.entities.grim.ImmutableGrimCommitTree;
import io.resys.thena.api.entities.grim.ImmutableGrimContainerVersion;
import io.resys.thena.api.entities.grim.ImmutableGrimMission;
import io.resys.thena.api.entities.grim.ImmutableGrimMissionContainer;
import io.resys.thena.api.entities.grim.ImmutableGrimMissionLink;
import io.resys.thena.api.entities.grim.ImmutableGrimMissionLinkTransitives;
import io.resys.thena.api.entities.grim.ImmutableGrimMissionTransitives;
import io.resys.thena.api.entities.grim.ImmutableGrimObjective;
import io.resys.thena.api.entities.grim.ImmutableGrimObjectiveGoal;
import io.resys.thena.api.entities.grim.ImmutableGrimObjectiveGoalTransitives;
import io.resys.thena.api.entities.grim.ImmutableGrimObjectiveTransitives;
import io.resys.thena.api.entities.grim.ImmutableGrimRemark;
import io.resys.thena.api.entities.grim.ImmutableGrimRemarkTransitives;
import io.resys.thena.api.entities.grim.ThenaGrimContainers.GrimContainerVersion;
import io.resys.thena.api.entities.grim.ThenaGrimContainers.GrimMissionContainer;
import io.resys.thena.api.entities.grim.ThenaGrimObject.GrimDocType;
import io.resys.thena.api.entities.grim.ThenaGrimObject.GrimRelationType;
import io.resys.thena.api.envelope.ImmutableQueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.api.exceptions.RepoException;
import io.resys.thena.spi.DbState;
import io.resys.thena.structures.grim.GrimState;
import io.resys.thena.support.OidUtils;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;


@RequiredArgsConstructor @Setter @Accessors(fluent = true)
public class GrimMissionCommitQueryImpl implements MissionCommitQuery {
  private final DbState startingState;
  private final String repoId;

  @Override
  public Uni<QueryEnvelope<GrimContainerVersion>> findCommit(String missionId, String currentCommitId) {
    return startingState.toGrimState(repoId).onItem().transformToUni(tx -> {
      return visitCommit(tx, missionId)
          .onItem()
          .transform(commit -> visitResponse(tx, commit, missionId, currentCommitId));
    });
  }

  private Uni<Tuple2<List<GrimCommit>, List<GrimCommitTree>>> visitCommit(GrimState tx, String missionId) {
    return Uni.combine().all().unis(
      tx.query().commit().findAllByMissionId(missionId),
      tx.query().commitTree().findAllByMissionId(missionId)
    )
    .asTuple()
    .onItem().transformToUni(tuple -> {
      
      final var commitsWithTrees = getCommitsWithTrees(tuple.getItem1(), tuple.getItem2());
      final var firstCommitWithTree = commitsWithTrees.stream().filter(e -> e.getParentCommitId() == null).findFirst();
      final var isHistoryLimit = firstCommitWithTree.isEmpty();
      
      if(isHistoryLimit) {
        final var customCommits = getCommitsAfterCutOff(tx, missionId, tuple.getItem1(), tuple.getItem2());
        return getLatestDataAsCommitTreeUsedInCaseHistoryIsNotAvailable(tx, missionId, customCommits, tuple.getItem2())
            .onItem().transform(latestData -> Tuple2.of(customCommits, latestData));
      }
      return Uni.createFrom().item(Tuple2.of(commitsWithTrees, tuple.getItem2()));
    });
  }
  
  private List<GrimCommit> getCommitsWithTrees(List<GrimCommit> commits, List<GrimCommitTree> trees) {
    final var treesByCommitId = trees.stream()
        .collect(Collectors.groupingBy(e -> e.getCommitId()));
    
    final var result = new ArrayList<GrimCommit>();
    for(final var commit : commits) {
      if(treesByCommitId.containsKey(commit.getCommitId())) {
        result.add(commit);
      }
    }
    return Collections.unmodifiableList(result);
  }
  

  private List<GrimCommit> getCommitsWithoutTrees(List<GrimCommit> commits, List<GrimCommitTree> trees) {
    final var treesByCommitId = trees.stream()
        .collect(Collectors.groupingBy(e -> e.getCommitId()));
    
    final var result = new ArrayList<GrimCommit>();
    for(final var commit : commits) {
      if(!treesByCommitId.containsKey(commit.getCommitId())) {
        result.add(commit);
      }
    }
    return Collections.unmodifiableList(result);
  }
  
  
  private List<GrimCommit> getCommitsAfterCutOff(GrimState tx, String missionId, List<GrimCommit> commits, List<GrimCommitTree> trees) {
    final var treesByCommitId = trees.stream().collect(Collectors.groupingBy(e -> e.getCommitId()));
    final var result = new ArrayList<GrimCommit>();
    final var commitsSorted = commits.stream().sorted((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt())).toList();
    
    
    var isFirstTreeFound = false;
    for(final var commit : commitsSorted) {
      final var isTreeDefined = treesByCommitId.containsKey(commit.getCommitId());
      
      if(isTreeDefined) {
        isFirstTreeFound = true;
      }
      
      if(isTreeDefined || !isFirstTreeFound) {
        result.add(commit);        
      } 
    }
    return result;
  }
  
  
  
  private Uni<List<GrimCommitTree>> getLatestDataAsCommitTreeUsedInCaseHistoryIsNotAvailable(
      GrimState tx, 
      String missionId, 
      List<GrimCommit> commits,
      List<GrimCommitTree> trees) {
    
    return tx.query().missions()
    .missionId(missionId)
    .excludeDocs(GrimDocType.GRIM_COMMIT, GrimDocType.GRIM_COMMIT_VIEWER)
    .findAll()
    .collect().asList().onItem().transform(e -> {
      final var container = e.iterator().next();
      
      
      final var firstCommit = commits.stream().filter(c -> c.getParentCommitId() == null)
          .findFirst()
          .orElseThrow(() -> new IllegalArgumentException("Can't find first commit!"));
      
      
      final var commitsWithoutTrees = getCommitsWithoutTrees(commits, trees)
          .stream().map(c -> c.getCommitId()).toList();
      
      final List<GrimCommitTree> firstCommitTree = ImmutableList.builder()
        .add(container.getMission())
        
        .addAll(container.getLinks().values().stream()
            .filter(entry -> commitsWithoutTrees.contains(entry.getCreatedWithCommitId()))
            .toList())
        
        .addAll(container.getMissionLabels().values().stream()
            .filter(entry -> commitsWithoutTrees.contains(entry.getCommitId()))
            .toList())
        
        .addAll(container.getRemarks().values().stream()
            .filter(entry -> commitsWithoutTrees.contains(entry.getCreatedWithCommitId()))
            .toList())
        
        .addAll(container.getObjectives().values().stream()
            .filter(entry -> commitsWithoutTrees.contains(entry.getCreatedWithCommitId()))
            .toList())
        
        .addAll(container.getGoals().values().stream()
            .filter(entry -> commitsWithoutTrees.contains(entry.getCreatedWithCommitId()))
            .toList())
        
        .addAll(container.getData().values().stream()
            .filter(entry -> commitsWithoutTrees.contains(entry.getCreatedWithCommitId()))
            .toList())
        
        .addAll(container.getAssignments().values().stream()
            .filter(entry -> commitsWithoutTrees.contains(entry.getCommitId()))
            .toList())
        
        .addAll(container.getCommands().values().stream()
            .filter(entry -> commitsWithoutTrees.contains(entry.getCommitId()))
            .toList())
        
        .build()
        .stream()
        .map(entity -> {
      
          final GrimCommitTree tree = ImmutableGrimCommitTree.builder()
            .id(OidUtils.gen())
            .commitId(firstCommit.getCommitId())
            .operationType(GrimCommitTreeOperation.ADD)
            .bodyAfter(JsonObject.mapFrom(entity))
            .build();
          
          return tree;
        })
        .toList();
      
      
      return ImmutableList.<GrimCommitTree>builder()
            .addAll(firstCommitTree)
            .addAll(trees)
            .build();
    });
  }
  

  
  private QueryEnvelope<GrimContainerVersion> visitResponse(
      GrimState tx, 
      Tuple2<List<GrimCommit>, List<GrimCommitTree>> commit, 
      String missionId, String currentCommitId) {
    
    return ImmutableQueryEnvelope.<GrimContainerVersion>builder()
      .repo(tx.getDataSource().getTenant())
      .status(QueryEnvelopeStatus.OK)
      .objects(new GrimMissionContainerVersionVisitor(tx, commit.getItem1(), commit.getItem2(), missionId, currentCommitId).accept())
      .build();
  }
  
  
  public static class GrimMissionContainerVersionVisitor {
    private final GrimState tx;
    private final String missionId; 
    private final String currentCommitId;
    private final Map<String, GrimCommit> commitsById;
    private final Map<String, List<GrimCommit>> commitsByParentId;
    private final Map<String, List<GrimCommitTree>> treesByCommitId;
    private GrimMissionContainer container;
    private GrimMissionContainer parentVersion;
    
    public GrimMissionContainerVersionVisitor(
        GrimState tx,
        List<GrimCommit> commitsById,
        List<GrimCommitTree> treesByCommitId,
        String missionId, String currentCommitId) {
      super();
      
      this.tx = tx;
      this.missionId = missionId;
      this.currentCommitId = currentCommitId;
      
      this.commitsById = commitsById.stream()
          .collect(Collectors.toMap(e -> e.getCommitId(), e -> e));
      
      this.commitsByParentId = commitsById.stream()
          .collect(Collectors.groupingBy(e -> Optional.ofNullable(e.getParentCommitId()).orElse("")));
      
      this.treesByCommitId = treesByCommitId.stream()
          .collect(Collectors.groupingBy(e -> e.getCommitId()));
    }

    public GrimContainerVersion accept() {
      final var tip = commitsById.values().stream()
          .filter(e -> e.getParentCommitId() == null)
          .findFirst().orElseThrow(() -> new RepoException("No starting commit for mission!"))
          ;
      
      visitCommit(tip);
      
      return ImmutableGrimContainerVersion.builder()
          .currentCommitId(currentCommitId)
          .missionId(missionId)
          .currentVersion(container)
          .parentVersion(parentVersion)
          .build();
    }
    
    private void visitCommit(GrimCommit commit) {
      if(commit.getCommitId().equals(this.currentCommitId)) {
        parentVersion = container;
      }
      
      container = ImmutableGrimMissionContainer.builder()
          .from(container == null ? ImmutableGrimMissionContainer.builder().build() : this.container)
          .putCommits(commit.getCommitId(), commit)
          .build();
      
      visitTree(commit);      
      if(commit.getCommitId().equals(this.currentCommitId)) {
        return;
      }
      visitNextCommit(commit);
    }
    
    private void visitNextCommit(GrimCommit commit) {
      final var next = commitsByParentId.get(Optional.ofNullable(commit.getCommitId()).orElse(""));
      if(next == null) {
        return;
      }
      next.forEach(entry -> visitCommit(entry));
    }
    
    private void visitTree(GrimCommit commit) {
      final var tree = treesByCommitId.get(commit.getCommitId());
      if(tree == null) {
        return;
      }
      
      // apply changes
      tree.forEach(this::visitOperation);
      
      // build transitive data
      visitMissionCommandTransitives(commit);
      visitMissionTransitives(commit);
      visitMissionLinkTransitives(commit);
      visitMissionRemarkTransitives(commit);
      visitMissionObjectiveTransitives(commit);
      visitMissionObjectiveGoalTransitives(commit);
    }
    
    private void visitMissionCommandTransitives(GrimCommit commit) {
      final var nextState = container.getCommands().values().stream().map(e -> {

        return ImmutableGrimCommands.builder()
            .from(e)
            .createdAt(this.commitsById.get(e.getCommitId()).getCreatedAt())
            .build();
      })
      .collect(Collectors.toMap(e -> e.getId(), e -> e));
      
      container = ImmutableGrimMissionContainer.builder()
          .from(container)
          .commands(nextState)
          .build();
    }
    
    private void visitMissionTransitives(GrimCommit commit) {
      final var nextState = container.getMissions().values().stream().map(e -> {
        
        final var dataExtension = container.getData().values().stream()
            .filter(d -> d.getRelation() == null)
            .filter(d -> e.getId().equals(d.getMissionId()))
            .map(d -> d.getDataExtension())
            .findFirst();
        
        final var transitives = ImmutableGrimMissionTransitives.builder()
            .createdAt(this.commitsById.get(e.getCreatedWithCommitId()).getCreatedAt())
            .updatedAt(this.commitsById.get(e.getCommitId()).getCreatedAt())
            .dataExtension(dataExtension.orElse(null))
            .treeUpdatedAt(commit.getCreatedAt())
            .treeUpdatedBy(commit.getCommitAuthor())
            .build();
        
        return ImmutableGrimMission.builder()
            .from(e)
            .transitives(transitives)
            .build();
      })
      .collect(Collectors.toMap(e -> e.getId(), e -> e));
      
      container = ImmutableGrimMissionContainer.builder()
          .from(container)
          .missions(nextState)
          .build();
    }

    private void visitMissionLinkTransitives(GrimCommit commit) {
      final var nextState = container.getLinks().values().stream().map(e -> {

        final var transitives = ImmutableGrimMissionLinkTransitives.builder()
            .createdAt(this.commitsById.get(e.getCreatedWithCommitId()).getCreatedAt())
            .updatedAt(this.commitsById.get(e.getCommitId()).getCreatedAt())
            .build();
        
        return ImmutableGrimMissionLink.builder()
            .from(e)
            .transitives(transitives)
            .build();
      })
      .collect(Collectors.toMap(e -> e.getId(), e -> e));
      
      container = ImmutableGrimMissionContainer.builder()
          .from(container)
          .links(nextState)
          .build();
    }

    private void visitMissionRemarkTransitives(GrimCommit commit) {
      
      final var nextState = container.getRemarks().values().stream().map(e -> {

        final var transitives = ImmutableGrimRemarkTransitives.builder()
            .createdBy(this.commitsById.get(e.getCreatedWithCommitId()).getCommitAuthor())
            .createdAt(this.commitsById.get(e.getCreatedWithCommitId()).getCreatedAt())
            .updatedAt(this.commitsById.get(e.getCommitId()).getCreatedAt())
            .build();
        
        return ImmutableGrimRemark.builder()
            .from(e)
            .transitives(transitives)
            .build();
      })
      .collect(Collectors.toMap(e -> e.getId(), e -> e));
      
      container = ImmutableGrimMissionContainer.builder()
          .from(container)
          .remarks(nextState)
          .build();
    }
    
    private void visitMissionObjectiveTransitives(GrimCommit commit) {
      
      
      final var nextState = container.getObjectives().values().stream().map(e -> {

        final var dataExtension = container.getData().values().stream()
            .filter(d -> d.getRelation() != null)
            .filter(d -> d.getRelation().getRelationType() == GrimRelationType.OBJECTIVE)
            .filter(d -> e.getMissionId().equals(d.getMissionId()))
            .filter(d -> e.getId().equals(d.getRelation().getTargetId()))
            .map(d -> d.getDataExtension())
            .findFirst();
        
        final var transitives = ImmutableGrimObjectiveTransitives.builder()
            .createdAt(this.commitsById.get(e.getCreatedWithCommitId()).getCreatedAt())
            .updatedAt(this.commitsById.get(e.getCommitId()).getCreatedAt())
            .dataExtension(dataExtension.orElse(null))
            .build();
        
        return ImmutableGrimObjective.builder()
            .from(e)
            .transitives(transitives)
            .build();
      })
      .collect(Collectors.toMap(e -> e.getId(), e -> e));
      
      container = ImmutableGrimMissionContainer.builder()
          .from(container)
          .objectives(nextState)
          .build();
    }
    
    private void visitMissionObjectiveGoalTransitives(GrimCommit commit) {
      final var nextState = container.getGoals().values().stream().map(e -> {
        final var dataExtension = container.getData().values().stream()
            .filter(d -> d.getRelation() != null)
            .filter(d -> d.getRelation().getRelationType() == GrimRelationType.GOAL)
            .filter(d -> commit.getMissionId().equals(d.getMissionId()))
            .filter(d -> e.getId().equals(d.getRelation().getTargetId()))
            .map(d -> d.getDataExtension())
            .findFirst();
        
        final var transitives = ImmutableGrimObjectiveGoalTransitives.builder()
            .missionId(commit.getMissionId())
            .dataExtension(dataExtension.orElse(null))
            .createdAt(this.commitsById.get(e.getCreatedWithCommitId()).getCreatedAt())
            .updatedAt(this.commitsById.get(e.getCommitId()).getCreatedAt())
            .build();
        
        return ImmutableGrimObjectiveGoal.builder()
            .from(e)
            .transitives(transitives)
            .build();
      })
      .collect(Collectors.toMap(e -> e.getId(), e -> e));
      
      container = ImmutableGrimMissionContainer.builder()
          .from(container)
          .goals(nextState)
          .build();
    }
    
    private void visitOperation(GrimCommitTree tree) {
      final var docType = Optional.ofNullable(tree.getBodyAfter())
          .or(() -> Optional.ofNullable(tree.getBodyBefore()))
          .map(body -> body.getString("docType"))
          .map(GrimDocType::valueOf);
      if(docType.isEmpty()) {
        return;
      }
      switch (docType.get()) {
        case GRIM_MISSION: visitMission(tree); break;
        case GRIM_MISSION_LINKS: visitLink(tree); break;
        case GRIM_MISSION_LABEL: visitLabel(tree); break;
        case GRIM_REMARK: visitRemark(tree); break;
        case GRIM_OBJECTIVE: visitObjective(tree); break;
        case GRIM_OBJECTIVE_GOAL: visitGoal(tree); break;
        case GRIM_MISSION_DATA: visitData(tree); break;
        case GRIM_ASSIGNMENT: visitAssignment(tree); break;
        case GRIM_COMMANDS: visitCommands(tree); break;
        default: break;
      }
    }
    
    private void visitMission(GrimCommitTree tree) {
      final var entity = parse(tree, GrimMission.class);
      if(tree.getOperationType() == GrimCommitTreeOperation.ADD) {
        container = ImmutableGrimMissionContainer.builder()
            .from(container)
            .putMissions(entity.getItem2().get().getId(), entity.getItem2().get())
            .build();
      } else if(tree.getOperationType() == GrimCommitTreeOperation.MERGE) {
        container = ImmutableGrimMissionContainer.builder()
            .from(container)
            .missions(container.getMissions().values().stream()
                .filter(e -> !e.getId().equals(entity.getItem1().get().getId()) )
                .collect(Collectors.toMap(e -> e.getId(), e -> e)))
            .putMissions(entity.getItem2().get().getId(), entity.getItem2().get())
            .build();
      } else if(tree.getOperationType() == GrimCommitTreeOperation.REMOVE) {
        container = ImmutableGrimMissionContainer.builder()
            .from(container)
            .missions(container.getMissions().values().stream()
                .filter(e -> !e.getId().equals(entity.getItem1().get().getId()) )
                .collect(Collectors.toMap(e -> e.getId(), e -> e)))
            .build();
      }
    }
    
    private void visitLabel(GrimCommitTree tree) {
      final var entity = parse(tree, GrimMissionLabel.class);
      if(tree.getOperationType() == GrimCommitTreeOperation.ADD) {
        container = ImmutableGrimMissionContainer.builder()
            .from(container)
            .putMissionLabels(entity.getItem2().get().getId(), entity.getItem2().get())
            .build();
      } else if(tree.getOperationType() == GrimCommitTreeOperation.MERGE) {
        container = ImmutableGrimMissionContainer.builder()
            .from(container)
            .missionLabels(container.getMissionLabels().values().stream()
                .filter(e -> !e.getId().equals(entity.getItem1().get().getId()) )
                .collect(Collectors.toMap(e -> e.getId(), e -> e)))
            .putMissionLabels(entity.getItem2().get().getId(), entity.getItem2().get())
            .build();
      } else if(tree.getOperationType() == GrimCommitTreeOperation.REMOVE) {
        container = ImmutableGrimMissionContainer.builder()
            .from(container)
            .missionLabels(container.getMissionLabels().values().stream()
                .filter(e -> !e.getId().equals(entity.getItem1().get().getId()) )
                .collect(Collectors.toMap(e -> e.getId(), e -> e)))
            .build();
      }
      
    }
    private void visitLink(GrimCommitTree tree) {
      
      final var entity = parse(tree, GrimMissionLink.class);
      if(tree.getOperationType() == GrimCommitTreeOperation.ADD) {
        container = ImmutableGrimMissionContainer.builder()
            .from(container)
            .putLinks(entity.getItem2().get().getId(), entity.getItem2().get())
            .build();
      } else if(tree.getOperationType() == GrimCommitTreeOperation.MERGE) {
        container = ImmutableGrimMissionContainer.builder()
            .from(container)
            .links(container.getLinks().values().stream()
                .filter(e -> !e.getId().equals(entity.getItem1().get().getId()) )
                .collect(Collectors.toMap(e -> e.getId(), e -> e)))
            .putLinks(entity.getItem2().get().getId(), entity.getItem2().get())
            .build();
      } else if(tree.getOperationType() == GrimCommitTreeOperation.REMOVE) {
        container = ImmutableGrimMissionContainer.builder()
            .from(container)
            .links(container.getLinks().values().stream()
                .filter(e -> !e.getId().equals(entity.getItem1().get().getId()) )
                .collect(Collectors.toMap(e -> e.getId(), e -> e)))
            .build();
      }
      
    }
    private void visitRemark(GrimCommitTree tree) {
      
      final var entity = parse(tree, GrimRemark.class);
      if(tree.getOperationType() == GrimCommitTreeOperation.ADD) {
        container = ImmutableGrimMissionContainer.builder()
            .from(container)
            .putRemarks(entity.getItem2().get().getId(), entity.getItem2().get())
            .build();
      } else if(tree.getOperationType() == GrimCommitTreeOperation.MERGE) {
        container = ImmutableGrimMissionContainer.builder()
            .from(container)
            .remarks(container.getRemarks().values().stream()
                .filter(e -> !e.getId().equals(entity.getItem1().get().getId()) )
                .collect(Collectors.toMap(e -> e.getId(), e -> e)))
            .putRemarks(entity.getItem2().get().getId(), entity.getItem2().get())
            .build();
      } else if(tree.getOperationType() == GrimCommitTreeOperation.REMOVE) {
        container = ImmutableGrimMissionContainer.builder()
            .from(container)
            .remarks(container.getRemarks().values().stream()
                .filter(e -> !e.getId().equals(entity.getItem1().get().getId()) )
                .collect(Collectors.toMap(e -> e.getId(), e -> e)))
            .build();
      }
    }
    private void visitObjective(GrimCommitTree tree) {
      
      final var entity = parse(tree, GrimObjective.class);
      if(tree.getOperationType() == GrimCommitTreeOperation.ADD) {
        container = ImmutableGrimMissionContainer.builder()
            .from(container)
            .putObjectives(entity.getItem2().get().getId(), entity.getItem2().get())
            .build();
      } else if(tree.getOperationType() == GrimCommitTreeOperation.MERGE) {
        container = ImmutableGrimMissionContainer.builder()
            .from(container)
            .objectives(container.getObjectives().values().stream()
                .filter(e -> !e.getId().equals(entity.getItem1().get().getId()) )
                .collect(Collectors.toMap(e -> e.getId(), e -> e)))
            .putObjectives(entity.getItem2().get().getId(), entity.getItem2().get())
            .build();
      } else if(tree.getOperationType() == GrimCommitTreeOperation.REMOVE) {
        container = ImmutableGrimMissionContainer.builder()
            .from(container)
            .objectives(container.getObjectives().values().stream()
                .filter(e -> !e.getId().equals(entity.getItem1().get().getId()) )
                .collect(Collectors.toMap(e -> e.getId(), e -> e)))
            .build();
      }
    }
    private void visitGoal(GrimCommitTree tree) {
      
      final var entity = parse(tree, GrimObjectiveGoal.class);
      if(tree.getOperationType() == GrimCommitTreeOperation.ADD) {
        container = ImmutableGrimMissionContainer.builder()
            .from(container)
            .putGoals(entity.getItem2().get().getId(), entity.getItem2().get())
            .build();
      } else if(tree.getOperationType() == GrimCommitTreeOperation.MERGE) {
        container = ImmutableGrimMissionContainer.builder()
            .from(container)
            .goals(container.getGoals().values().stream()
                .filter(e -> !e.getId().equals(entity.getItem1().get().getId()) )
                .collect(Collectors.toMap(e -> e.getId(), e -> e)))
            .putGoals(entity.getItem2().get().getId(), entity.getItem2().get())
            .build();
      } else if(tree.getOperationType() == GrimCommitTreeOperation.REMOVE) {
        container = ImmutableGrimMissionContainer.builder()
            .from(container)
            .goals(container.getGoals().values().stream()
                .filter(e -> !e.getId().equals(entity.getItem1().get().getId()) )
                .collect(Collectors.toMap(e -> e.getId(), e -> e)))
            .build();
      }
    }
    private void visitData(GrimCommitTree tree) {
      
      final var entity = parse(tree, GrimMissionData.class);
      if(tree.getOperationType() == GrimCommitTreeOperation.ADD) {
        container = ImmutableGrimMissionContainer.builder()
            .from(container)
            .putData(entity.getItem2().get().getId(), entity.getItem2().get())
            .build();
      } else if(tree.getOperationType() == GrimCommitTreeOperation.MERGE) {
        container = ImmutableGrimMissionContainer.builder()
            .from(container)
            .data(container.getData().values().stream()
                .filter(e -> !e.getId().equals(entity.getItem1().get().getId()) )
                .collect(Collectors.toMap(e -> e.getId(), e -> e)))
            .putData(entity.getItem2().get().getId(), entity.getItem2().get())
            .build();
      } else if(tree.getOperationType() == GrimCommitTreeOperation.REMOVE) {
        container = ImmutableGrimMissionContainer.builder()
            .from(container)
            .data(container.getData().values().stream()
                .filter(e -> !e.getId().equals(entity.getItem1().get().getId()) )
                .collect(Collectors.toMap(e -> e.getId(), e -> e)))
            .build();
      }
    }
    private void visitAssignment(GrimCommitTree tree) {
      
      final var entity = parse(tree, GrimAssignment.class);
      if(tree.getOperationType() == GrimCommitTreeOperation.ADD) {
        container = ImmutableGrimMissionContainer.builder()
            .from(container)
            .putAssignments(entity.getItem2().get().getId(), entity.getItem2().get())
            .build();
      } else if(tree.getOperationType() == GrimCommitTreeOperation.MERGE) {
        container = ImmutableGrimMissionContainer.builder()
            .from(container)
            .assignments(container.getAssignments().values().stream()
                .filter(e -> !e.getId().equals(entity.getItem1().get().getId()) )
                .collect(Collectors.toMap(e -> e.getId(), e -> e)))
            .putAssignments(entity.getItem2().get().getId(), entity.getItem2().get())
            .build();
      } else if(tree.getOperationType() == GrimCommitTreeOperation.REMOVE) {
        container = ImmutableGrimMissionContainer.builder()
            .from(container)
            .assignments(container.getAssignments().values().stream()
                .filter(e -> !e.getId().equals(entity.getItem1().get().getId()) )
                .collect(Collectors.toMap(e -> e.getId(), e -> e)))
            .build();
      }
    }
    private void visitCommands(GrimCommitTree tree) {
      
      final var entity = parse(tree, GrimCommands.class);
      if(tree.getOperationType() == GrimCommitTreeOperation.ADD) {
        container = ImmutableGrimMissionContainer.builder()
            .from(container)
            .putCommands(entity.getItem2().get().getId(), entity.getItem2().get())
            .build();
      } else if(tree.getOperationType() == GrimCommitTreeOperation.MERGE) {
        container = ImmutableGrimMissionContainer.builder()
            .from(container)
            .commands(container.getCommands().values().stream()
                .filter(e -> !e.getId().equals(entity.getItem1().get().getId()) )
                .collect(Collectors.toMap(e -> e.getId(), e -> e)))
            .putCommands(entity.getItem2().get().getId(), entity.getItem2().get())
            .build();
      } else if(tree.getOperationType() == GrimCommitTreeOperation.REMOVE) {
        container = ImmutableGrimMissionContainer.builder()
            .from(container)
            .commands(container.getCommands().values().stream()
                .filter(e -> !e.getId().equals(entity.getItem1().get().getId()) )
                .collect(Collectors.toMap(e -> e.getId(), e -> e)))
            .build();
      }
    }
    
    private <T> Tuple2<Optional<T>, Optional<T>> parse(GrimCommitTree tree, Class<T> type) {
      final var before = Optional.ofNullable(tree.getBodyBefore()).map(b -> b.mapTo(type));
      final var after =  Optional.ofNullable(tree.getBodyAfter()).map(b -> b.mapTo(type));
      return Tuple2.of(before, after);
    }
  }
}
