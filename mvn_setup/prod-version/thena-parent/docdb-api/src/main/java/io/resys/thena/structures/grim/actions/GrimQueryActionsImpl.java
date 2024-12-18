package io.resys.thena.structures.grim.actions;

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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.resys.thena.api.actions.GrimQueryActions;
import io.resys.thena.api.entities.grim.ThenaGrimContainers.GrimMissionContainer;
import io.resys.thena.api.envelope.ImmutableQueryEnvelope;
import io.resys.thena.api.envelope.ImmutableQueryEnvelopeList;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.api.envelope.QueryEnvelopeList;
import io.resys.thena.spi.DbState;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import io.smallrye.mutiny.tuples.Tuple3;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GrimQueryActionsImpl implements GrimQueryActions {
  private final DbState state;
  private final String repoId;
  
  @Override
  public CommitViewersQuery commitViewersQuery() {
    // TODO Auto-generated method stub
    return null;
  }
  @Override
  public MissionQuery missionQuery() {
    final var assignments = new ArrayList<Tuple3<String, Boolean, List<String>>>();
    final var links = new ArrayList<Tuple2<String, String>>();
    return new MissionQuery() {
      private String usedBy, usedFor, reporterId, likeTitle, likeDescription;
      private List<String> ids;
      private List<String> status;
      private List<String> priority;
      private GrimArchiveQueryType includeArchived;
      private LocalDate fromCreatedOrUpdated;
      private Boolean overdue;
      
      @Override
      public MissionQuery addMissionId(List<String> ids) {
        if(this.ids == null) {
          this.ids = new ArrayList<>();
        }
        this.ids.addAll(ids);
        return this;
      }
      @Override
      public MissionQuery addAssignment(String assignementType, boolean exact, List<String> assignmentId) {
        assignments.add(Tuple3.of(assignementType, exact, assignmentId));
        return this;
      }
      @Override
      public MissionQuery addAssignment(String assignementType, boolean exact, String...assignmentId) {
        return addAssignment(assignementType, exact, Arrays.asList(assignmentId));
      }
      @Override
      public MissionQuery status(List<String> status) {
        if(this.status == null) {
          this.status = new ArrayList<>();
        }
        this.status.addAll(status);
        return this;
      }
      @Override
      public MissionQuery priority(List<String> priority) {
        if(this.priority == null) {
          this.priority = new ArrayList<>();
        }
        this.priority.addAll(priority);
        return this;
      }
      @Override
      public MissionQuery addLink(String linkType, String extId) {
        links.add(Tuple2.of(linkType, extId));
        return this;
      }
      @Override
      public MissionQuery viewer(String usedBy, String usedFor) {
        this.usedBy = RepoAssert.notEmpty(usedBy, () -> "usedBy can't be empty!"); 
        this.usedFor = RepoAssert.notEmpty(usedFor, () -> "usedFor can't be empty!"); 
        return this;
      }
      @Override
      public MissionQuery archived(GrimArchiveQueryType includeArchived) {
        this.includeArchived = includeArchived;
        return this;
      }
      @Override
      public MissionQuery likeReporterId(String reporterId) {
        this.reporterId = reporterId;
        return this;
      }
      @Override
      public MissionQuery overdue(Boolean overdue) {
        this.overdue = overdue;
        return this;
      }
      @Override
      public MissionQuery likeTitle(String likeTitle) {
        this.likeTitle = likeTitle;
        return this;
      }
      @Override
      public MissionQuery likeDescription(String likeDescription) {
        this.likeDescription = likeDescription;
        return this;
      }
      @Override
      public MissionQuery fromCreatedOrUpdated(LocalDate fromCreatedOrUpdated) {
        this.fromCreatedOrUpdated = fromCreatedOrUpdated;
        return this;
      }
      @Override
      public Uni<QueryEnvelope<GrimMissionContainer>> get(String missionIdOrExtId) {
        return state.toGrimState(repoId).onItem().transformToUni(state -> {
          final var query = state.query().missions();
          if(usedBy != null) {
            query.viewer(usedBy, usedFor);
          }
          if(this.status != null) {
            query.status(this.status.toArray(new String[] {}));
          }
          if(this.priority != null) {
            query.priority(this.priority.toArray(new String[] {}));
          }
          
          assignments.forEach(e -> query.addAssignment(e.getItem1(), e.getItem2(), e.getItem3()));
          links.forEach(e -> query.addLink(e.getItem1(), e.getItem2()));
          return query
              .likeReporterId(reporterId)
              .archived(includeArchived)
              .fromCreatedOrUpdated(fromCreatedOrUpdated)
              .likeDescription(likeDescription)
              .likeTitle(likeTitle)
              .overdue(overdue)
              .getById(missionIdOrExtId).onItem().transform(items -> 
            ImmutableQueryEnvelope.<GrimMissionContainer>builder()
              .repo(state.getDataSource().getTenant())
              .status(QueryEnvelopeStatus.OK)
              .objects(items)
              .build()
          );
        });
      }
      
      @Override
      public Uni<QueryEnvelopeList<GrimMissionContainer>> findAll() {
        return state.toGrimState(repoId).onItem().transformToUni(state -> {
          final var query = state.query().missions();
          if(usedBy != null) {
            query.viewer(usedBy, usedFor);
          }
          if(this.ids != null) {
            query.missionId(this.ids.toArray(new String[] {}));
          }
          if(this.status != null) {
            query.status(this.status.toArray(new String[] {}));
          }
          if(this.priority != null) {
            query.priority(this.priority.toArray(new String[] {}));
          }
          
          links.forEach(e -> query.addLink(e.getItem1(), e.getItem2()));
          assignments.forEach(e -> query.addAssignment(e.getItem1(), e.getItem2(), e.getItem3()));
          return query
              .likeReporterId(reporterId)
              .archived(includeArchived)
              .fromCreatedOrUpdated(fromCreatedOrUpdated)
              .likeDescription(likeDescription)
              .likeTitle(likeTitle)
              .overdue(overdue)
              .findAll().collect().asList().onItem().transform(items -> 
                ImmutableQueryEnvelopeList.<GrimMissionContainer>builder()
                  .repo(state.getDataSource().getTenant())
                  .status(QueryEnvelopeStatus.OK)
                  .objects(items)
                  .build()
          );
        });
      }

    };
  }
}
