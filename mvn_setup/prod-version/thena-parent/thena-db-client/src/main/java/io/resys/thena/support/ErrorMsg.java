package io.resys.thena.support;

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

import io.vertx.core.json.JsonObject;

public interface ErrorMsg {

  public static Builder builder() {
    return new Builder();
  }
  
  public static class Builder {
    private JsonObject props;
    private String code;
    private Exception e;
    private String message;
    
    public Builder withProps(JsonObject props) { this.props = props; return this; }
    public Builder withCode(String code) { this.code = code; return this; }
    public Builder withMessage(String message) { this.message = message; return this; }
    public Builder withException(Exception e) { this.e = e; return this; }

    public String toString() {
      RepoAssert.notEmpty(code, () -> "code must be defined!");
      final var result = new StringBuilder();
      result.append("code/").append(code).append("/").append(message);
      if(props != null) {
        result
        .append("/props/").append(props.encode());
      }
      
      if(e != null) {
        result
        .append("/exception/").append(e.getClass().getSimpleName())
        .append("/").append(e.getMessage());
      }
      
      return result.toString();
    }
  }
}
