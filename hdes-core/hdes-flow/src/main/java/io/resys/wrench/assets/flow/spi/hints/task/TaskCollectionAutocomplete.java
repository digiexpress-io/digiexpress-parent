package io.resys.wrench.assets.flow.spi.hints.task;

/*-
 * #%L
 * wrench-assets-flow
 * %%
 * Copyright (C) 2016 - 2019 Copyright 2016 ReSys OÜ
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.resys.hdes.client.api.ast.AstFlow.FlowAstCommandRange;
import io.resys.hdes.client.api.ast.AstFlow.FlowAstRef;
import io.resys.hdes.client.api.ast.AstFlow.FlowAstTask;
import io.resys.hdes.client.api.ast.AstFlow.NodeFlow;
import io.resys.hdes.client.api.ast.AstFlow.NodeFlowVisitor;
import io.resys.hdes.client.api.ast.ImmutableAstFlow;
import io.resys.hdes.client.spi.flow.ast.FlowNodesFactory;
import io.resys.hdes.client.spi.flow.ast.beans.NodeFlowBean;

public class TaskCollectionAutocomplete implements NodeFlowVisitor {

  @Override
  public void visit(NodeFlow flow, ImmutableAstFlow.Builder modelBuilder) {
    Map<String, FlowAstTask> tasks = flow.getTasks();

    if(tasks.isEmpty()) {
      return;
    }

    List<FlowAstCommandRange> ranges = new ArrayList<>();
    for(FlowAstTask task : tasks.values()) {
      FlowAstRef ref = task.getRef();
      if(ref == null) {
        continue;
      }

      FlowAstCommandRange range;
      if(ref.getCollection() != null) {
        range = FlowNodesFactory.range().build(ref.getCollection().getStart());
      } else {
        range = FlowNodesFactory.range().build(ref.getStart(), ref.getEnd(), true);
      }

      ranges.add(range);
    }

    if(!ranges.isEmpty()) {
      modelBuilder
      .addAutocomplete(FlowNodesFactory.ac()
          .id(TaskCollectionAutocomplete.class.getSimpleName())
          .addField("        " + NodeFlowBean.KEY_COLLECTION, true)
          .addRange(ranges)
          .build())
      .addAutocomplete(FlowNodesFactory.ac()
          .id(TaskCollectionAutocomplete.class.getSimpleName())
          .addField("        " + NodeFlowBean.KEY_COLLECTION, false)
          .addRange(ranges)
          .build());
    }
  }
}
