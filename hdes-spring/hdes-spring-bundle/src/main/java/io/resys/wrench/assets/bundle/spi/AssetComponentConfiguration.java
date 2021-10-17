package io.resys.wrench.assets.bundle.spi;

/*-
 * #%L
 * wrench-assets-bundle
 * %%
 * Copyright (C) 2016 - 2021 Copyright 2020 ReSys OÜ
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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.io.IOUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.resys.hdes.client.spi.HdesClientImpl;
import io.resys.hdes.client.spi.HdesTypeDefsFactory.ServiceInit;
import io.resys.hdes.client.spi.flow.validators.IdValidator;
import io.resys.wrench.assets.bundle.api.repositories.AssetServiceRepository;
import io.resys.wrench.assets.bundle.api.repositories.AssetServiceRepository.ServiceBuilder;
import io.resys.wrench.assets.bundle.api.repositories.AssetServiceRepository.ServiceIdGen;
import io.resys.wrench.assets.bundle.api.repositories.AssetServiceRepository.ServicePostProcessor;
import io.resys.wrench.assets.bundle.api.repositories.AssetServiceRepository.ServicePostProcessorSupplier;
import io.resys.wrench.assets.bundle.api.repositories.AssetServiceRepository.ServiceStore;
import io.resys.wrench.assets.bundle.api.repositories.AssetServiceRepository.ServiceType;
import io.resys.wrench.assets.bundle.spi.clock.ClockRepository;
import io.resys.wrench.assets.bundle.spi.clock.SystemClockRepository;
import io.resys.wrench.assets.bundle.spi.dt.DtServiceBuilder;
import io.resys.wrench.assets.bundle.spi.flow.FlowServiceBuilder;
import io.resys.wrench.assets.bundle.spi.flow.FlowServiceDataModelValidator;
import io.resys.wrench.assets.bundle.spi.flowtask.FlowTaskServiceBuilder;
import io.resys.wrench.assets.bundle.spi.postprocessors.FlowDependencyServicePostProcessor;
import io.resys.wrench.assets.bundle.spi.postprocessors.GenericServicePostProcessorSupplier;
import io.resys.wrench.assets.bundle.spi.postprocessors.ListServicePostProcessor;
import io.resys.wrench.assets.bundle.spi.repositories.GenericAssetServiceRepository;
import io.resys.wrench.assets.bundle.spi.store.AssetLocation;
import io.resys.wrench.assets.bundle.spi.store.GenericServiceIdGen;
import io.resys.wrench.assets.bundle.spi.store.ListAssetLoader;
import io.resys.wrench.assets.bundle.spi.store.PostProcessingServiceStore;
import io.resys.wrench.assets.bundle.spi.tag.TagServiceBuilder;


@Configuration
public class AssetComponentConfiguration {
  private final ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
  
  @Bean
  public AssetServiceRepository assetServiceRepository(
      ApplicationContext context, ObjectMapper objectMapper, 
      ServiceStore origServiceStore) {
    
    final ServiceInit init = new ServiceInit() {
      @Override
      public <T> T get(Class<T> type) {
        return context.getAutowireCapableBeanFactory().createBean(type);
      }
    };
        
    final ClockRepository clockRepository = new SystemClockRepository();
    final HdesClientImpl hdesClient = HdesClientImpl.builder()
        .objectMapper(objectMapper)
        .serviceInit(init)
        .flowVisitors(new IdValidator())
        .build();
    
    final ServiceIdGen idGen = new GenericServiceIdGen();
    final Map<ServiceType, Function<ServiceStore, ServiceBuilder>> builders = new HashMap<>();
    builders.put(ServiceType.DT, (store) -> new DtServiceBuilder(idGen, hdesClient, clockRepository, getDefaultContent(ServiceType.DT)));
    builders.put(ServiceType.FLOW, (store) -> new FlowServiceBuilder(idGen, store, hdesClient, clockRepository, getDefaultContent(ServiceType.FLOW)));
    builders.put(ServiceType.FLOW_TASK, (store) -> new FlowTaskServiceBuilder(idGen, store, hdesClient, objectMapper, getDefaultContent(ServiceType.FLOW_TASK)));
    builders.put(ServiceType.TAG, (store) -> new TagServiceBuilder("", idGen, getDefaultContent(ServiceType.TAG)));
    
    final Map<ServiceType, ServicePostProcessor> postProcessors = new HashMap<>();
    postProcessors.put(ServiceType.FLOW_TASK, new FlowDependencyServicePostProcessor(builders));
    postProcessors.put(ServiceType.DT, new FlowDependencyServicePostProcessor(builders));
    postProcessors.put(ServiceType.DATA_TYPE, new ListServicePostProcessor(new FlowDependencyServicePostProcessor(builders)));
    
    final ServicePostProcessorSupplier servicePostProcessorSupplier = new GenericServicePostProcessorSupplier(postProcessors);
    final ServiceStore serviceStore = new PostProcessingServiceStore(origServiceStore, servicePostProcessorSupplier); 
    
    hdesClient.config().config(new FlowServiceDataModelValidator(serviceStore, hdesClient));
    return new GenericAssetServiceRepository(hdesClient, objectMapper, builders, serviceStore);
  }

  @Bean
  public Loader loader(AssetLocation location, AssetServiceRepository assetRepository) {
    return new Loader(location, assetRepository);
  }

  public static class Loader {
    private final AssetLocation location;
    private final AssetServiceRepository assetRepository;

    public Loader(AssetLocation location, AssetServiceRepository assetRepository) {
      super();
      this.location = location;
      this.assetRepository = assetRepository;
    }
    
    @EventListener({ContextRefreshedEvent.class})
    public void load() {
      ListAssetLoader result = new ListAssetLoader(assetRepository, location);
      result.load();
    }
  }


  protected String getDefaultContent(ServiceType type) {
    try {
      String location = getDefaultContentPattern(type);
      Resource resource = resolver.getResource(location);
      return IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);
    } catch(IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  protected String getDefaultContentPattern(ServiceType type) {
    switch(type) {
    case DT: return "classpath:defaults/default-dt.json";
    case FLOW: return "classpath:defaults/default-flow.json";
    case FLOW_TASK: return "classpath:defaults/default-flowtask.json";
    case TAG: return "classpath:defaults/default-tag.json";
    case DATA_TYPE: return "classpath:defaults/default-datatype.yaml";
    default: throw new IllegalArgumentException("No default content for service type: " + type + "!");
    }
  }
}
