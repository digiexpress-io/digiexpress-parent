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

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import io.resys.limaone.authoring.ImmutableNewPrintoutPageProps;
import io.resys.limaone.authoring.ImmutableNewPrintoutPageProps.Builder;
import io.resys.limaone.authoring.NewPrintoutPage;
import io.resys.limaone.model.ImmutablePrintoutPage;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.limaone.model.PrintoutPage;
import io.resys.limaone.persistence.AuthoringImpl.AuthoringConfig;
import io.smallrye.mutiny.Uni;



public class NewPrintoutPageImpl extends AuthoringTemplate<NewPrintoutPageImpl, Model<PrintoutPage>> implements NewPrintoutPage {

  private NewPrintoutPageProps props;

  public NewPrintoutPageImpl(AuthoringConfig config) {
    super(config);
  }

  @Override
  public NewPrintoutPage props(NewPrintoutPageProps props) {
    this.props = props;
    return this;
  }

  @Override
  public NewPrintoutPage props(Consumer<Builder> props) {
    final var builder = ImmutableNewPrintoutPageProps.builder();
    props.accept(builder);
    return props(builder.build());
  }

  @Override
  public Uni<Model<PrintoutPage>> build() {
    return config.getPersistence().worldBuilder()
      .createdAt(getCreatedAt())
      .author(getAuthor())
      .docs(BodyType.PRINTOUT_PAGE)
      .build(nextWorld -> {
        final var body = internalBuild(nextWorld.getCurrentWorld());
        return nextWorld.newModel(body.getServiceId() + "_" + body.getLocaleId(), body, props.getAssetDescription(), props.getAssetLabels());
      });
  }

  private PrintoutPage internalBuild(ModelWorld world) {
    Objects.requireNonNull(props, () -> "props must be defined");

    final var serviceRef = props.getServiceId();
    final var service = world.getPrintouts().containsKey(serviceRef) ?
        Optional.of(world.getPrintouts().get(serviceRef)) :
        world.getPrintouts().values().stream()
            .filter(p -> p.getBody().getServiceName().equalsIgnoreCase(serviceRef))
            .findFirst();

    final var localeRef = props.getLocaleId();
    final var locale = world.findOneLocale(localeRef);

    final var printoutIds = new ArrayList<String>();
    if(props.getPrintoutPageIds() != null) {
      for(final var printoutPageRef : props.getPrintoutPageIds()) {
        if(!world.getPrintoutPages().containsKey(printoutPageRef)) {
          throw new AuthoringException(props, "PrintoutPage template with id: '" + printoutPageRef + "' does not exist!");
        }
        printoutIds.add(printoutPageRef);
      }
    }

    final var printoutPage = ImmutablePrintoutPage.builder()
        .content(Optional.ofNullable(props.getContent()).orElse(""))
        .localeId(locale.map(e -> e.getId()).orElse(localeRef))
        .serviceId(service.map(e -> e.getId()).orElse(serviceRef))
        .printoutPageIds(printoutIds);

    if(locale.isEmpty()) {
      throw new AuthoringException(props,
          "Locale with id: '" + localeRef + "' does not exist in: '" + String.join(",", world.getLocales().keySet()) + "'!");
    }
    if(service.isEmpty()) {
      throw new AuthoringException(props,
          "Printout with id: '" + serviceRef + "' does not exist in: '" + String.join(",", world.getPrintouts().keySet()) + "'!");
    }

    final var duplicate = world.getPrintoutPages().values().stream()
        .filter(p -> p.getBody().getServiceId().equals(service.get().getId()))
        .filter(p -> p.getBody().getLocaleId().equals(locale.get().getId()))
        .findFirst();

    if(duplicate.isPresent()) {
      throw new AuthoringException(props, "PrintoutPage for locale: '" + localeRef + "' and service: '" + serviceRef + "' already exists!");
    }

    return printoutPage.build();
  }
}
