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

import io.resys.limaone.authoring.ImmutableModifyPrintoutProps;
import io.resys.limaone.authoring.ImmutableModifyPrintoutProps.Builder;
import io.resys.limaone.authoring.ModifyPrintout;
import io.resys.limaone.model.ImmutablePrintout;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.limaone.model.Printout;
import io.resys.limaone.persistence.AuthoringImpl.AuthoringConfig;
import io.resys.limaone.persistence.ModelWorldDb.NextWorld;
import io.smallrye.mutiny.Uni;


public class ModifyPrintoutImpl extends AuthoringTemplate<ModifyPrintoutImpl, Model<Printout>> implements ModifyPrintout {

  private ModifyPrintoutProps props;

  public ModifyPrintoutImpl(AuthoringConfig config) {
    super(config);
  }

  @Override
  public ModifyPrintout props(ModifyPrintoutProps props) {
    this.props = props;
    return this;
  }

  @Override
  public ModifyPrintout props(Consumer<Builder> props) {
    final var builder = ImmutableModifyPrintoutProps.builder();
    props.accept(builder);
    return props(builder.build());
  }

  @Override
  public Uni<Model<Printout>> build() {
    return config.getPersistence().worldBuilder()
      .createdAt(getCreatedAt())
      .author(getAuthor())
      .docs(BodyType.PRINTOUT, BodyType.LOCALE)
      .build(nextWorld -> {
        final var body = internalBuild(nextWorld);
        return nextWorld.mergeModel(props.getServiceId(), body.getServiceName(), body);
      });
  }

  private Printout internalBuild(NextWorld nextWorld) {
    Objects.requireNonNull(props, () -> "props must be defined");
    final ModelWorld world = nextWorld.getCurrentWorld();

    final var start = world.getPrintouts().get(props.getServiceId());
    if(start == null) {
      throw new AuthoringException(props, "Can't find printout: '" + props.getServiceId() + "' to update!");
    }

    if(props.getLabels() != null) {
      for(final var label : props.getLabels()) {
        final var localeId = label.getLocale();
        if(!world.getLocales().containsKey(localeId)) {
          throw new AuthoringException(props,
              "Locale with id: '" + localeId + "' does not exist in: '" + String.join(",", world.getLocales().keySet()) + "'!");
        }
      }
    }

    return ImmutablePrintout.builder()
        .from(start.getBody())
        .serviceName(props.getServiceName())
        .orchestratorName(props.getOrchestratorName())
        .description(props.getDescription() == null ? start.getBody().getDescription() : props.getDescription())
        .labels(props.getLabels() == null ? start.getBody().getLabels() : props.getLabels())
        .build();
  }
}
