package io.digiexpress.eveli.client.config;

/*-
 * #%L
 * eveli-client
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.flywaydb.core.api.Location;
import org.springframework.boot.autoconfigure.flyway.FlywayConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import lombok.extern.slf4j.Slf4j;

/** Appends Flyway locations for each enabled Metis capability. */
@Slf4j
@Configuration
public class EveliAutoConfigMetisFlyway {

  public static final Map<String, String> CAPABILITY_LOCATIONS = new LinkedHashMap<>(Map.of(
      "eveli.metis.search.enabled", "classpath:db/metis/search"));

  @Bean
  public FlywayConfigurationCustomizer metisFlywayLocations(Environment environment) {
    return configuration -> {
      final var locations = new ArrayList<>(Arrays.stream(configuration.getLocations())
          .map(Location::getDescriptor)
          .toList());

      var added = false;
      if (Boolean.TRUE.equals(environment.getProperty("eveli.metis.search.enabled", Boolean.class, Boolean.FALSE))
          && !Boolean.TRUE.equals(environment.getProperty("eveli.metis.enabled", Boolean.class, Boolean.FALSE))) {
        throw new IllegalStateException(
            "eveli.metis.search.enabled is true but eveli.metis.enabled is not. "
                + "Set both flags, and set spring.ai.model.chat and spring.ai.model.embedding "
                + "to a provider (for example ollama), not none.");
      }
      for (final var capability : CAPABILITY_LOCATIONS.entrySet()) {
        if (!environment.getProperty(capability.getKey(), Boolean.class, Boolean.FALSE)) {
          continue;
        }
        if (locations.contains(capability.getValue())) {
          continue;
        }
        locations.add(capability.getValue());
        added = true;
        log.info("Metis capability {} is enabled, flyway locations extended with {}",
            capability.getKey(), capability.getValue());
      }

      if (added) {
        configuration.locations(locations.toArray(String[]::new));
      }
    };
  }
}
