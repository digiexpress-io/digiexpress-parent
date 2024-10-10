package io.resys.thena.datasource;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2021 - 2022 Copyright 2021 ReSys OÜ
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

public interface ThenaSqlDataSourceErrorHandler {
  ThenaSqlDataSourceErrorHandler withOptions(TenantTableNames options);
  boolean notFound(Throwable e);
  boolean duplicate(Throwable e);
  boolean isLocked(Throwable e);
  
  void deadEnd(SqlSchemaFailed e);
  void deadEnd(SqlFailed e);
  void deadEnd(SqlTupleFailed e);
  void deadEnd(SqlTupleListFailed e);
  //void deadEnd(String additionalMsg, Throwable e);
  //void deadEnd(String additionalMsg, Throwable e, Object ...args);
  //void deadEnd(String additionalMsg);
  
	public static class SqlExecutionFailed extends RuntimeException {
		private static final long serialVersionUID = -6960481243464191887L;
		public SqlExecutionFailed(String message, Throwable cause) {
			super(message, cause);
		}
	}

	public static class SqlSchemaFailed extends RuntimeException {
		private static final long serialVersionUID = -6960481243464191887L;
		private final String sql;
		public SqlSchemaFailed(String message, String sql, Throwable cause) {
			super(message, cause);
			this.sql = sql;
		}		
		public String getSql() {
			return sql;
		}
	}	
	public static class SqlFailed extends RuntimeException {
		private static final long serialVersionUID = -6960481243464191887L;
		private final ThenaSqlClient.Sql sql;
		public SqlFailed(String message, ThenaSqlClient.Sql sql, Throwable cause) {
			super(message, cause);
			this.sql = sql;
		}		
		public ThenaSqlClient.Sql getSql() {
			return sql;
		}
	}	
	public static class SqlTupleFailed extends RuntimeException {
		private static final long serialVersionUID = -6960481243464191887L;
		private final ThenaSqlClient.SqlTuple sql;
		public SqlTupleFailed(String message, ThenaSqlClient.SqlTuple sql, Throwable cause) {
			super(message, cause);
			this.sql = sql;
		}		
		public ThenaSqlClient.SqlTuple getSql() {
			return sql;
		}
	}	
	public static class SqlTupleListFailed extends RuntimeException {
		private static final long serialVersionUID = -6960481243464191887L;
		private final ThenaSqlClient.SqlTupleList sql;
		public SqlTupleListFailed(String message, ThenaSqlClient.SqlTupleList sql, Throwable cause) {
			super(message, cause);
			this.sql = sql;
		}		
		public ThenaSqlClient.SqlTupleList getSql() {
			return sql;
		}
	}	
}
