package io.resys.hdes.ast.spi;

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

import io.resys.hdes.ast.api.nodes.BodyNode;
import io.resys.hdes.ast.api.nodes.ImmutablePosition;
import io.resys.hdes.ast.api.nodes.ImmutableToken;
import io.resys.hdes.ast.api.nodes.InvocationNode.SimpleInvocation;
import io.resys.hdes.ast.api.nodes.RootNode;
import io.resys.hdes.ast.spi.util.Assertions;

public class ImmutableRootNode implements RootNode {
  private final Map<String, BodyNode> body;
  private final Map<String, List<ErrorNode>> errors;
  // Source origin, body node id - location id
  private final Map<String, String> origin;
  private final Token token;

  public ImmutableRootNode(Map<String, BodyNode> body, Map<String, String> origin,  Map<String, List<ErrorNode>> errors) {
    super();
    this.body = body;
    this.origin = origin;
    this.errors = errors;
    this.token = ImmutableToken.builder()
        .start(ImmutablePosition.builder().line(0).col(0).build())
        .text("root-node")
        .build();
  }
  @Override
  public Map<String, BodyNode> getBody() {
    return body;
  }
  @Override
  public BodyNode getBody(String id) {
    Assertions.isTrue(body.containsKey(id), () ->
      new StringBuilder("No node by given identifier: ").append(id).append("!").append(System.lineSeparator())
      .append("Known identifiers: ").append(body.keySet()).toString()
    );
    
    return body.get(id);
  }
  @Override
  public String getOrigin(String id) {
    Assertions.isTrue(origin.containsKey(id), () ->
      new StringBuilder("No src by given identifier: ").append(id).append("!").append(System.lineSeparator())
      .append("Known identifiers: ").append(origin.keySet()).toString()
    );
    return origin.get(id);
  }
  @Override
  public List<ErrorNode> getErrors(String id) {
    Assertions.isTrue(origin.containsKey(id), () ->
      new StringBuilder("No src by given identifier: ").append(id).append("!").append(System.lineSeparator())
      .append("Known identifiers: ").append(origin.keySet()).toString()
    );
    return errors.get(id);
  }
  
  @Override
  public Map<String, List<ErrorNode>> getErrors() {
    return errors;
  }
  
  @Override
  public Token getToken() {
    return token;
  }
  @Override
  public Map<String, String> getOrigin() {
    return origin;
  }
  @Override
  public BodyNode getBody(SimpleInvocation bodyId) {
    return getBody(bodyId.getValue());
  }
}
