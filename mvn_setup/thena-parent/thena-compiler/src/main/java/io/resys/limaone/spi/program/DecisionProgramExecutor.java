package io.resys.limaone.spi.program;

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

import io.resys.hdes.client.api.programs.Program.ProgramContext;
import io.resys.limaone.ast.DecisionTable_AST;
import io.resys.limaone.ast.DecisionTable_AST.DecisionRowNode;
import io.resys.limaone.program.DecisionProgram;
import io.resys.limaone.program.DecisionProgram.DecisionLog;
import io.resys.limaone.program.DecisionProgram.DecisionLogEntry;
import io.resys.limaone.program.DecisionProgram.DecisionResult;
import io.resys.limaone.program.DecisionProgram.DecisionRowAccepts;
import io.resys.limaone.spi.compiler.decisiontable.DecisionRowException;
import io.resys.limaone.program.ImmutableDecisionLog;
import io.resys.limaone.program.ImmutableDecisionLogEntry;
import io.resys.limaone.program.ImmutableDecisionResult;
import io.resys.limaone.program.ProgramInput;
import io.resys.limaone.program.Runtime;

public class DecisionProgramExecutor {
  
  public static DecisionResult run(DecisionTable_AST ast, ProgramInput input, Runtime runtime) {
    final var decisions = ImmutableDecisionResult.builder();
    
    Iterator<DecisionRowNode> it = ast.getRows().iterator();
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
      throw new DecisionRowException("Expected 0-1 results but was: " + program.getMatches().size() + "!");
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
    default: throw new DecisionRowException("Unknown hit policy: " + hitPolicy + "!");
    }
  }

  private static DecisionLog visitRow(DecisionRowNode node, ProgramContext context) {
    Boolean match = null;
    final var data = ImmutableDecisionLog.builder();
    
    for(DecisionRowAccepts input : node.getAccepts()) {
      Serializable contextEntity = context.getValue(input.getKey());

      try {
        match = (Boolean) input.getExpression().run(input.getKey().toValue(contextEntity)).getValue();
      } catch(Exception e) {
        throw new DecisionRowException(
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
