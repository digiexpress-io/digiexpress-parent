package io.resys.limaone.persistence.fs;

import java.util.Collections;

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

import io.resys.limaone.fs.ImmutableLocaleProps;
import io.resys.limaone.fs.WorldFsProps.ConfigOption;
import io.resys.limaone.fs.WorldFsProps.LocaleProps;
import io.resys.limaone.model.Locale;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Props_LocaleBuilder {
  private final WorldFsState currentState;
  private final NodeAndBody node;
  
  public LocaleProps build() {
    final Locale locale = currentState.getBodyOfType(node);
    final var builder = ImmutableLocaleProps.builder();

    if (!Boolean.TRUE.equals(locale.getEnabled())) {
      builder.addConfigOptions(ConfigOption.DISABLED_MODE);
    }

    return builder
        .localeCode(locale.getValue())
        .description(node.getDescription().map(e -> e.getText()).orElse(null))
        .labels(node.getDescription().map(e -> e.getLabels()).orElse(Collections.emptyList()))
        .id(node.getObjectId())
        .type(node.getBodyType())
        .locked(false)
        .build();
  }
  
  public static LocaleProps of(WorldFsState currentState, NodeAndBody node) {
    return new Props_LocaleBuilder(currentState, node).build();
  }

}
