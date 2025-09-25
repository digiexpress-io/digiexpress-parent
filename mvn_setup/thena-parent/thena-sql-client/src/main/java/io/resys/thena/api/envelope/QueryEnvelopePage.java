package io.resys.thena.api.envelope;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2021 - 2023 Copyright 2021 ReSys OÜ
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

import jakarta.annotation.Nullable;

import org.immutables.value.Value;

import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;


@Value.Immutable
public interface QueryEnvelopePage<T extends ThenaContainer> extends ThenaEnvelope {
  @Nullable Tenant getRepo();    
  @Nullable List<T> getCurrentPageObjects();
  @Nullable Integer getCurrentPageNumber();
  @Nullable Integer getTotalPages();
  @Nullable Long getTotalObjectsOnPages(); // total objects across all pages
  
  QueryEnvelopeStatus getStatus();
  List<Message> getMessages();
}
