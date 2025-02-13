package io.resys.hdes.ast.api.visitors;

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

import io.resys.hdes.ast.api.nodes.BodyNode;
import io.resys.hdes.ast.api.nodes.HdesTree.DecisionTableTree;
import io.resys.hdes.ast.api.nodes.HdesTree.FlowTree;
import io.resys.hdes.ast.api.nodes.HdesTree.RootTree;
import io.resys.hdes.ast.api.nodes.HdesTree.ServiceTree;
import io.resys.hdes.ast.api.nodes.RootNode;

public interface HdesVisitor<T, R> {
  
  interface RootNodeVisitor<T, R> extends HdesVisitor<T, R> {
    T visitBody(RootNode root);
    R visitNode(BodyNode bodyNode, RootTree ctx);
    R visitFlow(FlowTree ctx);
    R visitDecisionTable(DecisionTableTree ctx);
    R visitService(ServiceTree ctx);
  }

}
