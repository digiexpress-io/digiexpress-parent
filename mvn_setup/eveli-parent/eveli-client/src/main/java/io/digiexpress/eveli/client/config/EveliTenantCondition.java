package io.digiexpress.eveli.client.config;

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

import java.util.Arrays;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import io.digiexpress.eveli.client.api.TenantConfigClient.TenantConfig;
import io.digiexpress.eveli.client.spi.tenant.TenantConfigClientProps;



public class EveliTenantCondition extends SpringBootCondition {
  
  private final List<String> tenantFeature;
  private static volatile TenantConfig tenant;
  
  public EveliTenantCondition(String ... tenantFeature) {
    super();
    this.tenantFeature = Arrays.asList(tenantFeature);
  }


  
  @Override
  public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
    final var config = getTenantConfig(context);
    final var enabled = tenantFeature.stream().filter(feature -> config.getFeatures().contains(feature)).count() > 0; 
    
    if(enabled) {
      return new ConditionOutcome(enabled, "eveli tenant feature: " + tenantFeature + " enabled");  
    }
    return new ConditionOutcome(enabled, "eveli tenant feature: " + tenantFeature + " disabled");    
  }

  
  private static TenantConfig getTenantConfig(ConditionContext context) {
    if(tenant == null) {
      final var props = new EveliProps();
      final var property = context.getEnvironment().getProperty(TenantConfigClientProps.SPRING_PROP_NAME, "");
      props.setTenantFeatures(Arrays.asList(property));
      tenant = new TenantConfigClientProps(props).getConfig();
    }
    return tenant;
  }
}
