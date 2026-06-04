package io.resys.limaone.persistence;

/*-
 * #%L
 * limaone-compiler
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
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

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import io.resys.limaone.authoring.ImmutableNewFlowTaskProps;
import io.resys.limaone.authoring.ImmutableNewFlowTaskProps.Builder;
import io.resys.limaone.authoring.NewFlowTask;
import io.resys.limaone.model.FlowTask;
import io.resys.limaone.model.ImmutableFlowTask;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.limaone.persistence.AuthoringImpl.AuthoringConfig;
import io.smallrye.mutiny.Uni;


public class NewFlowTaskImpl extends AuthoringTemplate<NewFlowTaskImpl, Model<FlowTask>> implements NewFlowTask {

  private NewFlowTaskProps props;

  public NewFlowTaskImpl(AuthoringConfig config) {
    super(config);
  }
  
  @Override
  public NewFlowTask props(NewFlowTaskProps props) {
    this.props = props;
    return this;
  }

  @Override
  public NewFlowTask props(Consumer<Builder> props) {
    final var builder = ImmutableNewFlowTaskProps.builder();
    props.accept(builder);
    return props(builder.build());
  }

  @Override
  public Uni<Model<FlowTask>> build() {
    return config.getPersistence().worldBuilder()
      .docs(BodyType.FLOW_TASK)
      .createdAt(getCreatedAt())
      .author(getAuthor())
      .build(nextWorld -> {
        final var body = internalBuild(nextWorld.getCurrentWorld());
        return nextWorld.newModel(body.getTaskName(), body, props.getAssetDescription());
      });
  }
  
  private FlowTask internalBuild(ModelWorld world) {
    Objects.requireNonNull(props, () -> "props must be defined");

    final String syntax; 
    if(props.getBody() == null) {
      syntax = """
// custom ref example - @ServiceRef(value="newService", type=BodyType.DT)
public class {name} {

  public Output execute(Input input) {
    Output output = new Output();
    output.sum = input.a + input.b;
    return output;
  }
  
  
  @ServiceData
  public static class Input implements Serializable {
    Integer a;
    Integer b;
  }
  
  @ServiceData
  public static class Output implements Serializable {
    Integer sum;
  }
}
""".replace("{name}", Optional.ofNullable(props.getName()).orElse("MyFirstTask"));
    } else {
      syntax = props.getBody();
    }
    
    final var flow = config.getEnvir().getAstParser().parseFlowTask().syntax(syntax).parse();
    return ImmutableFlowTask.builder()
        .taskName(flow.getName())
        .taskValue(syntax)
        .build();
  }
}
