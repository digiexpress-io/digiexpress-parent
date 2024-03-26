package io.resys.thena.storesql.support;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2021 - 2023 Copyright 2021 ReSys OÜ
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

public class SqlStatement {

  private final StringBuilder result = new StringBuilder();
  public SqlStatement append(int value) {
    result.append(value);
    return this;
  }  
  public SqlStatement append(String value) {
    result.append(value);
    return this;
  }
  public SqlStatement append(boolean appendIf, String value) {
    if(appendIf) {
      result.append(value);
    }
    return this;
  }
  public SqlStatement ln() {
    result.append("\n");
    return this;
  }
  public String toString() {
    return this.result.toString();
  }
  public String build() {
    return this.result.toString();
  }
  
  public static SqlStatement builder() {
    return new SqlStatement();
  } 
  
}
