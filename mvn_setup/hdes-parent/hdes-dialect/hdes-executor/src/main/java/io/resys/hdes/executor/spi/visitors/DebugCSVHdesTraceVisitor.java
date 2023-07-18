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
import java.util.Map;
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

public class DebugCSVHdesTraceVisitor implements TraceVisitor<String, String> {

  private final Function<Serializable, Map<String, Object>> writer;
  private static final String SEP = ",";
  private static final String RN = "\r\n";
  
  public DebugCSVHdesTraceVisitor(Function<Serializable, Map<String, Object>> writer) {
    this.writer = writer;
  }
  
  private String write(Serializable value, String prefix) {
    Map<String, Object> result = writer.apply(value);

    StringBuilder builder = new StringBuilder();
    for(var line : result.entrySet()) {
      builder.append(prefix).append(SEP).append(SEP).append(line.getKey()).append(",").append(line.getValue()).append(RN);
    }
    return builder.toString();
  }
  
  private String getParentCol(TraceTree tree) {
    int parents = 0;
    TraceTree start = tree;
    while(start.getParent().isPresent()) {
      parents++;
      start = start.getParent().get();
    }
    
    StringBuilder result = new StringBuilder();
    for(int index = 0; index < parents; index++) {
      result.append(SEP);
    }
    return result.toString();
  }
  
  @Override
  public String visitBody(TraceEnd end) {
    TraceTree tree = ImmutableHdesTraceVisitorTree.builder().build(end);
    return "Trace for: " + SEP + end.getId() + RN + visitBody(end, tree);
  }
  
  public String visitBody(TraceEnd end, TraceTree tree) {
    String parents = end.getParent()
        .map(p -> visitTrace(p, tree))
        .orElseGet(() -> "");
    
    LocalDateTime date = LocalDateTime.ofInstant(Instant.ofEpochMilli(end.getTime()), ZoneId.systemDefault());
    return new StringBuilder()
        .append(parents).append(RN)
        .append(getParentCol(tree))
        .append(date).append(SEP).append(end.getId()).append(" - end as").append(RN)
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
        .append(parents).append(RN)
        .append(date).append(SEP).append(trace.getId()).append(RN)
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
    String prefix = getParentCol(tree);
    return new StringBuilder()
        .append(prefix)
        .append("accepts: ").append(accepts.getClass().getSimpleName()).append(RN)
        .append(write(accepts, prefix))
        .toString();
  }

  @Override
  public String visitReturns(Returns returns, TraceTree tree) {
    if(returns == null) {
      return "";
    }
    String prefix = getParentCol(tree);
    return new StringBuilder()
        .append(prefix).append("returns: ").append(returns.getClass().getSimpleName()).append(RN)
        .append(write(returns, prefix))
        .toString();
  }

  @Override
  public String visitIteration(Maped iteration, TraceTree tree) {
    String prefix = getParentCol(tree);
    
    final var matches = new StringBuilder();

    int index = 0;
    for(var nested : iteration.getValues()) {
      final var next = tree.next(nested);
      
      matches
        .append(prefix).append(index++).append(RN)
        .append(visitBody(nested, next)).append(RN);
    }
    return new StringBuilder()
        .append(prefix).append(iteration.getValues().size()).append("elements:").append(RN)
        .append(matches.toString())
        .toString();
  }

  @Override
  public String visitHitPolicy(Matched hitpolicy, TraceTree tree) {
    final var matches = new StringBuilder();
    for(var when : hitpolicy.getMatches()) {
      matches.append(visitHitPolicyMatch(when, tree));
    }
    String prefix = getParentCol(tree);
    return new StringBuilder()
        .append(prefix).append("Matched found: ").append(hitpolicy.getMatches().size()).append(RN)
        .append(matches.toString())
        .append(hitpolicy.getReturns().map(r -> write(r, prefix)).orElseGet(() -> ""))
        .toString();
  }

  @Override
  public String visitHitPolicyMatch(MatchedCondition when, TraceTree tree) {
    String prefix = getParentCol(tree);
    return new StringBuilder()
        .append(prefix).append("matched ").append(when.getId()).append(": ").append(when.getSrc()).append(RN)
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
    String prefix = getParentCol(tree);
    for(var trace : calls.getValues()) {
      matches.append(prefix).append(SEP).append("Calling: ").append(trace.getId()).append(RN)
      .append(visitTrace(trace, tree.next(trace)));  
    }
    if(matches.length() == 0) {
      return new StringBuilder()
          .append(prefix).append("Makes no calls").append(RN)
          .toString();      
    } else {
      matches.insert(0, new StringBuilder()
          .append(prefix).append("Makes calls to").append(RN)
          .toString());
    }
    
    return new StringBuilder()
        .append(matches.toString())
        .toString();
  }
}
