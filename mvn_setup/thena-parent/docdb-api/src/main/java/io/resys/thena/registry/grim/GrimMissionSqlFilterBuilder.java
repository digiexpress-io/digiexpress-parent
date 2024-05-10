package io.resys.thena.registry.grim;

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
    if(GrimArchiveQueryType.ONLY_ARCHIVED.equals(filter.getArchived())) {
      and.get();
      builder.append(" mission.archived_at is NOT NULL").ln();      
    } else if(GrimArchiveQueryType.ONLY_IN_FORCE.equals(filter.getArchived())) {
      and.get();
      builder.append(" mission.archived_at is NULL").ln();
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

    // meta
    if(filter.getLikeTitle() != null || filter.getLikeDescription() != null) {
      and.get();
      builder
      .append("  exists(")
      .append("    select id from ").append(options.getGrimMissionData()).append(" as meta_filter").ln()
      .append("    where meta_filter.mission_id = mission.id").ln()
      .append("    and meta_filter.objective_id IS NULL").ln()
      .append("    and meta_filter.goal_id IS NULL").ln()
      .append("    and meta_filter.remark_id IS NULL").ln();
      
      if(filter.getLikeTitle() != null) {
        builder
        .append("    and meta_filter.title like $").append(index++).ln();
        params.add("%" + filter.getLikeTitle() + "%");
      }
      
      if(filter.getLikeDescription() != null) {
        builder
        .append("    and meta_filter.title like $").append(index++).ln();
        params.add("%" + filter.getLikeDescription() + "%");
      }
      
      builder
      .append("  )");
    }
    
    // assignments
    if(!filter.getAssignments().isEmpty()) {
      and.get();
      builder
      .append("  exists(")
      .append("    select id from ").append(options.getGrimAssignment()).append(" as assignment_filter")
      .append("    where assignment_filter.mission_id = mission.id")
      .append("    and(");
      
      var appendOr = false;
      for(final var assignment: filter.getAssignments()) {       
        builder
          .append(appendOr ? " OR" : "")
          .append(" (assignee = $").append(index++).ln()
          .append(" and assignment_type = $").append(index++).append(")").ln();
        
        params.add(assignment.getAssignmentValue());
        params.add(assignment.getAssignmentType());
        appendOr = true;
      }
      builder.append("  ))");
    }
    
    // link
    if(!filter.getLinks().isEmpty()) {
      and.get();
      builder
      .append("  exists(")
      .append("    select id from ").append(options.getGrimMissionLink()).append(" as link_filter")
      .append("    where link_filter.mission_id = mission.id")
      .append("    and(");
      
      var appendOr = false;
      for(final var link: filter.getLinks()) {       
        builder
          .append(appendOr ? " OR" : "")
          .append(" (link_type = $").append(index++)
          .append(" and external_id = $").append(index++).append(")").ln();
        
        params.add(link.getLinkType());
        params.add(link.getLinkValue());
        appendOr = true;
      }
      builder.append("  ))");
    }
    
    final var result = builder.toString();
    
    return ImmutableSqlTuple.builder()
        .value((result.isBlank() ? "" : " WHERE ") +builder.toString())
        .props(Tuple.from(params))
        .build();
  }
  
}
