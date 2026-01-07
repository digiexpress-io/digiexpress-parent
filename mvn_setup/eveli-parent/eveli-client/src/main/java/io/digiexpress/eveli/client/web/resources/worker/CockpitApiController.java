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

package io.digiexpress.eveli.client.web.resources.worker;

import java.util.Arrays;

import org.immutables.value.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.digiexpress.eveli.client.api.WorkerAuthClient;
import io.digiexpress.thena.cockpit.client.api.CockpitAware.CockpitAwareProvider;
import io.digiexpress.thena.cockpit.client.api.CockpitClient;
import io.digiexpress.thena.cockpit.client.api.CockpitContainer;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/worker/rest/api/cockpits")
@Slf4j
@RequiredArgsConstructor
public class CockpitApiController {
  private final CockpitClient cockpitClient;
  private final CockpitAwareProvider cockpitAwareProvider;
  private final WorkerAuthClient auth;
  

  @GetMapping("/activity")
  public Multi<CockpitActivity> findActivity() {
    return cockpitAwareProvider.get().onItem().transform(active -> 
      Arrays.asList(
        ImmutableCockpitActivityState.builder()
          .activeId(active.map(c -> c.getConfig().getId()).orElse(null))
          .build()
      ))
      .onItem().transformToMulti(items -> Multi.createFrom().items(items.stream()));
  }

  @PostMapping("/activity-state")
  public Multi<CockpitActivity> changeActivity(@RequestBody CockpitActivityChangeActiveId change) {
    return cockpitAwareProvider.set(change.getActiveId())
        .onItem().transformToMulti(ignore -> findActivity());
  }
  
  @PostMapping
  public Uni<CockpitContainer> createCockpit(@RequestBody CreateCockpitCommand command) {
    final var author = auth.getUser().getPrincipal().getUsername();
    return cockpitClient.commits().createOneCockpitConfig()
        .cockpitConfig(config -> config
            .configName(command.getConfigName())
            .configDesc(command.getConfigDescription())
            .build()
        )
        .commitAuthor(author)
        .commitMessage("created from rest api")
        .build()
        .onItem().transform(env -> env.getCockpitConfig());
  }
  
  
  @PostMapping("/{cockpitId}/tenants")
  public Uni<CockpitContainer> createCockpitTenant(
      @PathVariable("cockpitId") String id,
      @RequestBody CreateCockpitTenantCommand command) {
    
    final var author = auth.getUser().getPrincipal().getUsername();
    return cockpitClient.commits().modifyOneCockpitConfig()
        .cockpitConfigId(id)
        .modifyCockpitConfig(mod -> mod.addTenant(tenant -> tenant
            .externalId(command.getExternalId())
            .tenantType(command.getTenantType().name())
            .tenantDescription(command.getTenantDescription())
            .build()).build())
        .commitAuthor(author)
        .commitMessage("created from rest api")
        .build()
        .onItem().transform(env -> env.getCockpitConfig());
  }
  
  
  @PutMapping("/{cockpitId}/tenants/{tenantId}")
  public Uni<CockpitContainer> modifyCockpitTenant(
      @PathVariable("cockpitId") String id,
      @RequestBody ModifyCockpitTenantCommand command) {
    
    final var author = auth.getUser().getPrincipal().getUsername();
    return cockpitClient.commits().modifyOneCockpitConfig()
        .cockpitConfigId(id)
        .modifyCockpitConfig(mod -> mod.modifyTenant(command.getTenantId(), tenant -> tenant
            .externalId(command.getExternalId())
            .tenantDescription(command.getTenantDescription())
            .build()).build())
        .commitAuthor(author)
        .commitMessage("modify from rest api")
        .build()
        .onItem().transform(env -> env.getCockpitConfig());
  }
   
  @JsonSerialize(as = ImmutableCreateCockpitCommand.class)
  @JsonDeserialize(as = ImmutableCreateCockpitCommand.class)
  @Value.Immutable
  interface CreateCockpitCommand {
    String getConfigName();
    String getConfigDescription();
  }
  
  @JsonSerialize(as = ImmutableCreateCockpitTenantCommand.class)
  @JsonDeserialize(as = ImmutableCreateCockpitTenantCommand.class)
  @Value.Immutable
  interface CreateCockpitTenantCommand {
    String getExternalId();
    CockpitTenantType getTenantType();
    String getTenantDescription();
  }
  
  @JsonSerialize(as = ImmutableModifyCockpitTenantCommand.class)
  @JsonDeserialize(as = ImmutableModifyCockpitTenantCommand.class)
  @Value.Immutable
  interface ModifyCockpitTenantCommand {
    String getTenantId();
    String getExternalId();
    String getTenantDescription();
  }
  

  
  

  interface CockpitActivity {
    CockpitActivityType getActivityType();
  }

  @JsonSerialize(as = ImmutableCockpitActivityState.class)
  @JsonDeserialize(as = ImmutableCockpitActivityState.class)
  @Value.Immutable
  interface CockpitActivityState extends CockpitActivity {
    @Nullable String getActiveId();
    
    @Override
    default CockpitActivityType getActivityType() {
      return CockpitActivityType.STATE;
    }
  }  
  
  @JsonSerialize(as = ImmutableCockpitActivityState.class)
  @JsonDeserialize(as = ImmutableCockpitActivityState.class)
  @Value.Immutable
  interface CockpitActivityChangeActiveId {
    @Nullable String getActiveId();
  }
  
  
  public enum CockpitTenantType {
    WRENCH(), STENCIL()
  }
  public enum CockpitActivityType {
    STATE
  }
}
