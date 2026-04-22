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

import io.resys.limaone.authoring.ImmutableModifyPrintoutPageProps;
import io.resys.limaone.authoring.ImmutableModifyPrintoutPageProps.Builder;
import io.resys.limaone.authoring.ModifyPrintoutPage;
import io.resys.limaone.model.ImmutablePrintoutPage;
import io.resys.limaone.model.ImmutablePrintoutResource;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.limaone.model.PrintoutPage;
import io.resys.limaone.persistence.AuthoringImpl.AuthoringConfig;
import io.resys.limaone.persistence.ModelWorldDb.NextWorld;
import io.smallrye.mutiny.Uni;


public class ModifyPrintoutPageImpl extends AuthoringTemplate<ModifyPrintoutPageImpl, Model<PrintoutPage>> implements ModifyPrintoutPage {

  private ModifyPrintoutPageProps props;

  public ModifyPrintoutPageImpl(AuthoringConfig config) {
    super(config);
  }

  @Override
  public ModifyPrintoutPage props(ModifyPrintoutPageProps props) {
    this.props = props;
    return this;
  }

  @Override
  public ModifyPrintoutPage props(Consumer<Builder> props) {
    final var builder = ImmutableModifyPrintoutPageProps.builder();
    props.accept(builder);
    return props(builder.build());
  }

  @Override
  public Uni<Model<PrintoutPage>> build() {
    return config.getPersistence().worldBuilder()
      .createdAt(getCreatedAt())
      .author(getAuthor())
      .docs(BodyType.PRINTOUT_PAGE, BodyType.PRINTOUT_RESOURCE)
      .build(nextWorld -> {
        final var body = internalBuild(nextWorld);
        return nextWorld.mergeModel(props.getPageId(), body.getServiceId() + "_" + body.getLocaleId(), body);
      });
  }

  private PrintoutPage internalBuild(NextWorld nextWorld) {
    Objects.requireNonNull(props, () -> "props must be defined");
    final ModelWorld world = nextWorld.getCurrentWorld();

    final var start = world.getPrintoutPages().get(props.getPageId());
    if(start == null) {
      throw new AuthoringException(props, "Can't find printout page: '" + props.getPageId() + "' to update!");
    }

    final var targetLocale = Optional.ofNullable(props.getLocaleId()).orElse(start.getBody().getLocaleId());
    final var locale = world.findOneLocale(targetLocale);

    if(locale.isEmpty()) {
      throw new AuthoringException(props, "Locale: '" + targetLocale + "' does not exist!");
    }

    if(props.getResourceIds() != null) {
      for(final var resource : world.getPrintoutResources().values()) {

        final var isPageInResource = resource.getBody().getTemplateIds().contains(props.getPageId());
        final var isResourceInChanges = props.getResourceIds().contains(resource.getId());

        if(isPageInResource && isResourceInChanges) {
          continue;
        }

        if(isResourceInChanges && !isPageInResource) {
          final var newResource = ImmutablePrintoutResource.builder()
              .from(resource.getBody())
              .addTemplateIds(props.getPageId())
              .build();
          nextWorld.mergeModel(resource.getId(), newResource.getResourceName(), newResource);
        }

        if(isPageInResource && !isResourceInChanges) {
          final var templateIds = new ArrayList<>(resource.getBody().getTemplateIds());
          templateIds.remove(props.getPageId());

          final var newResource = ImmutablePrintoutResource.builder()
              .from(resource.getBody())
              .templateIds(templateIds)
              .build();
          nextWorld.mergeModel(resource.getId(), newResource.getResourceName(), newResource);
        }
      }
    }

    var templateDependencies = start.getBody().getTemplateIds();
    if(props.getTemplateIds() != null) {
      templateDependencies = new ArrayList<>();
      for(final var depTemplateRef : props.getTemplateIds()) {
        if(!world.getPrintoutPages().containsKey(depTemplateRef)) {
          throw new AuthoringException(props, "PrintoutPage template with id: '" + depTemplateRef + "' does not exist!");
        }
        templateDependencies.add(depTemplateRef);
      }
    }

    return ImmutablePrintoutPage.builder()
        .from(start.getBody())
        .content(props.getContent() == null ? start.getBody().getContent() : props.getContent())
        .localeId(locale.get().getId())
        .templateIds(templateDependencies)
        .build();
  }
}
