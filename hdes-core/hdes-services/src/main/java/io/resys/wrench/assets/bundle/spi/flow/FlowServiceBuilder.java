package io.resys.wrench.assets.bundle.spi.flow;

import java.util.Map;
import java.util.Optional;

/*-
 * #%L
 * wrench-component-assets
 * %%
 * Copyright (C) 2016 - 2017 Copyright 2016 ReSys OÜ
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

import org.springframework.util.StringUtils;

import io.resys.hdes.client.api.programs.FlowProgram;
import io.resys.wrench.assets.bundle.api.repositories.AssetServiceRepository.AssetService;
import io.resys.wrench.assets.bundle.api.repositories.AssetServiceRepository.ServiceBuilder;
import io.resys.wrench.assets.bundle.api.repositories.AssetServiceRepository.ServiceDataModel;
import io.resys.wrench.assets.bundle.api.repositories.AssetServiceRepository.ServiceIdGen;
import io.resys.wrench.assets.bundle.api.repositories.AssetServiceRepository.ServiceStore;
import io.resys.wrench.assets.bundle.spi.builders.ImmutableServiceBuilder;
import io.resys.wrench.assets.bundle.spi.builders.TemplateServiceBuilder;
import io.resys.wrench.assets.bundle.spi.clock.ClockRepository;
import io.resys.wrench.assets.flow.api.FlowRepository;

public class FlowServiceBuilder extends TemplateServiceBuilder {

  private final ServiceIdGen idGen;
  private final FlowRepository flowRepository;
  private final ClockRepository clockRepository;
  private final String defaultContent;
  private final ServiceStore store;
  private boolean rename;
  
  public FlowServiceBuilder(
      ServiceIdGen idGen,
      ServiceStore store,
      FlowRepository flowRepository,
      ClockRepository clockRepository, String defaultContent) {
    super();
    this.idGen = idGen;
    this.store = store;
    this.flowRepository = flowRepository;
    this.clockRepository = clockRepository;
    this.defaultContent = defaultContent;
  }

  @Override
  public AssetService build() {
    boolean isDefault = StringUtils.isEmpty(src);
    String content = isDefault ? defaultContent.replace("{{id}}", name): this.src;

    Map.Entry<String, FlowProgram> commandsAndModel = flowRepository.createModel().content(content)
        .rename(rename ? Optional.of(name) : Optional.empty())
        .build();
    FlowProgram model = commandsAndModel.getValue();
    
    String serviceId = id == null ? idGen.nextId() : id;
    String pointer = serviceId + ".json";

    if(lastModified == null) {
      lastModified = clockRepository.toTimestamp();
    }

    ServiceDataModel dataModel = new FlowServiceDataModelBuilder(store).build(serviceId, model, lastModified);

    return ImmutableServiceBuilder.newFlow()
        .setId(serviceId)
        .setRev(model.getAst().getRev() + "")
        .setName(model.getId())
        .setDescription(model.getAst().getDescription())
        .setSrc(commandsAndModel.getKey())
        .setPointer(pointer)
        .setModel(dataModel)
        .setExecution(() -> new FlowServiceExecution(model, flowRepository))
        .build();

  }

  @Override
  public ServiceBuilder rename() {
    rename = true;
    return this;
  }
}
