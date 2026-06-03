package io.resys.limaone.model;

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

import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.model.Model.Body;
import io.resys.limaone.model.Model.BodyType;
import jakarta.annotation.Nullable;

@Value.Immutable
@JsonSerialize(as = ImmutablePrintout.class)
@JsonDeserialize(as = ImmutablePrintout.class)
public interface Printout extends Body {
  String getServiceName(); // human readable name, what IS this PDF
  String getOrchestratorName(); // external name/id that will be called to resolve data/ most likely wrench flow name
  
  List<LocaleLabel> getLabels(); // localized labels, human readable names
  default BodyType getBodyType() { return BodyType.PRINTOUT; };
  
  @Nullable String getDescription();

}
