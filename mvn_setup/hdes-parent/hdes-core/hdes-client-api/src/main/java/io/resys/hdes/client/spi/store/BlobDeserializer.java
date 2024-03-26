package io.resys.hdes.client.spi.store;

/*-
 * #%L
 * hdes-client
 * %%
 * Copyright (C) 2020 - 2021 Copyright 2020 ReSys OÜ
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

import com.fasterxml.jackson.databind.ObjectMapper;

import io.resys.hdes.client.api.HdesStore.StoreEntity;
import io.resys.thena.api.entities.git.ThenaGitObject.Blob;
import io.resys.hdes.client.api.ImmutableStoreEntity;


public class BlobDeserializer implements ThenaConfig.Deserializer {

  private ObjectMapper objectMapper;
  
  public BlobDeserializer(ObjectMapper objectMapper) {
    super();
    this.objectMapper = objectMapper;
  }


  @Override
  public StoreEntity fromString(Blob value) {
    try {
      final ImmutableStoreEntity src = objectMapper.readValue(value.getValue().toString(), ImmutableStoreEntity.class);
      return ImmutableStoreEntity.builder().from(src).hash(value.getId()).build();
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage() + System.lineSeparator() + value, e);
    }
  }
}
