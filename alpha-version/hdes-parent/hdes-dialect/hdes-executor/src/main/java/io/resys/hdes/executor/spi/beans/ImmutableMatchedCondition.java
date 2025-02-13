package io.resys.hdes.executor.spi.beans;

/*-
 * #%L
 * hdes-executor
 * %%
 * Copyright (C) 2020 Copyright 2020 ReSys OÜ
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

import io.resys.hdes.ast.spi.util.Assertions;
import io.resys.hdes.executor.api.TraceBody.MatchedCondition;

public class ImmutableMatchedCondition implements MatchedCondition {
  private static final long serialVersionUID = -6531173966938568273L;
  
  private final String id;
  private final String src;
  
  private ImmutableMatchedCondition(String id, String src) {
    super();
    this.id = id;
    this.src = src;
  }
  
  public String getId() {
    return id;
  }
  public String getSrc() {
    return src;
  }

  public static Builder builder() {
    return new Builder();
  }
  public static class Builder {
    private String id;
    private String src;
    public Builder id(int id) {
      this.id = String.valueOf(id);
      return this;
    }
    public Builder id(String id) {
      this.id = id;
      return this;
    }
    public Builder src(String src) {
      this.src = src;
      return this;
    }
    public ImmutableMatchedCondition build() {
      Assertions.notNull(id, () -> "id can't be null!");
      Assertions.notNull(src, () -> "src can't be null!");
      
      return new ImmutableMatchedCondition(id, src);
    }
  }
}
