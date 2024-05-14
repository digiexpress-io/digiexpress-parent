package io.resys.hdes.client.spi.decision;

/*-
 * #%L
 * hdes-client-api
 * %%
 * Copyright (C) 2020 - 2021 Copyright 2020 ReSys OÜ
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.resys.hdes.client.api.exceptions.DecisionProgramException;
import io.resys.hdes.client.api.programs.DecisionProgram;
import io.resys.hdes.client.api.programs.DecisionProgram.DecisionLog;
import io.resys.hdes.client.api.programs.DecisionProgram.DecisionLogEntry;
import io.resys.hdes.client.api.programs.DecisionProgram.DecisionResult;
import io.resys.hdes.client.api.programs.DecisionProgram.DecisionRow;
import io.resys.hdes.client.api.programs.DecisionProgram.DecisionRowAccepts;
import io.resys.hdes.client.api.programs.ImmutableDecisionLog;
import io.resys.hdes.client.api.programs.ImmutableDecisionLogEntry;
import io.resys.hdes.client.api.programs.ImmutableDecisionResult;
import io.resys.hdes.client.api.programs.Program.ProgramContext;

public class DecisionProgramExecutor {
  
  public static DecisionResult run(DecisionProgram program, ProgramContext context) {
    final var decisions = ImmutableDecisionResult.builder();
    
    Iterator<DecisionRow> it = program.getRows().iterator();
    while(it.hasNext()) {
      final var node = it.next();
      final var decision = visitRow(node, context);
      
      if(decision.getMatch()) {
        decisions.addMatches(decision);
      } else {
        decisions.addRejections(decision);
      }
      
      final var isBreak = visitHitPolicy(program, decision);
      if(isBreak) {
        break;
      }
    }

    return decisions.build();
  }
  
  public static Map<String, Serializable> get(DecisionResult program) {
    if(program.getMatches().size() > 1) {
      throw new DecisionProgramException("Expected 0-1 results but was: " + program.getMatches().size() + "!");
    } else if(program.getMatches().size() == 1) {
      return toValues(program.getMatches().get(0));
    }
    return Collections.emptyMap();
  }

  public static List<Map<String, Serializable>> find(DecisionResult program) {
    if(program.getMatches().size() > 0) {
      return program.getMatches().stream().map(DecisionProgramExecutor::toValues).collect(Collectors.toList());
    }
    return Collections.emptyList();
  }
  
  public static Map<String, Serializable> toValues(DecisionLog decision) {
    Map<String, Serializable> result = new HashMap<>();
    for (DecisionLogEntry entry : decision.getReturns()) {
      result.put(entry.getHeaderType().getName(), entry.getUsedValue());
    }
    return result;
  }
  
  private static boolean visitHitPolicy(DecisionProgram program, DecisionLog decision) {
    final var hitPolicy = program.getHitPolicy();
    switch(hitPolicy) {
    case FIRST:
      // match only the first
      return decision.getMatch();
    case ALL:
      // match all
      return false;
    default: throw new DecisionProgramException("Unknown hit policy: " + hitPolicy + "!");
    }
  }

  private static DecisionLog visitRow(DecisionRow node, ProgramContext context) {
    Boolean match = null;
    final var data = ImmutableDecisionLog.builder();
    
    for(DecisionRowAccepts input : node.getAccepts()) {
      Serializable contextEntity = context.getValue(input.getKey());

      try {
        match = (Boolean) input.getExpression().run(input.getKey().toValue(contextEntity)).getValue();
      } catch(Exception e) {
        throw new DecisionProgramException(
            "Failed to evaluate expression: '" + input.getExpression().getSrc() + "'"
            + ", because: " + e.getMessage()
            + "!", e);    
      }
      data.addAccepts(ImmutableDecisionLogEntry.builder()
          .match(match)
          .headerType(input.getKey())
          .expression(input.getExpression().getSrc())
          .usedValue(contextEntity)
          .build());
      
      if(!match) {
        break;
      }
    }
    
    match = node.getAccepts().isEmpty() || Boolean.TRUE.equals(match);
    if(match) {
      for(final var returns : node.getReturns()) {
        data.addReturns(ImmutableDecisionLogEntry.builder()
            .match(true)
            .headerType(returns.getKey())
            .expression("")
            .usedValue(returns.getValue())
            .build());
      }
    }
    return data.order(node.getOrder()).match(match).build();
  }

}
