package io.resys.thena.processor.model;

/*-
 * #%L
 * thena-sql-client
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

import javax.lang.model.element.Element;

import com.squareup.javapoet.TypeName;

import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TableModel {
  String interfaceName;
  String packageName;
  String implClassName;
  
  String tableName;
  int order;
  String ddlSql;
  String constraintsSql;
  String dropSql;
  
  @Builder.Default
  List<SqlMethod> sqlMethods = List.of();
  
  public enum SqlPropsType {
    SQL,           // No parameters, returns Sql
    SQL_TUPLE,     // Single set of parameters, returns SqlTuple
    SQL_TUPLE_LIST // Batch operation, returns SqlTupleList
  }
  
  public enum SqlMethodType {

    SELECT,
    INSERT,
    UPDATE,
    DELETE,
    
    SELECT_ALL,
    INSERT_ALL,
    UPDATE_ALL,
    DELETE_ALL,
    
    CREATE_TABLE,
    CREATE_CONSTRAINTS,
    DROP_TABLE
  }
  
  @Value
  @Builder
  public static class MethodParameter {
    String name;
    TypeName type;
    int position;
  }
  
  @Value
  @Builder
  public static class SqlMethod {
    SqlMethodType type;
    
    String methodName;
    String sqlTemplate;
    String resolvedSql;
    @Nullable
    String mapperClassName;
    
    @Builder.Default
    List<MethodParameter> parameters = List.of();
    @Nullable
    TypeName returnType;
    @Nullable
    TypeName wrapperType;
    
    SqlPropsType propsType;
    
    @Builder.Default
    List<String> tableNames = List.of();  // NEW: extracted from {table_name} placeholders
    
    @Builder.Default
    boolean optional = true;  // For @Find queries
  }
  
  
  @Value
  @Builder
  public static class RegistryModel {
    Element element;
    String name;                    // "Grim"
    String packageName;             // "io.resys.thena.grim.spi.sql"
    String tableClassName;
    String registryClassName;
    String transactionContainerClassName;
    String transactionSaveClassName;
    String worldName;
    @Builder.Default
    List<String> nonTenantTables = List.of();  // ["process", "process_id_seq"]
  }
}
