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

import io.resys.thena.api.actions.GrimQueryActions.GrimArchiveQueryType;
import io.resys.thena.api.actions.GrimQueryActions.MissionOrderByType;
import io.resys.thena.api.actions.GrimQueryActions.MissionQuery;
import io.resys.thena.api.entities.PageQuery;
import io.resys.thena.api.entities.grim.ThenaGrimContainers.GrimMissionContainer;
import io.resys.thena.api.entities.grim.ThenaGrimObject.GrimDocType;
import io.resys.thena.api.envelope.ImmutableQueryEnvelope;
import io.resys.thena.api.envelope.ImmutableQueryEnvelopeList;
import io.resys.thena.api.envelope.ImmutableQueryEnvelopePage;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.api.envelope.QueryEnvelopeList;
import io.resys.thena.api.envelope.QueryEnvelopePage;
import io.resys.thena.structures.grim.GrimQueries.InternalMissionQuery;
import io.resys.thena.structures.grim.GrimState;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import io.smallrye.mutiny.tuples.Tuple3;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class GrimMissionQueryImpl implements MissionQuery {
  
  private final Uni<GrimState> state;
  
  private String reporterId, likeTitle, likeDescription;
  private List<String> ids;
  private List<String> status;
  private List<String> priority;
  private List<GrimDocType> docs;
  private GrimArchiveQueryType includeArchived;
  private LocalDate fromCreatedOrUpdated;
  private Boolean overdue;
  
  private String atLeastOneRemarkWithType;
  private Boolean atLeastOneRemarkWithAnyType;
  private String notViewedUsedBy, notViewedUsedFor;
  private String includeViewerUserId, includeViewerUsedFor;
  
  private final List<Tuple3<String, Boolean, List<String>>> assignments = new ArrayList<Tuple3<String, Boolean, List<String>>>();
  private final List<Tuple2<String, String>> links = new ArrayList<Tuple2<String, String>>();
  
  @Override
  public MissionQuery excludeDocs(GrimDocType ...docs) {
    this.docs = Arrays.asList(docs);
    return this;
  }
  @Override
  public MissionQuery atLeastOneRemarkWithType(String atLeastOneRemarkWithType) {
    this.atLeastOneRemarkWithType = atLeastOneRemarkWithType;
    return this;
  }
  @Override
  public MissionQuery atLeastOneRemarkWithAnyType() {
    this.atLeastOneRemarkWithAnyType = Boolean.TRUE;
    return this;
  }
  @Override
  public MissionQuery notViewed(String usedBy, String usedFor) {
    this.notViewedUsedBy = RepoAssert.notEmpty(usedBy, () -> "usedBy can't be empty!"); 
    this.notViewedUsedFor = RepoAssert.notEmpty(usedFor, () -> "usedFor can't be empty!"); 
    return this;
  }
  @Override
  public MissionQuery includeViewer(String usedBy, String usedFor) {
    this.includeViewerUserId = RepoAssert.notEmpty(usedBy, () -> "usedBy can't be empty!"); 
    this.includeViewerUsedFor = RepoAssert.notEmpty(usedFor, () -> "usedFor can't be empty!"); 
    return this;
  }
  @Override
  public MissionQuery notViewed(String usedFor) {
    this.notViewedUsedFor = RepoAssert.notEmpty(usedFor, () -> "usedFor can't be empty!"); 
    return this;
  }
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
  
  private InternalMissionQuery startQuery(GrimState state) {
    final var query = state.query().missions();
    if(this.ids != null) {
      query.missionId(this.ids.toArray(new String[] {}));
    }
    if(this.status != null) {
      query.status(this.status.toArray(new String[] {}));
    }
    if(this.priority != null) {
      query.priority(this.priority.toArray(new String[] {}));
    }
    if(docs != null && !docs.isEmpty()) {
      query.excludeDocs(docs.toArray(new GrimDocType[] {}));
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
        .atLeastOneRemarkWithType(atLeastOneRemarkWithType)
        .atLeastOneRemarkWithAnyType(atLeastOneRemarkWithAnyType)
        .notViewed(notViewedUsedBy, notViewedUsedFor)
        .includeViewer(includeViewerUserId, includeViewerUsedFor);
  }
  
  @Override
  public Uni<QueryEnvelope<GrimMissionContainer>> get(String missionIdOrExtId) {
    return this.state
        .onItem().transformToUni(state -> {  
          return startQuery(state).getById(missionIdOrExtId)
            .onItem().transform((items) -> ImmutableQueryEnvelope.<GrimMissionContainer>builder()
                .repo(state.getDataSource().getTenant())
                .status(QueryEnvelopeStatus.OK)
                .objects(items)
                .build());
        });
  }
  
  @Override
  public Uni<QueryEnvelopeList<GrimMissionContainer>> findAll() {
    return this.state
      .onItem().transformToUni(state -> {
        return startQuery(state).findAll().collect().asList()
          .onItem().transform(items -> ImmutableQueryEnvelopeList.<GrimMissionContainer>builder()
              .repo(state.getDataSource().getTenant())
              .status(QueryEnvelopeStatus.OK)
              .objects(items)
              .build());
      })
  ;      
  }

  @Override
  public Uni<QueryEnvelopePage<GrimMissionContainer>> paginate(PageQuery<MissionOrderByType> pageQuery) {
    return this.state.onItem().transformToUni(nextState -> {
    
      // count total results
      final var countQuery = startQuery(nextState).count();
      
      // get the id-s and query missions to them
      final var missionQuery = startQuery(nextState)
          .findAllIdentifiers(pageQuery.getSort().getOrders(), pageQuery.getOffset(), pageQuery.getPageSize())
          .onItem().transformToUni(identifiers -> findMissionsByIdentifiers(nextState, identifiers));
      
      return Uni.combine().all().unis(
          countQuery, missionQuery
      ).asTuple().map(tuple -> {
        
        final var totalObjectsOnPages = tuple.getItem1();
        final var totalPages = totalObjectsOnPages == 0 ? 1 : (int) Math.ceil((double) totalObjectsOnPages / (double) pageQuery.getPageSize());
        final int currentPageNumber = pageQuery.getOffset() == 0 ? 0 :  (int) Math.ceil((double) pageQuery.getOffset() / (double) pageQuery.getPageSize());
        
        final QueryEnvelopePage<GrimMissionContainer> result = ImmutableQueryEnvelopePage.<GrimMissionContainer>builder()
            .repo(nextState.getDataSource().getTenant())
            .status(QueryEnvelopeStatus.OK)
            .currentPageObjects(tuple.getItem2())
            .totalObjectsOnPages(totalObjectsOnPages)
            
            .currentPageNumber(currentPageNumber)
            .totalPages(totalPages)
            .build();
        
        return result;
      });

    });
  }
  
  
  private Uni<List<GrimMissionContainer>> findMissionsByIdentifiers(GrimState nextState, List<String> missionIdentifiers) {
    
    final var query = nextState.query().missions();
    if(docs != null && !docs.isEmpty()) {
      query.excludeDocs(docs.toArray(new GrimDocType[] {}));
    }
    return query
        .missionId(missionIdentifiers.toArray(new String[] {}))
        .findAll().collect().asList();
  } 
}
