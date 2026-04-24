package io.resys.limaone.persistence.fs;

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

import io.resys.limaone.fs.ImmutableLanguageProps;
import io.resys.limaone.fs.WorldFsProps.LanguageProps;
import io.resys.limaone.model.Locale;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Props_LocaleBuilder {
  private final WorldFsState currentState;
  private final NodeAndBody node;
  
  public LanguageProps build() {
    final Locale locale = currentState.getBodyOfType(node);
    return ImmutableLanguageProps.builder()
        .localeCode(locale.getValue())
        .id(node.getObjectId())
        .type(node.getBodyType())
        .locked(false)
        .build();
  }
  
  public static LanguageProps of(WorldFsState currentState, NodeAndBody node) {
    return new Props_LocaleBuilder(currentState, node).build();
  }

}
