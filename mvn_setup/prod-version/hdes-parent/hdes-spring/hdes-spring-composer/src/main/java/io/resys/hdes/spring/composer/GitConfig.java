package io.resys.hdes.spring.composer;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.resys.hdes.client.api.HdesStore;
import io.resys.hdes.client.api.HdesStore.HdesCredsSupplier;
import io.resys.hdes.client.api.ImmutableHdesCreds;
import io.resys.hdes.client.spi.GitStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import java.util.Optional;

@ConditionalOnProperty(name = "wrench.assets.git.enabled", havingValue = "true")
public class GitConfig {
  
  @Bean
  public HdesStore hdesStore(Optional<HdesCredsSupplier> authorProvider, GitConfigBean gitConfigBean, ObjectMapper objectMapper) {
    final HdesCredsSupplier creds;
    if(authorProvider.isEmpty()) {
      if(gitConfigBean.getEmail() != null && gitConfigBean.getEmail().contains("@")) {
        creds = () -> ImmutableHdesCreds.builder().user(gitConfigBean.getEmail().split("@")[0]).email(gitConfigBean.getEmail()).build(); 
      } else {
        creds = () -> ImmutableHdesCreds.builder().user("assetManager").email("assetManager@resys.io").build();  
      } 
    } else {
      creds = authorProvider.get();
    }
    
    return GitStore.builder()
        .remote(gitConfigBean.getRepositoryUrl())
        .branch(gitConfigBean.getBranchSpecifier())
        .sshPath(gitConfigBean.getPrivateKey())
        .storage(gitConfigBean.getRepositoryPath())
        .objectMapper(objectMapper)
        .creds(creds)
        .build();
  }
}
