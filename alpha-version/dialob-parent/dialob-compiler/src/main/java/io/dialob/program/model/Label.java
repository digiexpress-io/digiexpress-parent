/*
 * Copyright © 2015 - 2021 ReSys (info@dialob.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.dialob.program.model;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.immutables.value.Value;

@Value.Immutable
public interface Label extends ProgramNode {

  @Nonnull
  static Label createLabel(@Nonnull Map<String,String> map) {
    return ImmutableLabel.builder().putAllLabels(map).build();
  }

  @Nonnull
  Map<String, String> getLabels();

  @Nullable
  default String getLabel(@Nonnull String language) {
    return getLabels().get(language);
  }
}
