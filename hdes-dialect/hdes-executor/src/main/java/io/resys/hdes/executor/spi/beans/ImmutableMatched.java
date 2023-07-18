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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.resys.hdes.executor.api.TraceBody.Matched;

public class ImmutableMatched implements Matched {
  private static final long serialVersionUID = -6531173966938568273L;
  
  private final List<MatchedCondition> values;
  private final Optional<Returns> returns;
  
  private ImmutableMatched(List<MatchedCondition> values, Optional<Returns> returns) {
    super();
    this.values = values;
    this.returns = returns;
  }
  
  @Override
  public List<MatchedCondition> getMatches() {
    return values;
  }

  @Override
  public Optional<Returns> getReturns() {
    return returns;
  }
  public static Builder builder() {
    return new Builder();
  }
  
  public static class Builder {
    private final List<MatchedCondition> values = new ArrayList<>();
    private Returns returns;
    public Builder add(int id, String src) {
      values.add(ImmutableMatchedCondition.builder().id(id).src(src).build());
      return this;
    }
    public Builder returns(Returns returns) {
      this.returns = returns;
      return this;
    }
    public ImmutableMatched build() {
      return new ImmutableMatched(values, Optional.ofNullable(returns));
    }
  }
}
