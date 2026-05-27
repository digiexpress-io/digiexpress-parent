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

import io.resys.limaone.authoring.ImmutableModifyLocaleProps;
import io.resys.limaone.authoring.ImmutableModifyLocaleProps.Builder;
import io.resys.limaone.authoring.ModifyLocale;
import io.resys.limaone.model.ImmutableLocale;
import io.resys.limaone.model.Locale;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.limaone.persistence.AuthoringImpl.AuthoringConfig;
import io.resys.limaone.persistence.ModelWorldDb.NextWorld;
import io.smallrye.mutiny.Uni;


public class ModifyLocaleImpl extends AuthoringTemplate<ModifyLocaleImpl, Model<Locale>> implements ModifyLocale {

  private ModifyLocaleProps props;

  public ModifyLocaleImpl(AuthoringConfig config) {
    super(config);
  }
  
  @Override
  public ModifyLocale props(ModifyLocaleProps props) {
    this.props = props;
    return this;
  }

  @Override
  public ModifyLocale props(Consumer<Builder> props) {
    final var builder = ImmutableModifyLocaleProps.builder();
    props.accept(builder);
    return props(builder.build());
  }

  @Override
  public Uni<Model<Locale>> build() {
    return config.getPersistence().worldBuilder()
      .createdAt(getCreatedAt())
      .author(getAuthor())
      .docs(BodyType.LOCALE)
      .build(nextWorld -> {
        final var body = internalBuild(nextWorld);
        return nextWorld.mergeModel(props.getLocaleId(), body.getValue(), body);
      });
  }
  
  private Locale internalBuild(NextWorld nextWorld) {
    Objects.requireNonNull(props, () -> "props must be defined");
    final ModelWorld world = nextWorld.getCurrentWorld();
    
    final var start = world.getLocales().get(props.getLocaleId());
    if(start == null) {
      throw new AuthoringException(props, "Locale with id: '" + props.getLocaleId() + "' not found!");
    }
    
    // Check for duplicate value only if the value is actually being changed
    if(!start.getBody().getValue().equals(props.getValue())) {
      final var duplicate = world.getLocales().values().stream()
          .filter(p -> !p.getId().equals(props.getLocaleId()))
          .filter(p -> p.getBody().getValue().equalsIgnoreCase(props.getValue()))
          .findFirst();

      if(duplicate.isPresent()) {
        throw new AuthoringException(props, "Locale: '" + props.getValue() + "' already exists!");
      }
    }

    return ImmutableLocale.builder()
      .from(start.getBody())
      .value(props.getValue())
      .enabled(props.getEnabled())
      .disabledMode(props.getDisabledMode())
      .description(props.getDescription())
      .build();
  }
}
