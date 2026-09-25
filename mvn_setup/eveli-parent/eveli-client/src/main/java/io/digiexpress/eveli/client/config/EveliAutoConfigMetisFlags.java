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

import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Always imported. Search beans are conditional on both flags, so without this
 * check {@code eveli.metis.search.enabled=true} and {@code eveli.metis.enabled=false}
 * would skip search silently instead of failing boot.
 */
@Configuration
public class EveliAutoConfigMetisFlags {

  public EveliAutoConfigMetisFlags(Environment environment) {
    if (Boolean.TRUE.equals(environment.getProperty("eveli.metis.search.enabled", Boolean.class, Boolean.FALSE))
        && !Boolean.TRUE.equals(environment.getProperty("eveli.metis.enabled", Boolean.class, Boolean.FALSE))) {
      throw new IllegalStateException(
          "eveli.metis.search.enabled is true but eveli.metis.enabled is not. "
              + "Set both flags, and set spring.ai.model.chat and spring.ai.model.embedding "
              + "to ollama or google-genai, not none.");
    }
  }
}
