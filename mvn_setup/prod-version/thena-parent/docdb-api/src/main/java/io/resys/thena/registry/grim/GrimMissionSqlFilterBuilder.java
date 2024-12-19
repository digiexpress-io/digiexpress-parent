package io.resys.thena.registry.grim;

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

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.function.Supplier;

import io.resys.thena.api.actions.GrimQueryActions.GrimArchiveQueryType;
import io.resys.thena.api.registry.grim.GrimMissionFilter;
import io.resys.thena.datasource.ImmutableSqlTuple;
import io.resys.thena.datasource.TenantTableNames;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.resys.thena.storesql.support.SqlStatement;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GrimMissionSqlFilterBuilder {
  private final TenantTableNames options;
  
  
  public SqlTuple where(GrimMissionFilter filter) {
    int index = 1;
    final var params = new ArrayList<Object>();
    final var builder = new SqlStatement();
    final Supplier<Void> and = () -> {
      if(!builder.isEmpty()) {
        builder.ln().append(" AND ");
      }
      return null;
    };
    
    
    // by id
    if(filter.getMissionIds().isPresent()) {
      builder.append(" mission.id = ANY($").append(index++).append(")").ln();
      params.add(filter.getMissionIds().get().toArray());
    }
    
    // archive filter
    if(GrimArchiveQueryType.ONLY_ARCHIVED.equals(filter.getArchived())) {
      and.get();
      builder.append(" mission.archived_at is NOT NULL").ln();      
    } else if(GrimArchiveQueryType.ONLY_IN_FORCE.equals(filter.getArchived())) {
      and.get();
      builder.append(" mission.archived_at is NULL").ln();
    }
    
    // viewer filter
    if(filter.getNotViewedByUser() != null || filter.getNotViewedByUsage() != null) {
      and.get();
      builder
      .append("  NOT EXISTS(").ln()
      .append("    SELECT id FROM ").append(options.getGrimCommitViewer()).append(" AS viewer_filter").ln()
      .append("    WHERE viewer_filter.mission_id = mission.id").ln();

      if(filter.getNotViewedByUsage() != null) {
        builder.append("      AND LOWER(used_for) = $").append(index++).ln();
        params.add(filter.getNotViewedByUsage().toLowerCase());
      }
      if(filter.getNotViewedByUser() != null) {
        builder.append("      AND LOWER(used_by) = $").append(index++).ln();
        params.add(filter.getNotViewedByUser().toLowerCase());
      }
      builder.append("  )");
    }
    
    // external comment filter
    if(filter.getAtLeastOneRemarkWithType() != null) {
      and.get();
      builder
      .append("  EXISTS(").ln()
      .append("    SELECT id FROM ").append(options.getGrimRemark()).append(" AS remark_type_filter").ln()
      .append("    WHERE remark_type_filter.mission_id = mission.id").ln()
      .append("    AND remark_type_filter.remark_type = $").append(index++).ln()
      .append("  )");
      params.add(filter.getAtLeastOneRemarkWithType().toLowerCase());
    }
    
    
    // created/updated
    if(filter.getFromCreatedOrUpdated() != null) {
      and.get();
      builder
      .append(" (")
      .append("  exists(")
      .append("    select created_filter.commit_id from ").append(options.getGrimCommit()).append(" as created_filter").ln()
      .append("    where created_filter.mission_id = mission.created_commit_id").ln()
      .append("    and created_filter.created_at >= $").append(index++).append(")").ln()
      
      .append("  OR")

      .append("  exists(")
      .append("    select updated_filter.commit_id from ").append(options.getGrimCommit()).append(" as updated_filter").ln()
      .append("    where updated_filter.mission_id = mission.updated_tree_commit_id").ln()
      .append("    and updated_filter.created_at >= $").append(index++).append(")").ln()
      .append("  )");
      
      params.add(OffsetDateTime.of(filter.getFromCreatedOrUpdated().atStartOfDay(), ZoneOffset.UTC));
      params.add(OffsetDateTime.of(filter.getFromCreatedOrUpdated().atStartOfDay(), ZoneOffset.UTC));
    }

    // assignments
    if(!filter.getAssignments().isEmpty()) {
      for(final var assignment: filter.getAssignments()) {
        final var operator = assignment.isExact() ? "=" : "LIKE";
        
        and.get();
        builder
        .append("  EXISTS(").ln()
        .append("    SELECT id FROM ").append(options.getGrimAssignment()).append(" AS assignment_filter").ln()
        .append("    WHERE assignment_filter.mission_id = mission.id").ln()
        .append("      AND LOWER(assignee) ").append(operator).append(" ANY($").append(index++).append(")")
        .append("      AND assignment_type = $").append(index++).ln()
        .append("  )");

        params.add(assignment.getAssignmentValue().stream()
            .map(e -> assignment.isExact() ? e : "%" + e + "%")
            .map(String::toLowerCase)
            .toArray());
        params.add(assignment.getAssignmentType());
      }
    }
    
    // link
    if(!filter.getLinks().isEmpty()) {
      and.get();
      builder
      .append("  exists(").ln()
      .append("    select id from ").append(options.getGrimMissionLink()).append(" as link_filter").ln()
      .append("    where link_filter.mission_id = mission.id").ln()
      .append("    and(").ln();
      
      var appendOr = false;
      for(final var link: filter.getLinks()) {       
        builder
          .append(appendOr ? " OR" : "").ln()
          .append(" (link_type = $").append(index++).ln()
          .append(" and external_id = $").append(index++).append(")").ln();
        
        params.add(link.getLinkType());
        params.add(link.getLinkValue());
        appendOr = true;
      }
      builder.append("  ))").ln();
    }
    
    
    // reporter
    if(filter.getLikeReporterId() != null) {
      and.get();
      builder.append(" LOWER(mission.reporter_id) = $").append(index++).ln();
      params.add(filter.getLikeReporterId().toLowerCase());
    }
    
    // title
    if(filter.getLikeTitle() != null) {
      and.get();
      builder.append(" LOWER(mission.mission_title) like $").append(index++).ln();
      params.add("%" + filter.getLikeTitle().toLowerCase() + "%");
    }
    
    // description
    if(filter.getLikeDescription() != null) {
      and.get();
      builder.append(" LOWER(mission.mission_description) like $").append(index++).ln();
      params.add("%" + filter.getLikeDescription().toLowerCase() + "%");
    }
    
    
    // status
    if(!filter.getStatus().isEmpty()) {
      and.get();
      builder.append(" LOWER(mission.mission_status) = ANY($").append(index++).append(")").ln();
      params.add(filter.getStatus().stream().map(String::toLowerCase).toArray());
    }
    // priority
    if(!filter.getPriority().isEmpty()) {
      and.get();
      builder.append(" LOWER(mission.mission_priority) = ANY($").append(index++).append(")").ln();
      params.add(filter.getPriority().stream().map(String::toLowerCase).toArray());
    }
    
    // overdue
    if(Boolean.FALSE.equals(filter.getOverdue())) {
      and.get();
      builder.append(" mission.mission_due_date < CURRENT_DATE").ln();
    }
    
    final var result = builder.toString();
    
    return ImmutableSqlTuple.builder()
        .value((result.isBlank() ? "" : " WHERE ") +builder.toString())
        .props(Tuple.from(params))
        .build();
  } 
}
