package io.resys.hdes.client.spi.spring;

/*-
 * #%L
 * eveli-integration-git
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
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

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.resys.hdes.client.api.HdesStore;
import io.resys.hdes.client.api.HdesStore.HdesCredsSupplier;
import io.resys.hdes.client.api.ImmutableHdesCreds;
import io.resys.hdes.client.spi.GitStore;



@EnableConfigurationProperties(GitConfigProps.class)
public class GitAutoConfig {

  @Bean
  public HdesStore hdesStore(Optional<HdesCredsSupplier> authorProvider, GitConfigProps gitConfigBean, ObjectMapper objectMapper) {
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
