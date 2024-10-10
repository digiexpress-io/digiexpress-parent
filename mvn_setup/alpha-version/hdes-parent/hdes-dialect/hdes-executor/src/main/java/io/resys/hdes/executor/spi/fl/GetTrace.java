package io.resys.hdes.executor.spi.fl;

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

import java.io.Serializable;
import java.util.Optional;

import io.resys.hdes.executor.api.Trace;
import io.resys.hdes.executor.api.TraceBody.MapedIterator;
import io.resys.hdes.executor.api.TraceBody.Nested;

public class GetTrace {
  private final Trace trace;
  private String id;

  public GetTrace(Trace trace) {
    super();
    this.trace = trace;
  }
  
  public GetTrace step(String id) {
    this.id = id;
    return this;
  }
  
  @SuppressWarnings("unchecked")
  public <T extends Serializable> T body(Class<T> body) {
    Optional<Trace> current = Optional.of(trace);
    do {
      if (id != null && current.get().getId().equals(id)) {
        
        // Found by type
        if(body.isAssignableFrom(current.get().getBody().getClass())) {
          return (T) current.get().getBody();
        }
        
        if(current.get().getBody() instanceof Nested) {
          Nested calls = (Nested) current.get().getBody();
          if(calls.getValues().size() == 1) {
            Trace nested = calls.getValues().get(0);
            
            if(body.isAssignableFrom(nested.getBody().getClass())) {
              return (T) nested.getBody();
            }
          }
          
        } else if(current.get().getBody() instanceof MapedIterator) {
          MapedIterator nested = (MapedIterator) current.get().getBody();
          if(body.isAssignableFrom(nested.getValue().getClass())) {
            return (T) nested.getValue();
          }          
        }
      } else if(current.get().getBody() instanceof Nested) {
        
        Nested nested = (Nested) current.get().getBody();
        for(Trace nestedChild : nested.getValues()) {
          if(body.isAssignableFrom(nestedChild.getBody().getClass())) {
            return (T) nestedChild.getBody();
          }
        }
        
      } else if(body.isAssignableFrom(current.get().getBody().getClass())) {
        return (T) current.get().getBody();
      }
      
      
      current = current.get().getParent();
    } while (current.isPresent());
    
    throw new IllegalArgumentException("Can't find trace: " + id + " with body: " + body.getSimpleName());
  }

  public static GetTrace from(Trace trace) {
    return new GetTrace(trace);
  }
  
}
