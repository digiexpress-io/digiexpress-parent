package io.resys.thena.api.entities.fs;

/*-
 * #%L
 * thena-docdb-api
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.time.OffsetDateTime;
import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.resys.thena.api.envelope.ThenaContainer;



public interface ThenaFsContainers extends ThenaContainer {

  @Value.Immutable
  interface FsDirentContainer extends ThenaFsContainers { 
    
    Map<String, FsDirent> getDirents();    
    Map<String, FsDirentLabel> getDirentLabels();
    Map<String, FsDirentLink> getLinks();
    Map<String, FsDirentRemark> getRemarks();
    Map<String, FsDirentData> getData();
    Map<String, FsDirentAssignment> getAssignments();
    Map<String, FsCommit> getCommits(); 
    
    @JsonIgnore
    default FsDirent getDirent() {
      return this.getDirents().values().iterator().next();
    }

    @JsonIgnore
    default List<FsDirentContainer> groupByDirent() {
      final var builders = new HashMap<String, ImmutableFsDirentContainer.Builder>(); 
      for(final var mission : getDirents().values()) {
        builders.put(mission.getId(), ImmutableFsDirentContainer.builder().putDirents(mission.getId(), mission));
      }
      getDirentLabels().values().forEach(label -> builders.get(label.getDirentId()).putDirentLabels(label.getId(), label));
      getLinks().values().forEach(link -> builders.get(link.getDirentId()).putLinks(link.getId(), link));
      getRemarks().values().forEach(remark -> builders.get(remark.getDirentId()).putRemarks(remark.getId(), remark));
      getData().values().forEach(data -> builders.get(data.getDirentId()).putData(data.getId(), data));
      getAssignments().values().forEach(assignment -> builders.get(assignment.getDirentId()).putAssignments(assignment.getId(), assignment));
      getCommits().values().stream().filter(e -> builders.containsKey(e.getDirentId())).forEach(commit -> builders.get(commit.getDirentId()).putCommits(commit.getCommitId(), commit));
      return builders.values().stream().map(builder -> builder.build()).collect(Collectors.toList());
    }

    
    @JsonIgnore
    default Optional<OffsetDateTime> getCreatedAt(String commitId) {
      return Optional.ofNullable(this.getCommits().get(commitId)).map(e -> e.getCreatedAt());
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @JsonIgnore
    default FsDirentContainer sort() {
      final var result = ImmutableFsDirentContainer.builder();
      
      final Function<FsDirent, Comparable> getDirentSortingKey = (mission) -> {
        return Optional.ofNullable(mission.getTransitives())
            .map(e -> e.getUpdatedAt())
            .or(() -> getCreatedAt(mission.getCommitId()))
            .map(e -> (Comparable) e)
            .orElse((Comparable) mission.getCommitId());
      };
      
      final Function<FsDirentLabel, Comparable> getLabelSortingKey = (mission) -> {
        return getCreatedAt(mission.getCommitId())
            .map(e -> (Comparable) e)
            .orElse((Comparable) mission.getLabelType() + "/" + mission.getLabelValue());
      };
      
      final Function<FsDirentLink, Comparable> getLinksKey = (mission) -> {
        return Optional.ofNullable(mission.getTransitives())
            .map(e -> e.getUpdatedAt())
            .or(() -> getCreatedAt(mission.getCommitId()))
            .map(e -> (Comparable) e)
            .orElse((Comparable) mission.getLinkType() + "/" + mission.getLinkValue());
      };
      
      final Function<FsDirentRemark, Comparable> getRemarksKey = (mission) -> {
        return Optional.ofNullable(mission.getTransitives())
            .map(e -> e.getUpdatedAt())
            .or(() -> getCreatedAt(mission.getCommitId()))
            .map(e -> (Comparable) e)
            .orElse((Comparable) mission.getCommitId());
      };

      final Function<FsDirentData, Comparable> getDataKey = (mission) -> {
        return Optional.ofNullable(mission.getCreatedAt())
            .or(() -> getCreatedAt(mission.getCommitId()))
            .map(e -> (Comparable) e)
            .orElse((Comparable) mission.getCommitId());
      };
      
      final Function<FsDirentAssignment, Comparable> getAssignmentKey = (mission) -> {
        return getCreatedAt(mission.getCommitId())
            .map(e -> (Comparable) e)
            .orElse((Comparable) mission.getAssignmentType() + "/" + mission.getAssignee());
      };
      
      final Function<FsCommit, OffsetDateTime> getCommitKey = (mission) -> {
        return mission.getCreatedAt();
      };
      
      
      this.getDirents().values().stream()
        .sorted((a, b) -> getDirentSortingKey.apply(a).compareTo(getDirentSortingKey.apply(b)))
        .forEach(m -> result.putDirents(m.getId(), m));

      this.getDirentLabels().values().stream()
        .sorted((a, b) -> getLabelSortingKey.apply(a).compareTo(getLabelSortingKey.apply(b)))
        .forEach(m -> result.putDirentLabels(m.getId(), m));
      
      this.getLinks().values().stream()
        .sorted((a, b) -> getLinksKey.apply(a).compareTo(getLinksKey.apply(b)))
        .forEach(m -> result.putLinks(m.getId(), m));     

      this.getRemarks().values().stream()
        .sorted((a, b) -> getRemarksKey.apply(a).compareTo(getRemarksKey.apply(b)))
        .forEach(m -> result.putRemarks(m.getId(), m));    

      this.getData().values().stream()
        .sorted((a, b) -> getDataKey.apply(a).compareTo(getDataKey.apply(b)))
        .forEach(m -> result.putData(m.getId(), m));  
      
      this.getAssignments().values().stream()
        .sorted((a, b) -> getAssignmentKey.apply(a).compareTo(getAssignmentKey.apply(b)))
        .forEach(m -> result.putAssignments(m.getId(), m));  

      this.getCommits().values().stream()
        .sorted((a, b) -> getCommitKey.apply(a).compareTo(getCommitKey.apply(b)))
        .forEach(m -> result.putCommits(m.getCommitId(), m));
      
      return result.build();
    }
  }
 
  
  // world state
  @Value.Immutable
  interface FsProjectObjects extends ThenaFsContainers { 
    Map<String, FsDirent>  getDirents();
    Map<String, FsDirentLink> getLinks();
    Map<String, FsDirentRemark> getRemarks();
    Map<String, FsDirentData> getData();
    Map<String, FsDirentAssignment> getAssignments();
    Map<String, FsCommit> getCommits();
    Map<String, FsCommitTree> getCommitTrees();
    
  }
  
}
