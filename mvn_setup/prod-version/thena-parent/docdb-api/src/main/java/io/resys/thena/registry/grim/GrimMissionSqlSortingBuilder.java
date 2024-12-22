package io.resys.thena.registry.grim;

import java.util.List;
import java.util.function.Supplier;

import io.resys.thena.api.actions.GrimQueryActions.MissionOrderByType;
import io.resys.thena.api.entities.PageQuery.PageSortDirection;
import io.resys.thena.api.entities.PageQuery.PageSortingOrder;
import io.resys.thena.datasource.ImmutableSql;
import io.resys.thena.datasource.TenantTableNames;
import io.resys.thena.datasource.ThenaSqlClient.Sql;
import io.resys.thena.storesql.support.SqlStatement;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GrimMissionSqlSortingBuilder {
  private final TenantTableNames options;
  
  
  public Sql orderBy(List<PageSortingOrder<MissionOrderByType>> orderBy) {
    final var builder = new SqlStatement();
    final Supplier<Void> and = () -> {
      if(!builder.isEmpty()) {
        builder.ln().append(", ");
      }
      return null;
    };
    
    for(final var order : orderBy) {
      and.get();
      switch (order.getProperty()) {
        case MISSION_ID: builder.append("mission.id"); break;
        case MISSION_COMPLETED_AT: builder.append("mission.mission_completed_at"); break;
        case MISSION_REF_ID: builder.append("mission.mission_ref"); break;
        case MISSION_PRIORITY: builder.append("mission.mission_priority"); break;        
        
        case MISSION_ARCHIVED_AT: builder.append("mission.archived_at"); break;
        case MISSION_CREATED_AT: builder.append("created_at"); break;
        case MISSION_DESC: builder.append("mission.mission_description"); break;
        case MISSION_DUE_DATE: builder.append("mission.mission_due_date"); break;


        case MISSION_START_DATE: builder.append("mission.mission_start_date"); break;
        case MISSION_STATUS: builder.append("mission.mission_status"); break;
        case MISSION_TITLE: builder.append("mission.mission_title"); break;
        case MISSION_TREE_UPDATED_AT: builder.append("tree_updated_at"); break;
        
        default: throw new GrimOrderByException("Order by property: " + order.getProperty() + " is not supported!");
      }
      
      builder
      .append(" ").append(order.getDirection() == PageSortDirection.ASC ? "ASC" : "DESC")
      .append(" NULLS LAST")
      .ln();
    }
    
    final var result = builder.toString();
    return ImmutableSql.builder()
        .value((result.isBlank() ? "" : " ORDER BY ") + builder.toString())
        .build();
  }
  
  
  public static class GrimOrderByException extends RuntimeException {
    private static final long serialVersionUID = -7251738425609399151L;
    
    public GrimOrderByException(String msg) {
      super(msg);
    }
  }
}
