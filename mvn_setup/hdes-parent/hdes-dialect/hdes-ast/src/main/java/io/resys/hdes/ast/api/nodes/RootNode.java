package io.resys.hdes.ast.api.nodes;

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

import java.util.List;
import java.util.Map;

import io.resys.hdes.ast.api.nodes.InvocationNode.SimpleInvocation;

public interface RootNode extends HdesNode {
  
  Map<String, BodyNode> getBody();
  Map<String, String> getOrigin();
  Map<String, List<ErrorNode>> getErrors();
  
  // internal or external id depends how the source was build
  BodyNode getBody(String bodyId);
  BodyNode getBody(SimpleInvocation bodyId);
  String getOrigin(String id);
  List<ErrorNode> getErrors(String id);
}
