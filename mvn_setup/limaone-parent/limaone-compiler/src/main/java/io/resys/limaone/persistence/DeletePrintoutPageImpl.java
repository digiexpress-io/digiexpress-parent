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
import java.util.stream.Collectors;

import io.resys.limaone.authoring.DeletePrintoutPage;
import io.resys.limaone.authoring.ImmutableDeletePrintoutPageProps;
import io.resys.limaone.authoring.ImmutableDeletePrintoutPageProps.Builder;
import io.resys.limaone.model.ImmutablePrintoutResource;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.PrintoutPage;
import io.resys.limaone.persistence.AuthoringImpl.AuthoringConfig;
import io.smallrye.mutiny.Uni;


public class DeletePrintoutPageImpl extends AuthoringTemplate<DeletePrintoutPageImpl, Model<PrintoutPage>> implements DeletePrintoutPage {

  private DeletePrintoutPageProps props;

  public DeletePrintoutPageImpl(AuthoringConfig config) {
    super(config);
  }

  @Override
  public DeletePrintoutPage props(DeletePrintoutPageProps props) {
    this.props = props;
    return this;
  }

  @Override
  public DeletePrintoutPage props(Consumer<Builder> props) {
    final var builder = ImmutableDeletePrintoutPageProps.builder();
    props.accept(builder);
    return props(builder.build());
  }

  @Override
  public Uni<Model<PrintoutPage>> build() {
    Objects.requireNonNull(props, () -> "props must be defined");

    return config.getPersistence().worldBuilder()
      .createdAt(getCreatedAt())
      .author(getAuthor())
      .docs(BodyType.PRINTOUT_PAGE, BodyType.PRINTOUT_RESOURCE)
      .build(nextWorld -> {
        final var world = nextWorld.getCurrentWorld();
        final var pageId = props.getPrintoutPageId();

        final var page = world.getPrintoutPages().get(pageId);
        if(page == null) {
          throw new AuthoringException(props, "PrintoutPage with id: '" + pageId + "' not found!");
        }

        for(final var resource : world.getPrintoutResources().values()) {
          final var existingLinks = resource.getBody().getPrintoutPageIds();
          if(!existingLinks.contains(pageId)) {
            continue;
          }
          final var remaining = existingLinks.stream()
              .filter(id -> !id.equals(pageId))
              .collect(Collectors.toList());
          final var updated = ImmutablePrintoutResource.builder()
              .from(resource.getBody())
              .printoutPageIds(remaining)
              .build();
          nextWorld.mergeModel(resource.getId(), updated.getResourceName(), updated);
        }

        return nextWorld.deleteModel(pageId, page.getBody());
      });
  }
}
