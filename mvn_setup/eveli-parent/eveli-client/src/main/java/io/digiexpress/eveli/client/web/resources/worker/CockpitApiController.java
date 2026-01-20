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

import java.util.List;

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
import com.google.common.collect.ImmutableList;

import io.digiexpress.eveli.client.api.WorkerAuthClient;
import io.digiexpress.thena.cockpit.client.api.CockpitAware.CockpitAwareProvider;
import io.digiexpress.thena.cockpit.client.api.CockpitClient;
import io.digiexpress.thena.cockpit.client.api.CockpitContainer;
import io.digiexpress.thena.cockpit.client.api.entities.CockpitConfigTenant;
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
  
  @GetMapping
  public Multi<CockpitContainer> findAllCockpits() {
    return cockpitClient.queries().cockpitQuery().findAll();
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

  @GetMapping("/{cockpitId}")
  public Uni<CockpitContainer> getOneCockpit(@PathVariable("cockpitId") String id) {
    return cockpitClient.queries().cockpitQuery().getOne(id);
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
  
  
  
  
  @PostMapping("/activity/current-state")
  public Multi<CockpitActivity> changeActivity(@RequestBody CockpitActivityChangeActiveId change) {
    return cockpitAwareProvider.set(change.getActiveId())
        .onItem().transformToMulti(ignore -> findActivity());
  }
  
  @GetMapping("/activity")
  public Multi<CockpitActivity> findActivity() {
    return Uni.combine().all()
        .unis(
            cockpitAwareProvider.get(), 
            cockpitClient.queries().cockpitAwareQuery().findAll().collect().asList(),
            cockpitClient.queries().cockpitAvailableTenantsQuery().findAll().collect().asList()
        )
        .asTuple()
        .onItem().transform(tuple -> ImmutableList.<CockpitActivity>builder()
          .add(ImmutableCockpitActiveState.builder()
              .activeId(tuple.getItem1().map(c -> c.getConfig().getId()).orElse(null))
              .build())
          .add(ImmutableCockpitHardcodedTenant.builder()
              .hardcodedTenants(tuple.getItem2())
              .build())
          .add(ImmutableCockpitAvailableTenants.builder()
              .availableTenants(tuple.getItem3())
              .build())
          .build()
        )
        .onItem().transformToMulti(items -> Multi.createFrom().items(items.stream()));
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

  @JsonSerialize(as = ImmutableCockpitActiveState.class)
  @JsonDeserialize(as = ImmutableCockpitActiveState.class)
  @Value.Immutable
  interface CockpitActiveState extends CockpitActivity {
    @Nullable String getActiveId();
    
    @Override
    default CockpitActivityType getActivityType() {
      return CockpitActivityType.ACTIVE;
    }
  }
  
  @JsonSerialize(as = ImmutableCockpitHardcodedTenant.class)
  @JsonDeserialize(as = ImmutableCockpitHardcodedTenant.class)
  @Value.Immutable
  interface CockpitHardcodedTenant extends CockpitActivity {
    List<CockpitConfigTenant> getHardcodedTenants();
    
    @Override
    default CockpitActivityType getActivityType() {
      return CockpitActivityType.HARDCODED_TENANT;
    }
  }
  
  @JsonSerialize(as = ImmutableCockpitAvailableTenants.class)
  @JsonDeserialize(as = ImmutableCockpitAvailableTenants.class)
  @Value.Immutable
  interface CockpitAvailableTenants extends CockpitActivity {
    List<CockpitConfigTenant> getAvailableTenants();
    
    @Override
    default CockpitActivityType getActivityType() {
      return CockpitActivityType.AVAILABLE_TENANTS;
    }
  }
  

  
  @JsonSerialize(as = ImmutableCockpitActivityChangeActiveId.class)
  @JsonDeserialize(as = ImmutableCockpitActivityChangeActiveId.class)
  @Value.Immutable
  interface CockpitActivityChangeActiveId {
    @Nullable String getActiveId();
  }
  
  
  public enum CockpitTenantType {
    WRENCH(), STENCIL()
  }
  public enum CockpitActivityType {
    ACTIVE, HARDCODED_TENANT, AVAILABLE_TENANTS
  }
}
