package io.thestencil.client.spi.builders;

/*-
 * #%L
 * stencil-client
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

import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;

import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.ImmutableSiteCommitLog;
import io.thestencil.client.api.StencilClient.SiteCommitLog;
import io.thestencil.client.api.StencilClient.SiteCommitLogBuilder;
import io.thestencil.client.api.StencilStore;
import io.thestencil.client.spi.exceptions.QueryException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SiteCommitLogBuilderImpl implements SiteCommitLogBuilder {
  private final StencilStore store;

  @Override
  public Uni<List<SiteCommitLog>> build() {
    final var config = store.getConfig();
    

    final var siteName = config.getRepoName() + ":" + config.getHeadName();
    return config.getClient().git(config.getRepoName()).tenants().get().onItem()
      .transformToUni(repo -> {
        if(repo == null) {
         return Uni.createFrom().item(Collections.emptyList()); 
        }
      
        return config.getClient().git(config.getRepoName())
            .history().blobCommitQuery().branchName(config.getHeadName()).includBlob(false).findAll()
            .onItem().transform(state -> {
              if(state.getStatus() == QueryEnvelopeStatus.ERROR) {
                throw new QueryException(siteName, "SITE_COMMIT_LOG", state);
              }

              // Nothing present
              if(state.getObjects() == null) {
                throw new QueryException(siteName, "SITE_COMMIT_LOG", state);
              }

              
              return state.getObjects().getValues().stream().map(log -> {
                
                
                
                final SiteCommitLog result = ImmutableSiteCommitLog.builder()
                    .commitId(log.getCommit().getId())
                    .createdAt(log.getCommit().getDateTime().atOffset(ZoneOffset.UTC))
                    .createdBy(log.getCommit().getAuthor())
                    .objectId(log.getResourceName())
                    .build();
                
                return result;
              }).toList();
            });
      });
  }
}
