package io.resys.thena.structures.git.diff;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2021 Copyright 2021 ReSys OÜ
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.immutables.value.Value;

import io.resys.thena.api.ThenaClient;
import io.resys.thena.api.actions.GitCommitActions.CommitObjects;
import io.resys.thena.api.entities.git.Diff;
import io.resys.thena.api.entities.git.Diff.Divergence;
import io.resys.thena.api.entities.git.Diff.DivergenceType;
import io.resys.thena.api.entities.git.ImmutableDiff;
import io.resys.thena.api.entities.git.ImmutableDivergence;
import io.resys.thena.api.entities.git.ImmutableDivergenceRef;

public class DiffVisitor {

  @Value.Immutable
  public interface DiffCommitMatch {
    CommitHistory getSrc();
    CommitHistory getTarget();
  }
  
  public Diff visit(ThenaClient.GitRepoObjects repo, CommitObjects start, List<CommitObjects> targets) { 
    List<DiffCommitMatch> result = visitHistories(repo, CommitHistory.builder().from(repo, start.getCommit().getId()), targets);
    
    List<Divergence> values = result.stream()
      .map(m -> visitDivergence(repo, m))
      .collect(Collectors.toList());
    return ImmutableDiff.builder()
        .divergences(values)
        .repo(start.getRepo())
        .build();
  }
  
  private Divergence visitDivergence(ThenaClient.GitRepoObjects repo, DiffCommitMatch match) {
    final var main = match.getSrc();
    final var head = match.getTarget();
    
    
    return ImmutableDivergence.builder()
        .main(ImmutableDivergenceRef.builder()
            .commit(main.getCommit())
            .commits(main.getIndex())
            .tags(repo.getBranches().values().stream()
                .filter(t -> t.getCommit().equals(main.getCommit().getId()))
                .map(t -> t.getName())
                .collect(Collectors.toList()))
            .tags(repo.getTags().values().stream()
                .filter(t -> t.getCommit().equals(main.getCommit().getId()))
                .map(t -> t.getName())
                .collect(Collectors.toList()))
            .build())
        .head(ImmutableDivergenceRef.builder()
            .commit(head.getCommit())
            .commits(head.getIndex())
            .tags(repo.getBranches().values().stream()
                .filter(t -> t.getCommit().equals(head.getCommit().getId()))
                .map(t -> t.getName())
                .collect(Collectors.toList()))
            .tags(repo.getTags().values().stream()
                .filter(t -> t.getCommit().equals(head.getCommit().getId()))
                .map(t -> t.getName())
                .collect(Collectors.toList()))
            .build())
        //.actions(null)
        .type(DivergenceType.CONFLICT)
        .build();
  }
  
  private List<DiffCommitMatch> visitHistories(ThenaClient.GitRepoObjects repo, CommitHistory start, List<CommitObjects> end) {
    List<DiffCommitMatch> result = new ArrayList<>(); 
    List<CommitHistory> toBeVisited = new ArrayList<>(end.stream()
        .map(e -> CommitHistory.builder().from(repo, e.getCommit().getId(), start.getCommit().getDateTime()))
        .collect(Collectors.toList()));

    var next = start;
    do {
      Iterator<CommitHistory> iterator = toBeVisited.iterator();
      while(iterator.hasNext()) {
        
        final var target = iterator.next().getSelect();
        final var match = visitHistory(next, target);
        if(match.isPresent()) {
          iterator.remove();
          result.add(match.get());
          continue;
        }
      }

      next = next.getBefore().orElse(null);
    } while(next != null);

    return result;
  }

  public Optional<DiffCommitMatch> visitHistory(CommitHistory start, CommitHistory target) {
    var next = target;
    do {
      if(start.getCommit().getId().equals(next.getCommit().getId())) {
        return Optional.of(ImmutableDiffCommitMatch.builder().src(start).target(next).build()); 
      }      

      if(next.getCommit().getDateTime().isBefore(start.getCommit().getDateTime())) {
        return Optional.empty();
      }
      
      next = start.getBefore().orElse(null);
    } while(next != null);
    
    return Optional.empty();
  }
}
