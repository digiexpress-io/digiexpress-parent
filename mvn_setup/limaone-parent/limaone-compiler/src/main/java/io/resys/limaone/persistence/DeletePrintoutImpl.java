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

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import io.resys.limaone.authoring.DeletePrintout;
import io.resys.limaone.authoring.ImmutableDeletePrintoutProps;
import io.resys.limaone.authoring.ImmutableDeletePrintoutProps.Builder;
import io.resys.limaone.model.ImmutablePrintoutResource;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.Printout;
import io.resys.limaone.persistence.AuthoringImpl.AuthoringConfig;
import io.smallrye.mutiny.Uni;


public class DeletePrintoutImpl extends AuthoringTemplate<DeletePrintoutImpl, Model<Printout>> implements DeletePrintout {

  private DeletePrintoutProps props;

  public DeletePrintoutImpl(AuthoringConfig config) {
    super(config);
  }

  @Override
  public DeletePrintout props(DeletePrintoutProps props) {
    this.props = props;
    return this;
  }

  @Override
  public DeletePrintout props(Consumer<Builder> props) {
    final var builder = ImmutableDeletePrintoutProps.builder();
    props.accept(builder);
    return props(builder.build());
  }

  @Override
  public Uni<Model<Printout>> build() {
    Objects.requireNonNull(props, () -> "props must be defined");

    return config.getPersistence().worldBuilder()
      .createdAt(getCreatedAt())
      .author(getAuthor())
      .docs(BodyType.PRINTOUT, BodyType.PRINTOUT_PAGE, BodyType.PRINTOUT_RESOURCE)
      .build(nextWorld -> {
        final var world = nextWorld.getCurrentWorld();
        final var printoutId = props.getPrintoutId();

        final var printout = world.getPrintouts().get(printoutId);
        if(printout == null) {
          throw new AuthoringException(props, "Printout with id: '" + printoutId + "' not found!");
        }

        final Set<String> pageIdsToDelete = world.getPrintoutPages().values().stream()
            .filter(page -> printoutId.equals(page.getBody().getServiceId()))
            .map(Model::getId)
            .collect(Collectors.toCollection(HashSet::new));

        for(final var resource : world.getPrintoutResources().values()) {
          final var existingLinks = resource.getBody().getPrintoutPageIds();
          final var remaining = existingLinks.stream()
              .filter(id -> !pageIdsToDelete.contains(id))
              .collect(Collectors.toList());
          if(remaining.size() == existingLinks.size()) {
            continue;
          }
          final var updated = ImmutablePrintoutResource.builder()
              .from(resource.getBody())
              .printoutPageIds(remaining)
              .build();
          nextWorld.mergeModel(resource.getId(), updated.getResourceName(), updated);
        }

        for(final var pageId : pageIdsToDelete) {
          final var page = world.getPrintoutPages().get(pageId);
          nextWorld.deleteModel(pageId, page.getBody());
        }

        return nextWorld.deleteModel(printoutId, printout.getBody());
      });
  }
}
