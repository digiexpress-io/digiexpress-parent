package io.dialob.spring.composer.config;

/*-
 * #%L
 * hdes-spring-composer
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

import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.dialob.client.api.DialobStore;
import io.dialob.client.api.DialobStore.DialobCredsSupplier;
import io.dialob.client.api.ImmutableDialobCreds;
import io.dialob.client.spi.DialobStoreFileImpl;

@ConditionalOnProperty(name = "dialob.formdb.file.enabled", havingValue = "true")
public class FileConfig {
  
  @Bean
  public DialobStore dialobStore(Optional<DialobCredsSupplier> authorProvider, FileConfigBean gitConfigBean, ObjectMapper objectMapper) {
    final DialobCredsSupplier creds;
    if(authorProvider.isEmpty()) {
      if(gitConfigBean.getEmail() != null && gitConfigBean.getEmail().contains("@")) {
        creds = () -> ImmutableDialobCreds.builder().user(gitConfigBean.getEmail().split("@")[0]).email(gitConfigBean.getEmail()).build(); 
      } else {
        creds = () -> ImmutableDialobCreds.builder().user("assetManager").email("assetManager@resys.io").build();  
      } 
    } else {
      creds = authorProvider.get();
    }
    
    return DialobStoreFileImpl.builder()
        .pgDb(gitConfigBean.getPath())
        .objectMapper(objectMapper)
        .authorProvider(() -> creds.get().getUser())
        .build();
  }
}
