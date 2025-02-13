package io.resys.hdes.executor.spi.visitors;

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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.function.Function;

import io.resys.hdes.executor.api.Trace;
import io.resys.hdes.executor.api.Trace.TraceEnd;
import io.resys.hdes.executor.api.Trace.TraceVisitor;
import io.resys.hdes.executor.api.Trace.TraceTree;
import io.resys.hdes.executor.api.TraceBody;
import io.resys.hdes.executor.api.TraceBody.Accepts;
import io.resys.hdes.executor.api.TraceBody.Maped;
import io.resys.hdes.executor.api.TraceBody.MapedIterator;
import io.resys.hdes.executor.api.TraceBody.Matched;
import io.resys.hdes.executor.api.TraceBody.MatchedCondition;
import io.resys.hdes.executor.api.TraceBody.Nested;
import io.resys.hdes.executor.api.TraceBody.Returns;

public class DebugHdesTraceVisitor implements TraceVisitor<String, String> {

  private final Function<Serializable, String> writer;
  
  public DebugHdesTraceVisitor(Function<Serializable, String> writer) {
    this.writer = writer;
  }
  
  private String write(Serializable value) {
    String result = writer.apply(value);

    StringBuilder builder = new StringBuilder();
    for(String line : result.split("\n")) {
      builder.append("  ").append(line).append("\r\n");  
    }
    
    return builder.toString();
  }
  
  @Override
  public String visitBody(TraceEnd end) {
    TraceTree tree = ImmutableHdesTraceVisitorTree.builder().build(end);
    return visitBody(end, tree);
  }
  
  public String visitBody(TraceEnd end, TraceTree tree) {
    String parents = end.getParent()
        .map(p -> visitTrace(p, tree))
        .orElseGet(() -> "");
    LocalDateTime date = LocalDateTime.ofInstant(Instant.ofEpochMilli(end.getTime()), ZoneId.systemDefault());
    return new StringBuilder()
        .append(parents)
        .append("\r\n")
        .append("Trace end for: ").append(end.getId()).append(", finished at: ").append(date).append("\r\n")
        .append(visitReturns(end.getBody(), tree))
        .toString();
  }

  @Override
  public String visitTrace(Trace trace, TraceTree tree) {
    String parents = trace.getParent()
        .map(p -> visitTrace(p, tree))
        .orElseGet(() -> "");
    
    LocalDateTime date = LocalDateTime.ofInstant(Instant.ofEpochMilli(trace.getTime()), ZoneId.systemDefault());
    
    return new StringBuilder()
        .append(parents)
        .append("\r\n")
        .append("Trace for: ").append(trace.getId()).append(", finished at: ").append(date).append("\r\n")
        .append(visitTraceBody(trace.getBody(), tree))
        .toString();
  }

  @Override
  public String visitTraceBody(TraceBody traceBody, TraceTree tree) {
    if(traceBody instanceof Accepts) {
      return visitAccepts((Accepts) traceBody, tree);
    } else if(traceBody instanceof Returns) {
      return visitReturns((Returns) traceBody, tree);
    } else if(traceBody instanceof Nested) {
      return visitCalls((Nested) traceBody, tree);
    } else if(traceBody instanceof Matched) {
      return visitHitPolicy((Matched) traceBody, tree);
    } else if(traceBody instanceof Maped) {
      return visitIteration((Maped) traceBody, tree);
    } else if(traceBody instanceof MapedIterator) {
      return visitIterator((MapedIterator) traceBody, tree);
    }
    throw new IllegalArgumentException("Unknown body: " + traceBody);
  }

  
  @Override
  public String visitAccepts(Accepts accepts, TraceTree tree) {
    return new StringBuilder()
        .append("accepts: ").append(accepts.getClass().getSimpleName()).append("\r\n")
        .append(write(accepts))
        .toString();
  }

  @Override
  public String visitReturns(Returns returns, TraceTree tree) {
    return new StringBuilder()
        .append("returns: ").append(returns.getClass().getSimpleName()).append("\r\n")
        .append(write(returns))
        .toString();
  }

  @Override
  public String visitIteration(Maped iteration, TraceTree tree) {
    
    final var matches = new StringBuilder();

    int index = 0;
    for(var nested : iteration.getValues()) {
      final var next = tree.next(nested);
      matches.append(index++).append(": ");
      
      for(String line : visitBody(nested, next).split("\r\n")) {
        matches.append("  ").append(line).append("\r\n");  
      }
      matches.append("\r\n");  
    }
    return new StringBuilder()
        .append("Maps ").append(iteration.getValues().size()).append(" elements:").append("\r\n")
        .append(matches.toString())
        .toString();
  }

  @Override
  public String visitHitPolicy(Matched hitpolicy, TraceTree tree) {
    final var matches = new StringBuilder();

    for(var when : hitpolicy.getMatches()) {
      matches.append(visitHitPolicyMatch(when, tree));
    }
    
    return new StringBuilder()
        .append("Matched found: ").append(hitpolicy.getMatches().size()).append("\r\n")
        .append(matches.toString())
        .append(hitpolicy.getReturns().map(r -> write(r)).orElseGet(() -> ""))
        .toString();
  }

  @Override
  public String visitHitPolicyMatch(MatchedCondition when, TraceTree tree) {
    return new StringBuilder()
        .append(when.getId()).append(" - matched condition: ").append(when.getSrc()).append("\r\n")
        .toString();
  }
  
  @Override
  public String visitIterator(MapedIterator iterator, TraceTree tree) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String visitCalls(Nested calls, TraceTree tree) {
    
    final var matches = new StringBuilder();

    for(var trace : calls.getValues()) {
      matches.append("Calls: ").append(trace.getId()).append("\r\n");
      for(String line : visitTrace(trace, tree.next(trace)).split("\r\n")) {
        matches.append("  ").append(line).append("\r\n");  
      }
    }
    if(matches.length() == 0) {
      return new StringBuilder()
          .append("Makes no calls").append("\r\n")
          .toString();      
    }
    
    return new StringBuilder()
        .append(matches.toString())
        .toString();
  }
}
