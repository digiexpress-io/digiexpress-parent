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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import io.resys.thena.api.actions.GrimQueryActions.MissionOrderByType;
import io.resys.thena.api.entities.PageQuery.PageSortDirection;
import io.resys.thena.api.entities.PageQuery.PageSortingOrder;
import io.resys.thena.datasource.ImmutableSql;
import io.resys.thena.datasource.TenantTableNames;
import io.resys.thena.datasource.ThenaSqlClient.Sql;
import io.resys.thena.storesql.support.SqlStatement;
import lombok.Data;
import lombok.RequiredArgsConstructor;


public class GrimMissionSqlSortingBuilder {
  private final TenantTableNames options;
  
  private final List<Object> params;
  private final SqlStatement orderBy = new SqlStatement();
  private final SqlStatement orderByJoins = new SqlStatement();
  private final long offset;
  private final long limit;
  private int index;
  private int joinIndex = 1;
  
  public GrimMissionSqlSortingBuilder(TenantTableNames options, long offset, long limit) {
    super();
    this.options = options;
    this.params = new ArrayList<Object>();
    this.index = 1;
    this.offset = offset;
    this.limit = limit;
  }
  
  public GrimMissionSqlSortingBuilder(TenantTableNames options, List<Object> params, long offset, long limit) {
    super();
    this.options = options;
    this.params = params;
    this.index = params.size() + 1;
    this.offset = offset;
    this.limit = limit;
  }
  
  public OrderBySql orderBy(List<PageSortingOrder<MissionOrderByType>> orderBy) {
    generate(orderBy);
    
    final var orderByClause = ImmutableSql.builder()
        .value(this.orderBy.toString())
        .build();
    final var orderByJoins = ImmutableSql.builder()
        .value(this.orderByJoins.toString())
        .build();
    
    return new OrderBySql(orderByJoins, orderByClause);
  }

  private String generateAssigneeJoins(PageSortingOrder<MissionOrderByType> order) {
    final var alias = "assignment_" + joinIndex;
    
    orderByJoins
      .append(" LEFT JOIN (").ln()
      
      .append("  SELECT ").ln()
      .append("    array_agg(assignee ORDER BY assignee) as assignee_array, mission_id").ln()
      .append("  FROM ").append(options.getGrimAssignment()).ln()
      .append("  WHERE ").ln()
      .append("   assignment_type = $").append(index++).ln()
      .append("   AND objective_id is null").ln()
      .append("   AND goal_id is null").ln()
      .append("   AND remark_id is null").ln()
      .append("  GROUP BY mission_id").ln()
      
      .append(") AS ").append(alias).ln()
      .append(" ON(").append(alias).append(".mission_id = mission.id)").ln()
      ;
    
    params.add(order.getPropertyType());
    joinIndex++;
    return alias + ".assignee_array";
  }
  
  private void generate(List<PageSortingOrder<MissionOrderByType>> orderBy) {

    if(orderBy.isEmpty()) {
      return;
    }
    
    final Supplier<Void> and = () -> {
      if(!this.orderBy.isEmpty()) {
        this.orderBy.append(", ").ln();
      }
      return null;
    };
    
    for(final var order : orderBy) {
      and.get();
      switch (order.getProperty()) {
        case MISSION_ID: this.orderBy.append("  mission.id"); break;
        case MISSION_COMPLETED_AT: this.orderBy.append("  mission.mission_completed_at"); break;
        case MISSION_REF_ID: this.orderBy.append("  mission.mission_ref"); break;
        case MISSION_PRIORITY: this.orderBy.append("  mission.mission_priority"); break;        
        
        case MISSION_ARCHIVED_AT: this.orderBy.append("  mission.archived_at"); break;
        case MISSION_CREATED_AT: this.orderBy.append("  created_commit.created_at"); break;
        case MISSION_DESC: this.orderBy.append("  mission.mission_description"); break;
        case MISSION_DUE_DATE: this.orderBy.append("  mission.mission_due_date"); break;


        case MISSION_START_DATE: this.orderBy.append("  mission.mission_start_date"); break;
        case MISSION_STATUS: this.orderBy.append("  mission.mission_status"); break;
        case MISSION_REPORTER_ID: this.orderBy.append("  mission.reporter_id"); break;
        case MISSION_TITLE: this.orderBy.append("  mission.mission_title"); break;
        case MISSION_TREE_UPDATED_AT: this.orderBy.append("  tree_updated_at"); break;
        
        // special case with join
        case MISSION_ASSIGNMENT_VALUE: {
          this.orderBy.append("  ").append(generateAssigneeJoins(order)); 
          break;
        }
        
        default: throw new GrimOrderByException("Order by property: " + order.getProperty() + " is not supported!");
      }
            
      this.orderBy
        .append(" ").append(order.getDirection() == PageSortDirection.ASC ? "ASC" : "DESC")
        .append(" NULLS LAST");
    }
    
    
    this.orderBy
    .appendAtStart(" ORDER BY ")
    .ln()
    .append(" LIMIT $").append(index++).append(" OFFSET $").append(String.valueOf(index++)).ln();
  
    params.add(limit);
    params.add(offset);
  }
  
  
  @Data @RequiredArgsConstructor
  public static class OrderBySql {
    private final Sql orderByJoins;    
    private final Sql orderByClause;
  }
  
  
  public static class GrimOrderByException extends RuntimeException {
    private static final long serialVersionUID = -7251738425609399151L;
    
    public GrimOrderByException(String msg) {
      super(msg);
    }
  }
}
