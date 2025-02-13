package io.resys.hdes.executor.spi.beans;

import java.util.ArrayList;
import java.util.List;

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

import java.util.Optional;

import io.resys.hdes.ast.spi.util.Assertions;
import io.resys.hdes.executor.api.ImmutableSuspends;
import io.resys.hdes.executor.api.Trace;
import io.resys.hdes.executor.api.TraceBody;
import io.resys.hdes.executor.api.TraceBody.Await;
import io.resys.hdes.executor.api.TraceBody.PromiseDataId;
import io.resys.hdes.executor.api.TraceBody.Returns;
import io.resys.hdes.executor.api.TraceBody.Suspends;

public class ImmutableTrace implements Trace {
  private static final long serialVersionUID = 6238330531509939587L;

  private final String id;
  private final long time;
  private final Optional<Trace> parent;
  private final TraceBody body;
  
  protected ImmutableTrace(String id, long time, Optional<Trace> parent, TraceBody body) {
    super();
    this.id = id;
    this.time = time;
    this.parent = parent;
    this.body = body;
  }
  public String getId() {
    return id;
  }
  public long getTime() {
    return time;
  }
  public Optional<Trace> getParent() {
    return parent;
  }
  public TraceBody getBody() {
    return body;
  }

  public long getTimeFromStart() {
    Optional<Trace> current = Optional.of(this);
    long start;
    do {
      start = current.get().getTime();
      current = current.get().getParent();
    } while(current.isPresent());
    return time - start;
  }
  
  public static class ImmutableHdesTraceEnd extends ImmutableTrace implements TraceEnd {
    private static final long serialVersionUID = 6238330531509939587L;
    private final Suspends suspends;
    private ImmutableHdesTraceEnd(String id, long time, Optional<Trace> parent, Returns body) {
      super(id, time, parent, body);
      this.suspends = null;
    }
    private ImmutableHdesTraceEnd(String id, long time, Optional<Trace> parent, Suspends suspends) {
      super(id, time, parent, null);
      this.suspends = suspends;
    }
    public Returns getBody() {
      return (Returns) super.body;
    }
    @Override
    public Suspends getSuspends() {
      return suspends;
    }
  }
  
  public static class ImmutableHdesTraceStep extends ImmutableTrace implements TraceStep {
    private static final long serialVersionUID = 6238330531509939587L;

    private ImmutableHdesTraceStep(String id, long time, Optional<Trace> parent, TraceBody body) {
      super(id, time, parent, body);
    }
  }

  public static class ImmutableHdesTracePromise extends ImmutableTrace implements TracePromise {
    private static final long serialVersionUID = 6238330531509939587L;

    private ImmutableHdesTracePromise(String id, long time, Optional<Trace> parent, PromiseDataId body) {
      super(id, time, parent, body);
    }
    
    public PromiseDataId getBody() {
      return (PromiseDataId) super.body;
    }

    @Override
    public Suspends getSuspends() {
      return null;
    }
  }

  
  public static Builder builder() {
    return new Builder();
  }
  public static class Builder {
    private String id;
    private Long time;
    private Optional<Trace> parent;
    private TraceBody body;
    private List<Await> suspends;
    
    public Builder id(String id) {
      this.id = id;
      return this;
    }
    public Builder time(long time) {
      this.time = time;
      return this;
    }
    public Builder parent(Optional<Trace> parent) {
      this.parent = parent;
      return this;
    }
    public Builder parent(Trace parent) {
      this.parent = Optional.ofNullable(parent);
      return this;
    }
    public Builder body(TraceBody body) {
      this.body = body;
      return this;
    }
    public Builder suspend(Await at) {
      if(suspends == null) {
        suspends = new ArrayList<>();
      }
      suspends.add(at);
      return this;
    }
    
    public Trace build() {
      Assertions.notNull(id, () -> "id can't be null!");
      Assertions.notNull(body, () -> "body can't be null!");
      
      final long time = this.time == null ? System.currentTimeMillis() : this.time;
      final Optional<Trace> parent = this.parent == null ? Optional.empty() : this.parent;
      return new ImmutableTrace(id, time, parent, body);
    }
    
    public TraceEnd end() {
      Assertions.notNull(id, () -> "id can't be null!");
      Assertions.notNull(body, () -> "body can't be null!");
      Assertions.notNull(parent, () -> "parent can't be null!");
      Assertions.isTrue(body instanceof Returns, () -> "body is not HdesTraceReturns!");
      
      final long time = this.time == null ? System.currentTimeMillis() : this.time;
      return new ImmutableHdesTraceEnd(id, time, parent, (Returns) body);
    }
    
    public TraceEnd suspend() {
      Assertions.isTrue(body == null, () -> "body should be null!");
      Assertions.notNull(parent, () -> "parent can't be null!");
      Assertions.notNull(suspends, () -> "suspends should have at least one value!");
      Assertions.isTrue(!suspends.isEmpty(), () -> "suspends should have at least one value!");
      
      final long time = this.time == null ? System.currentTimeMillis() : this.time;
      return new ImmutableHdesTraceEnd(id, time, parent, ImmutableSuspends.builder().addAllValues(suspends).build());
    }    
    
    public TraceStep step() {
      final long time = this.time == null ? System.currentTimeMillis() : this.time;
      final Optional<Trace> parent = this.parent == null ? Optional.empty() : this.parent;
      return new ImmutableHdesTraceStep(id, time, parent, body);
    }
    
    public TracePromise promise() {
      Assertions.notNull(body, () -> "body can't be null!");
      Assertions.notNull(parent, () -> "parent can't be null!");
      final long time = this.time == null ? System.currentTimeMillis() : this.time;
      final Optional<Trace> parent = this.parent;
      return new ImmutableHdesTracePromise(id, time, parent, (PromiseDataId) body);
    }
  }
}
