package io.digiexpress.eveli.client.web.resources.assets;

import java.time.Duration;
import java.time.OffsetDateTime;

import org.immutables.value.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;

/*-
 * #%L
 * eveli-client
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
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

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.digiexpress.eveli.envir.api.EveliEnvirClient;
import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliDeployment;
import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliSources;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;



@RestController
@RequiredArgsConstructor
@RequestMapping("/worker/rest/api/assets/deployments")
@Slf4j
public class AssetsDeploymentController {
  
  private final EveliEnvirClient composer;
  private final ApplicationEventPublisher publisher;
  
  @JsonSerialize(as = ImmutableEveliDeploymentUpload.class)
  @JsonDeserialize(as = ImmutableEveliDeploymentUpload.class)
  @JsonIgnoreProperties(ignoreUnknown = true)
  @Value.Immutable
  public interface EveliDeploymentUpload {
    String getName();
    @Nullable String getExternalId();
    String getCreatedBy();
    OffsetDateTime getStartsAt();
    String getDescription();
    EveliSources getSources();
  }
  
  
  @Getter @RequiredArgsConstructor
  public static class CompileDeploymentEvent {
    private final String deploymentId;
    
  }
  
  @GetMapping("/{name}")
  public Uni<EveliDeployment> download(@PathVariable("name") String name) {
    return composer.deploymentQuery().emptyBranchBody(false).getOneById(name);
  }
  
  @PostMapping
  public Uni<EveliDeployment> upload(@RequestBody EveliDeploymentUpload body) {
    return composer.createOneDeployment()

        .dialob(body.getSources().getDialob())
        .stencil(body.getSources().getStencil())
        .wrench(body.getSources().getWrench())

        .name(body.getName())
        .startsAt(body.getStartsAt())
        .userId(body.getCreatedBy())
        
        .build()
        .onItem().invoke(deployment -> 
          publisher.publishEvent(new CompileDeploymentEvent(deployment.getId()))          
        );
  }

  @Async
  @EventListener
  public void compileDeployment(CompileDeploymentEvent event) {
    composer.deploymentCompiler()
      .deploymentId(event.getDeploymentId())
      .userId("system")
      .compile()
      .onItem().transformToUni(e -> 
        composer.deploymentBuilder().deploymentId(e.getId()).userId("system").build()
      )
      .await().atMost(Duration.ofMinutes(20));
  }
}
