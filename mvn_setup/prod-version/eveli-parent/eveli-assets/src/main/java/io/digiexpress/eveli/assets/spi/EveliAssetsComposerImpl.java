package io.digiexpress.eveli.assets.spi;

/*-
 * #%L
 * eveli-assets
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
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

import io.digiexpress.eveli.assets.api.EveliAssetClient;
import io.digiexpress.eveli.assets.api.EveliAssetComposer;
import io.digiexpress.eveli.assets.spi.builders.CreateBuilderImpl;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class EveliAssetsComposerImpl implements EveliAssetComposer {
  private final EveliAssetClient client;

  @Override
  public CreateBuilder create() {
    return new CreateBuilderImpl(client);
  }

  @Override
  public UpdateBuilder update() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public DeleteBuilder delete() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public MigrationBuilder migration() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public DeploymentBuilder deployment() {
    // TODO Auto-generated method stub
    return null;
  }

  public EveliAssetClient getClient() {
    return client;
  }

  @Override
  public AnyTagQuery anyAssetTagQuery() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public PublicationQuery publicationQuery() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public WorkflowQuery workflowQuery() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public WorkflowTagQuery workflowTagQuery() {
    // TODO Auto-generated method stub
    return null;
  }
  

}
