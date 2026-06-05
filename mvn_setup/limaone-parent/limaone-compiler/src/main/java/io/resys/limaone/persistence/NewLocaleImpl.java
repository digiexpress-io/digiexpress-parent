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

import io.resys.limaone.authoring.ImmutableNewLocaleProps;
import io.resys.limaone.authoring.ImmutableNewLocaleProps.Builder;
import io.resys.limaone.authoring.NewLocale;
import io.resys.limaone.model.ImmutableLocale;
import io.resys.limaone.model.Locale;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.limaone.persistence.AuthoringImpl.AuthoringConfig;
import io.smallrye.mutiny.Uni;


public class NewLocaleImpl extends AuthoringTemplate<NewLocaleImpl, Model<Locale>> implements NewLocale {

  private NewLocaleProps props;
  public NewLocaleImpl(AuthoringConfig config) {
    super(config);
  }
  
  @Override
  public NewLocale props(NewLocaleProps props) {
    this.props = props;
    return this;
  }

  @Override
  public NewLocale props(Consumer<Builder> props) {
    final var builder = ImmutableNewLocaleProps.builder();
    props.accept(builder);
    return props(builder.build());
  }

  @Override
  public Uni<Model<Locale>> build() {
    return config.getPersistence().worldBuilder()
      .docs(BodyType.LOCALE)
      .createdAt(getCreatedAt())
      .author(getAuthor())
      .build(nextWorld -> {
        final var body = internalBuild(nextWorld.getCurrentWorld());
        return nextWorld.newModel(body.getValue(), body, props.getAssetDescription(), props.getAssetLabels());
      });
  }
  
  private Locale internalBuild(ModelWorld world) {
    Objects.requireNonNull(props, () -> "props must be defined");

    final var locale = ImmutableLocale.builder()
        .value(props.getLocale())
        .enabled(true);
 
    final var duplicate = world.getLocales().values().stream()
        .filter(p -> p.getBody().getValue().equals(props.getLocale()))
        .findFirst();
    
    if(duplicate.isPresent()) {
      final var msg = "Locale: '" + props.getLocale() + "' already exists!";
      throw new AuthoringException(props, msg);
    }
    
    return locale.build();
  }
}
