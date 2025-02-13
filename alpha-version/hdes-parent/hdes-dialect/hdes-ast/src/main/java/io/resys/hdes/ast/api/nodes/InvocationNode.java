package io.resys.hdes.ast.api.nodes;

import java.util.List;

/*-
 * #%L
 * hdes-ast
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

import org.immutables.value.Value;

public interface InvocationNode extends HdesNode {
  
  enum StaticMethodType { SUM, AVG, MIN, MAX, IN }
  
  interface Placeholder extends InvocationNode {}

  // Flat placeholder starts with '_'
  @Value.Immutable
  interface NamedPlaceholder extends Placeholder {
    String getValue();
  }

  @Value.Immutable
  interface EmptyPlaceholder extends Placeholder {}
  
  // Flat one name thats not a placeholder or nested name
  @Value.Immutable
  interface SimpleInvocation extends InvocationNode {
    String getValue();
  }
  
  @Value.Immutable
  interface NestedInvocation extends InvocationNode {
    // path
    InvocationNode getPath();
    
    // last value on nested invocation
    InvocationNode getValue();
  }
  
  @Value.Immutable
  interface SortBy extends FlowNode {
    List<SortByDef> getValues();
  }

  @Value.Immutable
  interface SortByDef extends FlowNode {
    InvocationNode getName();
    Boolean getAsc();
  }
  
  /*
   *   @Override
  public SortBy visitSortBy(SortByContext ctx) {
    Nodes nodes = nodes(ctx);
    return ImmutableSortBy.builder().token(nodes.getToken()).values(nodes.list(SortByDef.class)).build();
  }
  
  @Override
  public SortByDef visitSortByArg(SortByArgContext ctx) {
    Nodes nodes = nodes(ctx);
    
    final boolean asc;
    if(ctx.getChildCount() > 1) {
      TerminalNode node = (TerminalNode) ctx.getChild(1);
      asc = node.getSymbol().getType() == HdesParser.ASC;
    } else {
      asc = true;
    }
    return ImmutableSortByDef.builder().token(nodes.getToken()).name(nodes.of(InvocationNode.class).get()).asc(asc).build();
  }
  
   */
}
