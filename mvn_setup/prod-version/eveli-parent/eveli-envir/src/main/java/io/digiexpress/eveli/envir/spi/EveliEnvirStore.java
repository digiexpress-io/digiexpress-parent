package io.digiexpress.eveli.envir.spi;

/*-
 * #%L
 * eveli-envir
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

import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliDeployment;
import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliDeploymentStatus;
import io.digiexpress.eveli.envir.api.ExternalDeploymentProvider;
import io.digiexpress.eveli.envir.api.ImmutableEveliDeployment;
import io.digiexpress.eveli.envir.api.ImmutableEveliSources;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.api.entities.doc.Doc;
import io.resys.thena.api.entities.doc.DocBranch;
import io.resys.thena.spi.DocStoreImpl;
import io.resys.thena.spi.ThenaDocConfig;



public class EveliEnvirStore extends DocStoreImpl<EveliEnvirStore> {

  public static String DOC_TYPE_DEPLOYMENT = "deployment";
  
  private final ExternalDeploymentProvider externalProvider;
  
  public EveliEnvirStore(
      ThenaDocConfig config, 
      DocStoreFactory<EveliEnvirStore> factory,
      ExternalDeploymentProvider externalProvider) {
    super(config, factory);
    this.externalProvider = externalProvider;
  }

  public static Builder<EveliEnvirStore> builder(ExternalDeploymentProvider externalProvider) {
    final DocStoreFactory<EveliEnvirStore> factory = (config, delegate) -> new EveliEnvirStore(config, delegate, externalProvider);
    return new Builder<EveliEnvirStore>(factory);
  }
  
  @Override
  public StoreTenantQuery<EveliEnvirStore> query() {
    final var resp = super.query().repoType(StructureType.doc).repoName(config.getRepoId());
    
    return resp;
  }
  
  public static EveliDeployment map(Doc doc, Optional<DocBranch> branch) {
    return ImmutableEveliDeployment.builder()
        .id(doc.getId())
        .externalId(doc.getExternalId())
        .startsAt(doc.getStartsAt())
        .status(EveliDeploymentStatus.valueOf(doc.getSubStatus()))
        .externalId(doc.getExternalId())
        .createdAt(doc.getCreatedAt())
        .errors(doc.getMeta())
        .sources(branch
            .map(src -> src.getValue().mapTo(ImmutableEveliSources.class))
            .orElse(null))
        .build();
  }

  public ExternalDeploymentProvider getExternalProvider() {
    return externalProvider;
  }
}
