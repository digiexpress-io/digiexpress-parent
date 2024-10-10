package io.resys.hdes.client.api.ast;

/*-
 * #%L
 * hdes-client-api
 * %%
 * Copyright (C) 2020 - 2023 Copyright 2020 ReSys OÜ
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


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.io.Serializable;
import java.util.List;

@Value.Immutable
@JsonSerialize(as = ImmutableAstTagSummary.class)
@JsonDeserialize(as = ImmutableAstTagSummary.class)
public interface AstTagSummary extends Serializable {

  String getTagName();
  List<SummaryItem> getFlows();
  List<SummaryItem> getDecisions();
  List<SummaryItem> getServices();

  @Value.Immutable
  @JsonSerialize(as = ImmutableSummaryItem.class)
  @JsonDeserialize(as = ImmutableSummaryItem.class)
  interface SummaryItem {
    String getId();
    String getName();
    String getBody();
  }

}
