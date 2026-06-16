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

import java.util.function.Consumer;

import io.resys.limaone.authoring.ImmutableModifyDescriptionProps;
import io.resys.limaone.authoring.ModifyDescription;
import io.resys.limaone.model.ImmutableDescription;
import io.resys.limaone.model.Model;
import io.resys.limaone.persistence.AuthoringImpl.AuthoringConfig;
import io.smallrye.mutiny.Uni;


public class ModifyDescriptionImpl extends AuthoringTemplate<ModifyDescriptionImpl, Model<?>> implements ModifyDescription {

  private ModifyDescriptionProps props;

  public ModifyDescriptionImpl(AuthoringConfig config) {
    super(config);
  }

  @Override
  public ModifyDescriptionImpl props(ModifyDescriptionProps props) {
    this.props = props;
    return this;
  }

  @Override
  public ModifyDescriptionImpl props(Consumer<ImmutableModifyDescriptionProps.Builder> props) {
    final var builder = ImmutableModifyDescriptionProps.builder();
    props.accept(builder);
    return props(builder.build());
  }

  @Override
  public Uni<Model<?>> build() {
    return config.getPersistence().worldBuilder()
      .createdAt(getCreatedAt())
      .author(getAuthor())
      .docsId(props.getId())
      .build(nextWorld -> {
        
        return nextWorld.mergeModel(props.getId(), ImmutableDescription.builder().text(props.getText()).build());
      });
  }
}
