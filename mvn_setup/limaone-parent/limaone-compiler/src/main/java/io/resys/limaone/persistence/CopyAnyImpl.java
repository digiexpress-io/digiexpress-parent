package io.resys.limaone.persistence;

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

import java.util.Objects;
import java.util.function.Consumer;

import io.resys.limaone.authoring.CopyAny;
import io.resys.limaone.authoring.ImmutableCopyAnyProps;
import io.resys.limaone.authoring.ImmutableCopyAnyProps.Builder;
import io.resys.limaone.model.Model;
import io.resys.limaone.persistence.AuthoringImpl.AuthoringConfig;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;

public class CopyAnyImpl extends AuthoringTemplate<CopyAnyImpl, Model<?>> implements CopyAny {
  
  private CopyAnyProps props;

  public CopyAnyImpl(AuthoringConfig config) {
    super(config);
  }
  
  @Override
  public CopyAny props(CopyAnyProps props) {
    this.props = Objects.requireNonNull(props, () -> "props must be defined");
    return this;
  }
  @Override
  public CopyAny props(Consumer<Builder> props) {
    Objects.requireNonNull(props, () -> "props must be defined");
    final var builder = ImmutableCopyAnyProps.builder();
    props.accept(builder);
    return props(builder.build());
  }
  @Override
  public Uni<Model<?>> build() {
    Objects.requireNonNull(props, () -> "props must be defined");
  
    return config.getPersistence().worldBuilder()
        .createdAt(getCreatedAt())
        .author(getAuthor())
        .docsId(props.getIdOfObjectToCopy())
        .build(nextWorld -> {
          
          final var model = nextWorld.getCurrentWorld().findAnyObject(props.getIdOfObjectToCopy());
          RepoAssert.isTrue(model.isPresent(), () -> "model must be loaded to rename it!");
          final var nextState = copy(model.get());
          return nextWorld.newModel(props.getNewObjectName(), nextState);
        });
  }
  
  
  private Model.Body copy(Model<?> model) {
    return ModifyAssetNameImpl.buildNextState(model, props.getNewObjectName());
  }
}
